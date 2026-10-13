package com.myopty.app;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import java.time.Instant;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Proves the shop's answering story end to end: a client lists the open questions,
 * posts an answer, the question leaves the queue, the customer who asked it can
 * now read the answer, and the customer is emailed. It also proves the role
 * boundary: a customer and an anonymous caller cannot reach {@code /api/shop/**}.
 *
 * <p>The mail path is real, not mocked: the context points at a throwaway Mailpit
 * container, and the email assertion polls its API — the same container the team
 * reads at :8025 locally, so the path under test is the path the team uses. There
 * is no fake database either; the filter chain's role rule, Bean Validation, the
 * service's state rules and the foreign keys all run for real.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class QuestionAnswerFlowTest {

    private static final String CUSTOMER_EMAIL = "customer@myopty.local";
    private static final String CUSTOMER_PASSWORD = "customer123";
    private static final String CLIENT_EMAIL = "client@myopty.local";
    private static final String CLIENT_PASSWORD = "client123";
    private static final String ANSWER_EMAIL_SUBJECT = "Your MyOpty question has been answered";
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
        // Point the MAIL_* contract at the container beside us rather than a Mailpit
        // that might be running on the host.
        registry.add("MAIL_HOST", MAILPIT::getHost);
        registry.add("MAIL_PORT", () -> MAILPIT.getMappedPort(SMTP_PORT));
    }

    @Autowired
    MockMvc mvc;

    @Test
    void theShopQueueListsAnOpenQuestionAndAnsweringClearsIt() throws Exception {
        MockHttpSession customer = login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        String text = "Do you repair frames bought elsewhere?";
        long id = ask(customer, text);

        MockHttpSession client = login(CLIENT_EMAIL, CLIENT_PASSWORD);
        mvc.perform(get("/api/shop/questions").session(client))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[*].questionText", hasItem(text)))
                // The shop must see who asked, so it can reply to a named person.
                .andExpect(
                        jsonPath("$.data[?(@.id == " + id + ")].customerEmail").value(contains(CUSTOMER_EMAIL)));

        String answer = "Yes — bring it in and we will assess the repair free of charge.";
        mvc.perform(put("/api/shop/questions/" + id + "/answer")
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answerText\":\"%s\"}".formatted(answer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.answered").value(true))
                .andExpect(jsonPath("$.data.answerText").value(answer));

        mvc.perform(get("/api/shop/questions").session(client))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].questionText", not(hasItem(text))));
    }

    @Test
    void theCustomerCanReadTheAnswerOnceItIsPosted() throws Exception {
        MockHttpSession customer = login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        String text = "Can I get my lenses the same day?";
        long id = ask(customer, text);

        MockHttpSession client = login(CLIENT_EMAIL, CLIENT_PASSWORD);
        String answer = "Single-vision lenses can often be ready the same day; progressive ones need longer.";
        mvc.perform(put("/api/shop/questions/" + id + "/answer")
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answerText\":\"%s\"}".formatted(answer)))
                .andExpect(status().isOk());

        mvc.perform(get("/api/questions").session(customer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == " + id + ")].answered").value(contains(true)))
                .andExpect(jsonPath("$.data[?(@.id == " + id + ")].answerText").value(contains(answer)));
    }

    @Test
    void theCustomerIsEmailedWhenTheirQuestionIsAnswered() throws Exception {
        MockHttpSession customer = login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long id = ask(customer, "Are the shop's prices negotiable?");

        MockHttpSession client = login(CLIENT_EMAIL, CLIENT_PASSWORD);
        mvc.perform(put("/api/shop/questions/" + id + "/answer")
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answerText\":\"Prices are fixed, but we do run seasonal promotions.\"}"))
                .andExpect(status().isOk());

        awaitMailpitHasSubject(ANSWER_EMAIL_SUBJECT);
    }

    @Test
    void aBlankAnswerIsRefused() throws Exception {
        MockHttpSession client = login(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(put("/api/shop/questions/1/answer")
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answerText\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void answeringAnAlreadyAnsweredQuestionIsRefused() throws Exception {
        MockHttpSession customer = login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long id = ask(customer, "Is there parking near the shop?");
        MockHttpSession client = login(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(put("/api/shop/questions/" + id + "/answer")
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answerText\":\"Yes, there is free parking in front.\"}"))
                .andExpect(status().isOk());

        mvc.perform(put("/api/shop/questions/" + id + "/answer")
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answerText\":\"Answered again.\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("ALREADY_ANSWERED"));
    }

    @Test
    void answeringAQuestionThatDoesNotExistIsNotFound() throws Exception {
        MockHttpSession client = login(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(put("/api/shop/questions/99999999/answer")
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answerText\":\"Nobody asked this.\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("QUESTION_NOT_FOUND"));
    }

    @Test
    void aCustomerCannotReachTheShopQueue() throws Exception {
        MockHttpSession customer = login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);

        mvc.perform(get("/api/shop/questions").session(customer))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void anAnonymousCallerCannotReachTheShopQueue() throws Exception {
        mvc.perform(get("/api/shop/questions"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));
    }

    private long ask(MockHttpSession customer, String text) throws Exception {
        MvcResult result = mvc.perform(post("/api/questions")
                        .session(customer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionText\":\"%s\"}".formatted(text)))
                .andExpect(status().isCreated())
                .andReturn();
        return new ObjectMapper()
                .readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();
    }

    private MockHttpSession login(String email, String password) throws Exception {
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
     * before {@code send} returned.
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
