package com.recruit.recruitmentapplication.service;

import com.recruit.recruitmentapplication.dto.JobPostingForm;
import com.recruit.recruitmentapplication.entity.Company;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.repository.CompanyRepository;
import com.recruit.recruitmentapplication.repository.JobPostingRepository;
import com.recruit.recruitmentapplication.repository.SkillRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobPostingService {
    private final JobPostingRepository jobPostingRepository;
    private final CompanyRepository companyRepository;
    private final SkillRepository skillRepository;

    public JobPostingService(JobPostingRepository jobPostingRepository, CompanyRepository companyRepository,
                             SkillRepository skillRepository) {
        this.jobPostingRepository = jobPostingRepository;
        this.companyRepository = companyRepository;
        this.skillRepository = skillRepository;
    }

    @Transactional(readOnly = true)
    public List<JobPosting> findOpenJobs(String keyword) {
        return keyword == null || keyword.trim().isEmpty()
                ? jobPostingRepository.findOpenJobsWithCompany()
                : jobPostingRepository.findOpenJobsByTitle(keyword.trim());
    }

    @Transactional(readOnly = true)
    public JobPosting findById(Long id) {
        return jobPostingRepository.findByIdWithCompany(id)
                .orElseThrow(() -> notFound(id));
    }

    @Transactional(readOnly = true)
    public List<JobPosting> findByCompany(Long companyId) {
        return jobPostingRepository.findByCompany_Id(companyId);
    }

    @Transactional(readOnly = true)
    public List<com.recruit.recruitmentapplication.entity.Skill> findAllSkills() {
        return skillRepository.findAll();
    }

    @Transactional
    public JobPosting create(JobPostingForm form) {
        validateSalary(form.getSalaryMin(), form.getSalaryMax());
        Company company = findCompany(form.getCompanyId());
        JobPosting posting = new JobPosting(
                form.getTitle().trim(), trimToNull(form.getDescription()), trimToNull(form.getLocation()),
                parseJobType(form.getJobType()), form.getSalaryMin(), form.getSalaryMax(), form.getDeadline());
        company.addJobPosting(posting);
        replaceSkills(posting, form);
        return jobPostingRepository.save(posting);
    }

    @Transactional
    public JobPosting update(Long id, JobPostingForm form) {
        validateSalary(form.getSalaryMin(), form.getSalaryMax());
        JobPosting posting = findById(id);
        Company selectedCompany = findCompany(form.getCompanyId());
        if (!posting.getCompany().getId().equals(selectedCompany.getId())) {
            posting.getCompany().removeJobPosting(posting);
            selectedCompany.addJobPosting(posting);
        }
        posting.setTitle(form.getTitle().trim());
        posting.setDescription(trimToNull(form.getDescription()));
        posting.setLocation(trimToNull(form.getLocation()));
        posting.setJobType(parseJobType(form.getJobType()));
        posting.setSalaryMin(form.getSalaryMin());
        posting.setSalaryMax(form.getSalaryMax());
        posting.setDeadline(form.getDeadline());
        replaceSkills(posting, form);
        return jobPostingRepository.save(posting);
    }

    @Transactional
    public JobPosting close(Long id) {
        JobPosting posting = findById(id);
        posting.setStatus(JobPosting.PostingStatus.CLOSED);
        return jobPostingRepository.save(posting);
    }

    @Transactional
    public void delete(Long id) {
        jobPostingRepository.delete(findById(id));
    }

    private Company findCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy công ty id=" + id));
    }

    private void replaceSkills(JobPosting posting, JobPostingForm form) {
        posting.clearRequiredSkills();
        for (Long skillId : form.getSkillIds()) {
            posting.addRequiredSkill(skillRepository.findById(skillId)
                    .orElseThrow(() -> new IllegalArgumentException("Skill không hợp lệ")));
        }
    }

    private JobPosting.JobType parseJobType(String value) {
        try {
            return JobPosting.JobType.valueOf(value);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Loại việc không hợp lệ");
        }
    }

    private void validateSalary(BigDecimal min, BigDecimal max) {
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new IllegalArgumentException("Lương tối thiểu không được lớn hơn lương tối đa");
        }
    }

    private String trimToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private IllegalArgumentException notFound(Long id) {
        return new IllegalArgumentException("Không tìm thấy tin tuyển dụng id=" + id);
    }
}
