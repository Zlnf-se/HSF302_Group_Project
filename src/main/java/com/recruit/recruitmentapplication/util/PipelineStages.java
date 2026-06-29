package com.recruit.recruitmentapplication.util;

import com.recruit.recruitmentapplication.entity.Application.ApplicationStatus;

/**
 * View helpers for the recruitment pipeline: Vietnamese labels, badge styling,
 * and the allowed "advance" transition for each stage.
 */
public final class PipelineStages {
    private PipelineStages() {
    }

    public static String label(ApplicationStatus status) {
        if (status == null) {
            return "-";
        }
        return switch (status) {
            case APPLIED -> "Đã nộp";
            case SCREENING -> "Sàng lọc";
            case INTERVIEW -> "Phỏng vấn";
            case OFFER -> "Đề nghị";
            case HIRED -> "Đã tuyển";
            case REJECTED -> "Từ chối";
            case WITHDRAWN -> "Đã rút";
        };
    }

    public static String badgeClass(ApplicationStatus status) {
        if (status == null) {
            return "badge";
        }
        return "badge badge-" + status.name().toLowerCase();
    }

    /** The next stage when advancing, or {@code null} if there is no forward move. */
    public static ApplicationStatus nextStatus(ApplicationStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case APPLIED -> ApplicationStatus.SCREENING;
            case SCREENING -> ApplicationStatus.INTERVIEW;
            case INTERVIEW -> ApplicationStatus.OFFER;
            case OFFER -> ApplicationStatus.HIRED;
            default -> null;
        };
    }

    /** Button label for the advance action, or {@code null} when none applies. */
    public static String advanceLabel(ApplicationStatus status) {
        ApplicationStatus next = nextStatus(status);
        if (next == null) {
            return null;
        }
        return next == ApplicationStatus.HIRED ? "Đánh dấu Đã tuyển" : "Chuyển sang " + label(next);
    }
}
