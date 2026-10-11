package com.myopty.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
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
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Proves the inventory stories end to end against a real database: a placed order
 * draws its frame and lens off the shelf and writes an audit row, a rejected order
 * puts them back, an order the shelf cannot fill is refused and changes nothing,
 * and the client reads a live availability report while customers and strangers
 * do not.
 *
 * <p>There is no fake here. The parts worth exercising — the conditional stock
 * update, the transactions that make a refused order roll back whole, the
 * {@code stock_entry} insert and the role rule on {@code /api/shop/**} — are all
 * real. Seed rows are looked up by value rather than id, and a scarce lens is
 * created by the shop rather than edited out of the seed, so this class never
 * depends on another test's stock level.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class InventoryFlowTest {

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
    void theClientReadsALiveAvailabilityReport() throws Exception {
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);

        mvc.perform(get("/api/shop/inventory/report").session(client))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalItems").value(totalActiveItems()))
                .andExpect(jsonPath("$.data.inStockCount").value(inStockCount()))
                .andExpect(jsonPath("$.data.lowStockCount").value(lowStockCount()))
                .andExpect(jsonPath("$.data.outOfStockCount").value(outOfStockCount()))
                .andExpect(jsonPath("$.data.items.length()").value(totalActiveItems()))
                .andExpect(jsonPath("$.data.items[0].status").isNotEmpty());
    }

    @Test
    void theAvailabilityReportIsForTheClientOnly() throws Exception {
        MockHttpSession customer = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);

        mvc.perform(get("/api/shop/inventory/report").session(customer)).andExpect(status().isForbidden());
        mvc.perform(get("/api/shop/inventory/report")).andExpect(status().isUnauthorized());
    }

    @Test
    void placingAnOrderDecrementsStockAndWritesAnAuditEntry() throws Exception {
        MockHttpSession customer = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        long frameId = anyActiveFrameId();
        long lensId = anyProgressiveLensId();
        int frameQty = stockOf("frame", frameId);
        int lensQty = stockOf("lens", lensId);
        long prescriptionId = submitPrescription(customer);

        mvc.perform(post("/api/orders")
                        .session(customer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(prescriptionId, "PROGRESSIVE", lensId, frameId, 2)))
                .andExpect(status().isCreated());

        String orderNumber = jdbc.queryForObject(
                "SELECT order_number FROM progressive_order ORDER BY id DESC LIMIT 1", String.class);
        assertThat(stockOf("frame", frameId)).isEqualTo(frameQty - 2);
        assertThat(stockOf("lens", lensId)).isEqualTo(lensQty - 2);
        assertThat(jdbc.queryForObject(
                        "SELECT COUNT(*) FROM stock_entry WHERE reference = ? AND change_type = 'SALE'",
                        Long.class,
                        orderNumber))
                .isEqualTo(2L);
    }

    @Test
    void anOrderForMoreStockThanTheShelfHasIsRefusedAndChangesNothing() throws Exception {
        MockHttpSession customer = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);
        long lensId = createLensWithStock(client, "Scarce Progressive 1.60", 2);
        long prescriptionId = submitPrescription(customer);
        long ordersBefore = jdbc.queryForObject("SELECT COUNT(*) FROM progressive_order", Long.class);

        mvc.perform(post("/api/orders")
                        .session(customer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(prescriptionId, "PROGRESSIVE", lensId, null, 5)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("OUT_OF_STOCK"));

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM progressive_order", Long.class))
                .isEqualTo(ordersBefore);
        assertThat(stockOf("lens", lensId)).isEqualTo(2);
    }

    @Test
    void rejectingAnOrderRestoresItsBookedStock() throws Exception {
        MockHttpSession customer = loginAs(CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
        MockHttpSession client = loginAs(CLIENT_EMAIL, CLIENT_PASSWORD);
        long frameId = anyActiveFrameId();
        long lensId = anyProgressiveLensId();
        int frameQty = stockOf("frame", frameId);
        int lensQty = stockOf("lens", lensId);
        long prescriptionId = submitPrescription(customer);
        long orderId = createOrder(customer, prescriptionId, lensId, frameId);
        String orderNumber =
                jdbc.queryForObject("SELECT order_number FROM progressive_order WHERE id = ?", String.class, orderId);
        assertThat(stockOf("lens", lensId)).isEqualTo(lensQty - 1);

        mvc.perform(put("/api/shop/orders/{id}/reject", orderId)
                        .session(client)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Wrong prescription.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));

        assertThat(stockOf("frame", frameId)).isEqualTo(frameQty);
        assertThat(stockOf("lens", lensId)).isEqualTo(lensQty);
        assertThat(jdbc.queryForObject(
                        "SELECT COUNT(*) FROM stock_entry WHERE reference = ? AND change_type = 'RETURN'",
                        Long.class,
                        orderNumber))
                .isEqualTo(2L);
    }

    private long createOrder(MockHttpSession session, long prescriptionId, long lensId, long frameId) throws Exception {
        mvc.perform(post("/api/orders")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(prescriptionId, "PROGRESSIVE", lensId, frameId, null)))
                .andExpect(status().isCreated());
        return jdbc.queryForObject("SELECT id FROM progressive_order ORDER BY id DESC LIMIT 1", Long.class);
    }

    private long createLensWithStock(MockHttpSession client, String name, int stock) throws Exception {
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

    private static String orderJson(
            long prescriptionId, String orderType, long lensId, Long frameId, Integer quantity) {
        return "{\"prescriptionId\":%d,\"orderType\":\"%s\",\"lensId\":%d,\"frameId\":%s,\"quantity\":%s}"
                .formatted(
                        prescriptionId,
                        orderType,
                        lensId,
                        frameId == null ? "null" : frameId,
                        quantity == null ? "null" : quantity);
    }

    private int stockOf(String table, long id) {
        return jdbc.queryForObject("SELECT stock_qty FROM " + table + " WHERE id = ?", Integer.class, id);
    }

    private long anyActiveFrameId() {
        return jdbc.queryForObject("SELECT id FROM frame WHERE is_active = TRUE LIMIT 1", Long.class);
    }

    private long anyProgressiveLensId() {
        return jdbc.queryForObject("SELECT id FROM lens WHERE type = 'PROGRESSIVE' LIMIT 1", Long.class);
    }

    private int totalActiveItems() {
        return countActive("frame") + countActive("lens");
    }

    private int inStockCount() {
        return bandCount("frame", "stock_qty > low_stock_threshold")
                + bandCount("lens", "stock_qty > low_stock_threshold");
    }

    private int lowStockCount() {
        return bandCount("frame", "stock_qty > 0 AND stock_qty <= low_stock_threshold")
                + bandCount("lens", "stock_qty > 0 AND stock_qty <= low_stock_threshold");
    }

    private int outOfStockCount() {
        return bandCount("frame", "stock_qty = 0") + bandCount("lens", "stock_qty = 0");
    }

    private int countActive(String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE is_active = TRUE", Integer.class);
    }

    private int bandCount(String table, String condition) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE is_active = TRUE AND " + condition, Integer.class);
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
