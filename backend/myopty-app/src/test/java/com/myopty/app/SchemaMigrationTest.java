package com.myopty.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

/**
 * Proves the schema story on a database nobody has to remember to start.
 *
 * <p>{@code docker compose up} gives a developer a database, but CI and a fresh clone have
 * neither. This runs the real Flyway migrations against a throwaway MySQL 8 container, which is
 * the only check that catches a migration that was written but never executed.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class SchemaMigrationTest {

    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.0")
            .withDatabaseName("myopty")
            .withUsername("myopty")
            .withPassword("myopty");

    /**
     * Overrides the datasource with the container's. {@code application.yml} reads
     * {@code ${DB_URL}} and friends, which are absent here on purpose: a test must not depend on
     * a developer's local {@code .env}, and a higher-precedence property source means those
     * placeholders are never resolved.
     */
    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void everyMigrationApplies() {
        List<String> versions = jdbc.queryForList(
                "SELECT version FROM flyway_schema_history WHERE success = TRUE ORDER BY installed_rank", String.class);

        assertThat(versions)
                .containsExactly(
                        "1", "1.1", "1.2", "1.3", "2", "3", "4", "5", "6", "7", "100", "101", "102", "103", "104",
                        "105", "200", "201", "202", "203", "300", "301", "302", "303");
    }

    @Test
    void createsEveryTableInTheSchema() {
        List<String> tables = jdbc.queryForList(
                "SELECT table_name FROM information_schema.tables "
                        + "WHERE table_schema = DATABASE() AND table_name <> 'flyway_schema_history' "
                        + "ORDER BY table_name",
                String.class);

        assertThat(tables)
                .containsExactly(
                        "app_user",
                        "availability_report",
                        "billing_report",
                        "category",
                        "client_profile",
                        "customer_profile",
                        "dealer",
                        "dealer_email",
                        "discount",
                        "frame",
                        "invoice",
                        "lens",
                        "monthly_report",
                        "order_notification",
                        "payment",
                        "payment_method",
                        "prescription",
                        "progressive_order",
                        "question",
                        "stock_entry",
                        "stock_update",
                        "todo_task");
    }

    /**
     * The seed migrations (V7, V105) exist so the team can build features against realistic
     * data. A migration that runs but inserts nothing, or whose discount points at product ids
     * that were never inserted, would fail here rather than in someone's demo.
     */
    @Test
    void sampleDataIsSeededAndSelfConsistent() {
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM category", Long.class))
                .isGreaterThanOrEqualTo(6);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM frame", Long.class))
                .isGreaterThanOrEqualTo(10);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM lens", Long.class)).isGreaterThanOrEqualTo(6);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM discount", Long.class))
                .isGreaterThanOrEqualTo(4);

        // The variety the shopping and inventory features need, not just row counts.
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM lens WHERE type = 'PROGRESSIVE'", Long.class))
                .isPositive();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM frame WHERE stock_qty < low_stock_threshold", Long.class))
                .isPositive();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM frame WHERE is_active = FALSE", Long.class))
                .isPositive();
        assertThat(jdbc.queryForObject(
                        "SELECT COUNT(*) FROM discount WHERE is_active = FALSE AND ended_at IS NOT NULL "
                                + "AND valid_to < CURDATE()",
                        Long.class))
                .isPositive();

        // Every id a discount names must be a frame or a lens that exists. The two tables have
        // independent id spaces, so the same number can legitimately name both — that ambiguity
        // is the documented cost of target_item_ids being JSON (see V103).
        List<Long> referenced = jdbc.queryForList("""
                SELECT jt.id FROM discount d,
                JSON_TABLE(d.target_item_ids, '$[*]' COLUMNS (id BIGINT PATH '$')) jt
                WHERE d.target_item_ids IS NOT NULL
                """, Long.class);
        assertThat(referenced).isNotEmpty();
        for (Long id : referenced) {
            Integer matches = jdbc.queryForObject("""
                    SELECT (SELECT COUNT(*) FROM frame WHERE id = ?)
                         + (SELECT COUNT(*) FROM lens WHERE id = ?)
                    """, Integer.class, id, id);
            assertThat(matches)
                    .as("discount target id %s names an existing product", id)
                    .isGreaterThanOrEqualTo(1);
        }
    }

    /**
     * A migration that creates the tables but forgets the relationships would still pass the two
     * tests above, and the failure would then surface as a constraint error in production.
     */
    @Test
    void ordersCannotReferenceAMissingPrescription() {
        jdbc.update("""
                INSERT INTO app_user (email, password_hash, full_name, role)
                VALUES ('schema-test@example.com', 'not-a-real-hash', 'Schema Test', 'CUSTOMER')
                """);
        Long customerId =
                jdbc.queryForObject("SELECT id FROM app_user WHERE email = 'schema-test@example.com'", Long.class);

        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO progressive_order (order_number, customer_id, prescription_id, lens_id)
                VALUES ('SCHEMA-TEST-1', ?, 999999, 999999)
                """, customerId))
                .hasMessageContaining("fk_progressive_order_prescription");
    }
}
