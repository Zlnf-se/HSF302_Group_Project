package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.EvaluationForm;
import com.recruit.recruitmentapplication.dto.InterviewScheduleForm;
import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Interview;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.service.ApplicationService;
import com.recruit.recruitmentapplication.service.InterviewService;
import com.recruit.recruitmentapplication.util.SessionConstants;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class InterviewController {
    private final InterviewService interviewService;
    private final ApplicationService applicationService;

    public InterviewController(InterviewService interviewService, ApplicationService applicationService) {
        this.interviewService = interviewService;
        this.applicationService = applicationService;
    }

    // Interviewer landing: applications assigned to the signed-in interviewer
    @GetMapping("/interviews/assigned")
    public String assigned(HttpSession session, Model model) {
        SessionUser current = current(session);
        model.addAttribute("applications", applicationService.findAssignedToInterviewer(current.getId()));
        return "interview/assigned-list";
    }

    // SCR-18 Interview Assignment
    @GetMapping("/applications/{appId}/interviews/new")
    public String scheduleForm(@PathVariable Long appId, HttpSession session, Model model) {
        if (!isStaff(current(session))) {
            return "redirect:/error/403";
        }
        model.addAttribute("app", applicationService.getDetail(appId));
        model.addAttribute("interviewers", interviewService.activeInterviewers());
        if (!model.containsAttribute("interviewScheduleForm")) {
            model.addAttribute("interviewScheduleForm", new InterviewScheduleForm());
        }
        return "interview/schedule-form";
    }

    @PostMapping("/applications/{appId}/interviews")
    public String schedule(@PathVariable Long appId,
                           @Valid @ModelAttribute("interviewScheduleForm") InterviewScheduleForm form,
                           BindingResult result, HttpSession session, Model model,
                           RedirectAttributes redirect) {
        if (!isStaff(current(session))) {
            return "redirect:/error/403";
        }
        if (result.hasErrors()) {
            model.addAttribute("app", applicationService.getDetail(appId));
            model.addAttribute("interviewers", interviewService.activeInterviewers());
            return "interview/schedule-form";
        }
        try {
            Interview interview = interviewService.schedule(appId, form);
            redirect.addFlashAttribute("flashSuccess",
                    "Đã lên lịch phỏng vấn. " + interview.getInterviewerName() + " đã được phân công.");
            return "redirect:/applications/" + appId;
        } catch (RuntimeException ex) {
            model.addAttribute("app", applicationService.getDetail(appId));
            model.addAttribute("interviewers", interviewService.activeInterviewers());
            model.addAttribute("scheduleError", ex.getMessage());
            return "interview/schedule-form";
        }
    }

    // SCR-19 Evaluation Form
    @GetMapping("/interviews/{id}/evaluate")
    public String evaluateForm(@PathVariable Long id, HttpSession session, Model model) {
        Interview interview = interviewService.getDetail(id);
        if (!isOwner(interview, current(session))) {
            return "redirect:/error/403";
        }
        model.addAttribute("interview", interview);
        if (!model.containsAttribute("evaluationForm")) {
            model.addAttribute("evaluationForm", new EvaluationForm());
        }
        return "interview/record-result-form";
    }

    @PostMapping("/interviews/{id}/evaluate")
    public String evaluate(@PathVariable Long id,
                           @Valid @ModelAttribute("evaluationForm") EvaluationForm form,
                           BindingResult result, HttpSession session, Model model,
                           RedirectAttributes redirect) {
        SessionUser current = current(session);
        Interview interview = interviewService.getDetail(id);
        if (!isOwner(interview, current)) {
            return "redirect:/error/403";
        }
        if (result.hasErrors()) {
            model.addAttribute("interview", interview);
            return "interview/record-result-form";
        }
        try {
            interviewService.submitEvaluation(id, current.getId(), form);
            redirect.addFlashAttribute("flashSuccess", "Đã gửi đánh giá. Cảm ơn bạn.");
            return "redirect:/applications/" + interview.getApplication().getId();
        } catch (SecurityException ex) {
            return "redirect:/error/403";
        } catch (RuntimeException ex) {
            model.addAttribute("interview", interview);
            model.addAttribute("evaluationError", ex.getMessage());
            return "interview/record-result-form";
        }
    }

    private boolean isOwner(Interview interview, SessionUser user) {
        return interview.getInterviewer() != null
                && interview.getInterviewer().getId().equals(user.getId());
    }

    private boolean isStaff(SessionUser user) {
        return Role.ADMIN.equals(user.getRoleName()) || Role.RECRUITER.equals(user.getRoleName());
    }

    private SessionUser current(HttpSession session) {
        return (SessionUser) session.getAttribute(SessionConstants.LOGGED_IN_USER);
    }
}
