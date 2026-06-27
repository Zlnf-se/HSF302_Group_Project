package com.recruit.recruitmentapplication;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Company;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.repository.CompanyRepository;
import com.recruit.recruitmentapplication.repository.JobPostingRepository;
import com.recruit.recruitmentapplication.util.SessionConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PhaseFourJobPostingWebTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private JobPostingRepository jobRepository;
    @Autowired private CompanyRepository companyRepository;

    @Test
    void anonymousIsRedirectedAndCandidateCanRead() throws Exception {
        JobPosting job = jobRepository.findByTitleAndCompany_Name("Senior Java Developer", "TechCorp Inc.").orElseThrow();
        mockMvc.perform(get("/jobs")).andExpect(redirectedUrl("/auth/login"));
        mockMvc.perform(get("/jobs").session(session(Role.CANDIDATE)))
                .andExpect(status().isOk()).andExpect(view().name("jobposting/list"))
                .andExpect(content().string(containsString("Senior Java Developer")))
                .andExpect(content().string(containsString("TechCorp Inc.")))
                .andExpect(content().string(not(containsString("Đăng tin mới"))));
        mockMvc.perform(get("/jobs/{id}", job.getId()).session(session(Role.CANDIDATE)))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Develop backend services")));
    }

    @Test
    void searchFiltersOpenJobsByTitle() throws Exception {
        mockMvc.perform(get("/jobs").param("keyword", "frontend").session(session(Role.CANDIDATE)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Frontend Developer")))
                .andExpect(content().string(not(containsString("Senior Java Developer"))));
    }

    @Test
    void candidateCannotUseWriteRoutes() throws Exception {
        JobPosting job = jobRepository.findByTitleAndCompany_Name("Senior Java Developer", "TechCorp Inc.").orElseThrow();
        MockHttpSession candidate = session(Role.CANDIDATE);
        mockMvc.perform(get("/jobs/new").session(candidate)).andExpect(redirectedUrl("/error/403"));
        mockMvc.perform(get("/jobs/{id}/edit", job.getId()).session(candidate)).andExpect(redirectedUrl("/error/403"));
        mockMvc.perform(post("/jobs").session(candidate)).andExpect(redirectedUrl("/error/403"));
        mockMvc.perform(post("/jobs/{id}/close", job.getId()).session(candidate)).andExpect(redirectedUrl("/error/403"));
        mockMvc.perform(post("/jobs/{id}/delete", job.getId()).session(candidate)).andExpect(redirectedUrl("/error/403"));
    }

    @Test
    void recruiterCanOpenFormWithCompaniesAndJobTypes() throws Exception {
        mockMvc.perform(get("/jobs/new").session(session(Role.RECRUITER)))
                .andExpect(status().isOk()).andExpect(view().name("jobposting/form"))
                .andExpect(content().string(containsString("name=\"companyId\"")))
                .andExpect(content().string(containsString("name=\"jobType\"")))
                .andExpect(content().string(containsString("TechCorp Inc.")))
                .andExpect(content().string(containsString("FULL_TIME")));
    }

    @Test
    void adminCanCreateJob() throws Exception {
        Company company = companyRepository.findByName("FinanceHub Ltd.").orElseThrow();
        mockMvc.perform(post("/jobs").session(session(Role.ADMIN))
                        .param("title", "Phase Four Web Job").param("description", "Created from web")
                        .param("location", "Ha Noi").param("jobType", "REMOTE")
                        .param("salaryMin", "1200").param("salaryMax", "2200")
                        .param("deadline", "2026-12-31").param("companyId", company.getId().toString()))
                .andExpect(status().is3xxRedirection());
        assertTrue(jobRepository.findByTitleAndCompany_Name("Phase Four Web Job", "FinanceHub Ltd.").isPresent());
    }

    private MockHttpSession session(String role) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConstants.LOGGED_IN_USER,
                new SessionUser(200L, role.toLowerCase(), "Test " + role, role));
        return session;
    }
}
