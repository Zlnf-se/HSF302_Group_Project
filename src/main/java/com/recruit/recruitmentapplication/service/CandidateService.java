package com.recruit.recruitmentapplication.service;

import com.recruit.recruitmentapplication.dto.CandidateProfileForm;
import com.recruit.recruitmentapplication.entity.*;
import com.recruit.recruitmentapplication.repository.*;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CandidateService {
    private final CandidateRepository candidateRepository; private final SkillRepository skillRepository; private final UserRepository userRepository;
    public CandidateService(CandidateRepository c, SkillRepository s, UserRepository u){candidateRepository=c;skillRepository=s;userRepository=u;}

    @Transactional(readOnly=true) public List<Candidate> findAll(){return candidateRepository.findAllWithSkills();}
    @Transactional(readOnly=true) public Candidate findById(Long id){return candidateRepository.findByIdWithSkills(id).orElseThrow(()->notFound(id));}

    @Transactional
    public Candidate getOrCreateProfileForUser(Long userId) {
        return candidateRepository.findByUserIdWithProfileAndSkills(userId).orElseGet(() -> {
            User user=userRepository.findById(userId).orElseThrow(()->new IllegalArgumentException("Không tìm thấy user id="+userId));
            Candidate candidate=new Candidate(user.getFullName(),user.getEmail(),null); candidate.setUser(user);
            return candidateRepository.save(candidate);
        });
    }

    @Transactional
    public Candidate updateProfile(Long id, CandidateProfileForm form) {
        Candidate c=findById(id); String email=form.getEmail().trim().toLowerCase();
        candidateRepository.findByEmail(email).filter(other->!other.getId().equals(id)).ifPresent(other->{throw new IllegalArgumentException("Email ứng viên đã tồn tại");});
        c.setName(form.getName().trim()); c.setEmail(email); c.setPhone(trim(form.getPhone()));
        CandidateProfile p=c.getProfile(); if(p==null){p=new CandidateProfile();c.setProfile(p);}
        p.setYearsOfExperience(form.getYearsOfExperience()); p.setEducationLevel(trim(form.getEducationLevel())); p.setCurrentTitle(trim(form.getCurrentTitle())); p.setLinkedinUrl(trim(form.getLinkedinUrl())); p.setResumeSummary(trim(form.getResumeSummary()));
        c.clearSkills(); for(Long skillId:form.getSkillIds()){c.addSkill(skillRepository.findById(skillId).orElseThrow(()->new IllegalArgumentException("Skill không hợp lệ")));}
        return candidateRepository.save(c);
    }
    @Transactional(readOnly=true) public List<Skill> findAllSkills(){return skillRepository.findAll();}
    private String trim(String v){return v==null||v.trim().isEmpty()?null:v.trim();}
    private IllegalArgumentException notFound(Long id){return new IllegalArgumentException("Không tìm thấy ứng viên id="+id);}
}
