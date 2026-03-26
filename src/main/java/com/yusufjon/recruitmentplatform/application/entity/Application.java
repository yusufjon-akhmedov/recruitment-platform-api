package com.yusufjon.recruitmentplatform.application.entity;

import com.yusufjon.recruitmentplatform.shared.enums.ApplicationStatus;
import com.yusufjon.recruitmentplatform.user.entity.User;
import com.yusufjon.recruitmentplatform.vacancy.entity.Vacancy;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacancy_id", nullable = false)
    private Vacancy vacancy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Application() {
    }

    public Application(Long id, User candidate, Vacancy vacancy, ApplicationStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.candidate = candidate;
        this.vacancy = vacancy;
        this.status = status;
        this.createdAt = createdAt;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ApplicationStatus.PENDING;
        }
    }

    public Long getId() {
        return id;
    }

    public User getCandidate() {
        return candidate;
    }

    public Vacancy getVacancy() {
        return vacancy;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCandidate(User candidate) {
        this.candidate = candidate;
    }

    public void setVacancy(Vacancy vacancy) {
        this.vacancy = vacancy;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}