package com.myopty.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
 * Proves the low-stock alert actually reaches a socket: an order that takes a
 * lens across its reorder threshold emails the configured shop address, and
 * Mailpit receives it.
 *
 * <p>The point is the path configuration alone cannot prove — the crossing rule,
 * the after-commit send and the {@code MAIL_*} SMTP handshake, together, against
 * a real database and a real SMTP server. The recipient and the switch are set
 * through the same {@code LOW_STOCK_*} contract {@code .env} uses, so the test
 * exercises the wiring a deployment uses rather than reaching past it.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class LowStockAlertMailTest {

    private static final int SMTP_PORT = 1025;
    private static final int API_PORT = 8025;
    private static final String ALERT_SUBJECT = "MyOpty low-stock alert";
    private static final String ALERT_RECIPIENT = "alerts@myopty.local";

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
        registry.add("LOW_STOCK_ALERTS_ENABLED", () -> "true");
        registry.add("LOW_STOCK_ALERT_EMAIL", () -> ALERT_RECIPIENT);
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void anOrderThatCrossesTheThresholdEmailsTheShop() throws Exception {
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);
        MockHttpSession customer = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        // Stock 6 is above the default threshold of 5, so buying 2 crosses into low.
        long lensId = createProgressiveLens(client, "Alert Test Progressive 1.60", 6);
        long prescriptionId = submitPrescription(customer);

        mvc.perform(post("/api/orders")
                        .session(customer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(prescriptionId, lensId)))
                .andExpect(status().isCreated());

        awaitMailpitHasSubject(ALERT_SUBJECT);
    }

    private long createProgressiveLens(MockHttpSession client, String name, int stock) throws Exception {
        mvc.perform(post("/api/shop/lenses")
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"name\":\"%s\",\"type\":\"PROGRESSIVE\",\"price\":\"1000.00\",\"stockQty\":%d,\"active\":true}"
                                        .formatted(name, stock)))
                .andExpect(status().isCreated());
        return jdbc.queryForObject("SELECT id FROM lens WHERE name = ? ORDER BY id DESC LIMIT 1", Long.class, name);
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

    private static String orderJson(long prescriptionId, long lensId) {
        return "{\"prescriptionId\":%d,\"orderType\":\"PROGRESSIVE\",\"lensId\":%d,\"frameId\":null,\"quantity\":2}"
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
     * before the order response returned.
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
