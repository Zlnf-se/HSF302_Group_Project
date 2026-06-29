package com.recruit.recruitmentapplication.dto;

import jakarta.validation.constraints.NotBlank;

public class UserRoleForm {
    @NotBlank(message = "Vai trò không được để trống")
    private String roleName;

    public UserRoleForm() {
    }

    public UserRoleForm(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
}
