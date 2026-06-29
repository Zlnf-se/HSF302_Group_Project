package com.recruit.recruitmentapplication.repository;

import com.recruit.recruitmentapplication.entity.InternalNote;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InternalNoteRepository extends JpaRepository<InternalNote, Long> {
    @Query("""
            SELECT n FROM InternalNote n
            LEFT JOIN FETCH n.author
            WHERE n.application.id = :applicationId
            ORDER BY n.createdAt DESC
            """)
    List<InternalNote> findByApplicationIdNewestFirst(@Param("applicationId") Long applicationId);
}
