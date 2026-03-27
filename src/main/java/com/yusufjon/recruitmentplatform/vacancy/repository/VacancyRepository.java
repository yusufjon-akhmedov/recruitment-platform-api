package com.yusufjon.recruitmentplatform.vacancy.repository;

/**
 * Provides JPA-based data access methods for vacancy records used by the service layer.
 */

import com.yusufjon.recruitmentplatform.company.entity.Company;
import com.yusufjon.recruitmentplatform.vacancy.entity.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface VacancyRepository extends JpaRepository<Vacancy, Long>, JpaSpecificationExecutor<Vacancy> {
    List<Vacancy> findByCompany(Company company);
    List<Vacancy> findByTitleContainingIgnoreCase(String title);
    List<Vacancy> findByLocationContainingIgnoreCase(String location);
}
