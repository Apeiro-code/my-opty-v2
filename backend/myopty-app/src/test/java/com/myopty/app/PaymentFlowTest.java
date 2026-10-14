package com.myopty.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

/**
 * Proves the billing stories end to end against a real database and the fake
 * gateway: a customer pays for an order and gets an invoice, reads the confirmation
 * and their history, and the shop lists and refunds the payment.
 *
 * <p>Nothing is faked except the gateway, and that is the point of it: the filter
 * chain, the controllers, pricing through the catalog and order contracts, the SQL,
 * the invoice and the refund arithmetic are all real. The frame and lens are read
 * from the seeded catalog, so the test does not depend on a particular id.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class PaymentFlowTest {

    private static final String CUSTOMER_EMAIL = "customer@myopty.local";
    private static final String CUSTOMER_PASSWORD = "customer123";
    private static final String CLIENT_EMAIL = "client@myopty.local";
    private static final String CLIENT_PASSWORD = "client123";

    private static final String VALID_VALUES = """
            {"sphLeft": "+06.25", "progressive": true}
            """;

    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.0")
            .withDatabaseName("myopty")
            .withUsername("myopty")
            .withPassword("myopty");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void aCustomerPaysForAnOrderAndGetsAnInvoice() throws Exception {
        MockHttpSession session = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long orderId = createOrder(session);

        mvc.perform(post("/api/payments/process")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(processJson(orderId, "CARD", "tok_visa")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CAPTURED"))
                .andExpect(jsonPath("$.data.method").value("CARD"))
                .andExpect(jsonPath("$.data.gatewayTransactionId").isNotEmpty())
                .andExpect(jsonPath("$.data.invoiceNumber").isNotEmpty());

        Long invoiceCount = jdbc.queryForObject("SELECT COUNT(*) FROM invoice WHERE order_id = ?", Long.class, orderId);
        assertThat(invoiceCount).isEqualTo(1L);
    }

    @Test
    void theConfirmationIsReadableByTransactionId() throws Exception {
        MockHttpSession session = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long orderId = createOrder(session);
        String transactionId = processPayment(session, orderId, "CARD", "tok_visa");

        mvc.perform(get("/api/payments/confirmation/{transactionId}", transactionId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionId").value(transactionId))
                .andExpect(jsonPath("$.data.status").value("CAPTURED"))
                .andExpect(jsonPath("$.data.invoiceNumber").isNotEmpty());
    }

    @Test
    void theCustomerSeesTheirPaymentHistory() throws Exception {
        MockHttpSession session = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long orderId = createOrder(session);
        String transactionId = processPayment(session, orderId, "CARD", "tok_visa");

        mvc.perform(get("/api/customer/payments").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].gatewayTransactionId").value(transactionId));
    }

    @Test
    void theShopListsAndRefundsAPayment() throws Exception {
        MockHttpSession customer = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long orderId = createOrder(customer);
        processPayment(customer, orderId, "CARD", "tok_visa");
        long paymentId = lastPaymentId();

        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);
        mvc.perform(get("/api/admin/payments").session(client))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.totalItems").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

        // A partial refund leaves the payment PARTIALLY_REFUNDED...
        mvc.perform(post("/api/admin/payments/{id}/refund", paymentId)
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":25.00,\"reason\":\"Goodwill\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PARTIALLY_REFUNDED"))
                .andExpect(jsonPath("$.data.refundedAmount").value(25.00));

        // ...and refunding the rest turns it REFUNDED.
        mvc.perform(post("/api/admin/payments/{id}/refund", paymentId)
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Customer changed their mind\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REFUNDED"));

        Long refundRows =
                jdbc.queryForObject("SELECT COUNT(*) FROM payment_refund WHERE payment_id = ?", Long.class, paymentId);
        assertThat(refundRows).isEqualTo(2L);
    }

    @Test
    void aDeclinedCardIsRecordedAsFailedAndAnswered() throws Exception {
        MockHttpSession session = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long orderId = createOrder(session);

        mvc.perform(post("/api/payments/process")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(processJson(orderId, "CARD", "fail")))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.error.code").value("PAYMENT_FAILED"));

        String status = jdbc.queryForObject(
                "SELECT status FROM payment WHERE order_id = ? ORDER BY id DESC LIMIT 1", String.class, orderId);
        assertThat(status).isEqualTo("FAILED");
    }

    @Test
    void aCashOnPickupPaymentIsConfirmedLater() throws Exception {
        MockHttpSession session = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long orderId = createOrder(session);

        mvc.perform(post("/api/payments/process")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(processJson(orderId, "CASH_ON_PICKUP", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        long paymentId = lastPaymentId();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                                "/api/payments/{id}/capture", paymentId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CAPTURED"));
    }

    @Test
    void anOrderCannotBePaidTwice() throws Exception {
        MockHttpSession session = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long orderId = createOrder(session);
        processPayment(session, orderId, "CARD", "tok_visa");

        mvc.perform(post("/api/payments/process")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(processJson(orderId, "CARD", "tok_visa")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("PAYMENT_NOT_ALLOWED"));
    }

    @Test
    void thePaymentMethodsAreListed() throws Exception {
        MockHttpSession session = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);

        mvc.perform(get("/api/payment-methods").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.code == 'CARD')]").exists())
                .andExpect(jsonPath("$.data[?(@.code == 'CASH_ON_PICKUP')]").exists());
    }

    @Test
    void anAnonymousPaymentIsRefused() throws Exception {
        mvc.perform(post("/api/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(processJson(1L, "CARD", "tok_visa")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aCustomerCannotReachTheAdminList() throws Exception {
        MockHttpSession session = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);

        mvc.perform(get("/api/admin/payments").session(session)).andExpect(status().isForbidden());
    }

    private String processPayment(MockHttpSession session, long orderId, String method, String token) throws Exception {
        mvc.perform(post("/api/payments/process")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(processJson(orderId, method, token)))
                .andExpect(status().isOk());
        return jdbc.queryForObject(
                "SELECT gateway_transaction_id FROM payment WHERE order_id = ? ORDER BY id DESC LIMIT 1",
                String.class,
                orderId);
    }

    private long lastPaymentId() {
        return jdbc.queryForObject("SELECT id FROM payment ORDER BY id DESC LIMIT 1", Long.class);
    }

    private long createOrder(MockHttpSession session) throws Exception {
        long prescriptionId = submitPrescription(session);
        mvc.perform(post("/api/orders")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prescriptionId\":%d,\"orderType\":\"PROGRESSIVE\",\"lensId\":%d,\"frameId\":%d}"
                                .formatted(prescriptionId, anyProgressiveLensId(), anyActiveFrameId())))
                .andExpect(status().isCreated());
        return jdbc.queryForObject("SELECT id FROM progressive_order ORDER BY id DESC LIMIT 1", Long.class);
    }

    private long submitPrescription(MockHttpSession session) throws Exception {
        mvc.perform(multipart("/api/prescriptions")
                        .file(new MockMultipartFile(
                                "prescription",
                                "",
                                MediaType.APPLICATION_JSON_VALUE,
                                VALID_VALUES.getBytes(StandardCharsets.UTF_8)))
                        .session(session))
                .andExpect(status().isCreated());
        Long customerId = jdbc.queryForObject("SELECT id FROM app_user WHERE email = ?", Long.class, CUSTOMER_EMAIL);
        return jdbc.queryForObject(
                "SELECT id FROM prescription WHERE customer_id = ? ORDER BY id DESC LIMIT 1", Long.class, customerId);
    }

    private long anyActiveFrameId() {
        return jdbc.queryForObject("SELECT id FROM frame WHERE is_active = TRUE LIMIT 1", Long.class);
    }

    private long anyProgressiveLensId() {
        return jdbc.queryForObject("SELECT id FROM lens WHERE type = 'PROGRESSIVE' LIMIT 1", Long.class);
    }

    private static String processJson(long orderId, String method, String token) {
        String details = token == null ? "{}" : "{\"token\":\"" + token + "\"}";
        return "{\"orderId\":%d,\"paymentMethod\":\"%s\",\"paymentDetails\":%s}".formatted(orderId, method, details);
    }

    private MockHttpSession loginAs(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }
}
