package com.yusufjon.recruitmentplatform.integration;

import com.yusufjon.recruitmentplatform.integration.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Company flow integration tests")
class CompanyIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("recruiter can create company")
    void recruiterCanCreateCompany() throws Exception {
        String recruiterToken = registerUserAndGetToken("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");

        mockMvc.perform(post("/api/companies")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(recruiterToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(java.util.Map.of(
                                "name", "Acme",
                                "description", "Product company",
                                "location", "Tashkent"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Acme"))
                .andExpect(jsonPath("$.description").value("Product company"))
                .andExpect(jsonPath("$.location").value("Tashkent"))
                .andExpect(jsonPath("$.recruiterId").value(1));
    }

    @Test
    @DisplayName("candidate cannot create company")
    void candidateCannotCreateCompany() throws Exception {
        String candidateToken = registerUserAndGetToken("Candidate One", "candidate@example.com", "secret123", "CANDIDATE");

        mockMvc.perform(post("/api/companies")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(candidateToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(java.util.Map.of(
                                "name", "Acme",
                                "description", "Product company",
                                "location", "Tashkent"
                        ))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only recruiters can create companies"))
                .andExpect(jsonPath("$.path").value("/api/companies"));
    }

    @Test
    @DisplayName("company list is public")
    void companyListIsPublic() throws Exception {
        String recruiterToken = registerUserAndGetToken("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");
        createCompany(recruiterToken, "Acme", "Product company", "Tashkent");

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Acme"))
                .andExpect(jsonPath("$[0].location").value("Tashkent"));
    }

    @Test
    @DisplayName("get company by id is public")
    void getCompanyByIdIsPublic() throws Exception {
        String recruiterToken = registerUserAndGetToken("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");
        Long companyId = createCompany(recruiterToken, "Acme", "Product company", "Tashkent");

        mockMvc.perform(get("/api/companies/{id}", companyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(companyId))
                .andExpect(jsonPath("$.name").value("Acme"))
                .andExpect(jsonPath("$.location").value("Tashkent"));
    }
}
