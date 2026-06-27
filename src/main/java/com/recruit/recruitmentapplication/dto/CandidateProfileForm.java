package com.recruit.recruitmentapplication.dto;

import com.recruit.recruitmentapplication.entity.Candidate;
import com.recruit.recruitmentapplication.entity.CandidateProfile;
import com.recruit.recruitmentapplication.entity.Skill;
import jakarta.validation.constraints.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CandidateProfileForm {
    @NotBlank @Size(max = 100) private String name;
    @NotBlank @Email @Size(max = 150) private String email;
    @Size(max = 20) private String phone;
    @PositiveOrZero private Integer yearsOfExperience;
    @Size(max = 100) private String educationLevel;
    @Size(max = 150) private String currentTitle;
    @Size(max = 300) private String linkedinUrl;
    @Size(max = 2000) private String resumeSummary;
    private Set<Long> skillIds = new HashSet<>();

    public CandidateProfileForm() {}
    public static CandidateProfileForm from(Candidate c) {
        CandidateProfileForm f = new CandidateProfileForm();
        f.name = c.getName(); f.email = c.getEmail(); f.phone = c.getPhone();
        CandidateProfile p = c.getProfile();
        if (p != null) { f.yearsOfExperience=p.getYearsOfExperience(); f.educationLevel=p.getEducationLevel(); f.currentTitle=p.getCurrentTitle(); f.linkedinUrl=p.getLinkedinUrl(); f.resumeSummary=p.getResumeSummary(); }
        f.skillIds = c.getSkills().stream().map(Skill::getId).collect(Collectors.toSet());
        return f;
    }
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
    public Integer getYearsOfExperience(){return yearsOfExperience;} public void setYearsOfExperience(Integer v){yearsOfExperience=v;}
    public String getEducationLevel(){return educationLevel;} public void setEducationLevel(String v){educationLevel=v;}
    public String getCurrentTitle(){return currentTitle;} public void setCurrentTitle(String v){currentTitle=v;}
    public String getLinkedinUrl(){return linkedinUrl;} public void setLinkedinUrl(String v){linkedinUrl=v;}
    public String getResumeSummary(){return resumeSummary;} public void setResumeSummary(String v){resumeSummary=v;}
    public Set<Long> getSkillIds(){return skillIds;} public void setSkillIds(Set<Long> v){skillIds=v==null?new HashSet<>():v;}
}
