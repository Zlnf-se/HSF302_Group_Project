package com.recruit.recruitmentapplication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "company_profiles")
public class CompanyProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 2000)
    private String description;

    @Column(length = 200)
    private String headquarters;

    @Column(name = "employee_count")
    private Integer employeeCount;

    @Column(name = "founded_year")
    private Integer foundedYear;

    @OneToOne(mappedBy = "profile", fetch = FetchType.LAZY)
    private Company company;

    public CompanyProfile() {
    }

    public CompanyProfile(String description, String headquarters, Integer employeeCount, Integer foundedYear) {
        this.description = description;
        this.headquarters = headquarters;
        this.employeeCount = employeeCount;
        this.foundedYear = foundedYear;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getHeadquarters() { return headquarters; }
    public void setHeadquarters(String headquarters) { this.headquarters = headquarters; }
    public Integer getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(Integer employeeCount) { this.employeeCount = employeeCount; }
    public Integer getFoundedYear() { return foundedYear; }
    public void setFoundedYear(Integer foundedYear) { this.foundedYear = foundedYear; }
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    @Override
    public String toString() {
        return "CompanyProfile{id=" + id + ", headquarters='" + headquarters + "'}";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof CompanyProfile profile)) return false;
        return id != null && id.equals(profile.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
