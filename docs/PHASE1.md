# Phase 1 - Recruitment Web System

## Technology stack

- Java 17, Spring Boot 3.2, Maven
- Spring MVC, Thymeleaf, Spring Data JPA, Jakarta Validation
- SQL Server (`recruitment_db`)
- BCrypt through `spring-security-crypto`; no Spring Security filter chain
- No Lombok

## Layered structure

```text
Controller -> Service -> Repository -> SQL Server
                |
               DTO
```

The current codebase already contains domain entities and repositories from earlier exercises.
Phase 1 adds the web scaffold while preserving those classes and converting them to plain Java.

## Domain relationships

```text
Role 1 -------- N User
User 1 -------- 1 Candidate
Company 1 ----- 1 CompanyProfile
Company 1 ----- N JobPosting
Candidate 1 --- 1 CandidateProfile
Candidate N --- N Skill
JobPosting N -- N Skill
Candidate 1 --- N Application N --- 1 JobPosting
Application 1 - N Interview
```

`Role` and `User` are identified in Phase 1 and are implemented in Phase 2 according to the assignment.

## SQL Server setup

Create the database before starting the application:

```sql
CREATE DATABASE recruitment_db;
```

Update `spring.datasource.username` and `spring.datasource.password` in
`src/main/resources/application.properties` for the local SQL Server instance.

Hibernate uses `ddl-auto=update` so web data is preserved between application restarts.
