package com.myopty.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
 * Proves the CUSTOMER/CLIENT access control works end to end against a real
 * database.
 *
 * <p>Everything here runs through the actual filter chain — no
 * {@code @WithMockUser}, no shortcut around the login endpoint — because the
 * contract under test is precisely that a password checked against the seeded
 * {@code app_user} rows produces a session the server remembers, and that the
 * role on that row is what opens or closes each path. A test that mocked the
 * principal would pass even if the session never survived the response.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowTest {

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
    void customerLoginEstablishesASessionTheServerRemembers() throws Exception {
        MockHttpSession session = login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);

        mvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(CUSTOMER_EMAIL))
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"));
    }

    @Test
    void clientLoginCarriesTheClientRole() throws Exception {
        MockHttpSession session = login(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(CLIENT_EMAIL))
                .andExpect(jsonPath("$.data.role").value("CLIENT"));
    }

    @Test
    void wrongPasswordIsRefusedWithTheErrorEnvelope() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(CUSTOMER_EMAIL, "not-the-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void unknownEmailLooksExactlyLikeWrongPassword() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("nobody@myopty.local", "irrelevant")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void anonymousRequestIsRefusedBeforeReachingTheController() throws Exception {
        mvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));
    }

    @Test
    void customerCannotReachClientRoutes() throws Exception {
        MockHttpSession session = login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);

        mvc.perform(get("/api/shop/orders").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    /**
     * The other half of the role rule: the client passes authorization and
     * reaches the dispatcher, where the test stops mattering — there is no
     * controller on the other side yet, so the answer is 404. The status is the
     * assertion: 403 would mean the rule wrongly held the door.
     */
    @Test
    void clientPassesAuthorizationOnClientRoutes() throws Exception {
        MockHttpSession session = login(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(get("/api/shop/orders").session(session)).andExpect(status().isNotFound());
    }

    /**
     * The account is disabled and restored inside this one test: JUnit does not
     * promise an order between methods, and a leftover DISABLED row would make
     * every later login test fail for a reason of its own.
     */
    @Test
    void disabledAccountsCannotLogInEvenWithTheRightPassword() throws Exception {
        jdbc.update("UPDATE app_user SET status = 'DISABLED' WHERE email = ?", CUSTOMER_EMAIL);
        try {
            mvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginBody(CUSTOMER_EMAIL, CUSTOMER_PASSWORD)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("ACCOUNT_NOT_ACTIVE"));
        } finally {
            jdbc.update("UPDATE app_user SET status = 'ACTIVE' WHERE email = ?", CUSTOMER_EMAIL);
        }
    }

    @Test
    void logoutEndsTheSession() throws Exception {
        MockHttpSession session = login(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);

        mvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // The servlet contract says the session object itself is dead; asking it
        // for anything afterwards is how that is proven from the outside.
        assertThatThrownBy(() -> session.getAttribute("SPRING_SECURITY_CONTEXT"))
                .isInstanceOf(IllegalStateException.class);

        // And a caller arriving the way a browser would — no session at all —
        // is anonymous again.
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void healthStaysPublic() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    private MockHttpSession login(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).as("a successful login must leave a session behind").isNotNull();
        return session;
    }

    private String loginBody(String email, String password) {
        return "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password);
    }
}
