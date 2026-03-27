package com.yusufjon.recruitmentplatform.application.dto;

/**
 * Holds the request data needed to create a application through the API.
 */

import jakarta.validation.constraints.NotNull;

public class CreateApplicationRequest {

    @NotNull(message = "Vacancy id is required")
    private Long vacancyId;

    public CreateApplicationRequest() {
    }

    public CreateApplicationRequest(Long vacancyId) {
        this.vacancyId = vacancyId;
    }

    public Long getVacancyId() {
        return vacancyId;
    }

    public void setVacancyId(Long vacancyId) {
        this.vacancyId = vacancyId;
    }
}