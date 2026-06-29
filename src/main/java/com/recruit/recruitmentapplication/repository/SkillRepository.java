package com.recruit.recruitmentapplication.repository;

import com.recruit.recruitmentapplication.entity.Skill;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByName(String name);

    boolean existsByName(String name);

    long countByName(String name);

    List<Skill> findByCategory(String category);
}
