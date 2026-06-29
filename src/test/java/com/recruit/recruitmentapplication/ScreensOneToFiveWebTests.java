package com.recruit.recruitmentapplication;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.repository.UserRepository;
import com.recruit.recruitmentapplication.service.UserService;
import com.recruit.recruitmentapplication.util.SessionConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ScreensOneToFiveWebTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private UserService userService;

    @Test
    void screenOneLoginUsesPdfRouteAndAcceptsEmailIdentity() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Đăng nhập")))
                .andExpect(content().string(containsString("Tên đăng nhập")))
                .andExpect(content().string(containsString("Tạo tài khoản")))
                .andExpect(content().string(containsString("Quên mật khẩu?")))
                .andExpect(content().string(containsString("action=\"/auth/login\"")))
                .andExpect(content().string(containsString("href=\"/auth/register\"")))
                .andExpect(content().string(containsString("href=\"/password-reset\"")))
                .andExpect(content().string(not(containsString("auth-logo"))))
                .andExpect(content().string(not(containsString("data-password-toggle"))))
                .andExpect(content().string(not(containsString("User Login to TalentHub"))));

        MvcResult login = mockMvc.perform(post("/login")
                        .param("username", "admin@recruit.com")
                        .param("password", "Admin@123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(request().sessionAttribute(SessionConstants.LOGGED_IN_USER, instanceOf(SessionUser.class)))
                .andReturn();

        mockMvc.perform(get("/login").session((MockHttpSession) login.getRequest().getSession(false)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }

    @Test
    void protectedRoutesRedirectToScreenOneLogin() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void screenTwoPasswordResetRequestAndConfirmArePublic() throws Exception {
        mockMvc.perform(get("/password-reset"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Reset your password")))
                .andExpect(content().string(containsString("Back to Sign In")));

        mockMvc.perform(post("/password-reset").param("email", "alice@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("If an account with this email exists")));

        String token = userService.requestPasswordReset("alice@example.com").orElseThrow();

        mockMvc.perform(get("/password-reset/confirm").param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Set your new password")));

        mockMvc.perform(post("/password-reset/confirm")
                        .param("token", token)
                        .param("newPassword", "ResetA123")
                        .param("confirmNewPassword", "ResetA123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?passwordUpdated=true"));
    }

    @Test
    void screenThreeRegisterRequiresConfirmationAndRedirectsAuthenticatedUsers() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create your account")))
                .andExpect(content().string(containsString("Confirm password")))
                .andExpect(content().string(containsString("At least 8 characters")));

        mockMvc.perform(post("/register")
                        .param("fullName", "Screen Three Candidate")
                        .param("username", "screen_three")
                        .param("email", "screen.three@example.com")
                        .param("password", "StrongA123")
                        .param("confirmPassword", "DifferentA123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Passwords do not match.")));

        MockHttpSession session = session("alice");
        mockMvc.perform(get("/register").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/candidates/me"));
    }

    @Test
    void screensFourAndFiveExposeProfileAndChangePassword() throws Exception {
        MockHttpSession session = session("alice");

        mockMvc.perform(get("/profile").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User Profile")))
                .andExpect(content().string(containsString("Alice Nguyen")))
                .andExpect(content().string(containsString("alice@example.com")))
                .andExpect(content().string(containsString("Change Password")));

        mockMvc.perform(get("/profile/password").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Current password")))
                .andExpect(content().string(containsString("Confirm new password")));

        mockMvc.perform(post("/profile/password").session(session)
                        .param("currentPassword", "wrong")
                        .param("newPassword", "AliceNew123")
                        .param("confirmNewPassword", "AliceNew123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Incorrect current password.")));

        mockMvc.perform(post("/profile/password").session(session)
                        .param("currentPassword", "Alice@123")
                        .param("newPassword", "AliceNew123")
                        .param("confirmNewPassword", "AliceNew123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?passwordChanged=true"));
    }

    private MockHttpSession session(String username) {
        User user = userRepository.findByUsernameWithRole(username).orElseThrow();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConstants.LOGGED_IN_USER, SessionUser.from(user));
        return session;
    }
}
