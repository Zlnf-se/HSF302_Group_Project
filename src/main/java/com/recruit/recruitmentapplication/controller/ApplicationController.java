package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.service.ApplicationService;
import com.recruit.recruitmentapplication.util.SessionConstants;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ApplicationController {
    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/applications/my")
    public String myApplications(@RequestParam(defaultValue = "ALL") String status,
                                 HttpSession session, Model model) {
        model.addAttribute("applications", applicationService.findMine(current(session).getId(), status));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", Application.ApplicationStatus.values());
        model.addAttribute("applicationService", applicationService);
        return "application/my-applications";
    }

    @PostMapping("/applications/{id}/withdraw")
    public String withdraw(@PathVariable Long id, HttpSession session) {
        applicationService.withdraw(current(session).getId(), id);
        return "redirect:/applications/my?withdrawn";
    }

    private SessionUser current(HttpSession session) {
        return (SessionUser) session.getAttribute(SessionConstants.LOGGED_IN_USER);
    }
}
