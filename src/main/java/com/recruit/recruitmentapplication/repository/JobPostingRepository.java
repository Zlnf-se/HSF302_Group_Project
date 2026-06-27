package com.recruit.recruitmentapplication.repository;

import com.recruit.recruitmentapplication.entity.JobPosting;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    List<JobPosting> findByStatus(JobPosting.PostingStatus status);

    List<JobPosting> findByJobType(JobPosting.JobType jobType);

    List<JobPosting> findByTitleContainingIgnoreCase(String keyword);

    @Query("SELECT jp FROM JobPosting jp JOIN FETCH jp.company "
            + "WHERE jp.status = 'OPEN' AND LOWER(jp.title) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "ORDER BY jp.postedDate DESC")
    List<JobPosting> findOpenJobsByTitle(@Param("keyword") String keyword);

    List<JobPosting> findByLocationContainingIgnoreCase(String location);

    List<JobPosting> findByCompany_Id(Long companyId);

    List<JobPosting> findByCompany_Name(String companyName);

    List<JobPosting> findBySalaryMinGreaterThanEqual(BigDecimal minSalary);

    List<JobPosting> findByDeadlineAfter(LocalDate date);

    List<JobPosting> findByStatusAndJobType(JobPosting.PostingStatus status, JobPosting.JobType type);

    long countByStatus(JobPosting.PostingStatus status);

    long countByCompany_Id(Long companyId);

    boolean existsByTitleAndCompany_Name(String title, String companyName);

    boolean existsByTitleAndCompany_Id(String title, Long companyId);

    Optional<JobPosting> findByTitleAndCompany_Name(String title, String companyName);

    @Query("SELECT jp FROM JobPosting jp JOIN FETCH jp.company WHERE jp.status = 'OPEN' ORDER BY jp.postedDate DESC")
    List<JobPosting> findOpenJobsWithCompany();

    @Query("SELECT jp FROM JobPosting jp JOIN FETCH jp.company WHERE jp.id = :id")
    Optional<JobPosting> findByIdWithCompany(@Param("id") Long id);

    @Query("SELECT DISTINCT jp FROM JobPosting jp LEFT JOIN FETCH jp.requiredSkills ORDER BY jp.id")
    List<JobPosting> findAllWithRequiredSkills();

    @Query("SELECT DISTINCT jp FROM JobPosting jp JOIN jp.requiredSkills s WHERE s.name = :skillName")
    List<JobPosting> findByRequiredSkillName(@Param("skillName") String skillName);

    @Query("""
            SELECT DISTINCT jp FROM JobPosting jp
            JOIN jp.requiredSkills s
            WHERE s.name = :skillName AND jp.status = 'OPEN'
            """)
    List<JobPosting> findOpenJobsBySkill(@Param("skillName") String skillName);

    @Query("""
            SELECT jp FROM JobPosting jp
            WHERE jp.salaryMin >= :min AND jp.salaryMax <= :max AND jp.status = 'OPEN'
            """)
    List<JobPosting> findByOpenSalaryRange(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

    @Query(value = """
            SELECT jp.* FROM job_postings jp
            JOIN (
                SELECT a.job_posting_id, COUNT(a.id) AS application_count
                FROM applications a
                GROUP BY a.job_posting_id
                ORDER BY application_count DESC
                OFFSET 0 ROWS FETCH NEXT :limit ROWS ONLY
            ) ranked ON jp.id = ranked.job_posting_id
            ORDER BY ranked.application_count DESC
            """, nativeQuery = true)
    List<JobPosting> findMostAppliedJobs(@Param("limit") int limit);

    @Query(value = """
            SELECT jp.* FROM job_postings jp
            WHERE jp.posted_date >= DATEADD(day, (:days * -1), CAST(CURRENT_TIMESTAMP AS date))
              AND jp.status = 'OPEN'
            """, nativeQuery = true)
    List<JobPosting> findRecentOpenJobs(@Param("days") int days);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE JobPosting jp SET jp.status = 'CLOSED' WHERE jp.deadline < :today AND jp.status = 'OPEN'")
    int closeExpiredPostings(@Param("today") LocalDate today);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE JobPosting jp SET jp.status = :status WHERE jp.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") JobPosting.PostingStatus status);
}
