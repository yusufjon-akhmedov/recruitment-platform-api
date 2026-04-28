package com.yusufjon.recruitmentplatform.vacancy.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Holds the filter, pagination, and sorting options used when listing vacancies.
 */

@Getter
@Setter
@NoArgsConstructor
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

}
