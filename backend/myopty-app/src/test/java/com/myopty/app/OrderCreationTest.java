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
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Proves the order stories end to end against a real database: a logged-in
 * customer links one of their prescriptions to a frame and lens with a chosen
 * order type, and the resulting row is real.
 *
 * <p>There is no fake here. The parts worth exercising — the filter chain, JSON
 * decoding of the enum, Bean Validation, the service's ownership check, the SQL
 * insert and the foreign keys that stand in for the catalog — are all real. The
 * frame and lens are looked up from the seeded catalog rather than hard-coded, so
 * the test does not depend on a particular id.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class OrderCreationTest {

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
    void aCustomerCanLinkAPrescriptionToAFrameAndLens() throws Exception {
        MockHttpSession session = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long prescriptionId = submitPrescription(session);
        long frameId = anyActiveFrameId();
        long lensId = anyProgressiveLensId();

        mvc.perform(post("/api/orders")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(prescriptionId, "PROGRESSIVE", lensId, frameId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderType").value("PROGRESSIVE"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.prescriptionId").value(prescriptionId))
                .andExpect(jsonPath("$.data.frameId").value(frameId))
                .andExpect(jsonPath("$.data.lensId").value(lensId))
                .andExpect(jsonPath("$.data.orderNumber").isNotEmpty());

        Long linkedPrescription = jdbc.queryForObject(
                "SELECT prescription_id FROM progressive_order ORDER BY id DESC LIMIT 1", Long.class);
        assertThat(linkedPrescription).isEqualTo(prescriptionId);
    }

    @Test
    void aLensesOnlyOrderDoesNotNeedAFrame() throws Exception {
        MockHttpSession session = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long prescriptionId = submitPrescription(session);
        long lensId = anyProgressiveLensId();

        mvc.perform(post("/api/orders")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(prescriptionId, "SINGLE_VISION", lensId, null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.frameId").doesNotExist())
                .andExpect(jsonPath("$.data.orderType").value("SINGLE_VISION"));
    }

    @Test
    void theCustomerCanReadBackTheirOwnOrder() throws Exception {
        MockHttpSession session = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long prescriptionId = submitPrescription(session);
        long orderId = createOrder(session, prescriptionId, anyProgressiveLensId(), anyActiveFrameId());

        mvc.perform(get("/api/orders/{id}", orderId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(orderId));
    }

    @Test
    void anotherCustomerCannotReadTheOrder() throws Exception {
        MockHttpSession session = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long prescriptionId = submitPrescription(session);
        long orderId = createOrder(session, prescriptionId, anyProgressiveLensId(), anyActiveFrameId());

        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);
        mvc.perform(get("/api/orders/{id}", orderId).session(client)).andExpect(status().isNotFound());
    }

    @Test
    void aPrescriptionThatIsNotTheCallersIsRefused() throws Exception {
        MockHttpSession session = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        Long clientId = jdbc.queryForObject("SELECT id FROM app_user WHERE email = ?", Long.class, CLIENT_EMAIL);
        jdbc.update(
                "INSERT INTO prescription (customer_id, sph_left, verification_status) VALUES (?, '+1.00', 'PENDING_REVIEW')",
                clientId);
        long foreignPrescription = jdbc.queryForObject(
                "SELECT id FROM prescription WHERE customer_id = ? ORDER BY id DESC LIMIT 1", Long.class, clientId);

        mvc.perform(post("/api/orders")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(foreignPrescription, "PROGRESSIVE", anyProgressiveLensId(), null)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void aRejectedPrescriptionCannotBeOrdered() throws Exception {
        MockHttpSession session = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long prescriptionId = submitPrescription(session);
        jdbc.update("UPDATE prescription SET verification_status = 'REJECTED' WHERE id = ?", prescriptionId);

        mvc.perform(post("/api/orders")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(prescriptionId, "PROGRESSIVE", anyProgressiveLensId(), null)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("PRESCRIPTION_REJECTED"));
    }

    @Test
    void anUnknownLensIsRefusedWithTheEnvelope() throws Exception {
        MockHttpSession session = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long prescriptionId = submitPrescription(session);

        mvc.perform(post("/api/orders")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(prescriptionId, "PROGRESSIVE", 999_999L, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("UNKNOWN_PRODUCT"));
    }

    @Test
    void anOrderWithNoOrderTypeIsRefused() throws Exception {
        MockHttpSession session = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long prescriptionId = submitPrescription(session);

        mvc.perform(post("/api/orders")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prescriptionId\":" + prescriptionId + ",\"lensId\":" + anyProgressiveLensId()
                                + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void theCustomerCanListTheirOwnPrescriptions() throws Exception {
        MockHttpSession session = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long prescriptionId = submitPrescription(session);

        mvc.perform(get("/api/prescriptions").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(prescriptionId));
    }

    @Test
    void anAnonymousOrderIsRefused() throws Exception {
        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(1L, "PROGRESSIVE", 1L, null)))
                .andExpect(status().isUnauthorized());
    }

    private long createOrder(MockHttpSession session, long prescriptionId, long lensId, Long frameId) throws Exception {
        mvc.perform(post("/api/orders")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(prescriptionId, "PROGRESSIVE", lensId, frameId)))
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

    private static String orderJson(long prescriptionId, String orderType, long lensId, Long frameId) {
        return "{\"prescriptionId\":%d,\"orderType\":\"%s\",\"lensId\":%d,\"frameId\":%s}"
                .formatted(prescriptionId, orderType, lensId, frameId == null ? "null" : frameId);
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
