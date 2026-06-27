package com.recruit.recruitmentapplication.dto;

import com.recruit.recruitmentapplication.entity.JobPosting;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class JobPostingForm {
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 200, message = "Tiêu đề tối đa 200 ký tự")
    private String title;
    @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
    private String description;
    @Size(max = 100, message = "Địa điểm tối đa 100 ký tự")
    private String location;
    @NotBlank(message = "Vui lòng chọn loại việc")
    private String jobType;
    @DecimalMin(value = "0", message = "Lương tối thiểu không được âm")
    private BigDecimal salaryMin;
    @DecimalMin(value = "0", message = "Lương tối đa không được âm")
    private BigDecimal salaryMax;
    private LocalDate deadline;
    @NotNull(message = "Vui lòng chọn công ty")
    private Long companyId;
    private Set<Long> skillIds = new HashSet<>();

    public JobPostingForm() {}

    public static JobPostingForm from(JobPosting posting) {
        JobPostingForm form = new JobPostingForm();
        form.setTitle(posting.getTitle());
        form.setDescription(posting.getDescription());
        form.setLocation(posting.getLocation());
        form.setJobType(posting.getJobType() == null ? null : posting.getJobType().name());
        form.setSalaryMin(posting.getSalaryMin());
        form.setSalaryMax(posting.getSalaryMax());
        form.setDeadline(posting.getDeadline());
        form.setCompanyId(posting.getCompany() == null ? null : posting.getCompany().getId());
        form.setSkillIds(posting.getRequiredSkills().stream().map(skill -> skill.getId()).collect(Collectors.toSet()));
        return form;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }
    public BigDecimal getSalaryMin() { return salaryMin; }
    public void setSalaryMin(BigDecimal salaryMin) { this.salaryMin = salaryMin; }
    public BigDecimal getSalaryMax() { return salaryMax; }
    public void setSalaryMax(BigDecimal salaryMax) { this.salaryMax = salaryMax; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public Set<Long> getSkillIds() { return skillIds; }
    public void setSkillIds(Set<Long> skillIds) { this.skillIds = skillIds == null ? new HashSet<>() : skillIds; }
}
