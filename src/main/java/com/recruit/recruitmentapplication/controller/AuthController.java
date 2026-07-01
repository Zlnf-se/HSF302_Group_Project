package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.LoginForm;
import com.recruit.recruitmentapplication.dto.RegisterForm;
import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.service.UserService;
import com.recruit.recruitmentapplication.util.SessionConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("registerForm") RegisterForm form,
                                  BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.register(form);
            return "redirect:/auth/login?registered=true";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("registrationError", ex.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        SessionUser loggedInUser = session == null
                ? null
                : (SessionUser) session.getAttribute(SessionConstants.LOGGED_IN_USER);
        if (loggedInUser != null) {
            return redirectForRole(loggedInUser.getRoleName());
        }

        model.addAttribute("loginForm", new LoginForm());
        return "auth/login";
    }

    @PostMapping("/login")
    public String processLogin(@Valid @ModelAttribute("loginForm") LoginForm form,
                               BindingResult result, HttpSession session, Model model) {
        if (result.hasErrors()) {
            return "auth/login";
        }

        User user = userService.authenticate(form.getUsername(), form.getPassword()).orElse(null);
        if (user == null) {
            model.addAttribute("loginError", "Incorrect username or password.");
            return "auth/login";
        }

        SessionUser sessionUser = SessionUser.from(user);
        session.setAttribute(SessionConstants.LOGGED_IN_USER, sessionUser);
        return redirectForRole(sessionUser.getRoleName());
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login?logout=true";
    }

    private String redirectForRole(String roleName) {
        if (Role.ADMIN.equals(roleName)) {
            return "redirect:/admin/users";
        }
        if (Role.HR_MANAGER.equals(roleName) || Role.RECRUITER.equals(roleName)) {
            return "redirect:/jobs";
        }
        if (Role.CANDIDATE.equals(roleName)) {
            return "redirect:/candidates/me";
        }
        return "redirect:/";
    }
}
