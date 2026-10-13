package com.myopty.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * The single runnable entry point. Every other module is a library.
 *
 * <p>{@code scanBasePackages} is set explicitly because the application class lives in
 * {@code com.myopty.app}, which is a sibling of the module packages rather than a parent of
 * them. Without it, component scanning would only reach {@code com.myopty.app} and the
 * context would come up empty.
 *
 * <p>{@code @EnableJdbcRepositories} names the same tree for the second time on purpose:
 * repository scanning has its own package default — the declaring class's package — and
 * {@code scanBasePackages} does not reach it, so without this line every
 * {@code com.myopty.shared.*} repository is silently not a bean.
 *
 * <p>Billing is the one module built on Spring Data JPA rather than JDBC, so it needs
 * both annotations to be reachable. {@code @EnableJpaRepositories} is the JPA twin of
 * the JDBC line above (and, unlike the auto-configuration, its default package is this
 * class's package, which would leave {@code com.myopty.billing} unscanned).
 * {@code @EntityScan} is JPA's counterpart to that scan: without it JPA's default entity
 * scan is also rooted here, so billing's {@code @Entity} classes are never registered.
 */
@SpringBootApplication(scanBasePackages = "com.myopty")
@EnableJdbcRepositories("com.myopty")
@EnableJpaRepositories(basePackages = "com.myopty")
@EntityScan(basePackages = "com.myopty")
public class MyOptyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyOptyApplication.class, args);
    }
}
