# Phase 4 Job Posting Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement PDF Phase 4 job-posting CRUD, search, JOIN FETCH, and role-based write access.

**Architecture:** A validated `JobPostingForm` feeds `JobPostingService`; the service owns enum conversion, salary validation, company association, and persistence. `JobPostingController` handles form flow while `AuthInterceptor` protects all write routes.

**Tech Stack:** Java 17, Spring Boot 3.2, Spring MVC, Thymeleaf, Spring Data JPA, Jakarta Validation, SQL Server, JUnit 5, MockMvc.

## Global Constraints

- Implement PDF sections 4.1-4.7.
- ADMIN and RECRUITER may manage postings for any company.
- Do not use Lombok or PostgreSQL configuration.
- Follow red-green-refactor and preserve existing Phase 1-3 behavior.

### Task 1: DTO, repository, and service

**Files:** Create `JobPostingForm.java`, `JobPostingService.java`, `PhaseFourJobPostingServiceTests.java`; modify `JobPostingRepository.java`.

- [ ] Write tests for defaults, form validation, OPEN search with company, create, salary rejection, update/reassignment, close, and delete.
- [ ] Run `.\mvnw.cmd -Dtest=PhaseFourJobPostingServiceTests test`; expect missing classes.
- [ ] Implement DTO, search JOIN FETCH query, and transactional service methods.
- [ ] Run the focused tests; expect zero failures.

### Task 2: Authorization and controller

**Files:** Create `JobPostingController.java`, `PhaseFourJobPostingWebTests.java`; modify `AuthInterceptor.java`.

- [ ] Write MockMvc tests for anonymous redirect, candidate reads, candidate write denial, recruiter/admin form access and CRUD.
- [ ] Run focused tests; expect missing route 404 failures.
- [ ] Add `/jobs` write-route recognition and implement all eight controller routes.
- [ ] Run focused tests; expect controller tests to pass.

### Task 3: Thymeleaf pages

**Files:** Replace three files in `templates/jobposting`; modify `style.css` only if required.

- [ ] Add rendering tests for search, company/job data, role-specific controls, detail, and all form fields.
- [ ] Run focused tests; expect placeholder-content failures.
- [ ] Implement list, detail, and shared form with validation errors and responsive layout.
- [ ] Run focused tests; expect zero failures.

### Task 4: Documentation and regression

**Files:** Create `docs/PHASE4.md`.

- [ ] Document relationship, JOIN FETCH, routes, permissions, and commands.
- [ ] Run `.\mvnw.cmd test`; expect all Phase 1-4 tests to pass.
- [ ] Scan source for Lombok and verify SQL Server URL, driver, dependency, and dialect.
