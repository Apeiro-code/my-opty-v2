package com.myopty.shared.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cross-origin access for the Next.js dev server.
 *
 * <p>Every origin listed in {@code CORS_ALLOWED_ORIGINS} is a place a logged-out visitor's
 * browser can send requests from, which is why it is a list read from configuration rather
 * than a wildcard.
 *
 * <p>Once Spring Security is on the classpath this stops taking effect, because the security
 * filter chain answers preflight requests before MVC does. Whoever adds the filter chain owns
 * this configuration from then on.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final List<String> allowedOrigins;

    public CorsConfig(@Value("${myopty.cors.allowed-origins}") List<String> allowedOrigins) {
        this.allowedOrigins = List.copyOf(allowedOrigins);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}