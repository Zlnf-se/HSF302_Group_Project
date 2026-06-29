package com.recruit.recruitmentapplication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "skills")
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 50)
    private String category;

    @ManyToMany(mappedBy = "requiredSkills", fetch = FetchType.LAZY)
    private Set<JobPosting> jobPostings = new HashSet<>();

    @ManyToMany(mappedBy = "skills", fetch = FetchType.LAZY)
    private Set<Candidate> candidates = new HashSet<>();

    public Skill() {
    }

    public Skill(String name, String category) {
        this.name = name;
        this.category = category;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Set<JobPosting> getJobPostings() { return jobPostings; }
    public void setJobPostings(Set<JobPosting> value) { this.jobPostings = value == null ? new HashSet<>() : value; }
    public Set<Candidate> getCandidates() { return candidates; }
    public void setCandidates(Set<Candidate> value) { this.candidates = value == null ? new HashSet<>() : value; }

    @Override
    public String toString() { return "Skill{id=" + id + ", name='" + name + "'}"; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Skill skill)) return false;
        if (id != null && skill.id != null) return id.equals(skill.id);
        return name != null && name.equals(skill.name);
    }

    @Override
    public int hashCode() { return name == null ? getClass().hashCode() : name.hashCode(); }
}
