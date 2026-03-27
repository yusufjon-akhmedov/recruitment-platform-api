package com.yusufjon.recruitmentplatform.application.service;

/**
 * Contains the business logic for application features and coordinates validation, security
 * checks, and repository access.
 */

import com.yusufjon.recruitmentplatform.application.dto.ApplicationResponse;
import com.yusufjon.recruitmentplatform.application.dto.CreateApplicationRequest;
import com.yusufjon.recruitmentplatform.application.dto.UpdateApplicationStatusRequest;
import com.yusufjon.recruitmentplatform.application.entity.Application;
import com.yusufjon.recruitmentplatform.application.repository.ApplicationRepository;
import com.yusufjon.recruitmentplatform.common.exception.BadRequestException;
import com.yusufjon.recruitmentplatform.common.exception.ForbiddenException;
import com.yusufjon.recruitmentplatform.common.exception.ResourceNotFoundException;
import com.yusufjon.recruitmentplatform.shared.enums.RoleName;
import com.yusufjon.recruitmentplatform.user.entity.User;
import com.yusufjon.recruitmentplatform.vacancy.entity.Vacancy;
import com.yusufjon.recruitmentplatform.vacancy.repository.VacancyRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final VacancyRepository vacancyRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              VacancyRepository vacancyRepository) {
        this.applicationRepository = applicationRepository;
        this.vacancyRepository = vacancyRepository;
    }

    public ApplicationResponse applyToVacancy(CreateApplicationRequest request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = (User) principal;

        if (currentUser.getRole().getName() != RoleName.CANDIDATE) {
            throw new ForbiddenException("Only candidates can apply to vacancies");
        }

        Vacancy vacancy = vacancyRepository.findById(request.getVacancyId())
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

        if (applicationRepository.existsByCandidateAndVacancy(currentUser, vacancy)) {
            throw new BadRequestException("You have already applied to this vacancy");
        }

        Application application = new Application();
        application.setCandidate(currentUser);
        application.setVacancy(vacancy);

        Application savedApplication = applicationRepository.save(application);

        return new ApplicationResponse(
                savedApplication.getId(),
                savedApplication.getCandidate().getId(),
                savedApplication.getVacancy().getId(),
                savedApplication.getStatus(),
                savedApplication.getCreatedAt()
        );
    }

    public java.util.List<ApplicationResponse> getMyApplications() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = (User) principal;

        java.util.List<Application> applications = applicationRepository.findByCandidate(currentUser);

        return applications.stream()
                .map(application -> new ApplicationResponse(
                        application.getId(),
                        application.getCandidate().getId(),
                        application.getVacancy().getId(),
                        application.getStatus(),
                        application.getCreatedAt()
                ))
                .toList();
    }

    public java.util.List<ApplicationResponse> getRecruiterApplications() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = (User) principal;

        if (currentUser.getRole().getName() != RoleName.RECRUITER) {
            throw new ForbiddenException("Only recruiters can view these applications");
        }

        java.util.List<Application> applications =
                applicationRepository.findByVacancy_Company_Recruiter(currentUser);

        return applications.stream()
                .map(application -> new ApplicationResponse(
                        application.getId(),
                        application.getCandidate().getId(),
                        application.getVacancy().getId(),
                        application.getStatus(),
                        application.getCreatedAt()
                ))
                .toList();
    }

    public ApplicationResponse updateApplicationStatus(Long id, UpdateApplicationStatusRequest request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = (User) principal;

        if (currentUser.getRole().getName() != RoleName.RECRUITER) {
            throw new ForbiddenException("Only recruiters can update application status");
        }

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!application.getVacancy().getCompany().getRecruiter().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can update only applications for your own vacancies");
        }

        application.setStatus(request.getStatus());

        Application updatedApplication = applicationRepository.save(application);

        return new ApplicationResponse(
                updatedApplication.getId(),
                updatedApplication.getCandidate().getId(),
                updatedApplication.getVacancy().getId(),
                updatedApplication.getStatus(),
                updatedApplication.getCreatedAt()
        );
    }
}