package com.yusufjon.recruitmentplatform.vacancy.service;

import com.yusufjon.recruitmentplatform.common.exception.ForbiddenException;
import com.yusufjon.recruitmentplatform.common.exception.ResourceNotFoundException;
import com.yusufjon.recruitmentplatform.company.entity.Company;
import com.yusufjon.recruitmentplatform.company.repository.CompanyRepository;
import com.yusufjon.recruitmentplatform.shared.enums.RoleName;
import com.yusufjon.recruitmentplatform.user.entity.User;
import com.yusufjon.recruitmentplatform.vacancy.dto.CreateVacancyRequest;
import com.yusufjon.recruitmentplatform.vacancy.dto.UpdateVacancyRequest;
import com.yusufjon.recruitmentplatform.vacancy.dto.VacancyResponse;
import com.yusufjon.recruitmentplatform.vacancy.entity.Vacancy;
import com.yusufjon.recruitmentplatform.vacancy.repository.VacancyRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class VacancyService {

    private final VacancyRepository vacancyRepository;
    private final CompanyRepository companyRepository;

    public VacancyService(VacancyRepository vacancyRepository, CompanyRepository companyRepository) {
        this.vacancyRepository = vacancyRepository;
        this.companyRepository = companyRepository;
    }

    public VacancyResponse createVacancy(CreateVacancyRequest request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = (User) principal;

        if (currentUser.getRole().getName() != RoleName.RECRUITER) {
            throw new ForbiddenException("Only recruiters can create vacancies");
        }

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        if (!company.getRecruiter().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can create vacancy only for your own company");
        }

        Vacancy vacancy = new Vacancy();
        vacancy.setTitle(request.getTitle());
        vacancy.setDescription(request.getDescription());
        vacancy.setLocation(request.getLocation());
        vacancy.setSalaryFrom(request.getSalaryFrom());
        vacancy.setSalaryTo(request.getSalaryTo());
        vacancy.setCompany(company);

        Vacancy savedVacancy = vacancyRepository.save(vacancy);

        return new VacancyResponse(
                savedVacancy.getId(),
                savedVacancy.getTitle(),
                savedVacancy.getDescription(),
                savedVacancy.getLocation(),
                savedVacancy.getSalaryFrom(),
                savedVacancy.getSalaryTo(),
                savedVacancy.getCompany().getId(),
                savedVacancy.getCreatedAt()
        );
    }

    public java.util.List<VacancyResponse> getAllVacancies(String title) {
        java.util.List<Vacancy> vacancies;

        if (title != null && !title.isBlank()) {
            vacancies = vacancyRepository.findByTitleContainingIgnoreCase(title);
        } else {
            vacancies = vacancyRepository.findAll();
        }

        return vacancies.stream()
                .map(vacancy -> new VacancyResponse(
                        vacancy.getId(),
                        vacancy.getTitle(),
                        vacancy.getDescription(),
                        vacancy.getLocation(),
                        vacancy.getSalaryFrom(),
                        vacancy.getSalaryTo(),
                        vacancy.getCompany().getId(),
                        vacancy.getCreatedAt()
                ))
                .toList();
    }

    public VacancyResponse getVacancyById(Long id) {
        Vacancy vacancy = vacancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

        return new VacancyResponse(
                vacancy.getId(),
                vacancy.getTitle(),
                vacancy.getDescription(),
                vacancy.getLocation(),
                vacancy.getSalaryFrom(),
                vacancy.getSalaryTo(),
                vacancy.getCompany().getId(),
                vacancy.getCreatedAt()
        );
    }

    public VacancyResponse updateVacancy(Long id, UpdateVacancyRequest request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = (User) principal;

        Vacancy vacancy = vacancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

        if (!vacancy.getCompany().getRecruiter().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can update only your own vacancy");
        }

        vacancy.setTitle(request.getTitle());
        vacancy.setDescription(request.getDescription());
        vacancy.setLocation(request.getLocation());
        vacancy.setSalaryFrom(request.getSalaryFrom());
        vacancy.setSalaryTo(request.getSalaryTo());

        Vacancy updatedVacancy = vacancyRepository.save(vacancy);

        return new VacancyResponse(
                updatedVacancy.getId(),
                updatedVacancy.getTitle(),
                updatedVacancy.getDescription(),
                updatedVacancy.getLocation(),
                updatedVacancy.getSalaryFrom(),
                updatedVacancy.getSalaryTo(),
                updatedVacancy.getCompany().getId(),
                updatedVacancy.getCreatedAt()
        );
    }

    public void deleteVacancy(Long id) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = (User) principal;

        Vacancy vacancy = vacancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

        if (!vacancy.getCompany().getRecruiter().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can delete only your own vacancy");
        }

        vacancyRepository.delete(vacancy);
    }
}