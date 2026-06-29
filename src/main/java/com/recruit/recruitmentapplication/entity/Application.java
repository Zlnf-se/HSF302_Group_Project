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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "applications", uniqueConstraints = @UniqueConstraint(columnNames = {"candidate_id", "job_posting_id"}))
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "stage_entered_at")
    private LocalDateTime stageEnteredAt = LocalDateTime.now();

    @Column(name = "cover_letter", length = 3000)
    private String coverLetter;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Interview> interviews = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<InternalNote> internalNotes = new ArrayList<>();

    public Application() {
    }

    public Application(Candidate candidate, JobPosting jobPosting, String coverLetter) {
        this.candidate = candidate;
        this.jobPosting = jobPosting;
        this.coverLetter = coverLetter;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }
    public JobPosting getJobPosting() { return jobPosting; }
    public void setJobPosting(JobPosting value) { this.jobPosting = value; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime value) { this.appliedAt = value; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String value) { this.coverLetter = value; }
    public LocalDateTime getStageEnteredAt() { return stageEnteredAt; }
    public void setStageEnteredAt(LocalDateTime value) { this.stageEnteredAt = value; }
    public List<Interview> getInterviews() { return interviews; }
    public void setInterviews(List<Interview> value) { this.interviews = value == null ? new ArrayList<>() : value; }
    public List<InternalNote> getInternalNotes() { return internalNotes; }
    public void setInternalNotes(List<InternalNote> value) { this.internalNotes = value == null ? new ArrayList<>() : value; }

    public void addInterview(Interview interview) {
        interviews.add(interview);
        interview.setApplication(this);
    }

    public void addInternalNote(InternalNote note) {
        internalNotes.add(note);
        note.setApplication(this);
    }

    @Override
    public String toString() { return "Application{id=" + id + ", status=" + status + ", appliedAt=" + appliedAt + "}"; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Application application)) return false;
        return id != null && id.equals(application.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }

    public enum ApplicationStatus {
        APPLIED, SCREENING, INTERVIEW, OFFER, HIRED, REJECTED, WITHDRAWN;

        public boolean isTerminal() {
            return this == HIRED || this == REJECTED || this == WITHDRAWN;
        }
    }
}
