package com.recruit.recruitmentapplication.entity;

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
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
public class Interview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_type", length = 30)
    private InterviewType interviewType = InterviewType.TECHNICAL;

    @Column(name = "interviewer_name", length = 100)
    private String interviewerName;

    @Column(length = 1000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private InterviewResult result = InterviewResult.PENDING;

    public Interview() {
    }

    public Interview(LocalDateTime scheduledAt, InterviewType interviewType, String interviewerName) {
        this.scheduledAt = scheduledAt;
        this.interviewType = interviewType;
        this.interviewerName = interviewerName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Application getApplication() { return application; }
    public void setApplication(Application value) { this.application = value; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime value) { this.scheduledAt = value; }
    public InterviewType getInterviewType() { return interviewType; }
    public void setInterviewType(InterviewType value) { this.interviewType = value; }
    public String getInterviewerName() { return interviewerName; }
    public void setInterviewerName(String value) { this.interviewerName = value; }
    public String getNotes() { return notes; }
    public void setNotes(String value) { this.notes = value; }
    public InterviewResult getResult() { return result; }
    public void setResult(InterviewResult value) { this.result = value; }

    @Override
    public String toString() {
        return "Interview{id=" + id + ", scheduledAt=" + scheduledAt + ", result=" + result + "}";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Interview interview)) return false;
        return id != null && id.equals(interview.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }

    public enum InterviewType { PHONE_SCREEN, TECHNICAL, HR, FINAL, CULTURE_FIT }
    public enum InterviewResult { PENDING, PASSED, FAILED, NO_SHOW }
}
