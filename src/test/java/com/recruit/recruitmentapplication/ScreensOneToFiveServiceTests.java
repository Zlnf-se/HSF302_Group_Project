package com.recruit.recruitmentapplication;

import com.recruit.recruitmentapplication.dto.ChangePasswordForm;
import com.recruit.recruitmentapplication.dto.RegisterForm;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.repository.UserRepository;
import com.recruit.recruitmentapplication.security.PasswordUtil;
import com.recruit.recruitmentapplication.service.UserService;
import jakarta.persistence.Column;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ScreensOneToFiveServiceTests {
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordUtil passwordUtil;

    @Test
    void failedLoginAttemptsColumnCanBeAddedToExistingSqlServerUsers() throws Exception {
        Column column = User.class.getDeclaredField("failedLoginAttempts").getAnnotation(Column.class);

        assertFalse(column.nullable());
        assertTrue(column.columnDefinition().toLowerCase().contains("default 0"));
    }

    @Test
    void registerAppliesPdfPasswordAndConfirmationRules() {
        RegisterForm weak = new RegisterForm();
        weak.setFullName("Weak Candidate");
        weak.setUsername("weak_candidate");
        weak.setEmail("weak@example.com");
        weak.setPassword("weakpass");
        weak.setConfirmPassword("weakpass");
        assertThrows(IllegalArgumentException.class, () -> userService.register(weak));

        RegisterForm valid = new RegisterForm();
        valid.setFullName("Valid Candidate");
        valid.setUsername("valid_candidate");
        valid.setEmail("valid@example.com");
        valid.setPassword("ValidA123");
        valid.setConfirmPassword("ValidA123");

        User user = userService.register(valid);

        assertEquals(Role.CANDIDATE, user.getRole().getName());
        assertTrue(passwordUtil.matches("ValidA123", user.getPassword()));
    }

    @Test
    void changePasswordRequiresCurrentPasswordAndDifferentStrongReplacement() {
        User alice = userRepository.findByUsernameWithRole("alice").orElseThrow();

        ChangePasswordForm wrongCurrent = new ChangePasswordForm();
        wrongCurrent.setCurrentPassword("bad");
        wrongCurrent.setNewPassword("AliceNew123");
        wrongCurrent.setConfirmNewPassword("AliceNew123");
        assertThrows(IllegalArgumentException.class, () -> userService.changePassword(alice.getId(), wrongCurrent));

        ChangePasswordForm valid = new ChangePasswordForm();
        valid.setCurrentPassword("Alice@123");
        valid.setNewPassword("AliceNew123");
        valid.setConfirmNewPassword("AliceNew123");

        userService.changePassword(alice.getId(), valid);

        User refreshed = userRepository.findByUsernameWithRole("alice").orElseThrow();
        assertFalse(passwordUtil.matches("Alice@123", refreshed.getPassword()));
        assertTrue(passwordUtil.matches("AliceNew123", refreshed.getPassword()));
    }

    @Test
    void passwordResetTokenIsSingleUseAndChangesPassword() {
        String token = userService.requestPasswordReset("alice@example.com").orElseThrow();

        userService.resetPassword(token, "ResetA123", "ResetA123");

        User alice = userRepository.findByUsernameWithRole("alice").orElseThrow();
        assertTrue(passwordUtil.matches("ResetA123", alice.getPassword()));
        assertThrows(IllegalArgumentException.class,
                () -> userService.resetPassword(token, "ResetB123", "ResetB123"));
    }
}
