package com.yusufjon.recruitmentplatform.company.service;

import com.yusufjon.recruitmentplatform.common.exception.ForbiddenException;
import com.yusufjon.recruitmentplatform.common.exception.ResourceNotFoundException;
import com.yusufjon.recruitmentplatform.company.dto.CompanyResponse;
import com.yusufjon.recruitmentplatform.company.dto.CreateCompanyRequest;
import com.yusufjon.recruitmentplatform.company.dto.UpdateCompanyRequest;
import com.yusufjon.recruitmentplatform.company.entity.Company;
import com.yusufjon.recruitmentplatform.company.repository.CompanyRepository;
import com.yusufjon.recruitmentplatform.shared.enums.RoleName;
import com.yusufjon.recruitmentplatform.support.SecurityTestUtils;
import com.yusufjon.recruitmentplatform.user.entity.Role;
import com.yusufjon.recruitmentplatform.user.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyService")
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyService companyService;

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clearAuthentication();
    }

    @Test
    @DisplayName("should allow recruiter to create company")
    void shouldAllowRecruiterToCreateCompany() {
        User recruiter = user(1L, RoleName.RECRUITER);
        CreateCompanyRequest request = new CreateCompanyRequest("Acme", "Product company", "Tashkent");
        SecurityTestUtils.authenticate(recruiter);

        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> {
            Company company = invocation.getArgument(0);
            company.setId(10L);
            return company;
        });

        CompanyResponse response = companyService.createCompany(request);

        assertEquals(10L, response.getId());
        assertEquals("Acme", response.getName());
        assertEquals("Product company", response.getDescription());
        assertEquals("Tashkent", response.getLocation());
        assertEquals(recruiter.getId(), response.getRecruiterId());
    }

    @Test
    @DisplayName("should reject company creation for non recruiter")
    void shouldRejectCompanyCreationForNonRecruiter() {
        User candidate = user(2L, RoleName.CANDIDATE);
        CreateCompanyRequest request = new CreateCompanyRequest("Acme", "Product company", "Tashkent");
        SecurityTestUtils.authenticate(candidate);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> companyService.createCompany(request));

        assertEquals("Only recruiters can create companies", exception.getMessage());
        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    @DisplayName("should get company by id successfully")
    void shouldGetCompanyByIdSuccessfully() {
        User recruiter = user(1L, RoleName.RECRUITER);
        Company company = company(10L, "Acme", "Product company", "Tashkent", recruiter);

        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));

        CompanyResponse response = companyService.getCompanyById(10L);

        assertEquals(company.getId(), response.getId());
        assertEquals(company.getName(), response.getName());
        assertEquals(company.getDescription(), response.getDescription());
        assertEquals(company.getLocation(), response.getLocation());
        assertEquals(recruiter.getId(), response.getRecruiterId());
    }

    @Test
    @DisplayName("should throw when company is not found by id")
    void shouldThrowWhenCompanyIsNotFoundById() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> companyService.getCompanyById(99L)
        );

        assertEquals("Company not found", exception.getMessage());
    }

    @Test
    @DisplayName("should update own company successfully")
    void shouldUpdateOwnCompanySuccessfully() {
        User recruiter = user(1L, RoleName.RECRUITER);
        Company company = company(10L, "Old Name", "Old description", "Old city", recruiter);
        UpdateCompanyRequest request = new UpdateCompanyRequest("New Name", "New description", "New city");
        SecurityTestUtils.authenticate(recruiter);

        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));
        when(companyRepository.save(company)).thenReturn(company);

        CompanyResponse response = companyService.updateCompany(10L, request);

        assertEquals("New Name", response.getName());
        assertEquals("New description", response.getDescription());
        assertEquals("New city", response.getLocation());
        verify(companyRepository).save(company);
    }

    @Test
    @DisplayName("should fail when updating another recruiters company")
    void shouldFailWhenUpdatingAnotherRecruitersCompany() {
        User currentRecruiter = user(1L, RoleName.RECRUITER);
        User ownerRecruiter = user(2L, RoleName.RECRUITER);
        Company company = company(10L, "Acme", "Product company", "Tashkent", ownerRecruiter);
        UpdateCompanyRequest request = new UpdateCompanyRequest("New Name", "New description", "New city");
        SecurityTestUtils.authenticate(currentRecruiter);

        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> companyService.updateCompany(10L, request)
        );

        assertEquals("You can update only your own company", exception.getMessage());
        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    @DisplayName("should delete own company successfully")
    void shouldDeleteOwnCompanySuccessfully() {
        User recruiter = user(1L, RoleName.RECRUITER);
        Company company = company(10L, "Acme", "Product company", "Tashkent", recruiter);
        SecurityTestUtils.authenticate(recruiter);

        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));

        companyService.deleteCompany(10L);

        verify(companyRepository).delete(company);
    }

    @Test
    @DisplayName("should fail when deleting another recruiters company")
    void shouldFailWhenDeletingAnotherRecruitersCompany() {
        User currentRecruiter = user(1L, RoleName.RECRUITER);
        User ownerRecruiter = user(2L, RoleName.RECRUITER);
        Company company = company(10L, "Acme", "Product company", "Tashkent", ownerRecruiter);
        SecurityTestUtils.authenticate(currentRecruiter);

        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> companyService.deleteCompany(10L)
        );

        assertEquals("You can delete only your own company", exception.getMessage());
        verify(companyRepository, never()).delete(any(Company.class));
    }

    private User user(Long id, RoleName roleName) {
        return new User(id, "Test User", "user" + id + "@example.com", "encoded-secret", new Role(1L, roleName), LocalDateTime.now());
    }

    private Company company(Long id, String name, String description, String location, User recruiter) {
        return new Company(id, name, description, location, recruiter);
    }
}
