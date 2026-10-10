package com.myopty.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Proves the shop's review and approval stories end to end against a real database:
 * a client lists the pending queues, verifies or rejects a prescription, and approves
 * or rejects an order — where approval is refused until the linked prescription is
 * verified. It also proves the role boundary: a customer and an anonymous caller
 * cannot reach {@code /api/shop/**}.
 *
 * <p>There is no fake here. The parts worth exercising — the filter chain's role
 * rule, JSON decoding of the enums, Bean Validation on the reject reason, the
 * service's state rules, the SQL updates and the foreign keys — are all real.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class ShopReviewFlowTest {

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
    void aClientCanListThePendingQueueAndVerifyAPrescription() throws Exception {
        MockHttpSession customer = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long prescriptionId = submitPrescription(customer);
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(get("/api/shop/prescriptions")
                        .param("status", "PENDING_REVIEW")
                        .session(client))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].id", hasItem((int) prescriptionId)));

        mvc.perform(put("/api/shop/prescriptions/{id}/verify", prescriptionId).session(client))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"));

        Long verifiedBy =
                jdbc.queryForObject("SELECT verified_by FROM prescription WHERE id = ?", Long.class, prescriptionId);
        assertThat(verifiedBy).isEqualTo(clientId());
    }

    @Test
    void aClientCanRejectAPrescriptionWithAReason() throws Exception {
        MockHttpSession customer = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long prescriptionId = submitPrescription(customer);
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(put("/api/shop/prescriptions/{id}/reject", prescriptionId)
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Add power is missing.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("REJECTED"))
                .andExpect(jsonPath("$.data.rejectionReason").value("Add power is missing."));

        String reason = jdbc.queryForObject(
                "SELECT rejection_reason FROM prescription WHERE id = ?", String.class, prescriptionId);
        assertThat(reason).isEqualTo("Add power is missing.");
    }

    @Test
    void aRejectionWithoutAReasonIsRefused() throws Exception {
        MockHttpSession customer = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long prescriptionId = submitPrescription(customer);
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(put("/api/shop/prescriptions/{id}/reject", prescriptionId)
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void anAlreadyReviewedPrescriptionCannotBeReviewedAgain() throws Exception {
        MockHttpSession customer = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long prescriptionId = submitPrescription(customer);
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(put("/api/shop/prescriptions/{id}/verify", prescriptionId).session(client))
                .andExpect(status().isOk());
        mvc.perform(put("/api/shop/prescriptions/{id}/reject", prescriptionId)
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"changed my mind\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("INVALID_STATE"));
    }

    @Test
    void anOrderCannotBeApprovedUntilItsPrescriptionIsVerified() throws Exception {
        MockHttpSession customer = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long prescriptionId = submitPrescription(customer);
        long orderId = createOrder(customer, prescriptionId);
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(put("/api/shop/orders/{id}/approve", orderId).session(client))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("PRESCRIPTION_NOT_VERIFIED"));

        mvc.perform(put("/api/shop/prescriptions/{id}/verify", prescriptionId).session(client))
                .andExpect(status().isOk());

        mvc.perform(get("/api/shop/orders").param("status", "PENDING").session(client))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].id", hasItem((int) orderId)));

        mvc.perform(put("/api/shop/orders/{id}/approve", orderId).session(client))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        String status = jdbc.queryForObject("SELECT status FROM progressive_order WHERE id = ?", String.class, orderId);
        assertThat(status).isEqualTo("APPROVED");
    }

    @Test
    void aClientCanRejectAnOrderWithAReason() throws Exception {
        MockHttpSession customer = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long prescriptionId = submitPrescription(customer);
        long orderId = createOrder(customer, prescriptionId);
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(put("/api/shop/orders/{id}/reject", orderId)
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Lens out of stock.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.rejectionReason").value("Lens out of stock."));
    }

    @Test
    void anOrderThatHasAlreadyBeenDecidedCannotBeDecidedAgain() throws Exception {
        MockHttpSession customer = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long prescriptionId = submitPrescription(customer);
        long orderId = createOrder(customer, prescriptionId);
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(put("/api/shop/orders/{id}/reject", orderId)
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Lens out of stock.\"}"))
                .andExpect(status().isOk());

        mvc.perform(put("/api/shop/orders/{id}/reject", orderId)
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"again\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("INVALID_STATE"));
    }

    @Test
    void anInvalidQueueStatusIsRefusedWithTheEnvelope() throws Exception {
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(get("/api/shop/orders").param("status", "NOT_A_STATUS").session(client))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_FILTER"));
    }

    @Test
    void aCustomerCannotReachTheShopEndpoints() throws Exception {
        MockHttpSession customer = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);

        mvc.perform(get("/api/shop/prescriptions").session(customer)).andExpect(status().isForbidden());
        mvc.perform(put("/api/shop/orders/{id}/approve", 1L).session(customer)).andExpect(status().isForbidden());
    }

    @Test
    void anAnonymousCallerCannotReachTheShopEndpoints() throws Exception {
        mvc.perform(get("/api/shop/prescriptions")).andExpect(status().isUnauthorized());
    }

    private long createOrder(MockHttpSession session, long prescriptionId) throws Exception {
        mvc.perform(post("/api/orders")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(prescriptionId, anyProgressiveLensId())))
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

    private long clientId() {
        return jdbc.queryForObject("SELECT id FROM app_user WHERE email = ?", Long.class, CLIENT_EMAIL);
    }

    private long anyProgressiveLensId() {
        return jdbc.queryForObject("SELECT id FROM lens WHERE type = 'PROGRESSIVE' LIMIT 1", Long.class);
    }

    private static String orderJson(long prescriptionId, long lensId) {
        return "{\"prescriptionId\":%d,\"orderType\":\"PROGRESSIVE\",\"lensId\":%d,\"frameId\":null}"
                .formatted(prescriptionId, lensId);
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
