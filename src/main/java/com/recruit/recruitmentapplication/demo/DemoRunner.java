package com.recruit.recruitmentapplication.demo;

import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.entity.Application.ApplicationStatus;
import com.recruit.recruitmentapplication.entity.Candidate;
import com.recruit.recruitmentapplication.entity.Interview;
import com.recruit.recruitmentapplication.entity.Interview.InterviewResult;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.entity.JobPosting.JobType;
import com.recruit.recruitmentapplication.entity.JobPosting.PostingStatus;
import com.recruit.recruitmentapplication.entity.Skill;
import com.recruit.recruitmentapplication.repository.ApplicationRepository;
import com.recruit.recruitmentapplication.repository.CandidateRepository;
import com.recruit.recruitmentapplication.repository.InterviewRepository;
import com.recruit.recruitmentapplication.repository.JobPostingRepository;
import com.recruit.recruitmentapplication.repository.SkillRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(2)
public class DemoRunner implements CommandLineRunner {
    private static final String ALICE_EMAIL = "alice@example.com";
    private static final String SENIOR_JAVA_JOB = "Senior Java Developer";

    private final ApplicationRepository applicationRepository;
    private final JobPostingRepository jobPostingRepository;
    private final CandidateRepository candidateRepository;
    private final InterviewRepository interviewRepository;
    private final SkillRepository skillRepository;
    private final ApplicationDuplicateProofService duplicateProofService;

    public DemoRunner(ApplicationRepository applicationRepository,
                      JobPostingRepository jobPostingRepository,
                      CandidateRepository candidateRepository,
                      InterviewRepository interviewRepository,
                      SkillRepository skillRepository,
                      ApplicationDuplicateProofService duplicateProofService) {
        this.applicationRepository = applicationRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.candidateRepository = candidateRepository;
        this.interviewRepository = interviewRepository;
        this.skillRepository = skillRepository;
        this.duplicateProofService = duplicateProofService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Application aliceJavaApplication = applicationRepository
                .findByCandidateEmailAndJobTitleWithDetails(ALICE_EMAIL, SENIOR_JAVA_JOB)
                .orElseThrow(() -> new IllegalStateException("Missing Alice Java application seed data"));

        demoUniqueConstraint(aliceJavaApplication);
        demoApplicationPipeline(aliceJavaApplication.getId());
        demoInterviewHistory(aliceJavaApplication.getId());
        demoCandidateSkillsJoinFetch();
        demonstrateN1Problem();
        demoRepositoryMethods();
        demoModifying(aliceJavaApplication.getId());
        demoStatistics();
    }

    private void demoUniqueConstraint(Application application) {
        Candidate candidate = application.getCandidate();
        JobPosting jobPosting = application.getJobPosting();

        System.out.println("\n--- Demo Unique Constraint: candidate_id + job_posting_id ---");
        System.out.printf("Dang co application: %s -> %s%n", candidate.getName(), jobPosting.getTitle());
        System.out.printf("So application truoc khi apply lai: %d%n", countApplications(candidate, jobPosting));

        try {
            duplicateProofService.tryCreateDuplicateApplication(candidate.getId(), jobPosting.getId());
            System.out.println("Ket qua apply lai: Tao duoc ban ghi trung (sai ky vong)");
        } catch (DataIntegrityViolationException ex) {
            System.out.println("Ket qua apply lai: Database tu choi vi trung candidate_id + job_posting_id");
        }

        System.out.printf("So application sau khi apply lai: %d%n", countApplications(candidate, jobPosting));
    }

    private void demoApplicationPipeline(Long applicationId) {
        System.out.println("\n--- Demo Application Pipeline ---");

        for (ApplicationStatus status : List.of(
                ApplicationStatus.SUBMITTED,
                ApplicationStatus.SHORTLISTED,
                ApplicationStatus.INTERVIEW_SCHEDULED,
                ApplicationStatus.OFFERED
        )) {
            applicationRepository.updateStatus(applicationId, status);
            Application currentApplication = applicationRepository
                    .findById(applicationId)
                    .orElseThrow(() -> new IllegalStateException("Missing application after status update"));

            System.out.printf("Trang thai doc lai tu DB: %s%n", currentApplication.getStatus());
        }
    }

