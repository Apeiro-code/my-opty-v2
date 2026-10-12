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
 * Proves the customer's frames-browsing story end to end against a real database:
 * an anonymous visitor reads the shop wall at {@code /api/frames} and sees every
 * active frame — and only active frames — in the visitor-safe shape.
 *
 * <p>There is no fake here. The parts worth exercising — the filter chain's public
 * path, the {@code is_active} filter, the {@code image_url} mapping and the shape
 * of the public response — are all real.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class StorefrontBrowseFlowTest {

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
    void anAnonymousVisitorSeesActiveFramesButNotHiddenOnes() throws Exception {
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);
        createFrame(client, "Zephyr 701", "Navy", "Titanium", 7, true);
        createFrame(client, "Zephyr 702", "Clear", "Acetate", 7, false);

        mvc.perform(get("/api/frames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[*].model", hasItem("Zephyr 701")))
                .andExpect(jsonPath("$.data[*].model", not(hasItem("Zephyr 702"))));
    }

    @Test
    void theWallCarriesTheThumbnailAndInStockFlagButNoInternalFields() throws Exception {
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);
        long frameId = createFrame(client, "Zephyr 800", "Navy", "Titanium", 7, true);
        jdbc.update("UPDATE frame SET image_url = ? WHERE id = ?", "/images/frames/zephyr-800.jpg", frameId);

        mvc.perform(get("/api/frames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.data[?(@.model == 'Zephyr 800')].imageUrl", hasItem("/images/frames/zephyr-800.jpg")))
                .andExpect(jsonPath("$.data[?(@.model == 'Zephyr 800')].inStock", hasItem(true)))
                .andExpect(jsonPath("$.data[*].stockQty").doesNotExist())
                .andExpect(jsonPath("$.data[*].active").doesNotExist())
                .andExpect(jsonPath("$.data[*].categoryId").doesNotExist());
    }

    @Test
    void anActiveFrameWithNothingOnTheShelfIsShownOutOfStock() throws Exception {
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);
        createFrame(client, "Zephyr 900", "Navy", "Titanium", 0, true);

        mvc.perform(get("/api/frames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.model == 'Zephyr 900')].inStock", hasItem(false)));
    }

    private long createFrame(
            MockHttpSession session, String model, String color, String material, int stockQty, boolean active)
            throws Exception {
        mvc.perform(post("/api/shop/frames")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(frameJson(model, color, material, stockQty, active)))
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

    private static String frameJson(String model, String color, String material, int stockQty, boolean active) {
        String template = "{\"model\":\"%s\",\"color\":\"%s\",\"material\":\"%s\",\"price\":7200.00,"
                + "\"stockQty\":%d,\"active\":%b,\"categoryId\":null}";
        return template.formatted(model, color, material, stockQty, active);
    }
}
