package com.recruit.recruitmentapplication.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public class InterviewScheduleForm {
    @NotNull(message = "Vui lòng chọn người phỏng vấn.")
    private Long interviewerId;

    @NotNull(message = "Vui lòng chọn ngày phỏng vấn.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Giờ phải theo định dạng HH:mm (24 giờ).")
    private String time;

    @Size(max = 500, message = "Địa điểm tối đa 500 ký tự.")
    private String location;

    public Long getInterviewerId() { return interviewerId; }
    public void setInterviewerId(Long interviewerId) { this.interviewerId = interviewerId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
