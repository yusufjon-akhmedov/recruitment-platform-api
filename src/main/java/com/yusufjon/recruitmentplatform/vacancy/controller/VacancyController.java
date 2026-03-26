package com.yusufjon.recruitmentplatform.vacancy.controller;

import com.yusufjon.recruitmentplatform.vacancy.dto.CreateVacancyRequest;
import com.yusufjon.recruitmentplatform.vacancy.dto.UpdateVacancyRequest;
import com.yusufjon.recruitmentplatform.vacancy.dto.VacancyResponse;
import com.yusufjon.recruitmentplatform.vacancy.service.VacancyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vacancies")
public class VacancyController {

    private final VacancyService vacancyService;

    public VacancyController(VacancyService vacancyService) {
        this.vacancyService = vacancyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VacancyResponse createVacancy(@Valid @RequestBody CreateVacancyRequest request) {
        return vacancyService.createVacancy(request);
    }

    @GetMapping
    public java.util.List<VacancyResponse> getAllVacancies(
            @RequestParam(required = false) String title
    ) {
        return vacancyService.getAllVacancies(title);
    }

    @GetMapping("/{id}")
    public VacancyResponse getVacancyById(@PathVariable Long id) {
        return vacancyService.getVacancyById(id);
    }

    @PutMapping("/{id}")
    public VacancyResponse updateVacancy(@PathVariable Long id,
                                         @Valid @RequestBody UpdateVacancyRequest request) {
        return vacancyService.updateVacancy(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVacancy(@PathVariable Long id) {
        vacancyService.deleteVacancy(id);
    }
}