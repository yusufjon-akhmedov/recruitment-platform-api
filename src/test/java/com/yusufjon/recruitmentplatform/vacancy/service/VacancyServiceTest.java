package com.yusufjon.recruitmentplatform.vacancy.service;

/**
 * Contains unit tests that verify the behavior of the vacancy service class by mocking its
 * collaborators.
 */

import com.yusufjon.recruitmentplatform.common.exception.ForbiddenException;
import com.yusufjon.recruitmentplatform.common.exception.BadRequestException;
import com.yusufjon.recruitmentplatform.common.exception.ResourceNotFoundException;
import com.yusufjon.recruitmentplatform.common.response.PageResponse;
import com.yusufjon.recruitmentplatform.company.entity.Company;
import com.yusufjon.recruitmentplatform.company.repository.CompanyRepository;
import com.yusufjon.recruitmentplatform.shared.enums.RoleName;
import com.yusufjon.recruitmentplatform.support.SecurityTestUtils;
import com.yusufjon.recruitmentplatform.user.entity.Role;
import com.yusufjon.recruitmentplatform.user.entity.User;
import com.yusufjon.recruitmentplatform.vacancy.dto.CreateVacancyRequest;
import com.yusufjon.recruitmentplatform.vacancy.dto.UpdateVacancyRequest;
import com.yusufjon.recruitmentplatform.vacancy.dto.VacancyFilterRequest;
import com.yusufjon.recruitmentplatform.vacancy.dto.VacancyResponse;
import com.yusufjon.recruitmentplatform.vacancy.entity.Vacancy;
import com.yusufjon.recruitmentplatform.vacancy.repository.VacancyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

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
@DisplayName("VacancyService")
class VacancyServiceTest {

