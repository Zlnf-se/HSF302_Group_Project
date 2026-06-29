package com.recruit.recruitmentapplication.service;

import com.recruit.recruitmentapplication.dto.ApplicationForm;
import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.entity.Candidate;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.repository.ApplicationRepository;
import com.recruit.recruitmentapplication.repository.CandidateRepository;
import com.recruit.recruitmentapplication.repository.JobPostingRepository;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ApplicationService {
    private static final long MAX_CV_SIZE = 5L * 1024L * 1024L;

    private final ApplicationRepository applicationRepository;
    private final CandidateService candidateService;
    private final CandidateRepository candidateRepository;
    private final JobPostingRepository jobPostingRepository;

    public ApplicationService(ApplicationRepository applicationRepository, CandidateService candidateService,
                              CandidateRepository candidateRepository, JobPostingRepository jobPostingRepository) {
        this.applicationRepository = applicationRepository;
        this.candidateService = candidateService;
        this.candidateRepository = candidateRepository;
        this.jobPostingRepository = jobPostingRepository;
    }

    @Transactional(readOnly = true)
    public boolean hasApplied(Long userId, Long jobId) {
        return candidateRepository.findByUser_Id(userId)
                .map(candidate -> applicationRepository.existsByCandidate_IdAndJobPosting_Id(candidate.getId(), jobId))
                .orElse(false);
    }

    @Transactional
    public Application submit(Long userId, Long jobId, ApplicationForm form) {
        Candidate candidate = candidateService.getOrCreateProfileForUser(userId);
        JobPosting jobPosting = jobPostingRepository.findByIdWithCompany(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job posting not found"));
        if (jobPosting.getStatus() != JobPosting.PostingStatus.OPEN) {
            throw new IllegalArgumentException("This position is no longer accepting applications.");
        }
        if (applicationRepository.existsByCandidate_IdAndJobPosting_Id(candidate.getId(), jobId)) {
            throw new IllegalArgumentException("You have already applied for this position.");
        }

        MultipartFile cv = validateCv(form.getCvFile());
        Application application = new Application(candidate, jobPosting, trimToNull(form.getCoverLetter()));
        application.setStatus(Application.ApplicationStatus.SUBMITTED);
        application.setCvFileName(cv.getOriginalFilename());
        application.setCvContentType(cv.getContentType());
        application.setCvFileSize(cv.getSize());
        try {
            application.setCvData(cv.getBytes());
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not read the uploaded CV file.");
        }
        return applicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public List<Application> findMine(Long userId, String status) {
        Application.ApplicationStatus selectedStatus = parseStatus(status);
        return applicationRepository.findByCandidateUserIdWithJobAndCompany(userId).stream()
                .filter(application -> selectedStatus == null || application.getStatus() == selectedStatus)
                .toList();
    }

    @Transactional
    public void withdraw(Long userId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        if (application.getCandidate().getUser() == null || !application.getCandidate().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Application not found");
        }
        if (!canWithdraw(application)) {
            throw new IllegalArgumentException("This application can no longer be withdrawn.");
        }
        application.setStatus(Application.ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);
    }

    public boolean canWithdraw(Application application) {
        return application.getStatus() == Application.ApplicationStatus.SUBMITTED
                || application.getStatus() == Application.ApplicationStatus.UNDER_REVIEW;
    }

    private MultipartFile validateCv(MultipartFile cv) {
        if (cv == null || cv.isEmpty()) {
            throw new IllegalArgumentException("Please upload your CV.");
        }
        if (cv.getSize() > MAX_CV_SIZE) {
            throw new IllegalArgumentException("CV file must be 5 MB or smaller.");
        }
        String filename = cv.getOriginalFilename() == null ? "" : cv.getOriginalFilename().toLowerCase();
        if (!filename.endsWith(".pdf") && !filename.endsWith(".docx")) {
            throw new IllegalArgumentException("CV file must be a PDF or DOCX file.");
        }
        return cv;
    }

    private Application.ApplicationStatus parseStatus(String status) {
        if (status == null || status.trim().isEmpty() || "ALL".equalsIgnoreCase(status.trim())) {
            return null;
        }
        try {
            return Application.ApplicationStatus.valueOf(status.trim());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String trimToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
