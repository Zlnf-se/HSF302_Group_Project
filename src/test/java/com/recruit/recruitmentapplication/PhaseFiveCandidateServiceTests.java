package com.recruit.recruitmentapplication;

import com.recruit.recruitmentapplication.dto.CandidateProfileForm;
import com.recruit.recruitmentapplication.dto.JobPostingForm;
import com.recruit.recruitmentapplication.entity.*;
import com.recruit.recruitmentapplication.repository.*;
import com.recruit.recruitmentapplication.security.PasswordUtil;
import com.recruit.recruitmentapplication.service.CandidateService;
import com.recruit.recruitmentapplication.service.JobPostingService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PhaseFiveCandidateServiceTests {
    @Autowired private CandidateService service;
    @Autowired private CandidateRepository candidateRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private SkillRepository skillRepository;
    @Autowired private PasswordUtil passwordUtil;
    @Autowired private JobPostingService jobPostingService;

    @Test
    void initializerLinksAliceAndBobUsersToCandidates() {
        User alice = userRepository.findByUsernameWithRole("alice").orElseThrow();
        User bob = userRepository.findByUsernameWithRole("bob").orElseThrow();
        assertEquals(Role.CANDIDATE, alice.getRole().getName());
        assertTrue(passwordUtil.matches("Alice@123", alice.getPassword()));
        assertEquals("Alice Nguyen", candidateRepository.findByUser_Id(alice.getId()).orElseThrow().getName());
        assertEquals("Bob Tran", candidateRepository.findByUser_Id(bob.getId()).orElseThrow().getName());
    }

    @Test
    void getOrCreateBuildsCandidateFromUser() {
        Role role = roleRepository.findByName(Role.CANDIDATE).orElseThrow();
        User user = userRepository.save(new User("newprofile", "hash", "newprofile@example.com", "New Profile", role));
        Candidate candidate = service.getOrCreateProfileForUser(user.getId());
        assertEquals("New Profile", candidate.getName());
        assertEquals("newprofile@example.com", candidate.getEmail());
        assertEquals(user.getId(), candidate.getUser().getId());
        assertEquals(candidate.getId(), service.getOrCreateProfileForUser(user.getId()).getId());
    }

    @Test
    void updateProfileReplacesSkillsAndCreatesProfile() {
        Role role = roleRepository.findByName(Role.CANDIDATE).orElseThrow();
        User user = userRepository.save(new User("updateskills", "hash", "updateskills@example.com", "Update Skills", role));
        Candidate candidate = service.getOrCreateProfileForUser(user.getId());
        Skill javaSkill = skillRepository.findByName("Java").orElseThrow();
        Skill docker = skillRepository.findByName("Docker").orElseThrow();
        CandidateProfileForm form = new CandidateProfileForm();
        form.setName("Updated Candidate"); form.setEmail("updateskills@example.com"); form.setPhone("0909999999");
        form.setYearsOfExperience(4); form.setEducationLevel("Bachelor"); form.setCurrentTitle("Backend Developer");
        form.setLinkedinUrl("https://linkedin.com/in/update"); form.setResumeSummary("Updated profile");
        form.setSkillIds(Set.of(javaSkill.getId(), docker.getId()));
        Candidate updated = service.updateProfile(candidate.getId(), form);
        candidateRepository.flush();
        assertEquals("Backend Developer", updated.getProfile().getCurrentTitle());
        assertEquals(Set.of("Java", "Docker"), updated.getSkills().stream().map(Skill::getName).collect(java.util.stream.Collectors.toSet()));
        assertThrows(UnsupportedOperationException.class, () -> updated.getSkills().clear());
    }

    @Test
    void recruiterFormCanAssignRequiredSkillsToJob() {
        Company company = companyRepository().findByName("TechCorp Inc.").orElseThrow();
        Skill javaSkill = skillRepository.findByName("Java").orElseThrow();
        Skill docker = skillRepository.findByName("Docker").orElseThrow();
        JobPostingForm form = new JobPostingForm();
        form.setTitle("Skill Assignment Job"); form.setDescription("Requires selected skills"); form.setLocation("HCM");
        form.setJobType("FULL_TIME"); form.setSalaryMin(new BigDecimal("1000")); form.setSalaryMax(new BigDecimal("2000"));
        form.setDeadline(LocalDate.now().plusDays(20)); form.setCompanyId(company.getId());
        form.setSkillIds(Set.of(javaSkill.getId(), docker.getId()));
        JobPosting job = jobPostingService.create(form);
        assertEquals(Set.of("Java", "Docker"), job.getRequiredSkills().stream().map(Skill::getName).collect(java.util.stream.Collectors.toSet()));
    }

    @Autowired private CompanyRepository companyRepositoryField;
    private CompanyRepository companyRepository() { return companyRepositoryField; }
}
