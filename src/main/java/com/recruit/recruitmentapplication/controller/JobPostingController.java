package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.ApplicationForm;
import com.recruit.recruitmentapplication.dto.JobPostingForm;
import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.service.ApplicationService;
import com.recruit.recruitmentapplication.service.CompanyService;
import com.recruit.recruitmentapplication.service.JobPostingService;
import com.recruit.recruitmentapplication.util.SessionConstants;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/jobs")
public class JobPostingController {
    private final JobPostingService jobPostingService;
    private final CompanyService companyService;
    private final ApplicationService applicationService;

    public JobPostingController(JobPostingService jobPostingService, CompanyService companyService,
                                ApplicationService applicationService) {
        this.jobPostingService = jobPostingService;
        this.companyService = companyService;
        this.applicationService = applicationService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "") String department,
                       @RequestParam(defaultValue = "") String location,
                       Model model) {
        model.addAttribute("jobs", jobPostingService.findOpenJobs(keyword, department, location));
        model.addAttribute("keyword", keyword);
        model.addAttribute("department", department);
        model.addAttribute("location", location);
        model.addAttribute("departments", jobPostingService.findOpenDepartments());
        model.addAttribute("locations", jobPostingService.findOpenLocations());
        return "jobposting/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        preparePublicDetail(id, session, model, new ApplicationForm(), null);
        return "jobposting/detail";
    }

    @PostMapping("/{id}/apply")
    public String apply(@PathVariable Long id, @ModelAttribute("applicationForm") ApplicationForm form,
                        HttpSession session, Model model) {
        try {
            applicationService.submit(current(session).getId(), id, form);
            return "redirect:/jobs/" + id + "?applied";
        } catch (IllegalArgumentException exception) {
            preparePublicDetail(id, session, model, form, exception.getMessage());
            return "jobposting/detail";
        }
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        prepareForm(model, new JobPostingForm(), false, null);
        return "jobposting/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("jobPostingForm") JobPostingForm form,
                         BindingResult result, Model model) {
        if (result.hasErrors()) return formView(model, form, result, false, null);
        try {
            return "redirect:/jobs/" + jobPostingService.create(form).getId();
        } catch (IllegalArgumentException exception) {
            result.reject("jobPostingForm.error", exception.getMessage());
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
                         BindingResult result, Model model) {
        if (result.hasErrors()) return formView(model, form, result, true, id);
        try {
            jobPostingService.update(id, form);
            return "redirect:/jobs/" + id;
        } catch (IllegalArgumentException exception) {
            result.reject("jobPostingForm.error", exception.getMessage());
            return formView(model, form, result, true, id);
        }
    }

    @PostMapping("/{id}/close")
    public String close(@PathVariable Long id) {
        jobPostingService.close(id);
        return "redirect:/jobs/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        jobPostingService.delete(id);
        return "redirect:/jobs";
    }

    private String formView(Model model, JobPostingForm form, BindingResult result, boolean edit, Long id) {
        prepareForm(model, form, edit, id);
        return "jobposting/form";
    }

    private void prepareForm(Model model, JobPostingForm form, boolean edit, Long id) {
        model.addAttribute("jobPostingForm", form);
        model.addAttribute("companies", companyService.findAll());
        model.addAttribute("jobTypes", JobPosting.JobType.values());
        model.addAttribute("skills", jobPostingService.findAllSkills());
        model.addAttribute("editMode", edit);
        model.addAttribute("jobId", id);
    }

    private void preparePublicDetail(Long id, HttpSession session, Model model, ApplicationForm form, String error) {
        JobPosting job = jobPostingService.findById(id);
        SessionUser user = current(session);
        boolean candidate = user != null && Role.CANDIDATE.equals(user.getRoleName());
        model.addAttribute("job", job);
        model.addAttribute("applicationForm", form);
        model.addAttribute("candidateUser", candidate);
        model.addAttribute("alreadyApplied", candidate && applicationService.hasApplied(user.getId(), id));
        model.addAttribute("applyError", error);
    }

    private SessionUser current(HttpSession session) {
        return session == null ? null : (SessionUser) session.getAttribute(SessionConstants.LOGGED_IN_USER);
    }
}
