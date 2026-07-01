package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.AdminUserCreateForm;
import com.recruit.recruitmentapplication.dto.UserRoleForm;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.entity.User.AccountStatus;
import com.recruit.recruitmentapplication.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String listUsers(@RequestParam(required = false) String q,
                            @RequestParam(required = false) String role,
                            @RequestParam(required = false) AccountStatus status,
                            Model model) {
        List<User> users = userService.searchUsers(q, role, status);
        model.addAttribute("users", users);
        model.addAttribute("q", q);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", AccountStatus.values());
        model.addAttribute("roles", userService.findAllRoles());
        model.addAttribute("lastActiveAdminId", resolveLastActiveAdminId());
        return "user/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        prepareCreateForm(model, new AdminUserCreateForm());
        return "user/create-form";
    }

    @PostMapping
    public String createAccount(@Valid @ModelAttribute("adminUserCreateForm") AdminUserCreateForm form,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            prepareCreateForm(model, form);
            return "user/create-form";
        }
        try {
            userService.createStaffAccount(form);
            return "redirect:/admin/users?created=true";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("accountError", ex.getMessage());
            prepareCreateForm(model, form);
            return "user/create-form";
        }
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id) {
        userService.deactivateUser(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/unlock")
    public String unlock(@PathVariable Long id) {
        userService.unlockUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/toggle-enabled")
    public String toggleEnabled(@PathVariable Long id) {
        userService.toggleEnabled(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/edit-role")
    public String showEditRole(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        model.addAttribute("userRoleForm", new UserRoleForm(user.getRole().getName()));
        model.addAttribute("roles", userService.findAllRoles());
        return "user/form";
    }

    @PostMapping("/{id}/edit-role")
    public String updateRole(@PathVariable Long id,
                             @Valid @ModelAttribute("userRoleForm") UserRoleForm form,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("user", userService.findById(id));
            model.addAttribute("roles", userService.findAllRoles());
            return "user/form";
        }
        userService.updateRole(id, form.getRoleName());
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        userService.delete(id);
        return "redirect:/admin/users";
    }

    private void prepareCreateForm(Model model, AdminUserCreateForm form) {
        model.addAttribute("adminUserCreateForm", form);
        model.addAttribute("creationRoles", userService.findStaffCreationRoles());
    }

    private Long resolveLastActiveAdminId() {
        List<User> activeAdmins = userService.searchUsers(null, Role.ADMIN, AccountStatus.ACTIVE);
        return activeAdmins.size() == 1 ? activeAdmins.get(0).getId() : null;
    }
}
