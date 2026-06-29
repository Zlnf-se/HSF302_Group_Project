package com.recruit.recruitmentapplication;

import com.recruit.recruitmentapplication.dto.CompanyForm;
import com.recruit.recruitmentapplication.entity.Company;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.repository.CompanyRepository;
import com.recruit.recruitmentapplication.repository.UserRepository;
import com.recruit.recruitmentapplication.security.PasswordUtil;
import com.recruit.recruitmentapplication.service.CompanyService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class PhaseThreeCompanyServiceTests {
    @Autowired private CompanyService companyService;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private Validator validator;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordUtil passwordUtil;

    @Test
    void companyFormRequiresNameIndustryAndWebsite() {
        CompanyForm form = new CompanyForm();

        Set<ConstraintViolation<CompanyForm>> violations = validator.validate(form);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("industry")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("website")));
    }

    @Test
    void createCascadesProfileAndSynchronizesBothSides() {
        Company saved = companyService.create(validForm("Phase Three Co."));

        assertNotNull(saved.getId());
        assertNotNull(saved.getProfile().getId());
        assertEquals("HCM", saved.getProfile().getHeadquarters());
        assertSame(saved, saved.getProfile().getCompany());

        Company loaded = companyRepository.findByIdWithProfile(saved.getId()).orElseThrow();
        assertEquals("Cloud recruitment platform", loaded.getProfile().getDescription());
    }

    @Test
    void createRejectsDuplicateCompanyName() {
        CompanyForm form = validForm("Duplicate Phase Three Co.");
        companyService.create(form);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> companyService.create(form)
        );

        assertTrue(error.getMessage().contains("tồn tại"));
    }

    @Test
    void updateChangesCompanyAndExistingProfile() {
        Company created = companyService.create(validForm("Company Before Update"));
        Long profileId = created.getProfile().getId();
        CompanyForm update = validForm("Company After Update");
        update.setHeadquarters("Ha Noi");
        update.setEmployeeCount(350);

        Company updated = companyService.update(created.getId(), update);

        assertEquals("Company After Update", updated.getName());
        assertEquals(profileId, updated.getProfile().getId());
        assertEquals("Ha Noi", updated.getProfile().getHeadquarters());
        assertEquals(350, updated.getProfile().getEmployeeCount());
    }

    @Test
    void deleteRemovesCompany() {
        Company created = companyService.create(validForm("Company To Delete"));

        companyService.delete(created.getId());
        companyRepository.flush();

        assertFalse(companyRepository.existsById(created.getId()));
    }

    @Test
    void initializerContainsTheThreeCompaniesRequiredByPdf() {
        Company techCorp = companyRepository.findByName("TechCorp Inc.").orElseThrow();
        Company financeHub = companyRepository.findByName("FinanceHub Ltd.").orElseThrow();
        Company creativeStudio = companyRepository.findByName("Creative Studio").orElseThrow();

        assertEquals("Cloud solutions leader", techCorp.getProfile().getDescription());
        assertEquals("HCM", techCorp.getProfile().getHeadquarters());
        assertEquals(500, techCorp.getProfile().getEmployeeCount());
        assertEquals(2010, techCorp.getProfile().getFoundedYear());
        assertEquals("Digital banking fintech", financeHub.getProfile().getDescription());
        assertEquals("UX/UI agency", creativeStudio.getProfile().getDescription());
    }

    @Test
    void initializerCreatesRecruiterAccountWithHashedPassword() {
        User recruiter = userRepository.findByUsernameWithRole("recruiter").orElseThrow();

        assertEquals(Role.RECRUITER, recruiter.getRole().getName());
        assertEquals("recruiter@recruit.com", recruiter.getEmail());
        assertFalse("Recruiter@123".equals(recruiter.getPassword()));
        assertTrue(passwordUtil.matches("Recruiter@123", recruiter.getPassword()));
    }

    private CompanyForm validForm(String name) {
        CompanyForm form = new CompanyForm();
        form.setName(name);
        form.setIndustry("Technology");
        form.setWebsite("example.com");
        form.setDescription("Cloud recruitment platform");
        form.setHeadquarters("HCM");
        form.setEmployeeCount(120);
        form.setFoundedYear(2020);
        return form;
    }
}
