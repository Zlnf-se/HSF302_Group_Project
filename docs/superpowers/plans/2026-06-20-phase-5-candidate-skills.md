# Phase 5 Candidate Skills Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver candidate self-profile management and secure candidate directory with shared skills.

**Architecture:** Candidate owns User/Profile/Skill links; `CandidateService` owns aggregate updates; `CandidateController` binds the logged-in session; `AuthInterceptor` enforces directory versus self-service routes.

**Tech Stack:** Java 17, Spring Boot 3.2, JPA, MVC, Thymeleaf, Validation, SQL Server, JUnit/MockMvc.

## Global Constraints

- PDF 5.1-5.10, no Lombok, SQL Server production.
- Prevent candidate IDOR; only ADMIN/RECRUITER access directory/detail.
- TDD red-green-refactor; preserve previous phases.

### Task 1: Domain and service
- [ ] Write failing tests for User link, seed users, get-or-create, JOIN FETCH skills, and replacement update.
- [ ] Add Candidate User mapping/unmodifiable skills, repository query, form, service, and idempotent users.
- [ ] Run focused tests to green.

### Task 2: Controller and security
- [ ] Write failing MockMvc tests for directory roles, IDOR denial, `/me`, and update.
- [ ] Extend interceptor and add CandidateController.
- [ ] Run focused tests to green.

### Task 3: Views and verification
- [ ] Add rendering assertions then replace list/detail/profile placeholders.
- [ ] Document Phase 5.
- [ ] Run all tests and static SQL Server/Lombok scans.
