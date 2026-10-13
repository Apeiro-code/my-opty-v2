package com.myopty.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
 * Proves a signed-in customer can edit their own profile and that the change
 * survives the very next request.
 *
 * <p>The "survives" half is the point of doing this against a real session rather
 * than a mocked principal: the profile fields are written to the database, but the
 * name the server reports comes from the session principal established at login.
 * A test that only checked the database would pass even if the session kept
 * handing back the old name.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class ProfileEditFlowTest {

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
    void customerEditsProfileAndTheNextRequestSeesIt() throws Exception {
        MockHttpSession session = login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);

        mvc.perform(put("/api/auth/me")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody("Kavindi Perera", "+94771234567", "42 Galle Road, Colombo", "SMS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Kavindi Perera"))
                .andExpect(jsonPath("$.data.phone").value("+94771234567"))
                .andExpect(jsonPath("$.data.address").value("42 Galle Road, Colombo"))
                .andExpect(jsonPath("$.data.preferredContact").value("SMS"))
                .andExpect(jsonPath("$.data.email").value(CUSTOMER_EMAIL));

        mvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Kavindi Perera"))
                .andExpect(jsonPath("$.data.phone").value("+94771234567"))
                .andExpect(jsonPath("$.data.address").value("42 Galle Road, Colombo"))
                .andExpect(jsonPath("$.data.preferredContact").value("SMS"));
    }

    /**
     * The form submits the same fields twice in a row — the second edit must
     * update the existing {@code customer_profile} row, not fail on the primary
     * key or silently do nothing.
     */
    @Test
    void aSecondEditUpdatesTheExistingProfileRow() throws Exception {
        MockHttpSession session = login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);

        mvc.perform(put("/api/auth/me")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody("First Name", null, "First address", "EMAIL")))
                .andExpect(status().isOk());

        mvc.perform(put("/api/auth/me")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody("Second Name", null, "Second address", "PHONE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Second Name"))
                .andExpect(jsonPath("$.data.address").value("Second address"))
                .andExpect(jsonPath("$.data.preferredContact").value("PHONE"));

        assertThat(jdbc.queryForObject(
                        "SELECT COUNT(*) FROM customer_profile WHERE user_id = "
                                + "(SELECT id FROM app_user WHERE email = ?)",
                        Long.class,
                        CUSTOMER_EMAIL))
                .as("a profile edit must upsert, not accumulate rows")
                .isEqualTo(1);
    }

    @Test
    void emailCannotBeChangedThroughTheProfileEdit() throws Exception {
        MockHttpSession session = login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);

        mvc.perform(put("/api/auth/me")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Someone\",\"email\":\"hacker@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(CUSTOMER_EMAIL));
    }

    @Test
    void aPhoneNumberAlreadyInUseIsRefused() throws Exception {
        String takenPhone = "+94770000000";
        jdbc.update("UPDATE app_user SET phone = ? WHERE email = ?", takenPhone, CLIENT_EMAIL);
        try {
            MockHttpSession session = login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);

            mvc.perform(put("/api/auth/me")
                            .session(session)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(profileBody("Kavindi Perera", takenPhone, null, "PHONE")))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error.code").value("PHONE_EXISTS"));
        } finally {
            jdbc.update("UPDATE app_user SET phone = NULL WHERE email = ?", CLIENT_EMAIL);
        }
    }

    @Test
    void aBlankNameIsRefused() throws Exception {
        MockHttpSession session = login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);

        mvc.perform(put("/api/auth/me")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody("   ", null, null, "EMAIL")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void aClientCannotEditACustomerProfile() throws Exception {
        MockHttpSession session = login(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(put("/api/auth/me")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody("Not Allowed", null, null, "EMAIL")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void anAnonymousCallerCannotEditAProfile() throws Exception {
        mvc.perform(put("/api/auth/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody("Anonymous", null, null, "EMAIL")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));
    }

    private MockHttpSession login(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private String profileBody(String name, String phone, String address, String preferredContact) {
        return "{\"name\":%s,\"phone\":%s,\"address\":%s,\"preferredContact\":%s}"
                .formatted(json(name), json(phone), json(address), json(preferredContact));
    }

    private String json(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }
}
