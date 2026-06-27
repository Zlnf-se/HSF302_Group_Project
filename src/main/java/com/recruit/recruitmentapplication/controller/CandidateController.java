package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.*;
import com.recruit.recruitmentapplication.entity.Candidate;
import com.recruit.recruitmentapplication.service.CandidateService;
import com.recruit.recruitmentapplication.util.SessionConstants;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller @RequestMapping("/candidates")
public class CandidateController {
    private final CandidateService service;
    public CandidateController(CandidateService service){this.service=service;}

    @GetMapping public String list(Model model){model.addAttribute("candidates",service.findAll());return "candidate/list";}
    @GetMapping("/{id}") public String detail(@PathVariable Long id,Model model){model.addAttribute("candidate",service.findById(id));return "candidate/detail";}

    @GetMapping("/me")
    public String myProfile(HttpSession session,Model model){Candidate c=service.getOrCreateProfileForUser(current(session).getId());prepare(model,c,CandidateProfileForm.from(c));return "candidate/profile-form";}

    @PostMapping("/me")
    public String updateMyProfile(@Valid @ModelAttribute("candidateProfileForm") CandidateProfileForm form,
                                  BindingResult result,HttpSession session,Model model){
        Candidate c=service.getOrCreateProfileForUser(current(session).getId());
        if(result.hasErrors()){prepare(model,c,form);return "candidate/profile-form";}
        try{service.updateProfile(c.getId(),form);return "redirect:/candidates/me?updated";}
        catch(IllegalArgumentException e){result.reject("candidateProfileForm.error",e.getMessage());prepare(model,c,form);return "candidate/profile-form";}
    }
    private void prepare(Model model,Candidate c,CandidateProfileForm form){model.addAttribute("candidate",c);model.addAttribute("candidateProfileForm",form);model.addAttribute("skills",service.findAllSkills());}
    private SessionUser current(HttpSession session){return (SessionUser)session.getAttribute(SessionConstants.LOGGED_IN_USER);}
}
