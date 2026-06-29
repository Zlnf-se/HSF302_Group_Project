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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interviewer_id", nullable = false)
    private User interviewer;

    @Column(name = "interviewer_name", length = 150)
    private String interviewerName;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(length = 500)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private InterviewStatus status = InterviewStatus.SCHEDULED;

    @Column
    private Integer rating;

    @Column(length = 4000)
    private String feedback;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;

    public Interview() {
    }

    public Interview(User interviewer, LocalDateTime scheduledAt, String location) {
        this.interviewer = interviewer;
        this.interviewerName = interviewer == null ? null : interviewer.getFullName();
        this.scheduledAt = scheduledAt;
        this.location = location;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Application getApplication() { return application; }
    public void setApplication(Application value) { this.application = value; }
    public User getInterviewer() { return interviewer; }
    public void setInterviewer(User interviewer) {
        this.interviewer = interviewer;
        if (interviewer != null) {
            this.interviewerName = interviewer.getFullName();
        }
    }
    public String getInterviewerName() { return interviewerName; }
    public void setInterviewerName(String value) { this.interviewerName = value; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime value) { this.scheduledAt = value; }
    public String getLocation() { return location; }
    public void setLocation(String value) { this.location = value; }
    public InterviewStatus getStatus() { return status; }
    public void setStatus(InterviewStatus value) { this.status = value; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime value) { this.evaluatedAt = value; }

    public boolean isEvaluated() { return status == InterviewStatus.EVALUATED; }

    @Override
    public String toString() {
        return "Interview{id=" + id + ", scheduledAt=" + scheduledAt + ", status=" + status + "}";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Interview interview)) return false;
        return id != null && id.equals(interview.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }

    public enum InterviewStatus { SCHEDULED, EVALUATED }
}
