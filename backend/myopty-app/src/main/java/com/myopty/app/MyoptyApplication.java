package com.myopty.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

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
 */
@SpringBootApplication(scanBasePackages = "com.myopty")
@EnableJdbcRepositories("com.myopty")
public class MyOptyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyOptyApplication.class, args);
    }
}
