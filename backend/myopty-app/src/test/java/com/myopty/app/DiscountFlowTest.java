package com.myopty.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
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
import tools.jackson.databind.ObjectMapper;

/**
 * Proves the discount stories end to end against a real database: the shop owner
 * creates one promotion covering several items at once, ends it early and finds it
 * again in the history, and the running promotions are readable by an anonymous
 * visitor for the storefront to price against.
 *
 * <p>There is no fake here. The parts worth exercising — the filter chain and its
 * public path, JSON decoding of the target list, the derived state at read time,
 * the soft end and the SQL that keeps the row — are all real. The seeded
 * promotions (V105) are already in the table, so assertions name the row each test
 * created rather than assuming the list holds only that row.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class DiscountFlowTest {

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

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void aClientCanCreateABulkDiscountForSeveralItems() throws Exception {
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);
        List<Long> frames = activeFrameIds(3);

        MvcResult result = mvc.perform(post("/api/shop/discounts")
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(discountJson("Bulk test", "15.00", frames)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sitewide").value(false))
                .andExpect(jsonPath("$.data.targetItemIds", hasSize(3)))
                .andExpect(jsonPath("$.data.state").value("ACTIVE"))
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty())
                .andReturn();

        long id = idOf(result);
        Long targetCount =
                jdbc.queryForObject("SELECT JSON_LENGTH(target_item_ids) FROM discount WHERE id = ?", Long.class, id);
        assertThat(targetCount).isEqualTo(3);
    }

    @Test
    void endingAPromotionKeepsItInTheHistory() throws Exception {
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);
        long id = createDiscount(client, "History test", "20.00");

        mvc.perform(delete("/api/shop/discounts/{id}", id).session(client))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state").value("ENDED"))
                .andExpect(jsonPath("$.data.endedAt").isNotEmpty());

        mvc.perform(get("/api/shop/discounts").param("state", "ENDED").session(client))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == " + id + ")]").isNotEmpty());

        mvc.perform(get("/api/shop/discounts").param("active", "false").session(client))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == " + id + ")]").isNotEmpty());

        mvc.perform(get("/api/shop/discounts").param("state", "ACTIVE").session(client))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == " + id + ")]").isEmpty());

        Boolean stillThere = jdbc.queryForObject("SELECT is_active FROM discount WHERE id = ?", Boolean.class, id);
        assertThat(stillThere).isFalse();
    }

    @Test
    void runningPromotionsAreReadableWithoutASession() throws Exception {
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);
        long id = createDiscount(client, "Public test", "10.00");

        mvc.perform(get("/api/discounts/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[?(@.id == " + id + ")]").isNotEmpty());
    }

    @Test
    void theShopDiscountSurfaceNeedsTheClientRole() throws Exception {
        mvc.perform(get("/api/shop/discounts")).andExpect(status().isUnauthorized());

        MockHttpSession customer = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        mvc.perform(get("/api/shop/discounts").session(customer)).andExpect(status().isForbidden());
    }

    private long createDiscount(MockHttpSession client, String description, String percentage) throws Exception {
        MvcResult result = mvc.perform(post("/api/shop/discounts")
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(discountJson(description, percentage, List.of())))
                .andExpect(status().isCreated())
                .andReturn();
        return idOf(result);
    }

    private long idOf(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("id").asLong();
    }

    private static String discountJson(String description, String percentage, List<Long> targets) {
        String targetJson = targets.isEmpty()
                ? "null"
                : "[" + String.join(",", targets.stream().map(String::valueOf).toList()) + "]";
        return "{\"description\":\"%s\",\"percentage\":%s,\"targetItemIds\":%s,\"validFrom\":\"%s\",\"validTo\":\"%s\"}"
                .formatted(
                        description,
                        percentage,
                        targetJson,
                        LocalDate.now(),
                        LocalDate.now().plusDays(10));
    }

    private List<Long> activeFrameIds(int count) {
        return jdbc.queryForList("SELECT id FROM frame WHERE is_active = TRUE ORDER BY id LIMIT ?", Long.class, count);
    }

    private MockHttpSession loginAs(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }
}
