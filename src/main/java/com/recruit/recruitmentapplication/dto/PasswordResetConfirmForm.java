package com.recruit.recruitmentapplication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordResetConfirmForm {
    @NotBlank(message = "Reset token is required.")
    private String token;

    @NotBlank(message = "New password is required.")
    @Size(min = 8, max = 100, message = "New password must be at least 8 characters.")
    private String newPassword;

    @NotBlank(message = "Please confirm your new password.")
    private String confirmNewPassword;

    public PasswordResetConfirmForm() {
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getConfirmNewPassword() { return confirmNewPassword; }
    public void setConfirmNewPassword(String confirmNewPassword) { this.confirmNewPassword = confirmNewPassword; }
}
