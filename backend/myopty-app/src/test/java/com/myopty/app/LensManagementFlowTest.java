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
 * Proves the shop's add-a-lens story end to end against a real database: a client
 * creates a lens, sees it listed, and the role boundary holds, because the
 * endpoints sit under {@code /api/shop/**}.
 *
 * <p>There is no fake here. The parts worth exercising — the filter chain's role
 * rule, JSON decoding of the {@code LensType} enum, Bean Validation on the name,
 * type, price and stock, the SQL insert, and the {@code is_active} column mapping —
 * are all real.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class LensManagementFlowTest {

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
    void aClientCanAddALensAndItAppearsInTheCollection() throws Exception {
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(post("/api/shop/lenses")
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(lensJson("Drive Progressive 1.60", "PROGRESSIVE", "Polarised", "14200.00", 6, true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Drive Progressive 1.60"))
                .andExpect(jsonPath("$.data.type").value("PROGRESSIVE"))
                .andExpect(jsonPath("$.data.active").value(true));

        mvc.perform(get("/api/shop/lenses").session(client))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].name", hasItem("Drive Progressive 1.60")));

        String type = jdbc.queryForObject(
                "SELECT type FROM lens WHERE name = ? ORDER BY id DESC LIMIT 1",
                String.class,
                "Drive Progressive 1.60");
        assertThat(type).isEqualTo("PROGRESSIVE");
    }

    @Test
    void aLensWithAMissingNameIsRefused() throws Exception {
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(post("/api/shop/lenses")
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"BIFOCAL\",\"price\":\"5200.00\",\"stockQty\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void aLensWithAMissingTypeIsRefused() throws Exception {
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(post("/api/shop/lenses")
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"No Type\",\"price\":\"5200.00\",\"stockQty\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void anUnknownLensTypeIsRefused() throws Exception {
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(post("/api/shop/lenses")
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(lensJson("Made Up", "TORIC", "None", "1000.00", 1, true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void aCustomerCannotAddALens() throws Exception {
        MockHttpSession customer = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);

        mvc.perform(get("/api/shop/lenses").session(customer)).andExpect(status().isForbidden());
        mvc.perform(post("/api/shop/lenses")
                        .session(customer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(lensJson("Sneaky Lens", "SINGLE_VISION", null, "1.00", 1, true)))
                .andExpect(status().isForbidden());
    }

    @Test
    void anAnonymousCallerCannotAddALens() throws Exception {
        mvc.perform(get("/api/shop/lenses")).andExpect(status().isUnauthorized());
    }

    @Test
    void aClientCanEditALensSoItsDetailsStayAccurate() throws Exception {
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);
        long lensId = createLens(client, "Essential Bifocal", "BIFOCAL", "Anti-reflective", "5200.00", 14, true);

        mvc.perform(put("/api/shop/lenses/{id}", lensId)
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(lensJson("Essential Bifocal", "BIFOCAL", "Polarised", "5400.00", 9, false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.coating").value("Polarised"))
                .andExpect(jsonPath("$.data.price").value(5400.00))
                .andExpect(jsonPath("$.data.stockQty").value(9))
                .andExpect(jsonPath("$.data.active").value(false));

        String coating = jdbc.queryForObject("SELECT coating FROM lens WHERE id = ?", String.class, lensId);
        Boolean active = jdbc.queryForObject("SELECT is_active FROM lens WHERE id = ?", Boolean.class, lensId);
        assertThat(coating).isEqualTo("Polarised");
        assertThat(active).isFalse();
    }

    @Test
    void editingALensThatDoesNotExistIsNotFound() throws Exception {
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(put("/api/shop/lenses/{id}", 9_999_999L)
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(lensJson("Ghost", "SINGLE_VISION", null, "1000.00", 1, true)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void aClientCanFileALensUnderACategory() throws Exception {
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);
        Long categoryId = jdbc.queryForObject("SELECT id FROM category WHERE slug = ?", Long.class, "progressive");

        mvc.perform(post("/api/shop/lenses")
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(lensJson(
                                "Comfort Progressive 1.60",
                                "PROGRESSIVE",
                                "Anti-reflective",
                                "12500.00",
                                7,
                                true,
                                categoryId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.categoryId").value(categoryId));

        Long stored = jdbc.queryForObject(
                "SELECT category_id FROM lens WHERE name = ? ORDER BY id DESC LIMIT 1",
                Long.class,
                "Comfort Progressive 1.60");
        assertThat(stored).isEqualTo(categoryId);
    }

    @Test
    void aLensFiledUnderAFrameCategoryIsRefused() throws Exception {
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);
        Long frameCategory = jdbc.queryForObject("SELECT id FROM category WHERE slug = ?", Long.class, "men");

        mvc.perform(post("/api/shop/lenses")
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(lensJson("Odd Lens", "SINGLE_VISION", null, "1000.00", 1, true, frameCategory)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_CATEGORY"));
    }

    @Test
    void aLensFiledUnderACategoryThatDoesNotExistIsRefused() throws Exception {
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(post("/api/shop/lenses")
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(lensJson("Odd Lens", "SINGLE_VISION", null, "1000.00", 1, true, 9_999_999L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_CATEGORY"));
    }

    private long createLens(
            MockHttpSession session,
            String name,
            String type,
            String coating,
            String price,
            int stockQty,
            boolean active)
            throws Exception {
        mvc.perform(post("/api/shop/lenses")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(lensJson(name, type, coating, price, stockQty, active)))
                .andExpect(status().isCreated());
        return jdbc.queryForObject("SELECT id FROM lens WHERE name = ? ORDER BY id DESC LIMIT 1", Long.class, name);
    }

    private MockHttpSession loginAs(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private static String lensJson(
            String name, String type, String coating, String price, int stockQty, boolean active) {
        return lensJson(name, type, coating, price, stockQty, active, null);
    }

    private static String lensJson(
            String name, String type, String coating, String price, int stockQty, boolean active, Long categoryId) {
        String coatingJson = coating == null ? "null" : "\"%s\"".formatted(coating);
        String categoryJson = categoryId == null ? "null" : categoryId.toString();
        return "{\"name\":\"%s\",\"type\":\"%s\",\"coating\":%s,\"price\":%s,\"stockQty\":%d,\"active\":%b,\"categoryId\":%s}"
                .formatted(name, type, coatingJson, price, stockQty, active, categoryJson);
    }
}
