package com.recruit.recruitmentapplication.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EvaluationForm {
    @NotNull(message = "Vui lòng chọn số sao đánh giá.")
    @Min(value = 1, message = "Đánh giá từ 1 đến 5 sao.")
    @Max(value = 5, message = "Đánh giá từ 1 đến 5 sao.")
    private Integer rating;

    @NotBlank(message = "Vui lòng nhập nhận xét.")
    private String feedback;

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
