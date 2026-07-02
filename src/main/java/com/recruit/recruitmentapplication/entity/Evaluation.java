package com.recruit.recruitmentapplication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluations", uniqueConstraints = @UniqueConstraint(columnNames = "interview_id"))
public class Evaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interviewer_id", nullable = false)
    private User interviewer;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false, length = 4000)
    private String feedback;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    public Evaluation() {
    }

    public Evaluation(Interview interview, User interviewer, Integer rating, String feedback) {
        this.interview = interview;
        this.interviewer = interviewer;
        this.rating = rating;
        this.feedback = feedback;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Interview getInterview() { return interview; }
    public void setInterview(Interview interview) { this.interview = interview; }
    public User getInterviewer() { return interviewer; }
    public void setInterviewer(User interviewer) { this.interviewer = interviewer; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    @Override
    public String toString() {
        return "Evaluation{id=" + id + ", rating=" + rating + ", submittedAt=" + submittedAt + "}";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Evaluation evaluation)) return false;
        return id != null && id.equals(evaluation.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
