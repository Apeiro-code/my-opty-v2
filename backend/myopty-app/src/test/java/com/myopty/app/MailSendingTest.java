package com.myopty.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

/**
 * Proves the MAIL_* contract actually reaches a socket.
 *
 * <p>Configuration that is written but never exercised is indistinguishable from working
 * configuration until the first notification is sent in production. This boots the real
 * context, points it at a throwaway Mailpit container, sends a message through the
 * auto-configured {@link JavaMailSender}, and then asks Mailpit's own API whether the
 * message arrived — the same container the team reads at :8025 locally, so the path under
 * test is the path the team uses.
 *
 * <p>{@code MAIL_HOST} is overridden with the container's address so the test does not
 * silently pass against a Mailpit running on the host. A MySQL container is present even
 * though mail has nothing to do with the database: Spring Data JDBC resolves its dialect by
 * opening a connection during startup, so a full context needs a real database — flyway is
 * switched off, because the schema is not what this test is about.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MailSendingTest {

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

    /**
     * Sets the contract variables rather than {@code spring.mail.*} directly: the point is
     * to prove that {@code application.yml} reads {@code MAIL_HOST}/{@code MAIL_PORT} the
     * way every other module's configuration does.
     */
    @DynamicPropertySource
    static void mailProperties(DynamicPropertyRegistry registry) {
        registry.add("MAIL_HOST", MAILPIT::getHost);
        registry.add("MAIL_PORT", () -> MAILPIT.getMappedPort(SMTP_PORT));
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private JavaMailSender mailSender;

    @Test
    void sendsAMessageThatMailpitReceives() throws Exception {
        // The sender must be configured from MAIL_HOST/MAIL_PORT, not left on JavaMail's
        // localhost:25 defaults — otherwise this test would be racing a local Mailpit
        // instead of the container beside it.
        JavaMailSenderImpl sender = (JavaMailSenderImpl) mailSender;
        assertThat(sender.getHost()).isEqualTo(MAILPIT.getHost());
        assertThat(sender.getPort()).isEqualTo(MAILPIT.getMappedPort(SMTP_PORT));

        var message = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom("no-reply@myopty.local");
        helper.setTo("developer@myopty.local");
        helper.setSubject("MailSendingTest: the mail path works");
        helper.setText("If this is visible in Mailpit, the configured sender works end to end.");
        mailSender.send(message);

        awaitMailpitHasSubject("MailSendingTest: the mail path works");
    }

    /**
     * SMTP delivery is asynchronous even against a container on loopback, so the assertion
     * polls Mailpit's REST API instead of assuming the receive happened before {@code send}
     * returned.
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