    @Mock
    private VacancyRepository vacancyRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private VacancyService vacancyService;

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clearAuthentication();
    }

    @Test
    @DisplayName("should allow recruiter to create vacancy for own company")
    void shouldAllowRecruiterToCreateVacancyForOwnCompany() {
        User recruiter = user(1L, RoleName.RECRUITER);
        Company company = company(10L, recruiter);
        CreateVacancyRequest request = new CreateVacancyRequest(
                "Java Developer",
                "Build APIs",
                "Tashkent",
                1000.0,
                2000.0,
                company.getId()
        );
        LocalDateTime createdAt = LocalDateTime.now();
        SecurityTestUtils.authenticate(recruiter);

        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));
        when(vacancyRepository.save(any(Vacancy.class))).thenAnswer(invocation -> {
            Vacancy vacancy = invocation.getArgument(0);
            vacancy.setId(20L);
            vacancy.setCreatedAt(createdAt);
            return vacancy;
        });

        VacancyResponse response = vacancyService.createVacancy(request);

        assertEquals(20L, response.getId());
        assertEquals("Java Developer", response.getTitle());
        assertEquals("Build APIs", response.getDescription());
        assertEquals("Tashkent", response.getLocation());
        assertEquals(1000.0, response.getSalaryFrom());
        assertEquals(2000.0, response.getSalaryTo());
        assertEquals(company.getId(), response.getCompanyId());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    @DisplayName("should reject vacancy creation for another recruiters company")
    void shouldRejectVacancyCreationForAnotherRecruitersCompany() {
        User currentRecruiter = user(1L, RoleName.RECRUITER);
        User companyOwner = user(2L, RoleName.RECRUITER);
        Company company = company(10L, companyOwner);
        CreateVacancyRequest request = new CreateVacancyRequest(
                "Java Developer",
                "Build APIs",
                "Tashkent",
                1000.0,
                2000.0,
                company.getId()
        );
        SecurityTestUtils.authenticate(currentRecruiter);

        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> vacancyService.createVacancy(request)
        );

        assertEquals("You can create vacancy only for your own company", exception.getMessage());
        verify(vacancyRepository, never()).save(any(Vacancy.class));
    }

    @Test
    @DisplayName("should throw when company is not found during vacancy creation")
    void shouldThrowWhenCompanyIsNotFoundDuringVacancyCreation() {
        User recruiter = user(1L, RoleName.RECRUITER);
        CreateVacancyRequest request = new CreateVacancyRequest(
                "Java Developer",
                "Build APIs",
                "Tashkent",
                1000.0,
                2000.0,
                99L
        );
        SecurityTestUtils.authenticate(recruiter);

        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> vacancyService.createVacancy(request)
        );

        assertEquals("Company not found", exception.getMessage());
        verify(vacancyRepository, never()).save(any(Vacancy.class));
    }

    @Test
    @DisplayName("should get vacancy by id successfully")
    void shouldGetVacancyByIdSuccessfully() {
        User recruiter = user(1L, RoleName.RECRUITER);
        Company company = company(10L, recruiter);
        LocalDateTime createdAt = LocalDateTime.now();
        Vacancy vacancy = vacancy(20L, "Java Developer", "Build APIs", "Tashkent", 1000.0, 2000.0, company, createdAt);

        when(vacancyRepository.findById(20L)).thenReturn(Optional.of(vacancy));

        VacancyResponse response = vacancyService.getVacancyById(20L);

        assertEquals(vacancy.getId(), response.getId());
        assertEquals(vacancy.getTitle(), response.getTitle());
        assertEquals(vacancy.getDescription(), response.getDescription());
        assertEquals(vacancy.getLocation(), response.getLocation());
        assertEquals(vacancy.getSalaryFrom(), response.getSalaryFrom());
        assertEquals(vacancy.getSalaryTo(), response.getSalaryTo());
        assertEquals(company.getId(), response.getCompanyId());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    @DisplayName("should throw when vacancy is not found by id")
    void shouldThrowWhenVacancyIsNotFoundById() {
        when(vacancyRepository.findById(404L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> vacancyService.getVacancyById(404L)
        );

        assertEquals("Vacancy not found", exception.getMessage());
    }

    @Test
    @DisplayName("should update own vacancy successfully")
    void shouldUpdateOwnVacancySuccessfully() {
        User recruiter = user(1L, RoleName.RECRUITER);
        Company company = company(10L, recruiter);
        LocalDateTime createdAt = LocalDateTime.now();
        Vacancy vacancy = vacancy(20L, "Java Developer", "Build APIs", "Tashkent", 1000.0, 2000.0, company, createdAt);
        UpdateVacancyRequest request = new UpdateVacancyRequest(
                "Senior Java Developer",
                "Build and maintain APIs",
                "Samarkand",
                1500.0,
                2500.0
        );
        SecurityTestUtils.authenticate(recruiter);

        when(vacancyRepository.findById(20L)).thenReturn(Optional.of(vacancy));
        when(vacancyRepository.save(vacancy)).thenReturn(vacancy);

        VacancyResponse response = vacancyService.updateVacancy(20L, request);

        assertEquals("Senior Java Developer", response.getTitle());
        assertEquals("Build and maintain APIs", response.getDescription());
        assertEquals("Samarkand", response.getLocation());
        assertEquals(1500.0, response.getSalaryFrom());
        assertEquals(2500.0, response.getSalaryTo());
        verify(vacancyRepository).save(vacancy);
    }

    @Test
    @DisplayName("should throw when updating a foreign vacancy")
    void shouldThrowWhenUpdatingAForeignVacancy() {
        User currentRecruiter = user(1L, RoleName.RECRUITER);
        User ownerRecruiter = user(2L, RoleName.RECRUITER);
        Company company = company(10L, ownerRecruiter);
        Vacancy vacancy = vacancy(20L, "Java Developer", "Build APIs", "Tashkent", 1000.0, 2000.0, company, LocalDateTime.now());
        UpdateVacancyRequest request = new UpdateVacancyRequest(
                "Senior Java Developer",
                "Build and maintain APIs",
                "Samarkand",
                1500.0,
                2500.0
        );
        SecurityTestUtils.authenticate(currentRecruiter);

        when(vacancyRepository.findById(20L)).thenReturn(Optional.of(vacancy));

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> vacancyService.updateVacancy(20L, request)
        );

        assertEquals("You can update only your own vacancy", exception.getMessage());
        verify(vacancyRepository, never()).save(any(Vacancy.class));
    }

    @Test
    @DisplayName("should throw when updating a missing vacancy")
    void shouldThrowWhenUpdatingAMissingVacancy() {
        SecurityTestUtils.authenticate(user(1L, RoleName.RECRUITER));

        when(vacancyRepository.findById(20L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> vacancyService.updateVacancy(20L, new UpdateVacancyRequest("Title", "Desc", "City", 1000.0, 2000.0))
        );

        assertEquals("Vacancy not found", exception.getMessage());
        verify(vacancyRepository, never()).save(any(Vacancy.class));
    }

    @Test
    @DisplayName("should delete own vacancy successfully")
    void shouldDeleteOwnVacancySuccessfully() {
        User recruiter = user(1L, RoleName.RECRUITER);
        Company company = company(10L, recruiter);
        Vacancy vacancy = vacancy(20L, "Java Developer", "Build APIs", "Tashkent", 1000.0, 2000.0, company, LocalDateTime.now());
        SecurityTestUtils.authenticate(recruiter);

        when(vacancyRepository.findById(20L)).thenReturn(Optional.of(vacancy));

        vacancyService.deleteVacancy(20L);

        verify(vacancyRepository).delete(vacancy);
    }

    @Test
    @DisplayName("should throw when deleting a foreign vacancy")
    void shouldThrowWhenDeletingAForeignVacancy() {
        User currentRecruiter = user(1L, RoleName.RECRUITER);
        User ownerRecruiter = user(2L, RoleName.RECRUITER);
        Company company = company(10L, ownerRecruiter);
        Vacancy vacancy = vacancy(20L, "Java Developer", "Build APIs", "Tashkent", 1000.0, 2000.0, company, LocalDateTime.now());
        SecurityTestUtils.authenticate(currentRecruiter);

        when(vacancyRepository.findById(20L)).thenReturn(Optional.of(vacancy));

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> vacancyService.deleteVacancy(20L)
        );

        assertEquals("You can delete only your own vacancy", exception.getMessage());
        verify(vacancyRepository, never()).delete(any(Vacancy.class));
    }

    @Test
    @DisplayName("should throw when deleting a missing vacancy")
    void shouldThrowWhenDeletingAMissingVacancy() {
        SecurityTestUtils.authenticate(user(1L, RoleName.RECRUITER));

        when(vacancyRepository.findById(20L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> vacancyService.deleteVacancy(20L)
        );

        assertEquals("Vacancy not found", exception.getMessage());
        verify(vacancyRepository, never()).delete(any(Vacancy.class));
    }

    @Test
    @DisplayName("should return paged vacancies using filters and sorting")
    void shouldReturnPagedVacanciesUsingFiltersAndSorting() {
        User recruiter = user(1L, RoleName.RECRUITER);
        Company company = company(10L, recruiter);
        LocalDateTime createdAt = LocalDateTime.now();
        Vacancy backendVacancy = vacancy(20L, "Backend Java Developer", "APIs", "Tashkent", 1000.0, 2000.0, company, createdAt);
        Vacancy fullstackVacancy = vacancy(21L, "Senior Java Fullstack", "Platform", "Samarkand", 1500.0, 2500.0, company, createdAt.plusDays(1));
        VacancyFilterRequest request = new VacancyFilterRequest();
        request.setTitle("java");
        request.setLocation("tashkent");
        request.setCompanyId(company.getId());
        request.setCompanyName("acme");
        request.setMinSalary(1500.0);
        request.setMaxSalary(2600.0);
        request.setPage(1);
        request.setSize(5);
        request.setSortBy("salaryTo");
        request.setSortDir("asc");

        when(vacancyRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(backendVacancy, fullstackVacancy), Pageable.ofSize(5).withPage(1), 7));

        PageResponse<VacancyResponse> response = vacancyService.getAllVacancies(request);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(vacancyRepository).findAll(any(Specification.class), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
        assertEquals(Sort.Direction.ASC, pageable.getSort().getOrderFor("salaryTo").getDirection());
        assertEquals(2, response.getContent().size());
        assertEquals("Backend Java Developer", response.getContent().get(0).getTitle());
        assertEquals("Senior Java Fullstack", response.getContent().get(1).getTitle());
        assertEquals(1, response.getPage());
        assertEquals(5, response.getSize());
        assertEquals(7, response.getTotalElements());
    }

    @Test
    @DisplayName("should throw when sortBy is invalid")
    void shouldThrowWhenSortByIsInvalid() {
        VacancyFilterRequest request = new VacancyFilterRequest();
        request.setSortBy("companyName");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> vacancyService.getAllVacancies(request));

        assertEquals("Invalid sortBy value", exception.getMessage());
    }

    @Test
    @DisplayName("should throw when sortDir is invalid")
    void shouldThrowWhenSortDirIsInvalid() {
        VacancyFilterRequest request = new VacancyFilterRequest();
        request.setSortDir("down");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> vacancyService.getAllVacancies(request));

        assertEquals("Invalid sortDir value", exception.getMessage());
    }

    @Test
    @DisplayName("should throw when salary range is invalid")
    void shouldThrowWhenSalaryRangeIsInvalid() {
        VacancyFilterRequest request = new VacancyFilterRequest();
        request.setMinSalary(3000.0);
        request.setMaxSalary(2000.0);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> vacancyService.getAllVacancies(request));

        assertEquals("Min salary cannot be greater than max salary", exception.getMessage());
    }

    @Test
    @DisplayName("should throw when page is negative")
    void shouldThrowWhenPageIsNegative() {
        VacancyFilterRequest request = new VacancyFilterRequest();
        request.setPage(-1);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> vacancyService.getAllVacancies(request));

        assertEquals("Page must be greater than or equal to 0", exception.getMessage());
    }

    @Test
    @DisplayName("should throw when size is outside the allowed range")
    void shouldThrowWhenSizeIsOutsideTheAllowedRange() {
        VacancyFilterRequest request = new VacancyFilterRequest();
        request.setSize(101);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> vacancyService.getAllVacancies(request));

        assertEquals("Size must be between 1 and 100", exception.getMessage());
    }

    private User user(Long id, RoleName roleName) {
        return new User(id, "Test User", "user" + id + "@example.com", "encoded-secret", new Role(1L, roleName), LocalDateTime.now());
    }

    private Company company(Long id, User recruiter) {
        return new Company(id, "Acme", "Product company", "Tashkent", recruiter);
    }

    private Vacancy vacancy(Long id, String title, String description, String location,
                            Double salaryFrom, Double salaryTo, Company company, LocalDateTime createdAt) {
        return new Vacancy(id, title, description, location, salaryFrom, salaryTo, company, createdAt);
    }
}
