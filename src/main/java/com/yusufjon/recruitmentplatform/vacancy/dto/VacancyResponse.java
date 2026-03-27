package com.yusufjon.recruitmentplatform.vacancy.dto;

/**
 * Represents the API response payload returned for vacancy data.
 */

import java.time.LocalDateTime;

public class VacancyResponse {

    private Long id;
    private String title;
    private String description;
    private String location;
    private Double salaryFrom;
    private Double salaryTo;
    private Long companyId;
    private LocalDateTime createdAt;

    public VacancyResponse() {
    }

    public VacancyResponse(Long id, String title, String description, String location,
                           Double salaryFrom, Double salaryTo, Long companyId, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.salaryFrom = salaryFrom;
        this.salaryTo = salaryTo;
        this.companyId = companyId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public Double getSalaryFrom() {
        return salaryFrom;
    }

    public Double getSalaryTo() {
        return salaryTo;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setSalaryFrom(Double salaryFrom) {
        this.salaryFrom = salaryFrom;
    }

    public void setSalaryTo(Double salaryTo) {
        this.salaryTo = salaryTo;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}