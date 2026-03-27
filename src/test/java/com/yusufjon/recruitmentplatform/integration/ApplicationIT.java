package com.yusufjon.recruitmentplatform.integration;

/**
 * Runs end-to-end integration tests for candidate applications, recruiter review flows, and
 * protected application endpoints.
 */

import com.yusufjon.recruitmentplatform.integration.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Application flow integration tests")
class ApplicationIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("candidate can apply to vacancy")
    void candidateCanApplyToVacancy() throws Exception {
        String recruiterToken = registerUserAndGetToken("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");
        Long companyId = createCompany(recruiterToken, "Acme", "Product company", "Tashkent");
        Long vacancyId = createVacancy(recruiterToken, companyId, "Java Developer", "Build APIs", "Tashkent", 1200.0, 2200.0);
        String candidateToken = registerUserAndGetToken("Candidate One", "candidate@example.com", "secret123", "CANDIDATE");

        mockMvc.perform(post("/api/applications")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(candidateToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(java.util.Map.of("vacancyId", vacancyId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.candidateId").value(2))
                .andExpect(jsonPath("$.vacancyId").value(vacancyId))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("duplicate apply is rejected")
    void duplicateApplyIsRejected() throws Exception {
        String recruiterToken = registerUserAndGetToken("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");
        Long companyId = createCompany(recruiterToken, "Acme", "Product company", "Tashkent");
        Long vacancyId = createVacancy(recruiterToken, companyId, "Java Developer", "Build APIs", "Tashkent", 1200.0, 2200.0);
        String candidateToken = registerUserAndGetToken("Candidate One", "candidate@example.com", "secret123", "CANDIDATE");
        applyToVacancy(candidateToken, vacancyId);

        mockMvc.perform(post("/api/applications")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(candidateToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(java.util.Map.of("vacancyId", vacancyId))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You have already applied to this vacancy"))
                .andExpect(jsonPath("$.path").value("/api/applications"));
    }

    @Test
    @DisplayName("candidate can access own applications")
    void candidateCanAccessOwnApplications() throws Exception {
        String recruiterToken = registerUserAndGetToken("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");
        Long companyId = createCompany(recruiterToken, "Acme", "Product company", "Tashkent");
        Long vacancyId = createVacancy(recruiterToken, companyId, "Java Developer", "Build APIs", "Tashkent", 1200.0, 2200.0);
        String candidateToken = registerUserAndGetToken("Candidate One", "candidate@example.com", "secret123", "CANDIDATE");
        applyToVacancy(candidateToken, vacancyId);

        mockMvc.perform(get("/api/applications/my")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(candidateToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].candidateId").value(2))
                .andExpect(jsonPath("$[0].vacancyId").value(vacancyId))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("recruiter can access recruiter applications")
    void recruiterCanAccessRecruiterApplications() throws Exception {
        String recruiterToken = registerUserAndGetToken("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");
        Long companyId = createCompany(recruiterToken, "Acme", "Product company", "Tashkent");
        Long vacancyId = createVacancy(recruiterToken, companyId, "Java Developer", "Build APIs", "Tashkent", 1200.0, 2200.0);
        String candidateToken = registerUserAndGetToken("Candidate One", "candidate@example.com", "secret123", "CANDIDATE");
        applyToVacancy(candidateToken, vacancyId);

        mockMvc.perform(get("/api/applications/recruiter")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(recruiterToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].candidateId").value(2))
                .andExpect(jsonPath("$[0].vacancyId").value(vacancyId))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("recruiter can update application status")
    void recruiterCanUpdateApplicationStatus() throws Exception {
        String recruiterToken = registerUserAndGetToken("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");
        Long companyId = createCompany(recruiterToken, "Acme", "Product company", "Tashkent");
        Long vacancyId = createVacancy(recruiterToken, companyId, "Java Developer", "Build APIs", "Tashkent", 1200.0, 2200.0);
        String candidateToken = registerUserAndGetToken("Candidate One", "candidate@example.com", "secret123", "CANDIDATE");
        Long applicationId = applyToVacancy(candidateToken, vacancyId);

        mockMvc.perform(patch("/api/applications/{id}/status", applicationId)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(recruiterToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(java.util.Map.of("status", "ACCEPTED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(applicationId))
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.vacancyId").value(vacancyId));
    }

    @Test
    @DisplayName("wrong role cannot update application status")
    void wrongRoleCannotUpdateApplicationStatus() throws Exception {
        String recruiterToken = registerUserAndGetToken("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");
        Long companyId = createCompany(recruiterToken, "Acme", "Product company", "Tashkent");
        Long vacancyId = createVacancy(recruiterToken, companyId, "Java Developer", "Build APIs", "Tashkent", 1200.0, 2200.0);
        String candidateToken = registerUserAndGetToken("Candidate One", "candidate@example.com", "secret123", "CANDIDATE");
        Long applicationId = applyToVacancy(candidateToken, vacancyId);

        mockMvc.perform(patch("/api/applications/{id}/status", applicationId)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(candidateToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(java.util.Map.of("status", "REJECTED"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only recruiters can update application status"))
                .andExpect(jsonPath("$.path").value("/api/applications/" + applicationId + "/status"));
    }
}
