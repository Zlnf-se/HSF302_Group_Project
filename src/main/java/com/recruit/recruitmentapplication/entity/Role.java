package com.recruit.recruitmentapplication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public class Role {
    public static final String ADMIN = "ADMIN";
    public static final String HR_MANAGER = "HR_MANAGER";
    public static final String RECRUITER = "RECRUITER";
    public static final String INTERVIEWER = "INTERVIEWER";
    public static final String CANDIDATE = "CANDIDATE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String name;

    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() { return "Role{id=" + id + ", name='" + name + "'}"; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Role role)) return false;
        return id != null && id.equals(role.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
