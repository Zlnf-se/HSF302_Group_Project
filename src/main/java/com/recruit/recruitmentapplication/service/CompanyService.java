package com.recruit.recruitmentapplication.service;

import com.recruit.recruitmentapplication.dto.CompanyForm;
import com.recruit.recruitmentapplication.entity.Company;
import com.recruit.recruitmentapplication.entity.CompanyProfile;
import com.recruit.recruitmentapplication.repository.CompanyRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public List<Company> findAll() {
        return companyRepository.findAllWithProfile();
    }

    @Transactional(readOnly = true)
    public Company findById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> companyNotFound(id));
    }

    @Transactional(readOnly = true)
    public Company findByIdWithProfile(Long id) {
        return companyRepository.findByIdWithProfile(id)
                .orElseThrow(() -> companyNotFound(id));
    }

    @Transactional
    public Company create(CompanyForm form) {
        String name = form.getName().trim();
        if (companyRepository.existsByName(name)) {
            throw duplicateName();
        }

        Company company = new Company(name, form.getIndustry().trim(), form.getWebsite().trim());
        company.setProfile(new CompanyProfile(
                trimToNull(form.getDescription()),
                trimToNull(form.getHeadquarters()),
                form.getEmployeeCount(),
                form.getFoundedYear()
        ));
        return companyRepository.save(company);
    }

    @Transactional
    public Company update(Long id, CompanyForm form) {
        Company company = findByIdWithProfile(id);
        String name = form.getName().trim();
        companyRepository.findByName(name)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> { throw duplicateName(); });

        company.setName(name);
        company.setIndustry(form.getIndustry().trim());
        company.setWebsite(form.getWebsite().trim());

        CompanyProfile profile = company.getProfile();
        if (profile == null) {
            profile = new CompanyProfile();
            company.setProfile(profile);
        }
        profile.setDescription(trimToNull(form.getDescription()));
        profile.setHeadquarters(trimToNull(form.getHeadquarters()));
        profile.setEmployeeCount(form.getEmployeeCount());
        profile.setFoundedYear(form.getFoundedYear());
        return companyRepository.save(company);
    }

    @Transactional
    public void delete(Long id) {
        companyRepository.delete(findById(id));
    }

    private IllegalArgumentException companyNotFound(Long id) {
        return new IllegalArgumentException("Không tìm thấy công ty id=" + id);
    }

    private IllegalArgumentException duplicateName() {
        return new IllegalArgumentException("Tên công ty đã tồn tại");
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
