package com.recruit.recruitmentapplication;

import com.recruit.recruitmentapplication.dto.JobPostingForm;
import com.recruit.recruitmentapplication.entity.Company;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.entity.JobPosting.PostingStatus;
import com.recruit.recruitmentapplication.repository.CompanyRepository;
import com.recruit.recruitmentapplication.repository.JobPostingRepository;
import com.recruit.recruitmentapplication.service.JobPostingService;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PhaseFourJobPostingServiceTests {
    @Autowired private JobPostingService service;
    @Autowired private JobPostingRepository repository;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private Validator validator;

    @Test
    void formRequiresTitleJobTypeAndCompany() {
        JobPostingForm form = new JobPostingForm();
        var paths = validator.validate(form).stream().map(v -> v.getPropertyPath().toString()).toList();
        assertTrue(paths.contains("title"));
        assertTrue(paths.contains("jobType"));
        assertTrue(paths.contains("companyId"));
    }

    @Test
    void createSetsDefaultsAndCompanyRelationship() {
        Company company = companyRepository.findByName("TechCorp Inc.").orElseThrow();
        JobPosting created = service.create(validForm("Phase Four Backend", company.getId()));
        assertNotNull(created.getId());
        assertEquals(PostingStatus.OPEN, created.getStatus());
        assertEquals(LocalDate.now(), created.getPostedDate());
        assertEquals(company.getId(), created.getCompany().getId());
        assertTrue(company.getJobPostings().contains(created));
    }

    @Test
    void searchReturnsOnlyOpenJobsWithInitializedCompany() {
        var jobs = service.findOpenJobs("java");
        assertFalse(jobs.isEmpty());
        assertTrue(jobs.stream().allMatch(j -> j.getStatus() == PostingStatus.OPEN));
        assertTrue(jobs.stream().allMatch(j -> j.getTitle().toLowerCase().contains("java")));
        assertTrue(jobs.stream().allMatch(j -> Hibernate.isInitialized(j.getCompany())));
    }

    @Test
    void createRejectsInvalidSalaryRange() {
        Company company = companyRepository.findByName("TechCorp Inc.").orElseThrow();
        JobPostingForm form = validForm("Invalid Salary Job", company.getId());
        form.setSalaryMin(new BigDecimal("3000"));
        form.setSalaryMax(new BigDecimal("1000"));
        assertThrows(IllegalArgumentException.class, () -> service.create(form));
    }

    @Test
    void updateCanMovePostingToAnotherCompany() {
        Company first = companyRepository.findByName("TechCorp Inc.").orElseThrow();
        Company second = companyRepository.findByName("FinanceHub Ltd.").orElseThrow();
        JobPosting created = service.create(validForm("Move This Job", first.getId()));
        JobPostingForm update = validForm("Moved Job", second.getId());
        update.setJobType("REMOTE");
        JobPosting updated = service.update(created.getId(), update);
        repository.flush();
        assertEquals("Moved Job", updated.getTitle());
        assertEquals(second.getId(), updated.getCompany().getId());
        assertEquals(JobPosting.JobType.REMOTE, updated.getJobType());
    }

    @Test
    void closeAndDeleteChangePersistentState() {
        Company company = companyRepository.findByName("Creative Studio").orElseThrow();
        JobPosting created = service.create(validForm("Close Delete Job", company.getId()));
        service.close(created.getId());
        assertEquals(PostingStatus.CLOSED, repository.findById(created.getId()).orElseThrow().getStatus());
        service.delete(created.getId());
        repository.flush();
        assertFalse(repository.existsById(created.getId()));
    }

    private JobPostingForm validForm(String title, Long companyId) {
        JobPostingForm form = new JobPostingForm();
        form.setTitle(title);
        form.setDescription("Phase Four description");
        form.setLocation("HCM");
        form.setJobType("FULL_TIME");
        form.setSalaryMin(new BigDecimal("1000"));
        form.setSalaryMax(new BigDecimal("2000"));
        form.setDeadline(LocalDate.now().plusDays(30));
        form.setCompanyId(companyId);
        return form;
    }
}
