package com.myopty.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Proves the fulfilment stories end to end: approval quotes a receive date, the
 * shop drives the order through processing, ready and dispatched, each move is
 * recorded as a customer notification, the customer can read their order and its
 * notifications, and the email actually reaches an SMTP server.
 *
 * <p>There is no fake here. A Mailpit container stands in for the provider the way
 * {@code MailSendingTest} uses it — the same container the team reads at :8025
 * locally — so "the customer was emailed" is asserted against a real socket, not a
 * mock that would pass even if the message were never built.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class OrderFulfilmentFlowTest {

    private static final String CUSTOMER_EMAIL = "customer@myopty.local";
    private static final String CUSTOMER_PASSWORD = "customer123";
    private static final String CLIENT_EMAIL = "client@myopty.local";
    private static final String CLIENT_PASSWORD = "client123";

    private static final String VALID_VALUES = """
            {"sphLeft": "+06.25", "progressive": true}
            """;

    private static final int SMTP_PORT = 1025;
    private static final int API_PORT = 8025;

    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.0")
            .withDatabaseName("myopty")
            .withUsername("myopty")
            .withPassword("myopty");

    @Container
    static final GenericContainer<?> MAILPIT =
            new GenericContainer<>("axllent/mailpit:v1.31").withExposedPorts(SMTP_PORT, API_PORT);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("MAIL_HOST", MAILPIT::getHost);
        registry.add("MAIL_PORT", () -> MAILPIT.getMappedPort(SMTP_PORT));
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void theWholeFulfilmentJourneyIsRecordedAndNotified() throws Exception {
        MockHttpSession customer = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long prescriptionId = submitPrescription(customer);
        long orderId = createOrder(customer, prescriptionId);
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(put("/api/shop/prescriptions/{id}/verify", prescriptionId).session(client))
                .andExpect(status().isOk());

        mvc.perform(put("/api/shop/orders/{id}/approve", orderId).session(client))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.receiveDate").isNotEmpty());

        mvc.perform(get("/api/shop/orders/active").session(client))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].id", hasItem((int) orderId)));

        mvc.perform(put("/api/shop/orders/{id}/receive-date", orderId)
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receiveDate\":\"2026-12-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.receiveDate").value("2026-12-01"));

        mvc.perform(put("/api/shop/orders/{id}/processing", orderId).session(client))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PROCESSING"));
        mvc.perform(put("/api/shop/orders/{id}/ready", orderId).session(client))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("READY"));
        mvc.perform(put("/api/shop/orders/{id}/dispatched", orderId).session(client))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DISPATCHED"));

        mvc.perform(get("/api/orders/{id}", orderId).session(customer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DISPATCHED"))
                .andExpect(jsonPath("$.data.receiveDate").value("2026-12-01"));
        mvc.perform(get("/api/orders").session(customer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].id", hasItem((int) orderId)));

        mvc.perform(get("/api/notifications")
                        .param("orderId", String.valueOf(orderId))
                        .session(customer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(4)))
                .andExpect(jsonPath("$.data[0].message", containsString("dispatched")))
                .andExpect(jsonPath("$.data[0].orderNumber").isNotEmpty())
                .andExpect(jsonPath("$.data[*].orderId", hasItem((int) orderId)));
        mvc.perform(get("/api/notifications").session(customer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].orderId", hasItem((int) orderId)));

        Integer rows = jdbc.queryForObject(
                "SELECT COUNT(*) FROM order_notification WHERE order_id = ?", Integer.class, orderId);
        assertThat(rows).isEqualTo(4);
        String lastStatus = jdbc.queryForObject(
                "SELECT status FROM order_notification WHERE order_id = ? ORDER BY id DESC LIMIT 1",
                String.class,
                orderId);
        assertThat(lastStatus).isEqualTo("SENT");

        awaitMailpitHasSubject("MyOpty order update");
    }

    @Test
    void theProductionLineMaySkipAStepButNotGoBackwards() throws Exception {
        MockHttpSession customer = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long prescriptionId = submitPrescription(customer);
        long orderId = createOrder(customer, prescriptionId);
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(put("/api/shop/prescriptions/{id}/verify", prescriptionId).session(client))
                .andExpect(status().isOk());
        mvc.perform(put("/api/shop/orders/{id}/approve", orderId).session(client))
                .andExpect(status().isOk());

        mvc.perform(put("/api/shop/orders/{id}/ready", orderId).session(client))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("READY"));

        mvc.perform(put("/api/shop/orders/{id}/ready", orderId).session(client))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("INVALID_STATE"));
    }

    @Test
    void aPendingOrderCannotBeMoved() throws Exception {
        MockHttpSession customer = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long prescriptionId = submitPrescription(customer);
        long orderId = createOrder(customer, prescriptionId);
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(put("/api/shop/orders/{id}/processing", orderId).session(client))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("INVALID_STATE"));
    }

    @Test
    void aCustomerCannotDriveTheProductionLine() throws Exception {
        MockHttpSession customer = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);

        mvc.perform(put("/api/shop/orders/{id}/processing", 1L).session(customer))
                .andExpect(status().isForbidden());
    }

    @Test
    void notificationsRequireASession() throws Exception {
        mvc.perform(get("/api/notifications")).andExpect(status().isUnauthorized());
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

    /**
     * SMTP delivery is asynchronous even against a container on loopback, so the
     * assertion polls Mailpit's REST API instead of assuming the receive happened
     * before the request returned.
     */
    private void awaitMailpitHasSubject(String subject) throws Exception {
        URI messages =
                URI.create("http://" + MAILPIT.getHost() + ":" + MAILPIT.getMappedPort(API_PORT) + "/api/v1/messages");
        HttpClient http = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        Instant deadline = Instant.now().plusSeconds(10);
        String lastSeen = "no messages yet";

        while (Instant.now().isBefore(deadline)) {
            HttpResponse<String> response =
                    http.send(HttpRequest.newBuilder(messages).GET().build(), HttpResponse.BodyHandlers.ofString());
            JsonNode body = objectMapper.readTree(response.body());
            lastSeen = "total=" + body.path("total").asInt();
            if (body.path("total").asInt() > 0) {
                for (JsonNode mail : body.path("messages")) {
                    if (Objects.equals(subject, mail.path("Subject").asText())) {
                        return;
                    }
                }
                lastSeen = "messages without the expected subject";
            }
            Thread.sleep(250);
        }
        throw new AssertionError("Mailpit never received '" + subject + "' (" + lastSeen + ")");
    }
}
