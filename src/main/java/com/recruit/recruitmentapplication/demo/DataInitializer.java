package com.recruit.recruitmentapplication.demo;

import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.entity.Application.ApplicationStatus;
import com.recruit.recruitmentapplication.entity.Candidate;
import com.recruit.recruitmentapplication.entity.CandidateProfile;
import com.recruit.recruitmentapplication.entity.Company;
import com.recruit.recruitmentapplication.entity.CompanyProfile;
import com.recruit.recruitmentapplication.entity.Interview;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.entity.JobPosting.JobType;
import com.recruit.recruitmentapplication.entity.JobPosting.PostingStatus;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.entity.Skill;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.repository.ApplicationRepository;
import com.recruit.recruitmentapplication.repository.CandidateRepository;
import com.recruit.recruitmentapplication.repository.CompanyRepository;
import com.recruit.recruitmentapplication.repository.InterviewRepository;
import com.recruit.recruitmentapplication.repository.JobPostingRepository;
import com.recruit.recruitmentapplication.repository.RoleRepository;
import com.recruit.recruitmentapplication.repository.SkillRepository;
import com.recruit.recruitmentapplication.repository.UserRepository;
import com.recruit.recruitmentapplication.security.PasswordUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {
    private final CompanyRepository companyRepository;
    private final JobPostingRepository jobPostingRepository;
    private final CandidateRepository candidateRepository;
    private final SkillRepository skillRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordUtil passwordUtil;

    public DataInitializer(CompanyRepository companyRepository,
                           JobPostingRepository jobPostingRepository,
                           CandidateRepository candidateRepository,
                           SkillRepository skillRepository,
                           ApplicationRepository applicationRepository,
                           InterviewRepository interviewRepository,
                           RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordUtil passwordUtil) {
        this.companyRepository = companyRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.candidateRepository = candidateRepository;
        this.skillRepository = skillRepository;
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordUtil = passwordUtil;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = seedRole(Role.ADMIN);
        Role recruiterRole = seedRole(Role.RECRUITER);
        Role interviewerRole = seedRole(Role.INTERVIEWER);
        Role candidateRole = seedRole(Role.CANDIDATE);
        seedAdmin(adminRole);
        User recruiter = seedRecruiter(recruiterRole);
        seedRecruiter(recruiterRole);
        User ivanInterviewer = seedUser("ivan", "Ivan@123", "ivan@recruit.com", "Ivan Interviewer", interviewerRole);
        User aliceUser = seedUser("alice", "Alice@123", "alice@example.com", "Alice Nguyen", candidateRole);
        User bobUser = seedUser("bob", "Bob@123", "bob@example.com", "Bob Tran", candidateRole);
        User carolUser = seedUser("carol", "Carol@123", "carol@example.com", "Carol Le", candidateRole);
        User davidUser = seedUser("david", "David@123", "david@example.com", "David Pham", candidateRole);

        Company techCorp = seedCompany(
                "TechCorp Inc.",
                "Technology",
                "techcorp.com",
                "Cloud solutions leader",
                "HCM",
                500,
                2010
        );
        Company financeHub = seedCompany(
                "FinanceHub Ltd.",
                "Finance",
                "financehub.com",
                "Digital banking fintech",
                "Ha Noi",
                200,
                2015
        );
        Company creativeStudio = seedCompany(
                "Creative Studio",
                "Design",
                "creativestudio.com",
                "UX/UI agency",
                "Da Nang",
                80,
                2018
        );

        JobPosting seniorJavaDeveloper = seedJobPosting(
                techCorp,
                "Senior Java Developer",
                "Engineering",
                "Develop backend services for the recruitment platform",
                "HCM",
                JobType.FULL_TIME,
                2000,
                3500,
                30,
                recruiter
        );
        JobPosting frontendDeveloper = seedJobPosting(
                techCorp,
                "Frontend Developer",
                "Engineering",
                "Build user interfaces for candidates and employers",
                "Remote",
                JobType.REMOTE,
                1500,
                2500,
                20,
                recruiter
        );
        JobPosting dataAnalyst = seedJobPosting(
                financeHub,
                "Data Analyst",
                "Data & Analytics",
                "Analyze finance and recruitment data",
                "Ha Noi",
                JobType.FULL_TIME,
                1200,
                2000,
                15,
                recruiter
        );
        JobPosting devOpsEngineer = seedJobPosting(
                financeHub,
                "DevOps Engineer",
                "Engineering",
                "Maintain cloud infrastructure and CI/CD",
                "Ha Noi",
                JobType.FULL_TIME,
                1800,
                3000,
                25,
                recruiter
        );
        JobPosting uxUiDesigner = seedJobPosting(
                creativeStudio,
                "UX/UI Designer",
                "Design",
                "Design candidate and recruiter experiences",
                "Da Nang",
                JobType.FULL_TIME,
                1000,
                1800,
                10,
                recruiter
        );

        Candidate alice = seedCandidate(
                aliceUser,
                "Alice Nguyen",
                "alice@example.com",
                "0901000001",
                5,
                "Bachelor CS",
                "Senior Java Developer",
                "https://linkedin.com/in/alice-nguyen",
                "Backend engineer with strong Java and cloud experience.",
                "Java", "Spring Boot", "PostgreSQL", "Docker"
        );
        Candidate bob = seedCandidate(
                bobUser,
                "Bob Tran",
                "bob@example.com",
                "0901000002",
                3,
                "Bachelor IT",
                "Frontend Developer",
                "https://linkedin.com/in/bob-tran",
                "Frontend developer focused on modern web applications.",
                "React", "TypeScript", "CSS", "REST API"
        );
        Candidate carol = seedCandidate(
                carolUser,
                "Carol Le",
                "carol@example.com",
                "0901000003",
                4,
                "Master Data Science",
                "Data Analyst",
                "https://linkedin.com/in/carol-le",
                "Data analyst experienced in reporting and visualization.",
                "SQL", "Python", "Power BI", "Excel"
        );
        Candidate david = seedCandidate(
                davidUser,
                "David Pham",
                "david@example.com",
                "0901000004",
                6,
                "Bachelor IT",
                "DevOps Engineer",
                "https://linkedin.com/in/david-pham",
                "DevOps engineer with container, cloud, and CI/CD experience.",
                "Docker", "Kubernetes", "AWS", "CI/CD", "Java"
        );

        addRequiredSkills(seniorJavaDeveloper, "Java", "Spring Boot", "PostgreSQL", "Docker");
        addRequiredSkills(frontendDeveloper, "React", "TypeScript", "CSS", "REST API");
        addRequiredSkills(dataAnalyst, "SQL", "Python", "Power BI", "Excel");
        addRequiredSkills(devOpsEngineer, "Docker", "Kubernetes", "AWS", "CI/CD");
        addRequiredSkills(uxUiDesigner, "Figma", "Adobe XD", "CSS", "Prototyping");

        Application aliceJavaApplication = seedApplication(
                alice,
                seniorJavaDeveloper,
                "I have 5 years of Java experience and want to help TechCorp build reliable backend services."
        );
        Application bobFrontendApplication = seedApplication(
                bob,
                frontendDeveloper,
                "I enjoy building clean user interfaces with React and TypeScript."
        );
        seedApplication(
                carol,
                dataAnalyst,
                "My data science background fits the analytics needs of this role."
        );
        seedApplication(
                alice,
                frontendDeveloper,
                "I can also support full-stack work for candidate-facing features."
        );

        // Build the demo pipeline scenario only once. Without this guard the seeder
        // creates a fresh interview (and evaluation) on every startup, accumulating duplicates.
        if (!interviewRepository.existsByApplication_IdAndInterviewer_Id(
                aliceJavaApplication.getId(), ivanInterviewer.getId())) {
            aliceJavaApplication = updateApplicationStatus(aliceJavaApplication, ApplicationStatus.SCREENING);
            aliceJavaApplication = updateApplicationStatus(aliceJavaApplication, ApplicationStatus.INTERVIEW);
            Interview aliceTechnicalInterview = seedInterview(
                    aliceJavaApplication,
                    ivanInterviewer,
                    LocalDateTime.now().plusDays(3),
                    "Phòng họp A / Google Meet"
            );
            recordEvaluation(aliceTechnicalInterview, 4, "Nền tảng Java vững, giao tiếp tốt.");
            updateApplicationStatus(aliceJavaApplication, ApplicationStatus.OFFER);
        }
        // Only set Bob's initial stage on first seed, so manual changes are not overwritten on restart.
        if (bobFrontendApplication.getStatus() == ApplicationStatus.APPLIED) {
            updateApplicationStatus(bobFrontendApplication, ApplicationStatus.SCREENING);
        }
    }

    private Role seedRole(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(new Role(name)));
    }

    private void seedAdmin(Role adminRole) {
        if (userRepository.existsByUsername("admin")) {
            return;
        }
        User admin = new User(
                "admin",
                passwordUtil.hash("Admin@123"),
                "admin@recruit.com",
                "System Administrator",
                adminRole
        );
        userRepository.save(admin);
    }

    private User seedRecruiter(Role recruiterRole) {
        return userRepository.findByUsername("recruiter").orElseGet(() -> userRepository.save(new User(
                "recruiter",
                passwordUtil.hash("Recruiter@123"),
                "recruiter@recruit.com",
                "Recruitment Manager",
                recruiterRole
        )));
    }

    private User seedUser(String username, String rawPassword, String email, String fullName, Role role) {
        return userRepository.findByUsername(username).orElseGet(() -> userRepository.save(
                new User(username, passwordUtil.hash(rawPassword), email, fullName, role)));
    }

    private Company seedCompany(
            String name,
            String industry,
            String website,
            String description,
            String headquarters,
            Integer employeeCount,
            Integer foundedYear
    ) {
        return companyRepository.findByName(name).orElseGet(() -> createCompany(
                name,
                industry,
                website,
                description,
                headquarters,
                employeeCount,
                foundedYear
        ));
    }

    private Company createCompany(
            String name,
            String industry,
            String website,
            String description,
            String headquarters,
            Integer employeeCount,
            Integer foundedYear
    ) {
        CompanyProfile profile = new CompanyProfile(description, headquarters, employeeCount, foundedYear);
        Company company = new Company(name, industry, website);

        company.setProfile(profile);
        return companyRepository.save(company);
    }

    private JobPosting seedJobPosting(
            Company company,
            String title,
            String department,
            String description,
            String location,
            JobType jobType,
            int salaryMin,
            int salaryMax,
            int deadlineDays,
            User owner
    ) {
        JobPosting posting = jobPostingRepository.findByTitleAndCompany_Name(title, company.getName()).orElseGet(() -> {
            JobPosting created = new JobPosting(
                    title,
                    department,
                    description,
                    location,
                    jobType,
                    BigDecimal.valueOf(salaryMin),
                    BigDecimal.valueOf(salaryMax),
                    LocalDate.now().plusDays(deadlineDays)
            );
            created.setPostedDate(LocalDate.now());
            created.setStatus(PostingStatus.ACTIVE);
            created.setCreatedBy(owner);

            company.addJobPosting(created);
            return jobPostingRepository.save(created);
        });
        // Backfill fields on rows seeded before these columns/values existed.
        boolean changed = false;
        if (posting.getCreatedBy() == null && owner != null) {
            posting.setCreatedBy(owner);
            changed = true;
        }
        if (posting.getDepartment() == null) {
            posting.setDepartment(department);
            changed = true;
        }
        if (posting.getLocation() == null) {
            posting.setLocation(location);
            changed = true;
        }
        if (changed) {
            jobPostingRepository.save(posting);
        }
        return posting;
    }

    private Skill seedSkill(String name) {
        return skillRepository.findByName(name)
                .orElseGet(() -> skillRepository.save(new Skill(name, resolveSkillCategory(name))));
    }

    private String resolveSkillCategory(String name) {
        return switch (name) {
            case "Java", "Spring Boot", "PostgreSQL", "Docker", "REST API", "Kubernetes", "AWS", "CI/CD" -> "Backend";
            case "React", "TypeScript", "CSS", "Figma", "Adobe XD", "Prototyping" -> "Frontend";
            case "SQL", "Python", "Power BI", "Excel" -> "Database";
            default -> "General";
        };
    }

    private Candidate seedCandidate(
            User user,
            String name,
            String email,
            String phone,
            Integer yearsOfExperience,
            String educationLevel,
            String currentTitle,
            String linkedinUrl,
            String resumeSummary,
            String... skillNames
    ) {
        Candidate candidate = candidateRepository.findByEmail(email).orElseGet(() -> createCandidate(
                user,
                name,
                email,
                phone,
                yearsOfExperience,
                educationLevel,
                currentTitle,
                linkedinUrl,
                resumeSummary
        ));

        if (candidate.getUser() == null) {
            candidate.setUser(user);
        }

        for (String skillName : skillNames) {
            candidate.addSkill(seedSkill(skillName));
        }
        return candidateRepository.save(candidate);
    }

    private Candidate createCandidate(
            User user,
            String name,
            String email,
            String phone,
            Integer yearsOfExperience,
            String educationLevel,
            String currentTitle,
            String linkedinUrl,
            String resumeSummary
    ) {
        CandidateProfile profile = new CandidateProfile(
                yearsOfExperience,
                educationLevel,
                currentTitle,
                linkedinUrl,
                resumeSummary
        );
        Candidate candidate = new Candidate(name, email, phone);

        candidate.setProfile(profile);
        candidate.setUser(user);
        return candidateRepository.save(candidate);
    }

    private void addRequiredSkills(JobPosting posting, String... skillNames) {
        for (String skillName : skillNames) {
            posting.addRequiredSkill(seedSkill(skillName));
        }
        jobPostingRepository.save(posting);
    }

    private Application seedApplication(Candidate candidate, JobPosting jobPosting, String coverLetter) {
        return applicationRepository
                .findByCandidate_IdAndJobPosting_Id(candidate.getId(), jobPosting.getId())
                .orElseGet(() -> {
                    Application application = new Application();
                    application.setCoverLetter(coverLetter);

                    candidate.addApplication(application);
                    jobPosting.addApplication(application);
                    return applicationRepository.save(application);
                });
    }

    private Application updateApplicationStatus(Application application, ApplicationStatus status) {
        application.setStatus(status);
        application.setStageEnteredAt(LocalDateTime.now());
        return applicationRepository.save(application);
    }

    private Interview seedInterview(
            Application application,
            User interviewer,
            LocalDateTime scheduledAt,
            String location
    ) {
        Interview interview = new Interview(interviewer, scheduledAt, location);

        application.addInterview(interview);
        return interviewRepository.save(interview);
    }

    private Interview recordEvaluation(Interview interview, int rating, String feedback) {
        interview.setRating(rating);
        interview.setFeedback(feedback);
        interview.setStatus(Interview.InterviewStatus.EVALUATED);
        interview.setEvaluatedAt(LocalDateTime.now());
        return interviewRepository.save(interview);
    }
}