package com.yusufjon.recruitmentplatform.application.controller;

import com.yusufjon.recruitmentplatform.application.dto.ApplicationResponse;
import com.yusufjon.recruitmentplatform.application.dto.CreateApplicationRequest;
import com.yusufjon.recruitmentplatform.application.dto.UpdateApplicationStatusRequest;
import com.yusufjon.recruitmentplatform.application.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse applyToVacancy(@Valid @RequestBody CreateApplicationRequest request) {
        return applicationService.applyToVacancy(request);
    }

    @GetMapping("/my")
    public java.util.List<ApplicationResponse> getMyApplications() {
        return applicationService.getMyApplications();
    }

    @GetMapping("/recruiter")
    public java.util.List<ApplicationResponse> getRecruiterApplications() {
        return applicationService.getRecruiterApplications();
    }

    @PatchMapping("/{id}/status")
    public ApplicationResponse updateApplicationStatus(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateApplicationStatusRequest request) {
        return applicationService.updateApplicationStatus(id, request);
    }
}