package com.yusufjon.recruitmentplatform.application.dto;

/**
 * Represents the API response payload returned for application data.
 */

import com.yusufjon.recruitmentplatform.shared.enums.ApplicationStatus;

import java.time.LocalDateTime;

public class ApplicationResponse {

    private Long id;
    private Long candidateId;
    private Long vacancyId;
    private ApplicationStatus status;
    private LocalDateTime createdAt;

    public ApplicationResponse() {
    }

    public ApplicationResponse(Long id, Long candidateId, Long vacancyId,
                               ApplicationStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.candidateId = candidateId;
        this.vacancyId = vacancyId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public Long getVacancyId() {
        return vacancyId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public void setVacancyId(Long vacancyId) {
        this.vacancyId = vacancyId;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}