    private void demoInterviewHistory(Long applicationId) {
        Application application = applicationRepository
                .findByIdWithInterviews(applicationId)
                .orElseThrow(() -> new IllegalStateException("Missing application interview history"));

        System.out.println("\n--- Interview History ---");
        for (Interview interview : application.getInterviews()) {
            System.out.printf(
                    "%s | Interviewer: %s | Result: %s | Notes: %s%n",
                    interview.getInterviewType(),
                    interview.getInterviewerName(),
                    interview.getResult(),
                    interview.getNotes()
            );
        }
    }

    private void demoCandidateSkillsJoinFetch() {
        System.out.println("\n--- Demo JOIN FETCH: Candidate + Skills ---");
        List<Candidate> candidates = candidateRepository.findAllWithSkills();

        for (Candidate candidate : candidates) {
            String skills = candidate.getSkills().stream()
                    .map(Skill::getName)
                    .sorted()
                    .collect(Collectors.joining(", "));
            System.out.printf("  %s -> %s%n", candidate.getName(), skills);
        }

        Skill javaSkill = skillRepository.findByName("Java")
                .orElseThrow(() -> new IllegalStateException("Missing Java skill seed data"));
        System.out.printf("%nBan ghi Java trong bang skills: id=%d, name=%s%n", javaSkill.getId(), javaSkill.getName());

        for (Candidate candidate : candidates) {
            candidate.getSkills().stream()
                    .filter(skill -> skill.getName().equals("Java"))
                    .findFirst()
                    .ifPresent(skill -> System.out.printf(
                            "  %s tham chieu Java Skill ID = %d%n",
                            candidate.getName(),
                            skill.getId()
                    ));
        }

        System.out.printf("So ban ghi Skill ten Java trong database: %d%n", skillRepository.countByName("Java"));
    }

    private void demonstrateN1Problem() {
        System.out.println("\n--- N+1 Problem Demo ---");

        System.out.println("BAD: findAll() roi truy cap jp.getCompany().getName() tung dong");
        for (JobPosting posting : jobPostingRepository.findAll()) {
            System.out.printf("  %s | Company: %s%n", posting.getTitle(), posting.getCompany().getName());
        }
        System.out.println("Neu bat SQL log: cach BAD thuong tao 1 query lay jobs + N query lay company.");

        System.out.println("\nGOOD: JOIN FETCH load san JobPosting + Company");
        for (JobPosting posting : jobPostingRepository.findOpenJobsWithCompany()) {
            System.out.printf("  %s | Company: %s%n", posting.getTitle(), posting.getCompany().getName());
        }
        System.out.println("JOIN FETCH tranh N+1 vi load job va company trong cung truy van.");
    }

