package com.yusufjon.recruitmentplatform.company.dto;

/**
 * Represents the API response payload returned for company data.
 */

public class CompanyResponse {

    private Long id;
    private String name;
    private String description;
    private String location;
    private Long recruiterId;

    public CompanyResponse() {
    }

    public CompanyResponse(Long id, String name, String description, String location, Long recruiterId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.recruiterId = recruiterId;
    }

    public Long getId() {
        return id;
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

    public Long getRecruiterId() {
        return recruiterId;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setRecruiterId(Long recruiterId) {
        this.recruiterId = recruiterId;
    }
}