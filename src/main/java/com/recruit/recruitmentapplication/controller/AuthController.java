package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.LoginForm;
import com.recruit.recruitmentapplication.dto.PasswordResetConfirmForm;
import com.recruit.recruitmentapplication.dto.PasswordResetRequestForm;
import com.recruit.recruitmentapplication.dto.RegisterForm;
import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Role;
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
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    private static final String GENERIC_LOGIN_ERROR = "Incorrect username or password.";
    private static final String LOCKOUT_ERROR = "Your account has been temporarily locked after too many failed attempts. Try again in 10 minutes or contact your administrator.";
    private static final String RESET_LINK_ERROR = "This link has expired or has already been used. Request a new reset link.";

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping({"/register", "/auth/register"})
    public String showRegisterForm(Model model, HttpSession session) {
        SessionUser current = currentUser(session);
        if (current != null) {
            return redirectToLanding(current);
        }
        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }

    @PostMapping({"/register", "/auth/register"})
    public String processRegister(@Valid @ModelAttribute("registerForm") RegisterForm form,
                                  BindingResult result, Model model, HttpSession session) {
        SessionUser current = currentUser(session);
        if (current != null) {
            return redirectToLanding(current);
        }
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.register(form);
            return "redirect:/login?registered=true";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("registrationError", ex.getMessage());
            return "auth/register";
        }
    }

    @GetMapping({"/login", "/auth/login"})
    public String showLoginForm(Model model, HttpSession session) {
        SessionUser current = currentUser(session);
        if (current != null) {
            return redirectToLanding(current);
        }
        model.addAttribute("loginForm", new LoginForm());
        return "auth/login";
    }

    @PostMapping({"/login", "/auth/login"})
    public String processLogin(@Valid @ModelAttribute("loginForm") LoginForm form,
                               BindingResult result, HttpSession session, Model model) {
        if (result.hasErrors()) {
            return "auth/login";
        }

        UserService.AuthenticationResult auth = userService.authenticateForLogin(form.getUsername(), form.getPassword());
        if (auth.isLocked()) {
            model.addAttribute("lockoutError", LOCKOUT_ERROR);
            return "auth/login";
        }
        if (!auth.isSuccess()) {
            model.addAttribute("loginError", GENERIC_LOGIN_ERROR);
            return "auth/login";
        }

        User user = auth.getUser();
        session.setAttribute(SessionConstants.LOGGED_IN_USER, SessionUser.from(user));
        return "redirect:" + landingPath(user.getRole().getName());
    }

    @GetMapping({"/logout", "/auth/logout"})
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }

    @GetMapping({"/password-reset", "/auth/password-reset"})
    public String showPasswordResetRequest(Model model) {
        model.addAttribute("passwordResetRequestForm", new PasswordResetRequestForm());
        return "auth/password-reset-request";
    }

    @PostMapping({"/password-reset", "/auth/password-reset"})
    public String processPasswordResetRequest(
            @Valid @ModelAttribute("passwordResetRequestForm") PasswordResetRequestForm form,
            BindingResult result, Model model) {
        if (!result.hasErrors()) {
            userService.requestPasswordReset(form.getEmail());
            model.addAttribute("resetRequested", true);
        }
        return "auth/password-reset-request";
    }

    @GetMapping({"/password-reset/confirm", "/auth/password-reset/confirm"})
    public String showPasswordResetConfirm(@RequestParam(name = "token", required = false) String token, Model model) {
        PasswordResetConfirmForm form = new PasswordResetConfirmForm();
        form.setToken(token);
        model.addAttribute("passwordResetConfirmForm", form);
        if (!userService.isPasswordResetTokenValid(token)) {
            model.addAttribute("resetLinkError", RESET_LINK_ERROR);
        }
        return "auth/password-reset-confirm";
    }

    @PostMapping({"/password-reset/confirm", "/auth/password-reset/confirm"})
    public String processPasswordResetConfirm(
            @Valid @ModelAttribute("passwordResetConfirmForm") PasswordResetConfirmForm form,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "auth/password-reset-confirm";
        }
        try {
            userService.resetPassword(form.getToken(), form.getNewPassword(), form.getConfirmNewPassword());
            return "redirect:/login?passwordUpdated=true";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("resetError", ex.getMessage());
            if (RESET_LINK_ERROR.equals(ex.getMessage())) {
                model.addAttribute("resetLinkError", ex.getMessage());
            }
            return "auth/password-reset-confirm";
        }
    }

    private SessionUser currentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(SessionConstants.LOGGED_IN_USER);
        return value instanceof SessionUser user ? user : null;
    }

    private String redirectToLanding(SessionUser user) {
        return "redirect:" + landingPath(user.getRoleName());
    }

    private String landingPath(String roleName) {
        if (Role.ADMIN.equals(roleName)) {
            return "/admin/users";
        }
        if (Role.RECRUITER.equals(roleName)) {
            return "/jobs";
        }
        if (Role.INTERVIEWER.equals(roleName)) {
            return "/interviews/assigned";
        }
        if (Role.CANDIDATE.equals(roleName)) {
            return "/candidates/me";
        }
        return "/";
    }
}
