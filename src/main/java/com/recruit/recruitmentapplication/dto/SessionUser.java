package com.recruit.recruitmentapplication.dto;

import com.recruit.recruitmentapplication.entity.User;
import java.io.Serializable;

public class SessionUser implements Serializable {
    private final Long id;
    private final String username;
    private final String fullName;
    private final String roleName;

    public SessionUser(Long id, String username, String fullName, String roleName) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.roleName = roleName;
    }

    public static SessionUser from(User user) {
        return new SessionUser(user.getId(), user.getUsername(), user.getFullName(), user.getRole().getName());
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getRoleName() { return roleName; }
}
