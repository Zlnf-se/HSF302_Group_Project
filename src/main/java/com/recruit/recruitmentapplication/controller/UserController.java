package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.UserRoleForm;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "user/list";
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
}
