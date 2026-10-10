package com.myopty.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
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
 * Proves the shop's frame add and edit stories end to end against a real database:
 * a client creates a frame, sees it listed, and edits it — and the role boundary
 * holds, because the endpoints sit under {@code /api/shop/**}.
 *
 * <p>There is no fake here. The parts worth exercising — the filter chain's role
 * rule, JSON decoding of the booleans, Bean Validation on the price and stock, the
 * SQL insert and update, and the {@code is_active} column mapping — are all real.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class FrameManagementFlowTest {

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
    void aClientCanAddAFrameAndItAppearsInTheList() throws Exception {
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(post("/api/shop/frames")
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(frameJson("Zephyr 500", "Navy", "Titanium", "7200.00", 7, true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.model").value("Zephyr 500"))
                .andExpect(jsonPath("$.data.active").value(true));

        mvc.perform(get("/api/shop/frames").session(client))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].model", hasItem("Zephyr 500")));

        Integer stockQty = jdbc.queryForObject(
                "SELECT stock_qty FROM frame WHERE model = ? ORDER BY id DESC LIMIT 1", Integer.class, "Zephyr 500");
        assertThat(stockQty).isEqualTo(7);
    }

    @Test
    void aClientCanEditAFrameSoItsDetailsStayAccurate() throws Exception {
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);
        long frameId = createFrame(client, "Kestrel 99", "Gunmetal", "Stainless Steel", "6800.00", 8, true);

        mvc.perform(put("/api/shop/frames/{id}", frameId)
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(frameJson("Kestrel 99", "Matte Grey", "Stainless Steel", "7100.00", 3, false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.color").value("Matte Grey"))
                .andExpect(jsonPath("$.data.price").value(7100.00))
                .andExpect(jsonPath("$.data.stockQty").value(3))
                .andExpect(jsonPath("$.data.active").value(false));

        String color = jdbc.queryForObject("SELECT color FROM frame WHERE id = ?", String.class, frameId);
        Boolean active = jdbc.queryForObject("SELECT is_active FROM frame WHERE id = ?", Boolean.class, frameId);
        assertThat(color).isEqualTo("Matte Grey");
        assertThat(active).isFalse();
    }

    @Test
    void editingAFrameThatDoesNotExistIsNotFound() throws Exception {
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(put("/api/shop/frames/{id}", 9_999_999L)
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(frameJson("Ghost", null, "Acetate", "1000.00", 1, true)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void aFrameWithAMissingPriceIsRefused() throws Exception {
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(post("/api/shop/frames")
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"model\":\"No Price\",\"material\":\"Acetate\",\"stockQty\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void aNegativeStockQuantityIsRefused() throws Exception {
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(post("/api/shop/frames")
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(frameJson("Bad Stock", null, "Acetate", "1000.00", -3, true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void aCustomerCannotManageFrames() throws Exception {
        MockHttpSession customer = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);

        mvc.perform(get("/api/shop/frames").session(customer)).andExpect(status().isForbidden());
        mvc.perform(post("/api/shop/frames")
                        .session(customer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(frameJson("Sneaky", null, "Acetate", "1.00", 1, true)))
                .andExpect(status().isForbidden());
    }

    @Test
    void anAnonymousCallerCannotManageFrames() throws Exception {
        mvc.perform(get("/api/shop/frames")).andExpect(status().isUnauthorized());
    }

    private long createFrame(
            MockHttpSession session,
            String model,
            String color,
            String material,
            String price,
            int stockQty,
            boolean active)
            throws Exception {
        mvc.perform(post("/api/shop/frames")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(frameJson(model, color, material, price, stockQty, active)))
                .andExpect(status().isCreated());
        return jdbc.queryForObject("SELECT id FROM frame WHERE model = ? ORDER BY id DESC LIMIT 1", Long.class, model);
    }

    private MockHttpSession loginAs(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private static String frameJson(
            String model, String color, String material, String price, int stockQty, boolean active) {
        String colorJson = color == null ? "null" : "\"%s\"".formatted(color);
        return "{\"model\":\"%s\",\"color\":%s,\"material\":\"%s\",\"price\":%s,\"stockQty\":%d,\"active\":%b}"
                .formatted(model, colorJson, material, price, stockQty, active);
    }
}
