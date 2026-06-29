package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.ChangePasswordForm;
import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.service.UserService;
import com.recruit.recruitmentapplication.util.SessionConstants;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AccountController {
    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        model.addAttribute("user", userService.findByIdWithRole(current(session).getId()));
        return "auth/profile";
    }

    @GetMapping("/profile/password")
    public String changePasswordForm(Model model) {
        model.addAttribute("changePasswordForm", new ChangePasswordForm());
        return "auth/change-password";
    }

    @PostMapping("/profile/password")
    public String changePassword(@Valid @ModelAttribute("changePasswordForm") ChangePasswordForm form,
                                 BindingResult result, HttpSession session, Model model) {
        if (result.hasErrors()) {
            return "auth/change-password";
        }
        try {
            userService.changePassword(current(session).getId(), form);
            return "redirect:/profile?passwordChanged=true";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("passwordError", ex.getMessage());
            return "auth/change-password";
        }
    }

    private SessionUser current(HttpSession session) {
        return (SessionUser) session.getAttribute(SessionConstants.LOGGED_IN_USER);
    }
}
