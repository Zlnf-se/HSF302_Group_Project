package com.recruit.recruitmentapplication;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RecruitmentApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
    }

    @Test
    void homePageRendersPhaseOneLayout() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Recruitment Web System")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Phase 1")));
    }

    @Test
    void passwordEncoderUsesOneWayHashing() {
        String encoded = passwordEncoder.encode("phase-one-password");

        org.junit.jupiter.api.Assertions.assertNotEquals("phase-one-password", encoded);
        org.junit.jupiter.api.Assertions.assertTrue(passwordEncoder.matches("phase-one-password", encoded));
    }

}
