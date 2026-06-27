package com.recruit.recruitmentapplication.repository;

import com.recruit.recruitmentapplication.entity.Interview;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByApplication_Id(Long applicationId);

    List<Interview> findByResult(Interview.InterviewResult result);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Interview i SET i.result = :result, i.notes = :notes WHERE i.id = :id")
    int recordResult(
            @Param("id") Long id,
            @Param("result") Interview.InterviewResult result,
            @Param("notes") String notes
    );
}
