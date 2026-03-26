package com.yusufjon.recruitmentplatform.company.entity;

import com.yusufjon.recruitmentplatform.user.entity.User;
import jakarta.persistence.*;

@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id", nullable = false)
    private User recruiter;

    public Company() {
    }

    public Company(Long id, String name, String description, String location, User recruiter) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.recruiter = recruiter;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public User getRecruiter() {
        return recruiter;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setRecruiter(User recruiter) {
        this.recruiter = recruiter;
    }
}