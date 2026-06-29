package com.recruit.recruitmentapplication.service;

import com.recruit.recruitmentapplication.dto.JobPostingForm;
import com.recruit.recruitmentapplication.dto.PipelineReportDto;
import com.recruit.recruitmentapplication.entity.Application.ApplicationStatus;
import com.recruit.recruitmentapplication.entity.Company;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.entity.JobPosting.PostingStatus;
import com.recruit.recruitmentapplication.repository.ApplicationRepository;
import com.recruit.recruitmentapplication.repository.CompanyRepository;
import com.recruit.recruitmentapplication.repository.JobPostingRepository;
import com.recruit.recruitmentapplication.repository.SkillRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobPostingService {
    private final JobPostingRepository jobPostingRepository;
    private final CompanyRepository companyRepository;
    private final SkillRepository skillRepository;
    private final ApplicationRepository applicationRepository;

    public JobPostingService(JobPostingRepository jobPostingRepository,
                             CompanyRepository companyRepository,
                             SkillRepository skillRepository,
                             ApplicationRepository applicationRepository) {
        this.jobPostingRepository = jobPostingRepository;
        this.companyRepository = companyRepository;
        this.skillRepository = skillRepository;
        this.applicationRepository = applicationRepository;
    }


    @Transactional(readOnly = true)
    public List<JobPosting> findOpenJobs(String keyword) {
        return keyword == null || keyword.trim().isEmpty()
                ? jobPostingRepository.findOpenJobsWithCompany()
                : jobPostingRepository.findOpenJobsByTitle(keyword.trim());
    }


    @Transactional(readOnly = true)
    public List<JobPosting> findAllForHR(String keyword, String statusFilter) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return jobPostingRepository.findByTitleContainingWithCompany(keyword.trim());
        }
        if (statusFilter != null && !statusFilter.isEmpty()) {
            try {
                PostingStatus status = PostingStatus.valueOf(statusFilter);
                return jobPostingRepository.findByStatusWithCompany(status);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return jobPostingRepository.findAllWithCompany();
    }

    @Transactional(readOnly = true)
    public Map<PostingStatus, Long> countByStatus() {
        Map<PostingStatus, Long> map = new LinkedHashMap<>();
        for (PostingStatus s : PostingStatus.values()) map.put(s, 0L);
        for (PostingStatus s : PostingStatus.values()) {
            map.put(s, jobPostingRepository.countByStatus(s));
        }
        return map;
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
                form.getTitle().trim(),
                trimToNull(form.getDepartment()),
                trimToNull(form.getDescription()),
                trimToNull(form.getLocation()),
                parseJobType(form.getJobType()),
                form.getSalaryMin(),
                form.getSalaryMax(),
                form.getDeadline());

        posting.setRequirements(trimToNull(form.getRequirements()));

        if (form.getStatus() != null && !form.getStatus().isEmpty()) {
            try { posting.setStatus(PostingStatus.valueOf(form.getStatus())); }
            catch (IllegalArgumentException ignored) {}
        }
        company.addJobPosting(posting);
        replaceSkills(posting, form);
        return jobPostingRepository.save(posting);
    }

    @Transactional
    public JobPosting update(Long id, JobPostingForm form) {
        validateSalary(form.getSalaryMin(), form.getSalaryMax());
        JobPosting posting = findByIdRaw(id);
        Company selectedCompany = findCompany(form.getCompanyId());

        if (!posting.getCompany().getId().equals(selectedCompany.getId())) {
            posting.getCompany().removeJobPosting(posting);
            selectedCompany.addJobPosting(posting);
        }

        posting.setTitle(form.getTitle().trim());
        posting.setDepartment(trimToNull(form.getDepartment()));
        posting.setDescription(trimToNull(form.getDescription()));
        posting.setRequirements(trimToNull(form.getRequirements()));
        posting.setLocation(trimToNull(form.getLocation()));
        posting.setJobType(parseJobType(form.getJobType()));
        posting.setSalaryMin(form.getSalaryMin());
        posting.setSalaryMax(form.getSalaryMax());
        posting.setDeadline(form.getDeadline());

        if (form.getStatus() != null && !form.getStatus().isEmpty()) {
            try { posting.setStatus(PostingStatus.valueOf(form.getStatus())); }
            catch (IllegalArgumentException ignored) {}
        }
        replaceSkills(posting, form);
        return jobPostingRepository.save(posting);
    }


    @Transactional(readOnly = true)
    public JobPosting findById(Long id) {
        return jobPostingRepository.findByIdWithCompany(id)
                .orElseThrow(() -> notFound(id));
    }

    @Transactional(readOnly = true)
    public long countApplications(Long jobId) {
        return applicationRepository.countByJobPosting_Id(jobId);
    }

    @Transactional(readOnly = true)
    public PipelineReportDto getPipelineForJob(Long jobId) {
        JobPosting job = findById(jobId);
        PipelineReportDto dto = new PipelineReportDto(job.getId(), job.getTitle());
        List<Object[]> rows = applicationRepository.countByStatusForJob(jobId);
        for (Object[] row : rows) {
            ApplicationStatus status = (ApplicationStatus) row[0];
            long count = ((Number) row[1]).longValue();
            dto.addCount(status, count);
        }
        return dto;
    }

    @Transactional
    public JobPosting close(Long id) {
        JobPosting posting = findByIdRaw(id);
        posting.setStatus(PostingStatus.CLOSED);
        return jobPostingRepository.save(posting);
    }

    @Transactional
    public JobPosting reopen(Long id) {
        JobPosting posting = findByIdRaw(id);
        posting.setStatus(PostingStatus.ACTIVE);
        return jobPostingRepository.save(posting);
    }

    @Transactional
    public void delete(Long id) {
        jobPostingRepository.delete(findByIdRaw(id));
    }



    @Transactional(readOnly = true)
    public List<PipelineReportDto> getPipelineReportAll() {
        List<Object[]> rows = applicationRepository.pipelineSummaryAll();
        Map<Long, PipelineReportDto> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Long jobId = ((Number) row[0]).longValue();
            String jobTitle = (String) row[1];
            ApplicationStatus status = (ApplicationStatus) row[2];
            long count = ((Number) row[3]).longValue();
            map.computeIfAbsent(jobId, k -> new PipelineReportDto(k, jobTitle))
                    .addCount(status, count);
        }
        List<PipelineReportDto> result = new ArrayList<>(map.values());
        result.sort((a, b) -> Integer.compare(b.getTotal(), a.getTotal()));
        return result;
    }


    @Transactional(readOnly = true)
    public PipelineReportDto getGlobalPipeline() {
        PipelineReportDto dto = new PipelineReportDto(null, "Toàn hệ thống");
        List<Object[]> rows = applicationRepository.countByStatusGlobal();
        for (Object[] row : rows) {
            ApplicationStatus status = (ApplicationStatus) row[0];
            long count = ((Number) row[1]).longValue();
            dto.addCount(status, count);
        }
        return dto;
    }


    @Transactional(readOnly = true)
    public List<JobPosting> findByCompany(Long companyId) {
        return jobPostingRepository.findByCompany_Id(companyId);
    }


    private JobPosting findByIdRaw(Long id) {
        return jobPostingRepository.findByIdWithCompany(id)
                .orElseThrow(() -> notFound(id));
    }

    private Company findCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy công ty id=" + id));
    }

    private void replaceSkills(JobPosting posting, JobPostingForm form) {
        posting.clearRequiredSkills();
        if (form.getSkillIds() != null) {
            for (Long skillId : form.getSkillIds()) {
                posting.addRequiredSkill(skillRepository.findById(skillId)
                        .orElseThrow(() -> new IllegalArgumentException("Skill không hợp lệ")));
            }
        }
    }

    private JobPosting.JobType parseJobType(String value) {
        try {
            return JobPosting.JobType.valueOf(value);
        } catch (RuntimeException e) {
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
