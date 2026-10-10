package com.myopty.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.myopty.order.service.ObjectStore;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Proves the prescription submission story end to end: a logged-in customer
 * posts multipart values and an optional document, a real database row is
 * written, and the bytes are handed to the object store.
 *
 * <p>The object store is replaced with an in-memory recorder. This is the one
 * seam worth faking: the rest of the path — the filter chain, JSON decoding of
 * the {@code prescription} part, Bean Validation, the service and the SQL insert —
 * is real, because those are what can actually break. A fake store keeps the test
 * off the filesystem and lets it assert the key the row carries is the key the
 * bytes were stored under.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class PrescriptionSubmissionTest {

    private static final String CUSTOMER_EMAIL = "customer@myopty.local";
    private static final String CUSTOMER_PASSWORD = "customer123";

    private static final String VALID_VALUES = """
            {
              "sphLeft": "+06.25", "sphRight": "+05.50",
              "cylLeft": "-1.75", "cylRight": "-1.50",
              "axisLeft": "180", "axisRight": "175",
              "addPowerLeft": "+2.00", "addPowerRight": "+2.00",
              "progressive": true
            }
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

    @TestConfiguration
    static class RecordingStoreConfig {
        @Bean
        @Primary
        RecordingObjectStore recordingObjectStore() {
            return new RecordingObjectStore();
        }
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    RecordingObjectStore store;

    @Test
    void aCustomerCanSubmitValuesAndADocument() throws Exception {
        MockHttpSession session = loginAsCustomer();

        mvc.perform(submit(session).file(prescriptionPart(VALID_VALUES)).file(documentPart("scan.pdf")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verificationStatus").value("PENDING_REVIEW"))
                .andExpect(jsonPath("$.data.progressive").value(true))
                .andExpect(jsonPath("$.data.hasDocument").value(true))
                .andExpect(jsonPath("$.data.sphLeft").value("+06.25"))
                .andExpect(jsonPath("$.data.cylRight").value("-1.50"));

        String storedKey = jdbc.queryForObject(
                "SELECT document_object_key FROM prescription ORDER BY id DESC LIMIT 1", String.class);
        assertThat(storedKey).isNotBlank();
        assertThat(store.storedKeys()).containsExactly(storedKey);
    }

    @Test
    void theDocumentIsOptional() throws Exception {
        MockHttpSession session = loginAsCustomer();

        mvc.perform(submit(session).file(prescriptionPart(VALID_VALUES)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.hasDocument").value(false));

        String storedKey = jdbc.queryForObject(
                "SELECT document_object_key FROM prescription ORDER BY id DESC LIMIT 1", String.class);
        assertThat(storedKey).isNull();
    }

    @Test
    void aPrescriptionWithNoOpticalValueIsRefused() throws Exception {
        MockHttpSession session = loginAsCustomer();
        String empty = "{\"progressive\":false}";

        mvc.perform(submit(session).file(prescriptionPart(empty)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void aMissingPrescriptionPartIsRefused() throws Exception {
        MockHttpSession session = loginAsCustomer();

        mvc.perform(submit(session).file(documentPart("scan.pdf")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MISSING_PRESCRIPTION"));
    }

    @Test
    void anUnsupportedDocumentTypeIsRefusedWithTheEnvelope() throws Exception {
        MockHttpSession session = loginAsCustomer();

        mvc.perform(submit(session)
                        .file(prescriptionPart(VALID_VALUES))
                        .file(documentPart("notes.txt", MediaType.TEXT_PLAIN_VALUE)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_DOCUMENT"));
    }

    @Test
    void jsonInsteadOfMultipartIsRefusedBeforeTheController() throws Exception {
        MockHttpSession session = loginAsCustomer();

        mvc.perform(post("/api/prescriptions")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_VALUES))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void anAnonymousSubmissionIsRefused() throws Exception {
        mvc.perform(submit(null).file(prescriptionPart(VALID_VALUES))).andExpect(status().isUnauthorized());
    }

    private MockMultipartHttpServletRequestBuilder submit(MockHttpSession session) throws Exception {
        MockMultipartHttpServletRequestBuilder builder = multipart("/api/prescriptions");
        if (session != null) {
            builder.session(session);
        }
        return builder;
    }

    private MockMultipartFile prescriptionPart(String json) {
        return new MockMultipartFile(
                "prescription", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes(StandardCharsets.UTF_8));
    }

    private MockMultipartFile documentPart(String filename) {
        return documentPart(filename, MediaType.APPLICATION_PDF_VALUE);
    }

    private MockMultipartFile documentPart(String filename, String contentType) {
        return new MockMultipartFile("document", filename, contentType, "scan".getBytes(StandardCharsets.UTF_8));
    }

    private MockHttpSession loginAsCustomer() throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(CUSTOMER_EMAIL, CUSTOMER_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    static final class RecordingObjectStore implements ObjectStore {

        private final List<String> storedKeys = new ArrayList<>();

        @Override
        public void put(String key, InputStream content, long contentLength, String contentType) {
            storedKeys.add(key);
        }

        @Override
        public void delete(String key) {
            storedKeys.remove(key);
        }

        List<String> storedKeys() {
            return storedKeys;
        }
    }
}
