package com.recruit.recruitmentapplication;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.*;
import com.recruit.recruitmentapplication.repository.*;
import com.recruit.recruitmentapplication.util.SessionConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @Transactional
class PhaseFiveCandidateWebTests {
    @Autowired MockMvc mockMvc; @Autowired UserRepository users; @Autowired CandidateRepository candidates; @Autowired SkillRepository skills;

    @Test void anonymousRedirectsAndAdminCanViewDirectory() throws Exception {
        Candidate alice=candidates.findByEmail("alice@example.com").orElseThrow();
        mockMvc.perform(get("/candidates")).andExpect(redirectedUrl("/auth/login"));
        mockMvc.perform(get("/candidates").session(session("admin"))).andExpect(status().isOk()).andExpect(content().string(containsString("Alice Nguyen")));
        mockMvc.perform(get("/candidates/{id}",alice.getId()).session(session("recruiter"))).andExpect(status().isOk()).andExpect(content().string(containsString("Spring Boot")));
    }

    @Test void candidateIsBlockedFromDirectoryAndArbitraryIds() throws Exception {
        Candidate bob=candidates.findByEmail("bob@example.com").orElseThrow(); MockHttpSession alice=session("alice");
        mockMvc.perform(get("/candidates").session(alice)).andExpect(redirectedUrl("/error/403"));
        mockMvc.perform(get("/candidates/{id}",bob.getId()).session(alice)).andExpect(redirectedUrl("/error/403"));
    }

    @Test void candidateCanOpenOwnProfileWithSkillCheckboxes() throws Exception {
        mockMvc.perform(get("/candidates/me").session(session("alice"))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Hồ sơ của tôi")))
                .andExpect(content().string(containsString("name=\"skillIds\"")))
                .andExpect(content().string(containsString("Java")));
    }

    @Test void candidateUpdatesOnlyOwnProfileFromSession() throws Exception {
        User alice=users.findByUsernameWithRole("alice").orElseThrow(); Skill javaSkill=skills.findByName("Java").orElseThrow();
        mockMvc.perform(post("/candidates/me").session(session("alice"))
                .param("name","Alice Updated").param("email","alice@example.com").param("phone","0901111222")
                .param("yearsOfExperience","6").param("educationLevel","Master").param("currentTitle","Lead Java Developer")
                .param("linkedinUrl","https://linkedin.com/in/alice-nguyen").param("resumeSummary","Updated summary")
                .param("skillIds",javaSkill.getId().toString())).andExpect(redirectedUrl("/candidates/me?updated"));
        assertEquals("Alice Updated",candidates.findByUser_Id(alice.getId()).orElseThrow().getName());
    }

    private MockHttpSession session(String username){User u=users.findByUsernameWithRole(username).orElseThrow();MockHttpSession s=new MockHttpSession();s.setAttribute(SessionConstants.LOGGED_IN_USER,SessionUser.from(u));return s;}
}
