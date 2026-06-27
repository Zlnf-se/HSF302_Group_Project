package com.recruit.recruitmentapplication.repository;

import com.recruit.recruitmentapplication.entity.Application;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByCandidate_Id(Long candidateId);

    List<Application> findByJobPosting_Id(Long jobPostingId);

    Optional<Application> findByCandidate_IdAndJobPosting_Id(Long candidateId, Long jobPostingId);

    List<Application> findByStatus(Application.ApplicationStatus status);

    boolean existsByCandidate_IdAndJobPosting_Id(Long candidateId, Long jobPostingId);

    long countByCandidate_IdAndJobPosting_Id(Long candidateId, Long jobPostingId);

    long countByJobPosting_Id(Long jobPostingId);

    @Query("SELECT a FROM Application a LEFT JOIN FETCH a.interviews WHERE a.id = :id")
    Optional<Application> findByIdWithInterviews(@Param("id") Long id);

    @Query("""
            SELECT a FROM Application a
            JOIN FETCH a.candidate
            JOIN FETCH a.jobPosting jp
            JOIN FETCH jp.company
            ORDER BY a.id
            """)
    List<Application> findAllWithCandidateJobAndCompany();

    @Query("""
            SELECT a FROM Application a
            JOIN FETCH a.candidate
            JOIN FETCH a.jobPosting jp
            JOIN FETCH jp.company
            WHERE a.candidate.email = :email AND jp.title = :jobTitle
            """)
    Optional<Application> findByCandidateEmailAndJobTitleWithDetails(
            @Param("email") String email,
            @Param("jobTitle") String jobTitle
    );

    @Query("""
            SELECT a FROM Application a
            JOIN FETCH a.candidate
            WHERE a.jobPosting.id = :jobId
            ORDER BY a.appliedAt DESC
            """)
    List<Application> findByJobWithCandidate(@Param("jobId") Long jobId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Application a SET a.status = :status WHERE a.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") Application.ApplicationStatus status);
}
