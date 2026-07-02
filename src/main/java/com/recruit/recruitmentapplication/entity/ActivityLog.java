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
@Table(name = "activity_logs")
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private ActivityEventType eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(name = "actor_username", length = 100)
    private String actorUsername;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ActivityLog() {
    }

    public ActivityLog(ActivityEventType eventType, User actor, String description, String ipAddress) {
        this.eventType = eventType;
        this.actor = actor;
        this.actorUsername = actor == null ? null : actor.getUsername();
        this.description = description;
        this.ipAddress = ipAddress;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ActivityEventType getEventType() { return eventType; }
    public void setEventType(ActivityEventType eventType) { this.eventType = eventType; }
    public User getActor() { return actor; }
    public void setActor(User actor) { this.actor = actor; }
    public String getActorUsername() { return actorUsername; }
    public void setActorUsername(String actorUsername) { this.actorUsername = actorUsername; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "ActivityLog{id=" + id + ", eventType=" + eventType + ", actorUsername='" + actorUsername + "'}";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ActivityLog activityLog)) return false;
        return id != null && id.equals(activityLog.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }

    public enum ActivityEventType {
        SIGN_IN_SUCCESS,
        SIGN_IN_FAILURE,
        ACCOUNT_CREATED,
        ACCOUNT_DEACTIVATED,
        ACCOUNT_UNLOCKED,
        ACCOUNT_LOCKED,
        APPLICATION_STATUS_CHANGED,
        CV_DOWNLOADED,
        EVALUATION_SUBMITTED
    }
}
