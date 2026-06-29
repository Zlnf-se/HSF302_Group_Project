package com.recruit.recruitmentapplication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterForm {
    @NotBlank(message = "Username is required.")
    @Size(min = 4, max = 50, message = "Username must be 4-50 characters.")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Username can contain only letters, digits, and underscores.")
    private String username;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters.")
    private String password;

    @NotBlank(message = "Please confirm your password.")
    private String confirmPassword;

    @NotBlank(message = "Email is required.")
    @Email(message = "Email is invalid.")
    private String email;

    @NotBlank(message = "Full name is required.")
    private String fullName;

    public RegisterForm() {
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
