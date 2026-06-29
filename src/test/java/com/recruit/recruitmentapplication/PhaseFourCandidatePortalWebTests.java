package com.recruit.recruitmentapplication;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.repository.ApplicationRepository;
import com.recruit.recruitmentapplication.repository.CandidateRepository;
import com.recruit.recruitmentapplication.repository.JobPostingRepository;
import com.recruit.recruitmentapplication.repository.UserRepository;
import com.recruit.recruitmentapplication.util.SessionConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PhaseFourCandidatePortalWebTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private JobPostingRepository jobPostings;
    @Autowired private CandidateRepository candidates;
    @Autowired private ApplicationRepository applications;
    @Autowired private UserRepository users;

    @Test
    void publicJobDetailShowsSignInPromptAndCandidateApplyForm() throws Exception {
        JobPosting job = jobPostings.findByTitleAndCompany_Name("DevOps Engineer", "FinanceHub Ltd.").orElseThrow();

        mockMvc.perform(get("/jobs/{id}", job.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Login or create an account to apply.")))
                .andExpect(content().string(not(containsString("name=\"cvFile\""))));

        mockMvc.perform(get("/jobs/{id}", job.getId()).session(session("david")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Apply Now")))
                .andExpect(content().string(containsString("name=\"cvFile\"")));
    }

    @Test
    void candidateAppliesWithPdfCvAndCannotApplyAgain() throws Exception {
        JobPosting job = jobPostings.findByTitleAndCompany_Name("DevOps Engineer", "FinanceHub Ltd.").orElseThrow();
        byte[] cvBytes = "%PDF-1.4 sample cv".getBytes();
        MockMultipartFile cv = new MockMultipartFile("cvFile", "portfolio.pdf", "application/pdf", cvBytes);

        mockMvc.perform(multipart("/jobs/{id}/apply", job.getId()).file(cv).session(session("bob"))
                        .param("coverLetter", "I would like to join this design team."))
                .andExpect(redirectedUrl("/jobs/" + job.getId() + "?applied"));

        Application saved = applications.findByCandidate_IdAndJobPosting_Id(
                candidates.findByEmail("bob@example.com").orElseThrow().getId(), job.getId()).orElseThrow();
        assertEquals(Application.ApplicationStatus.SUBMITTED, saved.getStatus());
        assertEquals("portfolio.pdf", saved.getCvFileName());
        assertEquals("application/pdf", saved.getCvContentType());
        assertArrayEquals(cvBytes, saved.getCvData());

        mockMvc.perform(get("/jobs/{id}", job.getId()).session(session("bob")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("You have already applied for this position.")))
                .andExpect(content().string(not(containsString("name=\"cvFile\""))));
    }

    @Test
    void candidateCanTrackAndWithdrawOwnApplications() throws Exception {
        JobPosting job = jobPostings.findByTitleAndCompany_Name("Data Analyst", "FinanceHub Ltd.").orElseThrow();
        Long candidateId = candidates.findByEmail("carol@example.com").orElseThrow().getId();

        mockMvc.perform(get("/applications/my").session(session("carol")))
                .andExpect(status().isOk())
                .andExpect(view().name("application/my-applications"))
                .andExpect(content().string(containsString("My Applications")))
                .andExpect(content().string(containsString("Data Analyst")))
                .andExpect(content().string(containsString("Withdraw")));

        mockMvc.perform(post("/applications/{id}/withdraw",
                        applications.findByCandidate_IdAndJobPosting_Id(candidateId, job.getId()).orElseThrow().getId())
                        .session(session("carol")))
                .andExpect(redirectedUrl("/applications/my?withdrawn"));

        assertEquals(Application.ApplicationStatus.WITHDRAWN,
                applications.findByCandidate_IdAndJobPosting_Id(candidateId, job.getId()).orElseThrow().getStatus());
        assertTrue(applications.existsByCandidate_IdAndJobPosting_Id(candidateId, job.getId()));
    }

    private MockHttpSession session(String username) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConstants.LOGGED_IN_USER, SessionUser.from(users.findByUsernameWithRole(username).orElseThrow()));
        return session;
    }
}
