package com.yusufjon.recruitmentplatform.company.repository;

/**
 * Provides JPA-based data access methods for company records used by the service layer.
 */

import com.yusufjon.recruitmentplatform.company.entity.Company;
import com.yusufjon.recruitmentplatform.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findByRecruiter(User recruiter);
}