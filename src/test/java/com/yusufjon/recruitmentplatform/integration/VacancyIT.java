package com.yusufjon.recruitmentplatform.integration;

/**
 * Runs end-to-end integration tests for vacancy creation, public vacancy reads, ownership checks,
 * filtering, pagination, and sorting.
 */

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
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Java Developer"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
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
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Java Developer"));
    }

    @Test
    @DisplayName("vacancy location filter works")
    void vacancyLocationFilterWorks() throws Exception {
        String recruiterToken = registerUserAndGetToken("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");
        Long companyId = createCompany(recruiterToken, "Acme", "Product company", "Tashkent");
        createVacancy(recruiterToken, companyId, "Java Developer", "Build APIs", "Tashkent", 1200.0, 2200.0);
        createVacancy(recruiterToken, companyId, "Python Developer", "Build services", "Samarkand", 1100.0, 2100.0);

        mockMvc.perform(get("/api/vacancies").param("location", "sam"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].location").value("Samarkand"));
    }

    @Test
    @DisplayName("vacancy company id and company name filters work")
    void vacancyCompanyFiltersWork() throws Exception {
        String recruiterToken = registerUserAndGetToken("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");
        Long acmeCompanyId = createCompany(recruiterToken, "Acme", "Product company", "Tashkent");
        Long betaCompanyId = createCompany(recruiterToken, "Beta Group", "Consulting company", "Tashkent");
        createVacancy(recruiterToken, acmeCompanyId, "Java Developer", "Build APIs", "Tashkent", 1200.0, 2200.0);
        createVacancy(recruiterToken, betaCompanyId, "Business Analyst", "Gather requirements", "Tashkent", 1300.0, 2100.0);

        mockMvc.perform(get("/api/vacancies").param("companyId", String.valueOf(acmeCompanyId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].companyId").value(acmeCompanyId));

        mockMvc.perform(get("/api/vacancies").param("companyName", "beta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Business Analyst"));
    }

    @Test
    @DisplayName("vacancy min salary and max salary filters work")
    void vacancyMinSalaryAndMaxSalaryFiltersWork() throws Exception {
        String recruiterToken = registerUserAndGetToken("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");
        Long companyId = createCompany(recruiterToken, "Acme", "Product company", "Tashkent");
        createVacancy(recruiterToken, companyId, "Junior Java Developer", "Build APIs", "Tashkent", 1000.0, 1400.0);
        createVacancy(recruiterToken, companyId, "Middle Java Developer", "Build services", "Tashkent", 1800.0, 2200.0);
        createVacancy(recruiterToken, companyId, "Senior Java Developer", "Lead projects", "Tashkent", 2600.0, 3200.0);

        mockMvc.perform(get("/api/vacancies")
                        .param("minSalary", "2000")
                        .param("maxSalary", "2500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Middle Java Developer"));
    }

    @Test
    @DisplayName("vacancy pagination works")
    void vacancyPaginationWorks() throws Exception {
        String recruiterToken = registerUserAndGetToken("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");
        Long companyId = createCompany(recruiterToken, "Acme", "Product company", "Tashkent");
        createVacancy(recruiterToken, companyId, "Alpha Engineer", "A", "Tashkent", 1000.0, 2000.0);
        createVacancy(recruiterToken, companyId, "Beta Engineer", "B", "Tashkent", 1200.0, 2200.0);
        createVacancy(recruiterToken, companyId, "Gamma Engineer", "C", "Tashkent", 1400.0, 2400.0);

        mockMvc.perform(get("/api/vacancies")
                        .param("page", "1")
                        .param("size", "1")
                        .param("sortBy", "title")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Beta Engineer"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    @DisplayName("vacancy sorting works")
    void vacancySortingWorks() throws Exception {
        String recruiterToken = registerUserAndGetToken("Recruiter One", "recruiter@example.com", "secret123", "RECRUITER");
        Long companyId = createCompany(recruiterToken, "Acme", "Product company", "Tashkent");
        createVacancy(recruiterToken, companyId, "Java Developer", "Build APIs", "Tashkent", 1200.0, 2200.0);
        createVacancy(recruiterToken, companyId, "Senior Java Developer", "Lead work", "Tashkent", 1800.0, 3200.0);

        mockMvc.perform(get("/api/vacancies")
                        .param("sortBy", "salaryTo")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].title").value("Senior Java Developer"))
                .andExpect(jsonPath("$.content[1].title").value("Java Developer"));
    }

    @Test
    @DisplayName("invalid sortBy is rejected")
    void invalidSortByIsRejected() throws Exception {
        mockMvc.perform(get("/api/vacancies").param("sortBy", "companyName"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid sortBy value"))
                .andExpect(jsonPath("$.path").value("/api/vacancies"));
    }

    @Test
    @DisplayName("invalid salary range is rejected")
    void invalidSalaryRangeIsRejected() throws Exception {
        mockMvc.perform(get("/api/vacancies")
                        .param("minSalary", "3000")
                        .param("maxSalary", "2000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Min salary cannot be greater than max salary"))
                .andExpect(jsonPath("$.path").value("/api/vacancies"));
    }

    @Test
    @DisplayName("invalid page and size values are rejected")
    void invalidPageAndSizeValuesAreRejected() throws Exception {
        mockMvc.perform(get("/api/vacancies").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Page must be greater than or equal to 0"));

        mockMvc.perform(get("/api/vacancies").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Size must be between 1 and 100"));
    }
}