    private void demoRepositoryMethods() {
        System.out.println("\n--- Demo Repository Methods ---");

        System.out.println("[Derived] Open jobs:");
        System.out.printf("  So luong: %d%n", jobPostingRepository.countByStatus(PostingStatus.OPEN));

        System.out.println("[Derived] Remote jobs:");
        printJobTitles(jobPostingRepository.findByJobType(JobType.REMOTE));

        System.out.println("[Derived] Jobs tu $1500+ salaryMin:");
        printJobTitles(jobPostingRepository.findBySalaryMinGreaterThanEqual(BigDecimal.valueOf(1500)));

        System.out.println("[Derived] Jobs co deadline sau hom nay:");
        printJobTitles(jobPostingRepository.findByDeadlineAfter(LocalDate.now()));

        System.out.println("[Derived] OPEN + REMOTE jobs:");
        printJobTitles(jobPostingRepository.findByStatusAndJobType(PostingStatus.OPEN, JobType.REMOTE));

        Application aliceApplication = applicationRepository
                .findByCandidateEmailAndJobTitleWithDetails(ALICE_EMAIL, SENIOR_JAVA_JOB)
                .orElseThrow(() -> new IllegalStateException("Missing Alice Java application seed data"));
        boolean existsAtCompany = jobPostingRepository.existsByTitleAndCompany_Id(
                SENIOR_JAVA_JOB,
                aliceApplication.getJobPosting().getCompany().getId()
        );
        System.out.printf("[Derived] Title '%s' ton tai tai cong ty Alice apply: %s%n", SENIOR_JAVA_JOB, existsAtCompany);

        System.out.println("[JPQL] Open jobs yeu cau 'Java':");
        for (JobPosting posting : jobPostingRepository.findOpenJobsBySkill("Java")) {
            System.out.printf("  %s | Company: %s%n", posting.getTitle(), posting.getCompany().getName());
        }

        System.out.println("[JPQL] Open jobs luong $1200-$3000:");
        printJobTitles(jobPostingRepository.findByOpenSalaryRange(BigDecimal.valueOf(1200), BigDecimal.valueOf(3000)));

        System.out.println("[JPQL] Candidates biet 'Docker':");
        for (Candidate candidate : candidateRepository.findBySkillName("Docker")) {
            System.out.printf("  %s | %s%n", candidate.getName(), candidate.getEmail());
        }

        System.out.println("[JPQL] Candidates >= 4 nam kinh nghiem:");
        for (Candidate candidate : candidateRepository.findByMinExperience(4)) {
            System.out.printf(
                    "  %s | %d nam%n",
                    candidate.getName(),
                    candidate.getProfile().getYearsOfExperience()
            );
        }
    }

