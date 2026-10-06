package com.myopty.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The single runnable entry point. Every other module is a library.
 *
 * <p>{@code scanBasePackages} is set explicitly because the application class lives in
 * {@code com.myopty.app}, which is a sibling of the module packages rather than a parent of
 * them. Without it, component scanning would only reach {@code com.myopty.app} and the
 * context would come up empty.
 */
@SpringBootApplication(scanBasePackages = "com.myopty")
public class MyOptyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyOptyApplication.class, args);
    }
}
