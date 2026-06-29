package com.recruit.recruitmentapplication.dto;

import com.recruit.recruitmentapplication.entity.Company;
import com.recruit.recruitmentapplication.entity.CompanyProfile;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class CompanyForm {
    @NotBlank(message = "Tên công ty không được để trống")
    @Size(max = 200, message = "Tên công ty tối đa 200 ký tự")
    private String name;

    @NotBlank(message = "Ngành nghề không được để trống")
    @Size(max = 100, message = "Ngành nghề tối đa 100 ký tự")
    private String industry;

    @NotBlank(message = "Website không được để trống")
    @Size(max = 200, message = "Website tối đa 200 ký tự")
    private String website;

    @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
    private String description;

    @Size(max = 200, message = "Trụ sở tối đa 200 ký tự")
    private String headquarters;

    @PositiveOrZero(message = "Số nhân viên không được âm")
    private Integer employeeCount;

    @Min(value = 1800, message = "Năm thành lập phải từ 1800")
    @Max(value = 2100, message = "Năm thành lập không hợp lệ")
    private Integer foundedYear;

    public CompanyForm() {
    }

    public static CompanyForm from(Company company) {
        CompanyForm form = new CompanyForm();
        form.setName(company.getName());
        form.setIndustry(company.getIndustry());
        form.setWebsite(company.getWebsite());
        CompanyProfile profile = company.getProfile();
        if (profile != null) {
            form.setDescription(profile.getDescription());
            form.setHeadquarters(profile.getHeadquarters());
            form.setEmployeeCount(profile.getEmployeeCount());
            form.setFoundedYear(profile.getFoundedYear());
        }
        return form;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getHeadquarters() { return headquarters; }
    public void setHeadquarters(String headquarters) { this.headquarters = headquarters; }
    public Integer getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(Integer employeeCount) { this.employeeCount = employeeCount; }
    public Integer getFoundedYear() { return foundedYear; }
    public void setFoundedYear(Integer foundedYear) { this.foundedYear = foundedYear; }
}
