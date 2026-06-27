package com.recruit.recruitmentapplication;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Company;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.repository.CompanyRepository;
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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PhaseThreeCompanyWebTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private CompanyRepository companyRepository;

    @Test
    void anonymousUserIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/companies"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    void candidateCanReadCompanyListAndDetail() throws Exception {
        Company company = companyRepository.findByName("TechCorp Inc.").orElseThrow();

        mockMvc.perform(get("/companies").session(session(Role.CANDIDATE)))
                .andExpect(status().isOk())
                .andExpect(view().name("company/list"))
                .andExpect(model().attributeExists("companies"));
        mockMvc.perform(get("/companies/{id}", company.getId()).session(session(Role.CANDIDATE)))
                .andExpect(status().isOk())
                .andExpect(view().name("company/detail"))
                .andExpect(model().attributeExists("company"));
    }

    @Test
    void candidateCannotOpenOrSubmitCompanyForms() throws Exception {
        Company company = companyRepository.findByName("TechCorp Inc.").orElseThrow();
        MockHttpSession session = session(Role.CANDIDATE);

        mockMvc.perform(get("/companies/new").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error/403"));
        mockMvc.perform(get("/companies/{id}/edit", company.getId()).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error/403"));
        mockMvc.perform(post("/companies").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error/403"));
        mockMvc.perform(post("/companies/{id}/delete", company.getId()).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error/403"));
    }

    @Test
    void recruiterCanOpenCreateAndEditForms() throws Exception {
        Company company = companyRepository.findByName("TechCorp Inc.").orElseThrow();
        MockHttpSession session = session(Role.RECRUITER);

        mockMvc.perform(get("/companies/new").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("company/form"))
                .andExpect(model().attributeExists("companyForm"));
        mockMvc.perform(get("/companies/{id}/edit", company.getId()).session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("company/form"))
                .andExpect(model().attribute("editMode", true));
    }

    @Test
    void adminCanCreateAndRecruiterCanUpdateCompany() throws Exception {
        mockMvc.perform(post("/companies")
                        .session(session(Role.ADMIN))
                        .param("name", "Web Created Company")
                        .param("industry", "Technology")
                        .param("website", "web-created.example")
                        .param("description", "Created through MockMvc")
                        .param("headquarters", "HCM")
                        .param("employeeCount", "45")
                        .param("foundedYear", "2021"))
                .andExpect(status().is3xxRedirection());

        Company created = companyRepository.findByName("Web Created Company").orElseThrow();
        assertEquals("Created through MockMvc", created.getProfile().getDescription());

        mockMvc.perform(post("/companies/{id}/edit", created.getId())
                        .session(session(Role.RECRUITER))
                        .param("name", "Web Updated Company")
                        .param("industry", "Design")
                        .param("website", "web-updated.example")
                        .param("description", "Updated through MockMvc")
                        .param("headquarters", "Da Nang")
                        .param("employeeCount", "50")
                        .param("foundedYear", "2021"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/companies/" + created.getId()));

        assertTrue(companyRepository.findByName("Web Updated Company").isPresent());
    }

    @Test
    void listRendersProfileDataAndRoleSpecificCreateAction() throws Exception {
        mockMvc.perform(get("/companies").session(session(Role.CANDIDATE)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("TechCorp Inc.")))
                .andExpect(content().string(containsString("500")))
                .andExpect(content().string(not(containsString("Thêm công ty"))));

        mockMvc.perform(get("/companies").session(session(Role.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Thêm công ty")));
    }

    @Test
    void detailRendersFullProfileAndHidesManagementFromCandidate() throws Exception {
        Company company = companyRepository.findByName("TechCorp Inc.").orElseThrow();

        mockMvc.perform(get("/companies/{id}", company.getId()).session(session(Role.CANDIDATE)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cloud solutions leader")))
                .andExpect(content().string(containsString("techcorp.com")))
                .andExpect(content().string(not(containsString("Chỉnh sửa"))))
                .andExpect(content().string(not(containsString("Xóa công ty"))));
    }

    @Test
    void sharedFormRendersAllCompanyAndProfileFields() throws Exception {
        mockMvc.perform(get("/companies/new").session(session(Role.RECRUITER)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"name\"")))
                .andExpect(content().string(containsString("name=\"industry\"")))
                .andExpect(content().string(containsString("name=\"website\"")))
                .andExpect(content().string(containsString("name=\"description\"")))
                .andExpect(content().string(containsString("name=\"headquarters\"")))
                .andExpect(content().string(containsString("name=\"employeeCount\"")))
                .andExpect(content().string(containsString("name=\"foundedYear\"")));
    }

    private MockHttpSession session(String roleName) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConstants.LOGGED_IN_USER,
                new SessionUser(100L, roleName.toLowerCase(), "Test " + roleName, roleName));
        return session;
    }
}
