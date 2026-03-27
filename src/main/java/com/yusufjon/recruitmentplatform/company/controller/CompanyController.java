package com.yusufjon.recruitmentplatform.company.controller;

/**
 * Exposes REST endpoints for company operations and delegates incoming requests to the
 * corresponding service layer.
 */

import com.yusufjon.recruitmentplatform.company.dto.CompanyResponse;
import com.yusufjon.recruitmentplatform.company.dto.CreateCompanyRequest;
import com.yusufjon.recruitmentplatform.company.dto.UpdateCompanyRequest;
import com.yusufjon.recruitmentplatform.company.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponse createCompany(@Valid @RequestBody CreateCompanyRequest request) {
        return companyService.createCompany(request);
    }

    @GetMapping
    public java.util.List<CompanyResponse> getAllCompanies() {
        return companyService.getAllCompanies();
    }

    @GetMapping("/{id}")
    public CompanyResponse getCompanyById(@PathVariable Long id) {
        return companyService.getCompanyById(id);
    }

    @PutMapping("/{id}")
    public CompanyResponse updateCompany(@PathVariable Long id,
                                         @Valid @RequestBody UpdateCompanyRequest request) {
        return companyService.updateCompany(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
    }
}