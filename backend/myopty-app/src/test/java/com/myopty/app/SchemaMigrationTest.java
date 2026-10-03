package com.myopty.app;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
                "SELECT version FROM flyway_schema_history WHERE success = TRUE ORDER BY installed_rank",
                String.class);

        assertThat(versions).containsExactly(
                "1", "1.1", "1.2",
                "2", "3", "4", "5", "6",
                "100", "101", "102", "103", "104",
                "200", "201", "202", "203",
                "300", "301", "302", "303");
    }

    @Test
    void createsEveryTableInTheSchema() {
        List<String> tables = jdbc.queryForList(
                "SELECT table_name FROM information_schema.tables "
                        + "WHERE table_schema = DATABASE() AND table_name <> 'flyway_schema_history' "
                        + "ORDER BY table_name",
                String.class);

        assertThat(tables).containsExactly(
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
     * A migration that creates the tables but forgets the relationships would still pass the two
     * tests above, and the failure would then surface as a constraint error in production.
     */
    @Test
    void ordersCannotReferenceAMissingPrescription() {
        jdbc.update("""
                INSERT INTO app_user (email, password_hash, full_name, role)
                VALUES ('schema-test@example.com', 'not-a-real-hash', 'Schema Test', 'CUSTOMER')
                """);
        Long customerId = jdbc.queryForObject(
                "SELECT id FROM app_user WHERE email = 'schema-test@example.com'", Long.class);

        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO progressive_order (order_number, customer_id, prescription_id, lens_id)
                VALUES ('SCHEMA-TEST-1', ?, 999999, 999999)
                """, customerId))
                .hasMessageContaining("fk_progressive_order_prescription");
    }
}