package com.yusufjon.recruitmentplatform.vacancy.repository;

import com.yusufjon.recruitmentplatform.company.entity.Company;
import com.yusufjon.recruitmentplatform.vacancy.entity.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VacancyRepository extends JpaRepository<Vacancy, Long> {
    List<Vacancy> findByCompany(Company company);
    List<Vacancy> findByTitleContainingIgnoreCase(String title);
    List<Vacancy> findByLocationContainingIgnoreCase(String location);
}