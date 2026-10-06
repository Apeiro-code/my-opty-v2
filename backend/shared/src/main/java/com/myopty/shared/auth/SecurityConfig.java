package com.myopty.shared.auth;

import com.myopty.shared.auth.dto.ApiError;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

/**
 * What is public, what needs a session, and what a client is told when refused.
 *
 * <p>CSRF protection is switched off, and that is a decision rather than an
 * omission, so the reasoning lives here where the next person will find it: the
 * session cookie is {@code SameSite=Lax} (see {@code application.yml}), so a
 * cross-site {@code POST} never carries it, and there is no browser client yet
 * that would use a token flow. When a frontend starts consuming these sessions
 * this has to be re-made consciously — turning protection back on costs a token
 * fetch on login and an {@code X-XSRF-TOKEN} header on every write.
 *
 * <p>CORS moved here from {@code CorsConfig} when the filter chain arrived,
 * because the security chain answers preflight requests before MVC ever sees
 * them — the old {@link org.springframework.web.servlet.config.annotation.WebMvcConfigurer}
 * would have silently stopped working. Same property, same origins list; the
 * difference is that credentials are now allowed, which the browser requires
 * for cookie-carrying requests and which is only safe because the origins are a
 * configured list and never a wildcard.
 *
 * <p>The role rule is written once, here, rather than in each controller:
 * {@code /api/shop/**} is the shop owner's side (the frontend's
 * {@code (client)/shop} group), so it needs {@code ROLE_CLIENT}; everything
 * else under {@code /api/**} needs a session but makes no distinction between
 * the two roles — finer-grained rules arrive with the endpoints that need them.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
        "/actuator/health", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api/auth/login"
    };

    private final List<String> allowedOrigins;
    private final ObjectMapper objectMapper;

    public SecurityConfig(
            @Value("${myopty.cors.allowed-origins}") List<String> allowedOrigins, ObjectMapper objectMapper) {
        this.allowedOrigins = List.copyOf(allowedOrigins);
        this.objectMapper = objectMapper;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository securityContextRepository)
            throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .securityContext(sc -> sc.securityContextRepository(securityContextRepository))
                .authorizeHttpRequests(auth -> auth.requestMatchers(PUBLIC_PATHS)
                        .permitAll()
                        .requestMatchers("/api/shop/**")
                        .hasRole("CLIENT")
                        .requestMatchers("/api/**")
                        .authenticated()
                        .anyRequest()
                        .authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, exception) -> writeError(
                                response,
                                HttpServletResponse.SC_UNAUTHORIZED,
                                "UNAUTHENTICATED",
                                "Authentication required."))
                        .accessDeniedHandler((request, response, exception) -> writeError(
                                response,
                                HttpServletResponse.SC_FORBIDDEN,
                                "FORBIDDEN",
                                "You do not have access to this resource.")))
                .logout(logout -> logout.logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            // The same envelope every other success carries; a
                            // logout has nothing left to put in `data`.
                            objectMapper.writeValue(
                                    response.getOutputStream(), Map.of("success", true, "data", Map.of()));
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"));
        return http.build();
    }

    /**
     * Where a session's identity is stored and read back.
     *
     * <p>Spring Security 6+ only <em>saves</em> the context when an
     * authentication filter explicitly asks it to — a login performed inside a
     * controller, as {@link AuthController} does, is never told to. Declaring
     * the repository as a bean makes the same object available to the filter
     * chain (for loading on every request) and to the controller (to save the
     * moment authentication succeeds), which is the whole mechanism: request
     * attribute first so the current request sees it immediately, session
     * second so the next one does.
     */
    @Bean
    SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(), new HttpSessionSecurityContextRepository());
    }

    /**
     * The preflight source for {@code /api/**}. The specific origins (rather
     * than {@code "*"}) are what make {@code allowCredentials} legal — a browser
     * refuses credentialed responses from a wildcard, and Spring rejects the
     * combination outright.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security 7's provider takes the user service in its constructor; the
     * encoder is set explicitly because the bean above must be the one that
     * verifies the seed hashes, not a default the framework guesses at.
     */
    @Bean
    AuthenticationProvider authenticationProvider(AppUserDetailsService users, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * The manager the login controller invokes. It is built by
     * {@link AuthenticationConfiguration} from the provider bean above, which is
     * how the authentication path gets exactly one set of beans — this class's
     * — instead of the framework defaults.
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    private void writeError(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiError.of(code, message));
    }
}
