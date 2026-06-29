package com.recruit.recruitmentapplication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class PasswordResetRequestForm {
    @NotBlank(message = "Email is required.")
    @Email(message = "Email is invalid.")
    private String email;

    public PasswordResetRequestForm() {
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
