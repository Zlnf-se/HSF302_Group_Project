package com.recruit.recruitmentapplication.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "job_postings")
public class JobPosting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 100)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", length = 30)
    private JobType jobType = JobType.FULL_TIME;

    @Column(name = "salary_min", precision = 12, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 12, scale = 2)
    private BigDecimal salaryMax;

    @Column(name = "posted_date")
    private LocalDate postedDate = LocalDate.now();

    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PostingStatus status = PostingStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "job_required_skills",
            joinColumns = @JoinColumn(name = "job_posting_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> requiredSkills = new HashSet<>();

    @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Application> applications = new ArrayList<>();

    public JobPosting() {
    }

    public JobPosting(String title, String description, String location, JobType jobType,
                      BigDecimal salaryMin, BigDecimal salaryMax, LocalDate deadline) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.jobType = jobType;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.deadline = deadline;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public JobType getJobType() { return jobType; }
    public void setJobType(JobType jobType) { this.jobType = jobType; }
    public BigDecimal getSalaryMin() { return salaryMin; }
    public void setSalaryMin(BigDecimal salaryMin) { this.salaryMin = salaryMin; }
    public BigDecimal getSalaryMax() { return salaryMax; }
    public void setSalaryMax(BigDecimal salaryMax) { this.salaryMax = salaryMax; }
    public LocalDate getPostedDate() { return postedDate; }
    public void setPostedDate(LocalDate postedDate) { this.postedDate = postedDate; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public PostingStatus getStatus() { return status; }
    public void setStatus(PostingStatus status) { this.status = status; }
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
    public Set<Skill> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(Set<Skill> value) { this.requiredSkills = value == null ? new HashSet<>() : value; }
    public List<Application> getApplications() { return applications; }
    public void setApplications(List<Application> value) { this.applications = value == null ? new ArrayList<>() : value; }

    public void addRequiredSkill(Skill skill) {
        requiredSkills.add(skill);
        skill.getJobPostings().add(this);
    }

    public void clearRequiredSkills() {
        for (Skill skill : new HashSet<>(requiredSkills)) {
            requiredSkills.remove(skill);
            skill.getJobPostings().remove(this);
        }
    }

    public void addApplication(Application application) {
        applications.add(application);
        application.setJobPosting(this);
    }

    @Override
    public String toString() {
        return "JobPosting{id=" + id + ", title='" + title + "', status=" + status
                + ", company=" + (company == null ? null : company.getName()) + "}";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof JobPosting posting)) return false;
        return id != null && id.equals(posting.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }

    public enum JobType { FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP, REMOTE }
    public enum PostingStatus { OPEN, ACTIVE, CLOSED, DRAFT }
}
