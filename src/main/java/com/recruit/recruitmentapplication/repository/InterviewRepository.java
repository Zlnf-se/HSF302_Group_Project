package com.recruit.recruitmentapplication.repository;

import com.recruit.recruitmentapplication.entity.Interview;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByApplication_IdOrderByScheduledAtDesc(Long applicationId);

    boolean existsByApplication_IdAndInterviewer_Id(Long applicationId, Long interviewerId);

    @Query("""
            SELECT i FROM Interview i
            JOIN FETCH i.application a
            JOIN FETCH a.candidate
            JOIN FETCH a.jobPosting jp
            JOIN FETCH jp.company
            JOIN FETCH i.interviewer
            WHERE i.id = :id
            """)
    Optional<Interview> findByIdWithDetail(@Param("id") Long id);
}
