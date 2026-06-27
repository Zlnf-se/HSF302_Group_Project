package com.recruit.recruitmentapplication.repository;

import com.recruit.recruitmentapplication.entity.Company;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByName(String name);

    boolean existsByName(String name);

    List<Company> findByIndustry(String industry);

    @Query("SELECT c FROM Company c LEFT JOIN FETCH c.profile WHERE c.id = :id")
    Optional<Company> findByIdWithProfile(@Param("id") Long id);

    @Query("SELECT c FROM Company c LEFT JOIN FETCH c.profile ORDER BY c.id")
    List<Company> findAllWithProfile();

    @Query("SELECT c FROM Company c LEFT JOIN FETCH c.jobPostings WHERE c.id = :id")
    Optional<Company> findByIdWithPostings(@Param("id") Long id);

    @Query("SELECT c FROM Company c WHERE SIZE(c.jobPostings) > 0")
    List<Company> findCompaniesWithOpenPositions();
}
