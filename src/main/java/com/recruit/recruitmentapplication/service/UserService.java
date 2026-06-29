package com.recruit.recruitmentapplication.service;

import com.recruit.recruitmentapplication.dto.ChangePasswordForm;
import com.recruit.recruitmentapplication.dto.RegisterForm;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.repository.RoleRepository;
import com.recruit.recruitmentapplication.repository.UserRepository;
import com.recruit.recruitmentapplication.security.PasswordUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 10;
    private static final int RESET_TOKEN_MINUTES = 30;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordUtil passwordUtil;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordUtil passwordUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordUtil = passwordUtil;
    }

    @Transactional
    public User register(RegisterForm form) {
        validateRegistration(form);
        String username = form.getUsername().trim();
        String email = form.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("This username is already taken. Please choose another.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("This email address is already registered.");
        }

        Role candidateRole = roleRepository.findByName(Role.CANDIDATE)
                .orElseThrow(() -> new IllegalStateException("Role CANDIDATE has not been initialized"));
        User user = new User(
                username,
                passwordUtil.hash(form.getPassword()),
                email,
                form.getFullName().trim(),
                candidateRole
        );
        return userRepository.save(user);
    }

    @Transactional
    public Optional<User> authenticate(String usernameOrEmail, String rawPassword) {
        AuthenticationResult result = authenticateForLogin(usernameOrEmail, rawPassword);
        return result.isSuccess() ? Optional.of(result.getUser()) : Optional.empty();
    }

    @Transactional
    public AuthenticationResult authenticateForLogin(String usernameOrEmail, String rawPassword) {
        if (usernameOrEmail == null || rawPassword == null) {
            return AuthenticationResult.failed();
        }

        Optional<User> found = findByUsernameOrEmail(usernameOrEmail);
        if (found.isEmpty()) {
            return AuthenticationResult.failed();
        }

        User user = found.get();
        if (!user.isEnabled()) {
            return AuthenticationResult.failed();
        }

        if (isLocked(user)) {
            return AuthenticationResult.locked(user);
        }

        if (passwordUtil.matches(rawPassword, user.getPassword())) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            return AuthenticationResult.success(userRepository.save(user));
        }

        int failedAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(failedAttempts);
        if (failedAttempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES));
            userRepository.save(user);
            return AuthenticationResult.locked(user);
        }
        userRepository.save(user);
        return AuthenticationResult.failed();
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAllWithRole();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found id=" + id));
    }

    @Transactional(readOnly = true)
    public User findByIdWithRole(Long id) {
        User user = findById(id);
        user.getRole().getName();
        return user;
    }

    @Transactional(readOnly = true)
    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional
    public User updateRole(Long userId, String roleName) {
        User user = findById(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid role"));
        user.setRole(role);
        return userRepository.save(user);
    }

    @Transactional
    public User toggleEnabled(Long userId) {
        User user = findById(userId);
        user.setEnabled(!user.isEnabled());
        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long userId) {
        User user = findById(userId);
        userRepository.delete(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordForm form) {
        User user = findById(userId);
        if (!passwordUtil.matches(form.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Incorrect current password.");
        }
        if (!Objects.equals(form.getNewPassword(), form.getConfirmNewPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        if (passwordUtil.matches(form.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from your current password.");
        }
        validatePasswordStrength(form.getNewPassword());
        user.setPassword(passwordUtil.hash(form.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public Optional<String> requestPasswordReset(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        Optional<User> found = userRepository.findByEmail(email.trim().toLowerCase());
        if (found.isEmpty()) {
            return Optional.empty();
        }
        User user = found.get();
        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_MINUTES));
        userRepository.save(user);
        return Optional.of(token);
    }

    @Transactional(readOnly = true)
    public boolean isPasswordResetTokenValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return userRepository.findByPasswordResetToken(token)
                .filter(user -> user.getPasswordResetExpiresAt() != null)
                .filter(user -> user.getPasswordResetExpiresAt().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    @Transactional
    public void resetPassword(String token, String newPassword, String confirmNewPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("This link has expired or has already been used. Request a new reset link."));
        if (user.getPasswordResetExpiresAt() == null || user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
            clearPasswordReset(user);
            userRepository.save(user);
            throw new IllegalArgumentException("This link has expired or has already been used. Request a new reset link.");
        }
        if (!Objects.equals(newPassword, confirmNewPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        validatePasswordStrength(newPassword);
        user.setPassword(passwordUtil.hash(newPassword));
        clearPasswordReset(user);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }

    private Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        String identity = usernameOrEmail.trim();
        if (identity.isEmpty()) {
            return Optional.empty();
        }
        Optional<User> byUsername = userRepository.findByUsernameWithRole(identity);
        if (byUsername.isPresent()) {
            return byUsername;
        }
        return userRepository.findByEmailWithRole(identity.toLowerCase());
    }

    private boolean isLocked(User user) {
        LocalDateTime lockedUntil = user.getLockedUntil();
        if (lockedUntil == null) {
            return false;
        }
        if (lockedUntil.isAfter(LocalDateTime.now())) {
            return true;
        }
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
        return false;
    }

    private void validateRegistration(RegisterForm form) {
        if (form.getUsername() == null || !form.getUsername().trim().matches("^[A-Za-z0-9_]{4,50}$")) {
            throw new IllegalArgumentException("Username must be 4-50 characters. Letters, digits, and underscores only.");
        }
        if (form.getFullName() == null || form.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (form.getEmail() == null || form.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (!Objects.equals(form.getPassword(), form.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        validatePasswordStrength(form.getPassword());
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8
                || password.chars().noneMatch(Character::isUpperCase)
                || password.chars().noneMatch(Character::isDigit)) {
            throw new IllegalArgumentException("Password must be at least 8 characters, including 1 uppercase letter and 1 number.");
        }
    }

    private void clearPasswordReset(User user) {
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);
    }

    public static class AuthenticationResult {
        private final User user;
        private final boolean locked;

        private AuthenticationResult(User user, boolean locked) {
            this.user = user;
            this.locked = locked;
        }

        public static AuthenticationResult success(User user) {
            return new AuthenticationResult(user, false);
        }

        public static AuthenticationResult failed() {
            return new AuthenticationResult(null, false);
        }

        public static AuthenticationResult locked(User user) {
            return new AuthenticationResult(user, true);
        }

        public boolean isSuccess() { return user != null && !locked; }
        public boolean isLocked() { return locked; }
        public User getUser() { return user; }
    }
}
