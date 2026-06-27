package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.JobPostingForm;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.service.CompanyService;
import com.recruit.recruitmentapplication.service.JobPostingService;
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

    public JobPostingController(JobPostingService jobPostingService, CompanyService companyService) {
        this.jobPostingService = jobPostingService;
        this.companyService = companyService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword, Model model) {
        model.addAttribute("jobs", jobPostingService.findOpenJobs(keyword));
        model.addAttribute("keyword", keyword);
        return "jobposting/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("job", jobPostingService.findById(id));
        return "jobposting/detail";
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
}
