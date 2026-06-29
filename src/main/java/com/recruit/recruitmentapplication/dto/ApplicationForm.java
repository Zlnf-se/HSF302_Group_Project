package com.recruit.recruitmentapplication.dto;

import org.springframework.web.multipart.MultipartFile;

public class ApplicationForm {
    private String coverLetter;
    private MultipartFile cvFile;

    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
    public MultipartFile getCvFile() { return cvFile; }
    public void setCvFile(MultipartFile cvFile) { this.cvFile = cvFile; }
}
