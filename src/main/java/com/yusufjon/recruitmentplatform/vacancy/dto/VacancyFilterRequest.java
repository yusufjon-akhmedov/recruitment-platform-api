package com.yusufjon.recruitmentplatform.vacancy.dto;

/**
 * Holds the filter, pagination, and sorting options used when listing vacancies.
 */

public class VacancyFilterRequest {

    private String title;
    private String location;
    private Long companyId;
    private String companyName;
    private Double minSalary;
    private Double maxSalary;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDir;

    public VacancyFilterRequest() {
    }

    public VacancyFilterRequest(String title, String location, Long companyId, String companyName,
                                Double minSalary, Double maxSalary, Integer page, Integer size,
                                String sortBy, String sortDir) {
        this.title = title;
        this.location = location;
        this.companyId = companyId;
        this.companyName = companyName;
        this.minSalary = minSalary;
        this.maxSalary = maxSalary;
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.sortDir = sortDir;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public Double getMinSalary() {
        return minSalary;
    }

    public Double getMaxSalary() {
        return maxSalary;
    }

    public Integer getPage() {
        return page;
    }

    public Integer getSize() {
        return size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public String getSortDir() {
        return sortDir;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setMinSalary(Double minSalary) {
        this.minSalary = minSalary;
    }

    public void setMaxSalary(Double maxSalary) {
        this.maxSalary = maxSalary;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }
}
