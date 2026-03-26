package com.yusufjon.recruitmentplatform.company.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateCompanyRequest {

    @NotBlank(message = "Company name is required")
    private String name;

    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    public CreateCompanyRequest() {
    }

    public CreateCompanyRequest(String name, String description, String location) {
        this.name = name;
        this.description = description;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}