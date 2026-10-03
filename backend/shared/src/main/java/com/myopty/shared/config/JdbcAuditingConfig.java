package com.myopty.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;

/**
 * Populates {@code created_at} and {@code updated_at} on every table that has them.
 *
 * <p>The columns are {@code NOT NULL} in all the migrations, so an entity that does not carry
 * {@code @CreatedDate} / {@code @LastModifiedDate} fails on insert rather than quietly writing
 * a zero timestamp.
 */
@Configuration
@EnableJdbcAuditing
public class JdbcAuditingConfig {
}