package com.recruit.recruitmentapplication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AdminUserCreateForm {
    @NotBlank(message = "Full name is required.")
    private String fullName;

    @NotBlank(message = "Username is required.")
    @Size(min = 4, max = 50, message = "Username must be 4-50 characters.")
    private String username;

    @NotBlank(message = "Email is required.")
    @Email(message = "Email is invalid.")
    private String email;

    @NotBlank(message = "Role is required.")
    private String roleName;

    @NotBlank(message = "Initial password is required.")
    @Size(min = 8, max = 100, message = "Initial password must be at least 8 characters.")
    @Pattern(regexp = ".*[A-Z].*", message = "Initial password must include an uppercase letter.")
    @Pattern(regexp = ".*\\d.*", message = "Initial password must include a number.")
    private String initialPassword;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getInitialPassword() { return initialPassword; }
    public void setInitialPassword(String initialPassword) { this.initialPassword = initialPassword; }
}
