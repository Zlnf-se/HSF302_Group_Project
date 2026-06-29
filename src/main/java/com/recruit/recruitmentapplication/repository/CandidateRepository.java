package com.recruit.recruitmentapplication.repository;

import com.recruit.recruitmentapplication.entity.Candidate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    Optional<Candidate> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Candidate> findByUser_Id(Long userId);

    List<Candidate> findByNameContainingIgnoreCase(String name);

    @Query("SELECT DISTINCT c FROM Candidate c JOIN c.skills s WHERE s.name = :skillName")
    List<Candidate> findBySkillName(@Param("skillName") String skillName);

    @Query("SELECT c FROM Candidate c WHERE c.profile.yearsOfExperience >= :years")
    List<Candidate> findByMinExperience(@Param("years") int years);

    @Query("SELECT DISTINCT c FROM Candidate c LEFT JOIN FETCH c.profile LEFT JOIN FETCH c.skills WHERE c.id = :id")
    Optional<Candidate> findByIdWithSkills(@Param("id") Long id);

    @Query("SELECT DISTINCT c FROM Candidate c LEFT JOIN FETCH c.profile LEFT JOIN FETCH c.skills WHERE c.user.id = :userId")
    Optional<Candidate> findByUserIdWithProfileAndSkills(@Param("userId") Long userId);

    @Query("SELECT DISTINCT c FROM Candidate c LEFT JOIN FETCH c.profile LEFT JOIN FETCH c.skills ORDER BY c.id")
    List<Candidate> findAllWithSkills();

    @Query("SELECT c FROM Candidate c LEFT JOIN FETCH c.applications WHERE c.id = :id")
    Optional<Candidate> findByIdWithApplications(@Param("id") Long id);

    @Query(value = """
            SELECT c.* FROM candidates c
            JOIN (
                SELECT a.candidate_id, COUNT(a.id) AS application_count
                FROM applications a
                GROUP BY a.candidate_id
                ORDER BY application_count DESC
                OFFSET 0 ROWS FETCH NEXT :limit ROWS ONLY
            ) ranked ON c.id = ranked.candidate_id
            ORDER BY ranked.application_count DESC
            """, nativeQuery = true)
    List<Candidate> findMostActiveCandidates(@Param("limit") int limit);
}
