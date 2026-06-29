package com.recruit.recruitmentapplication.dto;

import com.recruit.recruitmentapplication.entity.Application.ApplicationStatus;
import com.recruit.recruitmentapplication.util.PipelineStages;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class PipelineReportDto {

    private Long jobId;
    private String jobTitle;
    private int total;

    private Map<ApplicationStatus, Long> statusCounts = new LinkedHashMap<>();
    private List<Row> applications = new ArrayList<>();

    public PipelineReportDto() {}

    public PipelineReportDto(Long jobId, String jobTitle) {
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        for (ApplicationStatus s : ApplicationStatus.values()) {
            statusCounts.put(s, 0L);
        }
    }

    public void addCount(ApplicationStatus status, long count) {
        statusCounts.put(status, count);
        this.total += (int) count;
    }

    public long getCount(ApplicationStatus status) {
        return statusCounts.getOrDefault(status, 0L);
    }

    public double getHiredRate() {
        if (total == 0) return 0;

        long hired =
                statusCounts.getOrDefault(
                        ApplicationStatus.HIRED,
                        0L
                );

        return Math.round(
                (double) hired / total * 1000.0
        ) / 10.0;
    }

    public double getRejectedRate() {
        if (total == 0) return 0;
        long rejected = statusCounts.getOrDefault(ApplicationStatus.REJECTED, 0L);
        return Math.round((double) rejected / total * 1000.0) / 10.0;
    }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public Map<ApplicationStatus, Long> getStatusCounts() { return statusCounts; }
    public void setStatusCounts(Map<ApplicationStatus, Long> statusCounts) { this.statusCounts = statusCounts; }

    public List<Row> getApplications() { return applications; }
    public void setApplications(List<Row> applications) {
        this.applications = applications == null ? new ArrayList<>() : applications;
    }
    public void addApplication(Row row) { this.applications.add(row); }

    /** Stage counts keyed by Vietnamese label, for the bar chart. */
    public Map<String, Long> getStageCounts() {
        Map<String, Long> labelled = new LinkedHashMap<>();
        for (Map.Entry<ApplicationStatus, Long> entry : statusCounts.entrySet()) {
            labelled.put(PipelineStages.label(entry.getKey()), entry.getValue());
        }
        return labelled;
    }

    /** One candidate row in the report table. */
    public static class Row {
        private final String candidateName;
        private final ApplicationStatus status;
        private final long daysInStage;
        private final String interviewerName;

        public Row(String candidateName, ApplicationStatus status, long daysInStage, String interviewerName) {
            this.candidateName = candidateName;
            this.status = status;
            this.daysInStage = daysInStage;
            this.interviewerName = interviewerName;
        }

        public String getCandidateName() { return candidateName; }
        public ApplicationStatus getStatus() { return status; }
        public long getDaysInStage() { return daysInStage; }
        public String getInterviewerName() { return interviewerName; }
    }
}