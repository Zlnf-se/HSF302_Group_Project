package com.recruit.recruitmentapplication.service;

import com.recruit.recruitmentapplication.dto.EvaluationForm;
import com.recruit.recruitmentapplication.dto.InterviewScheduleForm;
import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.entity.Application.ApplicationStatus;
import com.recruit.recruitmentapplication.entity.Interview;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.repository.ApplicationRepository;
import com.recruit.recruitmentapplication.repository.InterviewRepository;
import com.recruit.recruitmentapplication.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InterviewService {
    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public InterviewService(InterviewRepository interviewRepository,
                            ApplicationRepository applicationRepository,
                            UserRepository userRepository) {
        this.interviewRepository = interviewRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<User> activeInterviewers() {
        return userRepository.findByRole_NameAndEnabledTrueOrderByFullNameAsc(Role.INTERVIEWER);
    }

    @Transactional(readOnly = true)
    public List<Interview> forApplication(Long applicationId) {
        return interviewRepository.findByApplication_IdOrderByScheduledAtDesc(applicationId);
    }

    @Transactional(readOnly = true)
    public boolean isAssignedInterviewer(Long applicationId, Long interviewerId) {
        return interviewRepository.existsByApplication_IdAndInterviewer_Id(applicationId, interviewerId);
    }

    @Transactional(readOnly = true)
    public Interview getDetail(Long interviewId) {
        return interviewRepository.findByIdWithDetail(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch phỏng vấn id=" + interviewId));
    }

    @Transactional
    public Interview schedule(Long applicationId, InterviewScheduleForm form) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn ứng tuyển id=" + applicationId));
        if (application.getStatus() != ApplicationStatus.INTERVIEW) {
            throw new IllegalStateException("Chỉ có thể lên lịch khi đơn đang ở giai đoạn Phỏng vấn.");
        }

        User interviewer = userRepository.findById(form.getInterviewerId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người phỏng vấn."));
        if (!Role.INTERVIEWER.equals(interviewer.getRole().getName()) || !interviewer.isEnabled()) {
            throw new IllegalArgumentException("Tài khoản được chọn không phải Interviewer đang hoạt động.");
        }

        LocalDateTime scheduledAt = LocalDateTime.of(form.getDate(), parseTime(form.getTime()));
        if (scheduledAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Phỏng vấn phải được lên lịch vào thời điểm trong tương lai.");
        }

        String location = form.getLocation() == null || form.getLocation().trim().isEmpty()
                ? null : form.getLocation().trim();
        Interview interview = new Interview(interviewer, scheduledAt, location);
        application.addInterview(interview);
        applicationRepository.save(application);
        return interview;
    }

    @Transactional
    public Interview submitEvaluation(Long interviewId, Long interviewerId, EvaluationForm form) {
        Interview interview = interviewRepository.findByIdWithDetail(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch phỏng vấn id=" + interviewId));
        if (interview.getInterviewer() == null || !interview.getInterviewer().getId().equals(interviewerId)) {
            throw new SecurityException("Bạn không được phân công cho buổi phỏng vấn này.");
        }
        if (interview.isEvaluated()) {
            throw new IllegalStateException("Đánh giá đã được gửi và không thể chỉnh sửa.");
        }
        interview.setRating(form.getRating());
        interview.setFeedback(form.getFeedback() == null ? null : form.getFeedback().trim());
        interview.setStatus(Interview.InterviewStatus.EVALUATED);
        interview.setEvaluatedAt(LocalDateTime.now());
        return interviewRepository.save(interview);
    }

    private LocalTime parseTime(String value) {
        try {
            return LocalTime.parse(value);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Giờ phải theo định dạng HH:mm (24 giờ).");
        }
    }
}
