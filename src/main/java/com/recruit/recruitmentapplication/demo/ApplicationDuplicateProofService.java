package com.recruit.recruitmentapplication.demo;

import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.entity.Candidate;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.repository.ApplicationRepository;
import com.recruit.recruitmentapplication.repository.CandidateRepository;
import com.recruit.recruitmentapplication.repository.JobPostingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationDuplicateProofService {
    private final ApplicationRepository applicationRepository;
    private final CandidateRepository candidateRepository;
    private final JobPostingRepository jobPostingRepository;

    public ApplicationDuplicateProofService(ApplicationRepository applicationRepository,
                                            CandidateRepository candidateRepository,
                                            JobPostingRepository jobPostingRepository) {
        this.applicationRepository = applicationRepository;
        this.candidateRepository = candidateRepository;
        this.jobPostingRepository = jobPostingRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void tryCreateDuplicateApplication(Long candidateId, Long jobPostingId) {
        Candidate candidate = candidateRepository.getReferenceById(candidateId);
        JobPosting jobPosting = jobPostingRepository.getReferenceById(jobPostingId);

        Application duplicateApplication = new Application(
                candidate,
                jobPosting,
                "Duplicate application for unique constraint proof"
        );

        applicationRepository.saveAndFlush(duplicateApplication);
    }
}
