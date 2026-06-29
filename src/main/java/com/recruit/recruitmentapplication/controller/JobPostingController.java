package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.JobPostingForm;
import com.recruit.recruitmentapplication.dto.PipelineReportDto;
import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.entity.JobPosting.PostingStatus;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.repository.ApplicationRepository;
import com.recruit.recruitmentapplication.service.CompanyService;
import com.recruit.recruitmentapplication.service.JobPostingService;
import com.recruit.recruitmentapplication.util.SessionConstants;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
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

    // SCR-10 Job List (HR Manager / Admin) — also serves the public listing for other roles.
    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "") String status,
                       @RequestParam(defaultValue = "") String department,
                       HttpSession session,
                       Model model) {
        SessionUser current = current(session);
        if (!isStaff(current)) {
            model.addAttribute("hrView", false);
            model.addAttribute("jobs", jobPostingService.findOpenJobs(keyword));
            model.addAttribute("keyword", keyword);
            return "jobposting/list";
        }

        // Owner-scoped for HR Manager, everything for Admin.
        List<JobPosting> base = jobPostingService.findManaged(current.getId(), isAdmin(current));

        List<String> departments = base.stream()
                .map(JobPosting::getDepartment)
                .filter(d -> d != null && !d.isBlank())
                .distinct().sorted().toList();

        // Counts per status (over the full managed set, independent of the active filter).
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (PostingStatus s : PostingStatus.values()) {
            statusCounts.put(s.name(), base.stream().filter(j -> j.getStatus() == s).count());
        }

        PostingStatus statusFilter = parseStatus(status);
        String dept = department.isBlank() ? null : department;
        String kw = keyword.isBlank() ? null : keyword.toLowerCase();
        List<JobPosting> jobs = base.stream()
                .filter(j -> statusFilter == null || j.getStatus() == statusFilter)
                .filter(j -> dept == null || dept.equals(j.getDepartment()))
                .filter(j -> kw == null || (j.getTitle() != null && j.getTitle().toLowerCase().contains(kw)))
                .toList();

        model.addAttribute("hrView", true);
        model.addAttribute("jobs", jobs);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("department", department);
        model.addAttribute("departments", departments);
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("totalCount", (long) base.size());
        model.addAttribute("statuses", PostingStatus.values());
        return "jobposting/list";
    }

    // SCR-12 Job Detail
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        JobPosting job = jobPostingService.findById(id);
        if (!canManage(job, current(session))) {
            return "redirect:/error/403";
        }
        long appCount = jobPostingService.countApplications(id);

        Map<Application.ApplicationStatus, Long> statusMap = new LinkedHashMap<>();
        for (Application.ApplicationStatus s : Application.ApplicationStatus.values()) statusMap.put(s, 0L);
        for (Object[] row : applicationRepository.countByStatusForJob(id)) {
            statusMap.put((Application.ApplicationStatus) row[0], ((Number) row[1]).longValue());
        }

        model.addAttribute("job", job);
        model.addAttribute("appCount", appCount);
        model.addAttribute("statusMap", statusMap);
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
                         @RequestParam(required = false) String action,
                         HttpSession session,
                         Model model,
                         RedirectAttributes flash) {
        if (result.hasErrors()) return formView(model, form, false, null);
        try {
            JobPosting created = jobPostingService.create(form, action, current(session).getId());
            flash.addFlashAttribute("successMessage",
                    "PUBLISH".equalsIgnoreCase(action) ? "Đã đăng tin tuyển dụng!" : "Đã lưu tin nháp.");
            return "redirect:/jobs/" + created.getId();
        } catch (IllegalArgumentException ex) {
            result.reject("jobPostingForm.error", ex.getMessage());
            return formView(model, form, false, null);
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, HttpSession session, Model model) {
        JobPosting job = jobPostingService.findById(id);
        if (!canManage(job, current(session))) {
            return "redirect:/error/403";
        }
        prepareForm(model, JobPostingForm.from(job), true, id);
        return "jobposting/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("jobPostingForm") JobPostingForm form,
                         BindingResult result,
                         @RequestParam(required = false) String action,
                         HttpSession session,
                         Model model,
                         RedirectAttributes flash) {
        if (!canManage(jobPostingService.findById(id), current(session))) {
            return "redirect:/error/403";
        }
        if (result.hasErrors()) return formView(model, form, true, id);
        try {
            jobPostingService.update(id, form, action);
            flash.addFlashAttribute("successMessage", "Cập nhật tin tuyển dụng thành công!");
            return "redirect:/jobs/" + id;
        } catch (IllegalArgumentException ex) {
            result.reject("jobPostingForm.error", ex.getMessage());
            return formView(model, form, true, id);
        }
    }

    @PostMapping("/{id}/publish")
    public String publish(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        if (!canManage(jobPostingService.findById(id), current(session))) {
            return "redirect:/error/403";
        }
        jobPostingService.publish(id);
        flash.addFlashAttribute("successMessage", "Đã đăng tin tuyển dụng.");
        return "redirect:/jobs/" + id;
    }

    @PostMapping("/{id}/close")
    public String close(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        if (!canManage(jobPostingService.findById(id), current(session))) {
            return "redirect:/error/403";
        }
        jobPostingService.close(id);
        flash.addFlashAttribute("successMessage", "Đã đóng tin tuyển dụng.");
        return "redirect:/jobs/" + id;
    }

    @PostMapping("/{id}/reopen")
    public String reopen(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        if (!canManage(jobPostingService.findById(id), current(session))) {
            return "redirect:/error/403";
        }
        jobPostingService.reopen(id);
        flash.addFlashAttribute("successMessage", "Đã mở lại tin tuyển dụng.");
        return "redirect:/jobs/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        if (!canManage(jobPostingService.findById(id), current(session))) {
            return "redirect:/error/403";
        }
        jobPostingService.delete(id);
        flash.addFlashAttribute("successMessage", "Đã xóa tin tuyển dụng.");
        return "redirect:/jobs";
    }

    // SCR-20 Pipeline Report (per job)
    @GetMapping("/{id}/report")
    public String report(@PathVariable Long id, HttpSession session, Model model) {
        SessionUser current = current(session);
        JobPosting job = jobPostingService.findById(id);
        if (!canManage(job, current)) {
            return "redirect:/error/403";
        }
        PipelineReportDto report = jobPostingService.getPipelineForJob(id);

        model.addAttribute("report", report);
        model.addAttribute("job", job);
        model.addAttribute("managedJobs", jobPostingService.findManaged(current.getId(), isAdmin(current)));
        return "jobposting/report";
    }

    private String formView(Model model, JobPostingForm form, boolean edit, Long id) {
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

    private PostingStatus parseStatus(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return PostingStatus.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /** Admin manages every posting; an HR Manager only the postings they created. */
    private boolean canManage(JobPosting job, SessionUser user) {
        if (user == null) return false;
        if (isAdmin(user)) return true;
        return Role.RECRUITER.equals(user.getRoleName())
                && job.getCreatedBy() != null
                && job.getCreatedBy().getId().equals(user.getId());
    }

    private boolean isStaff(SessionUser user) {
        return user != null
                && (Role.ADMIN.equals(user.getRoleName()) || Role.RECRUITER.equals(user.getRoleName()));
    }

    private boolean isAdmin(SessionUser user) {
        return user != null && Role.ADMIN.equals(user.getRoleName());
    }

    private SessionUser current(HttpSession session) {
        return (SessionUser) session.getAttribute(SessionConstants.LOGGED_IN_USER);
    }
}
