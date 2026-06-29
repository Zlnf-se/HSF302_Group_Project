package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.JobPostingForm;
import com.recruit.recruitmentapplication.dto.PipelineReportDto;
import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.repository.ApplicationRepository;
import com.recruit.recruitmentapplication.service.CompanyService;
import com.recruit.recruitmentapplication.service.JobPostingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/jobs")
public class JobPostingController {

    private final JobPostingService jobPostingService;
    private final CompanyService companyService;
    private final ApplicationRepository applicationRepository;

    public JobPostingController(JobPostingService jobPostingService,
                                CompanyService companyService,
                                ApplicationRepository applicationRepository) {
        this.jobPostingService = jobPostingService;
        this.companyService = companyService;
        this.applicationRepository = applicationRepository;
    }


    @GetMapping
    public String list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String statusFilter,
            @RequestParam(defaultValue = "false") boolean hrView,
            Model model) {

        List<JobPosting> jobs;
        if (hrView) {
            jobs = jobPostingService.findAllForHR(keyword, statusFilter);
            model.addAttribute("hrView", true);
            model.addAttribute("statusFilter", statusFilter);
            model.addAttribute("statusCounts", jobPostingService.countByStatus());
            model.addAttribute("allStatuses", JobPosting.PostingStatus.values());
        } else {
            jobs = jobPostingService.findOpenJobs(keyword);
            model.addAttribute("hrView", false);
        }

        model.addAttribute("jobs", jobs);
        model.addAttribute("keyword", keyword);
        return "jobposting/list";
    }


    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        JobPosting job = jobPostingService.findById(id);
        long appCount = jobPostingService.countApplications(id);

        List<Object[]> statusRows = applicationRepository.countByStatusForJob(id);
        java.util.Map<Application.ApplicationStatus, Long> statusMap = new java.util.LinkedHashMap<>();
        for (Application.ApplicationStatus s : Application.ApplicationStatus.values()) statusMap.put(s, 0L);
        for (Object[] row : statusRows) {
            statusMap.put((Application.ApplicationStatus) row[0], ((Number) row[1]).longValue());
        }

        model.addAttribute("job", job);
        model.addAttribute("appCount", appCount);
        model.addAttribute("statusMap", statusMap);
        model.addAttribute("applicationStatuses", Application.ApplicationStatus.values());
        return "jobposting/detail";
    }


    @GetMapping("/new")
    public String showCreateForm(Model model) {
        prepareForm(model, new JobPostingForm(), false, null);
        return "jobposting/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("jobPostingForm") JobPostingForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes flash) {
        if (result.hasErrors()) return formView(model, form, result, false, null);
        try {
            JobPosting created = jobPostingService.create(form);
            flash.addFlashAttribute("successMessage", "Đăng tin tuyển dụng thành công!");
            return "redirect:/jobs/" + created.getId();
        } catch (IllegalArgumentException ex) {
            result.reject("jobPostingForm.error", ex.getMessage());
            return formView(model, form, result, false, null);
        }
    }


    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        JobPosting job = jobPostingService.findById(id);
        prepareForm(model, JobPostingForm.from(job), true, id);
        return "jobposting/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("jobPostingForm") JobPostingForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes flash) {
        if (result.hasErrors()) return formView(model, form, result, true, id);
        try {
            jobPostingService.update(id, form);
            flash.addFlashAttribute("successMessage", "Cập nhật tin tuyển dụng thành công!");
            return "redirect:/jobs/" + id;
        } catch (IllegalArgumentException ex) {
            result.reject("jobPostingForm.error", ex.getMessage());
            return formView(model, form, result, true, id);
        }
    }


    @PostMapping("/{id}/close")
    public String close(@PathVariable Long id, RedirectAttributes flash) {
        jobPostingService.close(id);
        flash.addFlashAttribute("successMessage", "Đã đóng tin tuyển dụng.");
        return "redirect:/jobs/" + id;
    }

    @PostMapping("/{id}/reopen")
    public String reopen(@PathVariable Long id, RedirectAttributes flash) {
        jobPostingService.reopen(id);
        flash.addFlashAttribute("successMessage", "Đã mở lại tin tuyển dụng.");
        return "redirect:/jobs/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes flash) {
        jobPostingService.delete(id);
        flash.addFlashAttribute("successMessage", "Đã xóa tin tuyển dụng.");
        return "redirect:/jobs?hrView=true";
    }


    @GetMapping("/pipeline-report")
    public String pipelineReport(Model model) {
        List<PipelineReportDto> reports = jobPostingService.getPipelineReportAll();
        PipelineReportDto global = jobPostingService.getGlobalPipeline();

        model.addAttribute("reports", reports);
        model.addAttribute("global", global);
        model.addAttribute("allStatuses", Application.ApplicationStatus.values());
        return "jobposting/pipeline-report";
    }

    @GetMapping("/{id}/pipeline-report")
    public String pipelineReportForJob(@PathVariable Long id, Model model) {
        PipelineReportDto report = jobPostingService.getPipelineForJob(id);
        JobPosting job = jobPostingService.findById(id);
        List<Application> applications = applicationRepository.findByJobWithCandidate(id);

        model.addAttribute("report", report);
        model.addAttribute("job", job);
        model.addAttribute("applications", applications);
        model.addAttribute("allStatuses", Application.ApplicationStatus.values());
        return "jobposting/pipeline-report-single";
    }


    private String formView(Model model, JobPostingForm form, BindingResult result, boolean edit, Long id) {
        prepareForm(model, form, edit, id);
        return "jobposting/form";
    }

    private void prepareForm(Model model, JobPostingForm form, boolean edit, Long id) {
        model.addAttribute("jobPostingForm", form);
        model.addAttribute("companies", companyService.findAll());
        model.addAttribute("jobTypes", JobPosting.JobType.values());
        model.addAttribute("postingStatuses", JobPosting.PostingStatus.values());
        model.addAttribute("skills", jobPostingService.findAllSkills());
        model.addAttribute("editMode", edit);
        model.addAttribute("jobId", id);
    }
}
