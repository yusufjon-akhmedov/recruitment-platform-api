package com.yusufjon.recruitmentplatform.application.dto;

/**
 * Holds the request data needed to update a application status through the API.
 */

import com.yusufjon.recruitmentplatform.shared.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateApplicationStatusRequest {

    @NotNull(message = "Status is required")
    private ApplicationStatus status;

    public UpdateApplicationStatusRequest(ApplicationStatus status) {
        this.status = status;
    }
}