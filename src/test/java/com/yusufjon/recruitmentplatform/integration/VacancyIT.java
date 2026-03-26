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

@DisplayName("Vacancy flow integration tests")
class VacancyIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("recruiter can create vacancy")
    void recruiterCanCreateVacancy() throws Exception {
        String recruiterToken = registerUserAndGetToken("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");
        Long companyId = createCompany(recruiterToken, "Acme", "Product company", "Tashkent");

        mockMvc.perform(post("/api/vacancies")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(recruiterToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(java.util.Map.of(
                                "title", "Java Developer",
                                "description", "Build APIs",
                                "location", "Tashkent",
                                "salaryFrom", 1200.0,
                                "salaryTo", 2200.0,
                                "companyId", companyId
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Java Developer"))
                .andExpect(jsonPath("$.companyId").value(companyId));
    }

    @Test
    @DisplayName("non owner recruiter cannot create vacancy for another recruiters company")
    void nonOwnerRecruiterCannotCreateVacancyForAnotherRecruitersCompany() throws Exception {
        String ownerToken = registerUserAndGetToken("Owner Recruiter", "owner@example.com", "secret123", "RECRUITER");
        String anotherRecruiterToken = registerUserAndGetToken("Another Recruiter", "another@example.com", "secret123", "RECRUITER");
        Long companyId = createCompany(ownerToken, "Acme", "Product company", "Tashkent");

        mockMvc.perform(post("/api/vacancies")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(anotherRecruiterToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(java.util.Map.of(
                                "title", "Java Developer",
                                "description", "Build APIs",
                                "location", "Tashkent",
                                "salaryFrom", 1200.0,
                                "salaryTo", 2200.0,
                                "companyId", companyId
                        ))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You can create vacancy only for your own company"))
                .andExpect(jsonPath("$.path").value("/api/vacancies"));
    }

    @Test
    @DisplayName("vacancy list is public")
    void vacancyListIsPublic() throws Exception {
        String recruiterToken = registerUserAndGetToken("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");
        Long companyId = createCompany(recruiterToken, "Acme", "Product company", "Tashkent");
        createVacancy(recruiterToken, companyId, "Java Developer", "Build APIs", "Tashkent", 1200.0, 2200.0);

        mockMvc.perform(get("/api/vacancies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Java Developer"));
    }

    @Test
    @DisplayName("get vacancy by id is public")
    void getVacancyByIdIsPublic() throws Exception {
        String recruiterToken = registerUserAndGetToken("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");
        Long companyId = createCompany(recruiterToken, "Acme", "Product company", "Tashkent");
        Long vacancyId = createVacancy(recruiterToken, companyId, "Java Developer", "Build APIs", "Tashkent", 1200.0, 2200.0);

        mockMvc.perform(get("/api/vacancies/{id}", vacancyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vacancyId))
                .andExpect(jsonPath("$.title").value("Java Developer"))
                .andExpect(jsonPath("$.companyId").value(companyId));
    }

    @Test
    @DisplayName("vacancy title filter works")
    void vacancyTitleFilterWorks() throws Exception {
        String recruiterToken = registerUserAndGetToken("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");
        Long companyId = createCompany(recruiterToken, "Acme", "Product company", "Tashkent");
        createVacancy(recruiterToken, companyId, "Java Developer", "Build APIs", "Tashkent", 1200.0, 2200.0);
        createVacancy(recruiterToken, companyId, "Python Developer", "Build services", "Samarkand", 1100.0, 2100.0);

        mockMvc.perform(get("/api/vacancies").param("title", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Java Developer"));
    }
}
