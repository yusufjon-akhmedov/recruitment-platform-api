package com.yusufjon.recruitmentplatform.application.repository;

/**
 * Provides JPA-based data access methods for application records used by the service layer.
 */

import com.yusufjon.recruitmentplatform.application.entity.Application;
import com.yusufjon.recruitmentplatform.user.entity.User;
import com.yusufjon.recruitmentplatform.vacancy.entity.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByCandidate(User candidate);
    boolean existsByCandidateAndVacancy(User candidate, Vacancy vacancy);
    List<Application> findByVacancy_Company_Recruiter(User recruiter);
}