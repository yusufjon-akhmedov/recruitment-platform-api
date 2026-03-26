package com.yusufjon.recruitmentplatform.vacancy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class UpdateVacancyRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Salary from is required")
    @Positive(message = "Salary from must be positive")
    private Double salaryFrom;

    @NotNull(message = "Salary to is required")
    @Positive(message = "Salary to must be positive")
    private Double salaryTo;

    public UpdateVacancyRequest() {
    }

    public UpdateVacancyRequest(String title, String description, String location,
                                Double salaryFrom, Double salaryTo) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.salaryFrom = salaryFrom;
        this.salaryTo = salaryTo;
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
}