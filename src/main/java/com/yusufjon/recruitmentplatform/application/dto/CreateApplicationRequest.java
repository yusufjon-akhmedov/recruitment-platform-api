package com.yusufjon.recruitmentplatform.application.dto;

/**
 * Holds the request data needed to create a application through the API.
 */

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateApplicationRequest {

    @NotNull(message = "Vacancy id is required")
    private Long vacancyId;

    public CreateApplicationRequest(Long vacancyId) {
        this.vacancyId = vacancyId;
    }
}