    private void demoModifying(Long aliceJavaApplicationId) {
        System.out.println("\n--- Demo @Modifying + Native SQL ---");

        JobPosting expiredPosting = prepareExpiredOpenPosting();
        System.out.printf(
                "Truoc closeExpiredPostings(): %s | deadline=%s | status=%s%n",
                expiredPosting.getTitle(),
                expiredPosting.getDeadline(),
                expiredPosting.getStatus()
        );
        int closed = jobPostingRepository.closeExpiredPostings(LocalDate.now());
        System.out.printf("So dong job postings het han bi dong: %d%n", closed);
        JobPosting closedPosting = jobPostingRepository.findById(expiredPosting.getId())
                .orElseThrow(() -> new IllegalStateException("Missing expired posting after closeExpiredPostings"));
        System.out.printf(
                "Sau closeExpiredPostings(): %s | deadline=%s | status=%s%n",
                closedPosting.getTitle(),
                closedPosting.getDeadline(),
                closedPosting.getStatus()
        );

        System.out.println("\nNative SQL GROUP BY + COUNT - Top 3 jobs nhan nhieu ung vien nhat:");
        System.out.println("Ket qua sap xep theo COUNT(applications.id) DESC:");
        for (JobPosting posting : jobPostingRepository.findMostAppliedJobs(3)) {
            long count = applicationRepository.countByJobPosting_Id(posting.getId());
            System.out.printf("  %-25s | applications=%d%n", posting.getTitle(), count);
        }

        System.out.println("\nBulk update nhanh hon load-modify-save vi:");
        System.out.println("  @Modifying: 1 cau UPDATE xu ly nhieu dong va tra ve so dong bi anh huong.");
        System.out.println("  Load-modify-save: SELECT danh sach entity -> lap tung entity -> UPDATE tung dong.");

        Interview interview = interviewRepository.findByApplication_Id(aliceJavaApplicationId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing interview seed data"));
        interview.setResult(InterviewResult.PENDING);
        interview.setNotes("Waiting for technical evaluation");
        interview = interviewRepository.saveAndFlush(interview);
        System.out.printf(
                "\nTruoc recordResult(): Interview #%d | result=%s | notes=%s%n",
                interview.getId(),
                interview.getResult(),
                interview.getNotes()
        );
        int recorded = interviewRepository.recordResult(
                interview.getId(),
                InterviewResult.PASSED,
                "Strong Java skills - recorded by @Modifying"
        );
        Interview updatedInterview = interviewRepository.findById(interview.getId())
                .orElseThrow(() -> new IllegalStateException("Missing interview after recordResult"));
        System.out.printf(
                "recordResult() update: %d dong | Result: %s | Notes: %s%n",
                recorded,
                updatedInterview.getResult(),
                updatedInterview.getNotes()
        );

        Application bobApplication = applicationRepository
                .findByCandidateEmailAndJobTitleWithDetails("bob@example.com", "Frontend Developer")
                .orElseThrow(() -> new IllegalStateException("Missing Bob frontend application seed data"));
        ApplicationStatus beforeStatus = bobApplication.getStatus();
        int updated = applicationRepository.updateStatus(bobApplication.getId(), ApplicationStatus.SHORTLISTED);
        Application refreshedBobApplication = applicationRepository.findById(bobApplication.getId())
                .orElseThrow(() -> new IllegalStateException("Missing Bob application after updateStatus"));
        System.out.printf(
                "Bob application: %s -> %s (%d dong update)%n",
                beforeStatus,
                refreshedBobApplication.getStatus(),
                updated
        );
    }

    private JobPosting prepareExpiredOpenPosting() {
        JobPosting posting = jobPostingRepository
                .findByTitleAndCompany_Name("UX/UI Designer", "Creative Studio")
                .orElseThrow(() -> new IllegalStateException("Missing UX/UI Designer seed data"));

        posting.setStatus(PostingStatus.OPEN);
        posting.setDeadline(LocalDate.now().minusDays(1));
        return jobPostingRepository.saveAndFlush(posting);
    }

    private void demoStatistics() {
        System.out.println("\n--- Thong Ke He Thong ---");
        System.out.printf("Tong so job postings dang OPEN: %d%n", jobPostingRepository.countByStatus(PostingStatus.OPEN));
        System.out.printf("Tong so applications dang SUBMITTED: %d%n", applicationRepository.findByStatus(ApplicationStatus.SUBMITTED).size());
        System.out.printf("Jobs OPEN trong 30 ngay gan day: %d%n", jobPostingRepository.findRecentOpenJobs(30).size());

        System.out.println("Top 3 jobs duoc apply nhieu nhat:");
        for (JobPosting posting : jobPostingRepository.findMostAppliedJobs(3)) {
            System.out.printf("  %s (%d applications)%n", posting.getTitle(), applicationRepository.countByJobPosting_Id(posting.getId()));
        }

        System.out.println("Top 3 candidates nang dong nhat:");
        for (Candidate candidate : candidateRepository.findMostActiveCandidates(3)) {
            System.out.printf("  %s (%d applications)%n", candidate.getName(), applicationRepository.findByCandidate_Id(candidate.getId()).size());
        }

        System.out.printf("So jobs yeu cau skill 'Java': %d%n", jobPostingRepository.findOpenJobsBySkill("Java").size());
        System.out.printf("So candidates co skill 'Docker': %d%n", candidateRepository.findBySkillName("Docker").size());
    }

    private long countApplications(Candidate candidate, JobPosting jobPosting) {
        return applicationRepository.countByCandidate_IdAndJobPosting_Id(candidate.getId(), jobPosting.getId());
    }

    private void printJobTitles(List<JobPosting> postings) {
        if (postings.isEmpty()) {
            System.out.println("  Khong co ket qua");
            return;
        }

        for (JobPosting posting : postings) {
            System.out.printf("  %s%n", posting.getTitle());
        }
    }
}
