package com.yusufjon.recruitmentplatform.vacancy.specification;

/**
 * Builds reusable Spring Data specifications for vacancy search filters.
 */

import com.yusufjon.recruitmentplatform.vacancy.dto.VacancyFilterRequest;
import com.yusufjon.recruitmentplatform.vacancy.entity.Vacancy;
import org.springframework.data.jpa.domain.Specification;

public final class VacancySpecification {

    private VacancySpecification() {
    }

    public static Specification<Vacancy> withFilters(VacancyFilterRequest request) {
        Specification<Vacancy> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (titleContains(request.getTitle()) != null) {
            specification = specification.and(titleContains(request.getTitle()));
        }

        if (locationContains(request.getLocation()) != null) {
            specification = specification.and(locationContains(request.getLocation()));
        }

        if (companyIdEquals(request.getCompanyId()) != null) {
            specification = specification.and(companyIdEquals(request.getCompanyId()));
        }

        if (companyNameContains(request.getCompanyName()) != null) {
            specification = specification.and(companyNameContains(request.getCompanyName()));
        }

        if (minSalaryMatches(request.getMinSalary()) != null) {
            specification = specification.and(minSalaryMatches(request.getMinSalary()));
        }

        if (maxSalaryMatches(request.getMaxSalary()) != null) {
            specification = specification.and(maxSalaryMatches(request.getMaxSalary()));
        }

        return specification;
    }

    private static Specification<Vacancy> titleContains(String title) {
        if (isBlank(title)) {
            return null;
        }

        String normalizedTitle = "%" + title.trim().toLowerCase() + "%";
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), normalizedTitle);
    }

    private static Specification<Vacancy> locationContains(String location) {
        if (isBlank(location)) {
            return null;
        }

        String normalizedLocation = "%" + location.trim().toLowerCase() + "%";
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("location")), normalizedLocation);
    }

    private static Specification<Vacancy> companyIdEquals(Long companyId) {
        if (companyId == null) {
            return null;
        }

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("company").get("id"), companyId);
    }

    private static Specification<Vacancy> companyNameContains(String companyName) {
        if (isBlank(companyName)) {
            return null;
        }

        String normalizedCompanyName = "%" + companyName.trim().toLowerCase() + "%";
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.join("company").get("name")), normalizedCompanyName);
    }

    private static Specification<Vacancy> minSalaryMatches(Double minSalary) {
        if (minSalary == null) {
            return null;
        }

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("salaryTo"), minSalary);
    }

    private static Specification<Vacancy> maxSalaryMatches(Double maxSalary) {
        if (maxSalary == null) {
            return null;
        }

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("salaryFrom"), maxSalary);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
