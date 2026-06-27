package com.recruit.recruitmentapplication.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;

@Entity
@Table(name = "candidates")
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", unique = true)
    private CandidateProfile profile;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "candidate_skills",
            joinColumns = @JoinColumn(name = "candidate_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Application> applications = new ArrayList<>();

    public Candidate() {
    }

    public Candidate(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public CandidateProfile getProfile() { return profile; }

    public void setProfile(CandidateProfile profile) {
        this.profile = profile;
        if (profile != null && profile.getCandidate() != this) {
            profile.setCandidate(this);
        }
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Set<Skill> getSkills() { return Collections.unmodifiableSet(skills); }
    public void setSkills(Set<Skill> skills) { this.skills = skills == null ? new HashSet<>() : skills; }
    public List<Application> getApplications() { return applications; }
    public void setApplications(List<Application> applications) {
        this.applications = applications == null ? new ArrayList<>() : applications;
    }

    public void addSkill(Skill skill) {
        skills.add(skill);
        skill.getCandidates().add(this);
    }

    public void clearSkills() {
        for (Skill skill : new HashSet<>(skills)) {
            skills.remove(skill);
            skill.getCandidates().remove(this);
        }
    }

    public void addApplication(Application application) {
        applications.add(application);
        application.setCandidate(this);
    }

    @Override
    public String toString() {
        return "Candidate{id=" + id + ", name='" + name + "', email='" + email + "'}";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Candidate candidate)) return false;
        return id != null && id.equals(candidate.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
