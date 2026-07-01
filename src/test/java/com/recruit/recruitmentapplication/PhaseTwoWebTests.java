package com.recruit.recruitmentapplication;

import com.recruit.recruitmentapplication.dto.RegisterForm;
import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.repository.RoleRepository;
import com.recruit.recruitmentapplication.repository.UserRepository;
import com.recruit.recruitmentapplication.security.PasswordUtil;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PhaseTwoWebTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserService userService;
    @Autowired private PasswordUtil passwordUtil;

    @Test
    void rolesAndHashedAdminAreSeeded() {
        assertTrue(roleRepository.findByName(Role.ADMIN).isPresent());
        assertTrue(roleRepository.findByName(Role.RECRUITER).isPresent());
        assertTrue(roleRepository.findByName(Role.CANDIDATE).isPresent());

        User admin = userRepository.findByUsernameWithRole("admin").orElseThrow();
        assertEquals(Role.ADMIN, admin.getRole().getName());
        assertFalse("Admin@123".equals(admin.getPassword()));
        assertTrue(passwordUtil.matches("Admin@123", admin.getPassword()));
    }

    @Test
    @Transactional
    void registrationHashesPasswordAndAssignsCandidateRole() {
        RegisterForm form = new RegisterForm();
        form.setUsername("phase2candidate");
        form.setPassword("secret123");
        form.setEmail("phase2candidate@example.com");
        form.setFullName("Phase Two Candidate");

        User user = userService.register(form);

        assertEquals(Role.CANDIDATE, user.getRole().getName());
        assertTrue(user.isEnabled());
        assertFalse("secret123".equals(user.getPassword()));
        assertTrue(passwordUtil.matches("secret123", user.getPassword()));
    }

    @Test
    @Transactional
    void administratorCanChangeRoleAndToggleAccountStatus() {
        RegisterForm form = new RegisterForm();
        form.setUsername("managedcandidate");
        form.setPassword("secret123");
        form.setEmail("managedcandidate@example.com");
        form.setFullName("Managed Candidate");
        User user = userService.register(form);

        userService.updateRole(user.getId(), Role.RECRUITER);
        assertEquals(Role.RECRUITER, userService.findById(user.getId()).getRole().getName());

        userService.toggleEnabled(user.getId());
        assertFalse(userService.findById(user.getId()).isEnabled());

        userService.toggleEnabled(user.getId());
        assertTrue(userService.findById(user.getId()).isEnabled());
    }

    @Test
    void authPagesArePublicAndAdminPageRequiresLogin() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("User Login to TalentHub")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Username or email")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Show")));
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"fullName\"")));
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    void adminCanLoginAndOpenUserManagement() throws Exception {
        MvcResult login = mockMvc.perform(post("/auth/login")
                        .param("username", "admin")
                        .param("password", "Admin@123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(request().sessionAttribute(SessionConstants.LOGGED_IN_USER,
                        org.hamcrest.Matchers.instanceOf(SessionUser.class)))
                .andReturn();

        mockMvc.perform(get("/admin/users").session((MockHttpSession) login.getRequest().getSession(false)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("admin@recruit.com")));
    }

    @Test
    void userCanLoginWithEmailAddress() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .param("username", "admin@recruit.com")
                        .param("password", "Admin@123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(request().sessionAttribute(SessionConstants.LOGGED_IN_USER,
                        org.hamcrest.Matchers.instanceOf(SessionUser.class)));
    }

    @Test
    void authenticatedUsersOpeningLoginAreRedirectedByRole() throws Exception {
        MockHttpSession candidate = new MockHttpSession();
        candidate.setAttribute(SessionConstants.LOGGED_IN_USER,
                new SessionUser(10L, "candidate", "Candidate User", Role.CANDIDATE));
        mockMvc.perform(get("/auth/login").session(candidate))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/candidates/me"));

        MockHttpSession recruiter = new MockHttpSession();
        recruiter.setAttribute(SessionConstants.LOGGED_IN_USER,
                new SessionUser(11L, "recruiter", "Recruiter User", Role.RECRUITER));
        mockMvc.perform(get("/auth/login").session(recruiter))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/jobs"));
    }

    @Test
    void loginFailureUsesGenericErrorMessage() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .param("username", "missing@example.com")
                        .param("password", "wrong-password"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Incorrect username or password.")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("locked"))));
    }

    @Test
    void candidateLoginRedirectsToOwnProfileArea() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .param("username", "alice")
                        .param("password", "Alice@123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/candidates/me"));
    }

    @Test
    void nonAdminSessionIsBlockedFromAdminRoutes() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConstants.LOGGED_IN_USER,
                new SessionUser(99L, "candidate", "Candidate User", Role.CANDIDATE));

        mockMvc.perform(get("/admin/users").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error/403"));
    }
}
