package com.recruit.recruitmentapplication;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.entity.User.AccountStatus;
import com.recruit.recruitmentapplication.repository.RoleRepository;
import com.recruit.recruitmentapplication.repository.UserRepository;
import com.recruit.recruitmentapplication.security.PasswordUtil;
import com.recruit.recruitmentapplication.util.SessionConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Scr08UserManagementWebTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private RoleRepository roles;
    @Autowired private UserRepository users;
    @Autowired private PasswordUtil passwordUtil;

    @Test
    void userManagementShowsScr08ControlsAndNoDeleteAction() throws Exception {
        mockMvc.perform(get("/admin/users").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("User Management")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Search name or email")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Create Account")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Date created")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("All roles")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("All statuses")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Delete"))));
    }

    @Test
    void userManagementFiltersBySearchRoleAndStatus() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .param("q", "alice@example.com")
                        .param("role", Role.CANDIDATE)
                        .param("status", AccountStatus.ACTIVE.name())
                        .session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Alice Nguyen")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Bob Tran"))));

        mockMvc.perform(get("/admin/users")
                        .param("q", "no-match-for-scr08")
                        .session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No accounts match your filters.")));
    }

    @Test
    @Transactional
    void adminCreatesOnlyStaffAccountsFromUserManagement() throws Exception {
        mockMvc.perform(post("/admin/users")
                        .param("fullName", "SCR08 Interviewer")
                        .param("username", "scr08_interviewer")
                        .param("email", "scr08_interviewer@example.com")
                        .param("roleName", Role.INTERVIEWER)
                        .param("initialPassword", "Initial1")
                        .session(adminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users?created=true"));

        User created = users.findByUsernameWithRole("scr08_interviewer").orElseThrow();
        assertEquals(Role.INTERVIEWER, created.getRole().getName());
        assertEquals(AccountStatus.ACTIVE, created.getAccountStatus());
        assertTrue(passwordUtil.matches("Initial1", created.getPassword()));

        mockMvc.perform(post("/admin/users")
                        .param("fullName", "Illegal Admin")
                        .param("username", "scr08_admin_attempt")
                        .param("email", "scr08_admin_attempt@example.com")
                        .param("roleName", Role.ADMIN)
                        .param("initialPassword", "Initial1")
                        .session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Role must be HR Manager or Interviewer.")));
    }

    @Test
    void adminDeactivatesAccountsButNotTheLastActiveAdmin() throws Exception {
        User recruiter = saveUser("scr08_recruiter", "scr08_recruiter@example.com", Role.RECRUITER, AccountStatus.ACTIVE);

        mockMvc.perform(post("/admin/users/{id}/deactivate", recruiter.getId()).session(adminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        assertEquals(AccountStatus.INACTIVE, users.findById(recruiter.getId()).orElseThrow().getAccountStatus());

        User admin = users.findByUsernameWithRole("admin").orElseThrow();
        mockMvc.perform(get("/admin/users").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("/admin/users/" + admin.getId() + "/deactivate"))));
    }

    @Test
    void adminUnlocksLockedAccountsAndInactiveAccountsShowDisabledReactivate() throws Exception {
        User locked = saveUser("scr08_locked", "scr08_locked@example.com", Role.INTERVIEWER, AccountStatus.LOCKED);
        saveUser("scr08_inactive", "scr08_inactive@example.com", Role.INTERVIEWER, AccountStatus.INACTIVE);

        mockMvc.perform(get("/admin/users").param("status", AccountStatus.LOCKED.name()).session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Locked")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unlock")));

        mockMvc.perform(post("/admin/users/{id}/unlock", locked.getId()).session(adminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        assertEquals(AccountStatus.ACTIVE, users.findById(locked.getId()).orElseThrow().getAccountStatus());

        mockMvc.perform(get("/admin/users").param("status", AccountStatus.INACTIVE.name()).session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Reactivate")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Not available in this version")));
    }

    private MockHttpSession adminSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConstants.LOGGED_IN_USER,
                new SessionUser(1L, "admin", "System Administrator", Role.ADMIN));
        return session;
    }

    private User saveUser(String username, String email, String roleName, AccountStatus status) {
        Role role = roles.findByName(roleName).orElseThrow();
        User user = new User(username, passwordUtil.hash("Initial1"), email, username, role);
        user.setAccountStatus(status);
        return users.save(user);
    }
}
