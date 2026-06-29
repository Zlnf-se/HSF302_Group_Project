package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.entity.Application.ApplicationStatus;
import com.recruit.recruitmentapplication.entity.Interview;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.service.ApplicationService;
import com.recruit.recruitmentapplication.service.InterviewService;
import com.recruit.recruitmentapplication.service.JobPostingService;
import com.recruit.recruitmentapplication.util.SessionConstants;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ApplicationController {
    private final ApplicationService applicationService;
    private final InterviewService interviewService;
    private final JobPostingService jobPostingService;

    public ApplicationController(ApplicationService applicationService,
                                 InterviewService interviewService,
                                 JobPostingService jobPostingService) {
        this.applicationService = applicationService;
        this.interviewService = interviewService;
        this.jobPostingService = jobPostingService;
    }

    // SCR-16 Application List
    @GetMapping("/jobs/{jobId}/applications")
    public String list(@PathVariable Long jobId,
                       @RequestParam(name = "stage", required = false) String stage,
                       Model model) {
        List<Application> all = applicationService.findForJob(jobId);
        ApplicationStatus selected = parseStage(stage);
        List<Application> visible = selected == null
                ? all
                : all.stream().filter(a -> a.getStatus() == selected).toList();

        model.addAttribute("job", jobPostingService.findById(jobId));
        model.addAttribute("applications", visible);
        model.addAttribute("counts", applicationService.countByStage(all));
        model.addAttribute("totalCount", (long) all.size());
        model.addAttribute("stages", ApplicationStatus.values());
        model.addAttribute("selectedStage", selected);
        return "application/list";
    }

    // SCR-17 Application Detail
    @GetMapping("/applications/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        SessionUser current = current(session);
        boolean staff = isStaff(current);
        boolean interviewer = Role.INTERVIEWER.equals(current.getRoleName());

        if (!staff && interviewer && !interviewService.isAssignedInterviewer(id, current.getId())) {
            return "redirect:/error/403";
        }
        if (!staff && !interviewer) {
            return "redirect:/error/403";
        }

        Application application = applicationService.getDetail(id);
        List<Interview> interviews = interviewService.forApplication(id);

        model.addAttribute("app", application);
        model.addAttribute("interviews", interviews);
        model.addAttribute("isStaff", staff);
        model.addAttribute("isInterviewer", interviewer);
        if (staff) {
            model.addAttribute("notes", applicationService.notesFor(id));
            model.addAttribute("evaluations", interviews.stream().filter(Interview::isEvaluated).toList());
        }
        if (interviewer) {
            model.addAttribute("myInterview", interviews.stream()
                    .filter(i -> i.getInterviewer() != null && i.getInterviewer().getId().equals(current.getId()))
                    .findFirst().orElse(null));
        }
        return "application/detail";
    }

    @PostMapping("/applications/{id}/advance")
    public String advance(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        if (!isStaff(current(session))) {
            return "redirect:/error/403";
        }
        try {
            applicationService.advance(id);
            redirect.addFlashAttribute("flashSuccess", "Đã cập nhật trạng thái ứng tuyển.");
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("flashError", ex.getMessage());
        }
        return "redirect:/applications/" + id;
    }

    @PostMapping("/applications/{id}/reject")
    public String reject(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        if (!isStaff(current(session))) {
            return "redirect:/error/403";
        }
        try {
            applicationService.reject(id);
            redirect.addFlashAttribute("flashSuccess", "Đã từ chối ứng viên.");
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("flashError", ex.getMessage());
        }
        return "redirect:/applications/" + id;
    }

    @PostMapping("/applications/{id}/notes")
    public String addNote(@PathVariable Long id,
                          @RequestParam("content") String content,
                          HttpSession session, RedirectAttributes redirect) {
        SessionUser current = current(session);
        if (!isStaff(current)) {
            return "redirect:/error/403";
        }
        try {
            applicationService.addNote(id, current.getId(), content);
            redirect.addFlashAttribute("flashSuccess", "Đã thêm ghi chú.");
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("flashError", ex.getMessage());
        }
        return "redirect:/applications/" + id;
    }

    private ApplicationStatus parseStage(String stage) {
        if (stage == null || stage.isBlank() || "ALL".equalsIgnoreCase(stage)) {
            return null;
        }
        try {
            return ApplicationStatus.valueOf(stage.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean isStaff(SessionUser user) {
        return Role.ADMIN.equals(user.getRoleName()) || Role.RECRUITER.equals(user.getRoleName());
    }

    private SessionUser current(HttpSession session) {
        return (SessionUser) session.getAttribute(SessionConstants.LOGGED_IN_USER);
    }
}
