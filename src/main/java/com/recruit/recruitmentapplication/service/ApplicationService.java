package com.recruit.recruitmentapplication.service;

import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.entity.Application.ApplicationStatus;
import com.recruit.recruitmentapplication.entity.InternalNote;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.repository.ApplicationRepository;
import com.recruit.recruitmentapplication.repository.InternalNoteRepository;
import com.recruit.recruitmentapplication.repository.UserRepository;
import com.recruit.recruitmentapplication.util.PipelineStages;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final InternalNoteRepository internalNoteRepository;
    private final UserRepository userRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              InternalNoteRepository internalNoteRepository,
                              UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.internalNoteRepository = internalNoteRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Application> findForJob(Long jobId) {
        return applicationRepository.findByJobWithCandidate(jobId);
    }

    @Transactional(readOnly = true)
    public Map<ApplicationStatus, Long> countByStage(List<Application> applications) {
        Map<ApplicationStatus, Long> counts = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus status : ApplicationStatus.values()) {
            counts.put(status, 0L);
        }
        for (Application application : applications) {
            counts.merge(application.getStatus(), 1L, Long::sum);
        }
        return counts;
    }

    @Transactional(readOnly = true)
    public Application getDetail(Long id) {
        return applicationRepository.findDetailById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn ứng tuyển id=" + id));
    }

    @Transactional(readOnly = true)
    public List<Application> findAssignedToInterviewer(Long interviewerId) {
        return applicationRepository.findAssignedToInterviewer(interviewerId);
    }

    @Transactional
    public Application advance(Long id) {
        Application application = require(id);
        ApplicationStatus next = PipelineStages.nextStatus(application.getStatus());
        if (next == null) {
            throw new IllegalStateException("Không thể chuyển tiếp trạng thái hiện tại.");
        }
        return changeStatus(application, next);
    }

    @Transactional
    public Application reject(Long id) {
        Application application = require(id);
        if (application.getStatus().isTerminal()) {
            throw new IllegalStateException("Đơn ứng tuyển đã ở trạng thái kết thúc.");
        }
        return changeStatus(application, ApplicationStatus.REJECTED);
    }

    @Transactional
    public InternalNote addNote(Long applicationId, Long authorId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung ghi chú không được để trống.");
        }
        Application application = require(applicationId);
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng id=" + authorId));
        InternalNote note = new InternalNote(author, content.trim());
        application.addInternalNote(note);
        applicationRepository.save(application);
        return note;
    }

    @Transactional(readOnly = true)
    public List<InternalNote> notesFor(Long applicationId) {
        return internalNoteRepository.findByApplicationIdNewestFirst(applicationId);
    }

    private Application changeStatus(Application application, ApplicationStatus status) {
        application.setStatus(status);
        application.setStageEnteredAt(LocalDateTime.now());
        return applicationRepository.save(application);
    }

    private Application require(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn ứng tuyển id=" + id));
    }
}
