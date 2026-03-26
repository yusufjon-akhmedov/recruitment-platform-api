package com.yusufjon.recruitmentplatform.integration;

import com.yusufjon.recruitmentplatform.integration.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Public and private access integration tests")
class AccessIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("public GET endpoints are accessible without token")
    void publicGetEndpointsAreAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        mockMvc.perform(get("/api/vacancies"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("protected endpoints reject unauthenticated access")
    void protectedEndpointsRejectUnauthenticatedAccess() throws Exception {
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(java.util.Map.of(
                                "name", "Acme",
                                "description", "Product company",
                                "location", "Tashkent"
                        ))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/applications/my"))
                .andExpect(status().isForbidden());
    }
}
