package com.yusufjon.recruitmentplatform.application.dto;

/**
 * Represents the API response payload returned for application data.
 */

import com.yusufjon.recruitmentplatform.shared.enums.ApplicationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ApplicationResponse {

    private Long id;
    private Long candidateId;
    private Long vacancyId;
    private ApplicationStatus status;
    private LocalDateTime createdAt;

    public ApplicationResponse(Long id, Long candidateId, Long vacancyId,
                               ApplicationStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.candidateId = candidateId;
        this.vacancyId = vacancyId;
        this.status = status;
        this.createdAt = createdAt;
    }
}