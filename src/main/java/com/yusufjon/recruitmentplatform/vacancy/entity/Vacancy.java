package com.yusufjon.recruitmentplatform.vacancy.entity;

import com.yusufjon.recruitmentplatform.company.entity.Company;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vacancies")
public class Vacancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Double salaryFrom;

    @Column(nullable = false)
    private Double salaryTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Vacancy() {
    }

    public Vacancy(Long id, String title, String description, String location,
                   Double salaryFrom, Double salaryTo, Company company, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.salaryFrom = salaryFrom;
        this.salaryTo = salaryTo;
        this.company = company;
        this.createdAt = createdAt;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public Double getSalaryFrom() {
        return salaryFrom;
    }

    public Double getSalaryTo() {
        return salaryTo;
    }

    public Company getCompany() {
        return company;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setSalaryFrom(Double salaryFrom) {
        this.salaryFrom = salaryFrom;
    }

    public void setSalaryTo(Double salaryTo) {
        this.salaryTo = salaryTo;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}