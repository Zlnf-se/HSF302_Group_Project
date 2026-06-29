package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.CompanyForm;
import com.recruit.recruitmentapplication.entity.Company;
import com.recruit.recruitmentapplication.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/companies")
public class CompanyController {
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("companies", companyService.findAll());
        return "company/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("company", companyService.findByIdWithProfile(id));
        return "company/detail";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        prepareFormModel(model, new CompanyForm(), false, null);
        return "company/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("companyForm") CompanyForm form,
                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            prepareFormModel(model, form, false, null);
            return "company/form";
        }
        try {
            Company company = companyService.create(form);
            return "redirect:/companies/" + company.getId();
        } catch (IllegalArgumentException exception) {
            result.reject("companyForm.error", exception.getMessage());
            prepareFormModel(model, form, false, null);
            return "company/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Company company = companyService.findByIdWithProfile(id);
        prepareFormModel(model, CompanyForm.from(company), true, id);
        return "company/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("companyForm") CompanyForm form,
                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            prepareFormModel(model, form, true, id);
            return "company/form";
        }
        try {
            companyService.update(id, form);
            return "redirect:/companies/" + id;
        } catch (IllegalArgumentException exception) {
            result.reject("companyForm.error", exception.getMessage());
            prepareFormModel(model, form, true, id);
            return "company/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        companyService.delete(id);
        return "redirect:/companies";
    }

    private void prepareFormModel(Model model, CompanyForm form, boolean editMode, Long companyId) {
        model.addAttribute("companyForm", form);
        model.addAttribute("editMode", editMode);
        model.addAttribute("companyId", companyId);
    }
}
