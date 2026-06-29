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
@Table(name = "candidate_profiles")
public class CandidateProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "education_level", length = 100)
    private String educationLevel;

    @Column(name = "current_title", length = 150)
    private String currentTitle;

    @Column(name = "linkedin_url", length = 300)
    private String linkedinUrl;

    @Column(name = "resume_summary", length = 2000)
    private String resumeSummary;

    @OneToOne(mappedBy = "profile", fetch = FetchType.LAZY)
    private Candidate candidate;

    public CandidateProfile() {
    }

    public CandidateProfile(Integer yearsOfExperience, String educationLevel, String currentTitle,
                            String linkedinUrl, String resumeSummary) {
        this.yearsOfExperience = yearsOfExperience;
        this.educationLevel = educationLevel;
        this.currentTitle = currentTitle;
        this.linkedinUrl = linkedinUrl;
        this.resumeSummary = resumeSummary;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(Integer value) { this.yearsOfExperience = value; }
    public String getEducationLevel() { return educationLevel; }
    public void setEducationLevel(String value) { this.educationLevel = value; }
    public String getCurrentTitle() { return currentTitle; }
    public void setCurrentTitle(String value) { this.currentTitle = value; }
    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String value) { this.linkedinUrl = value; }
    public String getResumeSummary() { return resumeSummary; }
    public void setResumeSummary(String value) { this.resumeSummary = value; }
    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }

    @Override
    public String toString() {
        return "CandidateProfile{id=" + id + ", currentTitle='" + currentTitle + "'}";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof CandidateProfile profile)) return false;
        return id != null && id.equals(profile.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
