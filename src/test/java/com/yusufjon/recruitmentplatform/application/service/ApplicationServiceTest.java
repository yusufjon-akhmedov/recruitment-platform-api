package com.yusufjon.recruitmentplatform.application.service;

/**
 * Contains unit tests that verify the behavior of the application service class by mocking its
 * collaborators.
 */

import com.yusufjon.recruitmentplatform.application.dto.ApplicationResponse;
import com.yusufjon.recruitmentplatform.application.dto.CreateApplicationRequest;
import com.yusufjon.recruitmentplatform.application.dto.UpdateApplicationStatusRequest;
import com.yusufjon.recruitmentplatform.application.entity.Application;
import com.yusufjon.recruitmentplatform.application.repository.ApplicationRepository;
import com.yusufjon.recruitmentplatform.common.exception.BadRequestException;
import com.yusufjon.recruitmentplatform.common.exception.ForbiddenException;
import com.yusufjon.recruitmentplatform.common.exception.ResourceNotFoundException;
import com.yusufjon.recruitmentplatform.company.entity.Company;
import com.yusufjon.recruitmentplatform.shared.enums.ApplicationStatus;
import com.yusufjon.recruitmentplatform.shared.enums.RoleName;
import com.yusufjon.recruitmentplatform.support.SecurityTestUtils;
import com.yusufjon.recruitmentplatform.user.entity.Role;
import com.yusufjon.recruitmentplatform.user.entity.User;
import com.yusufjon.recruitmentplatform.vacancy.entity.Vacancy;
import com.yusufjon.recruitmentplatform.vacancy.repository.VacancyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationService")
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private VacancyRepository vacancyRepository;

    @InjectMocks
    private ApplicationService applicationService;

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clearAuthentication();
    }

    @Test
    @DisplayName("should allow candidate to apply to vacancy")
    void shouldAllowCandidateToApplyToVacancy() {
        User candidate = user(1L, RoleName.CANDIDATE);
        User recruiter = user(2L, RoleName.RECRUITER);
        Vacancy vacancy = vacancy(10L, recruiter);
        CreateApplicationRequest request = new CreateApplicationRequest(vacancy.getId());
        LocalDateTime createdAt = LocalDateTime.now();
        SecurityTestUtils.authenticate(candidate);

        when(vacancyRepository.findById(vacancy.getId())).thenReturn(Optional.of(vacancy));
        when(applicationRepository.existsByCandidateAndVacancy(candidate, vacancy)).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> {
            Application application = invocation.getArgument(0);
            application.setId(50L);
            application.setStatus(ApplicationStatus.PENDING);
            application.setCreatedAt(createdAt);
            return application;
        });

        ApplicationResponse response = applicationService.applyToVacancy(request);

        assertEquals(50L, response.getId());
        assertEquals(candidate.getId(), response.getCandidateId());
        assertEquals(vacancy.getId(), response.getVacancyId());
        assertEquals(ApplicationStatus.PENDING, response.getStatus());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    @DisplayName("should reject vacancy application for non candidate")
    void shouldRejectVacancyApplicationForNonCandidate() {
        User recruiter = user(2L, RoleName.RECRUITER);
        SecurityTestUtils.authenticate(recruiter);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> applicationService.applyToVacancy(new CreateApplicationRequest(10L))
        );

        assertEquals("Only candidates can apply to vacancies", exception.getMessage());
        verify(vacancyRepository, never()).findById(any());
    }

    @Test
    @DisplayName("should reject duplicate application")
    void shouldRejectDuplicateApplication() {
        User candidate = user(1L, RoleName.CANDIDATE);
        User recruiter = user(2L, RoleName.RECRUITER);
        Vacancy vacancy = vacancy(10L, recruiter);
        SecurityTestUtils.authenticate(candidate);

        when(vacancyRepository.findById(vacancy.getId())).thenReturn(Optional.of(vacancy));
        when(applicationRepository.existsByCandidateAndVacancy(candidate, vacancy)).thenReturn(true);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> applicationService.applyToVacancy(new CreateApplicationRequest(vacancy.getId()))
        );

        assertEquals("You have already applied to this vacancy", exception.getMessage());
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    @DisplayName("should throw when vacancy is not found during apply")
    void shouldThrowWhenVacancyIsNotFoundDuringApply() {
        User candidate = user(1L, RoleName.CANDIDATE);
        SecurityTestUtils.authenticate(candidate);

        when(vacancyRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> applicationService.applyToVacancy(new CreateApplicationRequest(99L))
        );

        assertEquals("Vacancy not found", exception.getMessage());
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    @DisplayName("should let candidate get own applications")
    void shouldLetCandidateGetOwnApplications() {
        User candidate = user(1L, RoleName.CANDIDATE);
        User recruiter = user(2L, RoleName.RECRUITER);
        Application application = application(50L, candidate, vacancy(10L, recruiter), ApplicationStatus.PENDING, LocalDateTime.now());
        SecurityTestUtils.authenticate(candidate);

        when(applicationRepository.findByCandidate(candidate)).thenReturn(List.of(application));

        List<ApplicationResponse> response = applicationService.getMyApplications();

        assertEquals(1, response.size());
        assertEquals(application.getId(), response.get(0).getId());
        assertEquals(candidate.getId(), response.get(0).getCandidateId());
        assertEquals(application.getVacancy().getId(), response.get(0).getVacancyId());
        assertEquals(ApplicationStatus.PENDING, response.get(0).getStatus());
    }

    @Test
    @DisplayName("should let recruiter get applications for own vacancies")
    void shouldLetRecruiterGetApplicationsForOwnVacancies() {
        User recruiter = user(2L, RoleName.RECRUITER);
        User candidate = user(1L, RoleName.CANDIDATE);
        Application application = application(50L, candidate, vacancy(10L, recruiter), ApplicationStatus.REVIEWING, LocalDateTime.now());
        SecurityTestUtils.authenticate(recruiter);

        when(applicationRepository.findByVacancy_Company_Recruiter(recruiter)).thenReturn(List.of(application));

        List<ApplicationResponse> response = applicationService.getRecruiterApplications();

        assertEquals(1, response.size());
        assertEquals(application.getId(), response.get(0).getId());
        assertEquals(candidate.getId(), response.get(0).getCandidateId());
        assertEquals(application.getVacancy().getId(), response.get(0).getVacancyId());
        assertEquals(ApplicationStatus.REVIEWING, response.get(0).getStatus());
    }

    @Test
    @DisplayName("should let recruiter update application status")
    void shouldLetRecruiterUpdateApplicationStatus() {
        User recruiter = user(2L, RoleName.RECRUITER);
        User candidate = user(1L, RoleName.CANDIDATE);
        Application application = application(50L, candidate, vacancy(10L, recruiter), ApplicationStatus.PENDING, LocalDateTime.now());
        UpdateApplicationStatusRequest request = new UpdateApplicationStatusRequest(ApplicationStatus.ACCEPTED);
        SecurityTestUtils.authenticate(recruiter);

        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(applicationRepository.save(application)).thenReturn(application);

        ApplicationResponse response = applicationService.updateApplicationStatus(application.getId(), request);

        assertEquals(ApplicationStatus.ACCEPTED, response.getStatus());
        assertEquals(candidate.getId(), response.getCandidateId());
        assertEquals(application.getVacancy().getId(), response.getVacancyId());
        verify(applicationRepository).save(application);
    }

    @Test
    @DisplayName("should reject application status update for foreign vacancy")
    void shouldRejectApplicationStatusUpdateForForeignVacancy() {
        User currentRecruiter = user(2L, RoleName.RECRUITER);
        User ownerRecruiter = user(3L, RoleName.RECRUITER);
        User candidate = user(1L, RoleName.CANDIDATE);
        Application application = application(50L, candidate, vacancy(10L, ownerRecruiter), ApplicationStatus.PENDING, LocalDateTime.now());
        UpdateApplicationStatusRequest request = new UpdateApplicationStatusRequest(ApplicationStatus.REJECTED);
        SecurityTestUtils.authenticate(currentRecruiter);

        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> applicationService.updateApplicationStatus(application.getId(), request)
        );

        assertEquals("You can update only applications for your own vacancies", exception.getMessage());
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    @DisplayName("should throw when application is not found during status update")
    void shouldThrowWhenApplicationIsNotFoundDuringStatusUpdate() {
        User recruiter = user(2L, RoleName.RECRUITER);
        SecurityTestUtils.authenticate(recruiter);

        when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> applicationService.updateApplicationStatus(999L, new UpdateApplicationStatusRequest(ApplicationStatus.ACCEPTED))
        );

        assertEquals("Application not found", exception.getMessage());
        verify(applicationRepository, never()).save(any(Application.class));
    }

    private User user(Long id, RoleName roleName) {
        return new User(id, "Test User", "user" + id + "@example.com", "encoded-secret", new Role(1L, roleName), LocalDateTime.now());
    }

    private Vacancy vacancy(Long id, User recruiter) {
        Company company = new Company(100L, "Acme", "Product company", "Tashkent", recruiter);
        return new Vacancy(id, "Java Developer", "Build APIs", "Tashkent", 1000.0, 2000.0, company, LocalDateTime.now());
    }

    private Application application(Long id, User candidate, Vacancy vacancy, ApplicationStatus status, LocalDateTime createdAt) {
        return new Application(id, candidate, vacancy, status, createdAt);
    }
}
