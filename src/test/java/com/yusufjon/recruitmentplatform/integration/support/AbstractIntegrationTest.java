package com.yusufjon.recruitmentplatform.integration.support;

/**
 * Provides the shared integration-test setup, including MockMvc access, a PostgreSQL
 * Testcontainer, and database cleanup between tests.
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yusufjon.recruitmentplatform.auth.service.EmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = Replace.NONE)
public abstract class AbstractIntegrationTest {

    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("recruitment_platform_test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        POSTGRESQL_CONTAINER.start();
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    protected EmailSender emailSender;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    }

    @BeforeEach
    void resetDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE email_verification_tokens, applications, vacancies, companies, users RESTART IDENTITY CASCADE");
        Mockito.reset(emailSender);
    }

    protected String registerUserAndGetToken(String fullName, String email, String password, String role) throws Exception {
        registerUser(fullName, email, password, role);
        verifyEmailForUser(email);
        return loginAndGetToken(email, password);
    }

    protected String loginAndGetToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        return readJson(result).get("token").asText();
    }

    protected String registerUser(String fullName, String email, String password, String role) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(Map.of(
                                "fullName", fullName,
                                "email", email,
                                "password", password,
                                "role", role
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        return readJson(result).get("token").asText();
    }

    protected void verifyEmailForUser(String email) throws Exception {
        mockMvc.perform(get("/api/auth/verify-email")
                        .param("token", getVerificationToken(email)))
                .andExpect(status().isOk());
    }

    protected String getVerificationToken(String email) {
        return jdbcTemplate.queryForObject(
                """
                SELECT evt.token
                FROM email_verification_tokens evt
                JOIN users u ON evt.user_id = u.id
                WHERE u.email = ?
                """,
                String.class,
                email
        );
    }

    protected boolean isEmailVerified(String email) {
        Boolean value = jdbcTemplate.queryForObject(
                "SELECT email_verified FROM users WHERE email = ?",
                Boolean.class,
                email
        );
        return Boolean.TRUE.equals(value);
    }

    protected int countVerificationTokens(String email) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM email_verification_tokens evt
                JOIN users u ON evt.user_id = u.id
                WHERE u.email = ?
                """,
                Integer.class,
                email
        );
        return count == null ? 0 : count;
    }

    protected boolean isVerificationTokenUsed(String token) {
        Boolean value = jdbcTemplate.queryForObject(
                "SELECT verified_at IS NOT NULL FROM email_verification_tokens WHERE token = ?",
                Boolean.class,
                token
        );
        return Boolean.TRUE.equals(value);
    }

    protected void expireVerificationToken(String token) {
        jdbcTemplate.update(
                "UPDATE email_verification_tokens SET expires_at = NOW() - INTERVAL '1 hour' WHERE token = ?",
                token
        );
    }

    protected Long createCompany(String token, String name, String description, String location) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/companies")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(Map.of(
                                "name", name,
                                "description", description,
                                "location", location
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        return readJson(result).get("id").asLong();
    }

    protected Long createVacancy(String token, Long companyId, String title, String description,
                                 String location, double salaryFrom, double salaryTo) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/vacancies")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(Map.of(
                                "title", title,
                                "description", description,
                                "location", location,
                                "salaryFrom", salaryFrom,
                                "salaryTo", salaryTo,
                                "companyId", companyId
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        return readJson(result).get("id").asLong();
    }

    protected Long applyToVacancy(String token, Long vacancyId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/applications")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(Map.of("vacancyId", vacancyId))))
                .andExpect(status().isCreated())
                .andReturn();

        return readJson(result).get("id").asLong();
    }

    protected JsonNode patchApplicationStatus(String token, Long applicationId, String statusValue) throws Exception {
        MvcResult result = mockMvc.perform(patch("/api/applications/{id}/status", applicationId)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(Map.of("status", statusValue))))
                .andExpect(status().isOk())
                .andReturn();

        return readJson(result);
    }

    protected JsonNode getJson(String urlTemplate, Object... uriVars) throws Exception {
        MvcResult result = mockMvc.perform(get(urlTemplate, uriVars))
                .andExpect(status().isOk())
                .andReturn();

        return readJson(result);
    }

    protected JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    protected String asJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    protected String bearerToken(String token) {
        return "Bearer " + token;
    }
}
