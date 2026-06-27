# Phase 5 Candidate Skills Design

## Goal

Implement PDF sections 5.1-5.10 without Lombok and with SQL Server: link Candidate to User, manage candidate profiles and skills, protect profiles from IDOR, and preserve JobPosting required skills.

## Design

Existing CandidateProfile, Skill, and both many-to-many join tables are retained. Candidate gains a lazy unique one-to-one `user_id` without cascade-all. Candidate skill exposure becomes unmodifiable; service methods replace selections through explicit bidirectional helpers.

`CandidateProfileForm` combines candidate/profile fields and `Set<Long> skillIds`, with validation matching entity limits. `CandidateService.getOrCreateProfileForUser` derives a new candidate's name/email from User. `updateProfile` verifies unique email, updates or creates CandidateProfile, removes old inverse references, adds selected managed Skill rows, and saves.

Admin/recruiter may list and view any candidate. Candidate users may only access GET/POST `/candidates/me`; interceptor blocks `/candidates` and `/candidates/{id}` to prevent IDOR. The profile form uses the authenticated `SessionUser.id`, never a client-supplied candidate/user ID.

Data initialization creates BCrypt-backed `alice` and `bob` CANDIDATE users and links them to existing Alice/Bob candidates idempotently. Existing job/candidate skill seeds remain shared Skill rows.

Tests cover mappings, unmodifiable sets, get-or-create, profile/skill replacement, seeds, role access, IDOR denial, self-profile update, HTML rendering, full regression, SQL Server configuration, and absence of Lombok.
