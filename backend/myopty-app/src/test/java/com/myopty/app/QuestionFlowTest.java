package com.myopty.app;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Proves the customer Q&amp;A: asking files the question under the session's
 * customer, reading returns only that customer's, and the public FAQ shows the
 * seeded answers without exposing who asked.
 *
 * <p>Running through the real filter chain is what makes the role and the scoping
 * claims meaningful — a mocked principal would prove neither that an anonymous
 * caller is stopped before the controller nor that the customer id comes from the
 * session rather than the request.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class QuestionFlowTest {

    private static final String CUSTOMER_EMAIL = "customer@myopty.local";
    private static final String CUSTOMER_PASSWORD = "customer123";
    private static final String CLIENT_EMAIL = "client@myopty.local";
    private static final String CLIENT_PASSWORD = "client123";

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
    void theSeededFaqIsPublicAndAnonymous() throws Exception {
        mvc.perform(get("/api/questions/faq"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.data[0].answerText").isNotEmpty())
                // The anonymous FAQ must not leak who asked: no customer field may
                // appear on any entry.
                .andExpect(jsonPath("$.data[*].customerId").doesNotExist())
                .andExpect(jsonPath("$.data[*].questionText").exists());
    }

    @Test
    void aCustomerAsksAndTheQuestionComesBackInTheirOwnList() throws Exception {
        MockHttpSession session = login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        String text = "Do you offer blue-light filtering lenses?";

        mvc.perform(post("/api/questions")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionText\":\"%s\"}".formatted(text)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.questionText").value(text))
                .andExpect(jsonPath("$.data.answered").value(false))
                .andExpect(jsonPath("$.data.id").isNumber());

        mvc.perform(get("/api/questions").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].questionText", hasItem(text)));
    }

    /**
     * The list is scoped to the session, not to a query parameter: a question
     * filed under another user must never appear in this customer's list.
     */
    @Test
    void theOwnListDoesNotIncludeAnotherCustomersQuestion() throws Exception {
        String otherText = "ANOTHER-CUSTOMER-QUESTION";
        Long clientId = jdbc.queryForObject("SELECT id FROM app_user WHERE email = ?", Long.class, CLIENT_EMAIL);
        jdbc.update(
                "INSERT INTO question (customer_id, question_text, is_faq) VALUES (?, ?, FALSE)", clientId, otherText);

        MockHttpSession session = login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        mvc.perform(get("/api/questions").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].questionText", not(hasItem(otherText))));
    }

    @Test
    void anAnonymousCallerCannotAsk() throws Exception {
        mvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionText\":\"Anyone there?\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));
    }

    @Test
    void aClientCannotAskACustomerQuestion() throws Exception {
        MockHttpSession session = login(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(post("/api/questions")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionText\":\"I am the shop, not the customer.\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void aBlankQuestionIsRefused() throws Exception {
        MockHttpSession session = login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);

        mvc.perform(post("/api/questions")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionText\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    private MockHttpSession login(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }
}
