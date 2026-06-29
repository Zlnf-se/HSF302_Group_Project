package com.recruit.recruitmentapplication.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String name;

    @Column(length = 100)
    private String industry;

    @Column(length = 200)
    private String website;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "profile_id", unique = true)
    private CompanyProfile profile;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<JobPosting> jobPostings = new ArrayList<>();

    public Company() {
    }

    public Company(String name, String industry, String website) {
        this.name = name;
        this.industry = industry;
        this.website = website;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public CompanyProfile getProfile() { return profile; }

    public void setProfile(CompanyProfile profile) {
        this.profile = profile;
        if (profile != null && profile.getCompany() != this) {
            profile.setCompany(this);
        }
    }

    public List<JobPosting> getJobPostings() { return Collections.unmodifiableList(jobPostings); }
    public void setJobPostings(List<JobPosting> jobPostings) {
        this.jobPostings = jobPostings == null ? new ArrayList<>() : jobPostings;
    }

    public void addJobPosting(JobPosting posting) {
        jobPostings.add(posting);
        posting.setCompany(this);
    }

    public void removeJobPosting(JobPosting posting) {
        jobPostings.remove(posting);
        posting.setCompany(null);
    }

    @Override
    public String toString() {
        return "Company{id=" + id + ", name='" + name + "', industry='" + industry
                + "', profileId=" + (profile == null ? null : profile.getId()) + "}";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Company company)) return false;
        return id != null && id.equals(company.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
