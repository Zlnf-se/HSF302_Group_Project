# Phase 3 Company CRUD Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete PDF Phase 3 with validated Company/CompanyProfile CRUD pages and role-based write access.

**Architecture:** Keep `Company` as the owning side and expose web input through a combined `CompanyForm`. `CompanyService` owns persistence rules, `CompanyController` owns HTTP/form flow, and `AuthInterceptor` enforces session roles before controller execution.

**Tech Stack:** Java 17, Spring Boot 3.2, Spring MVC, Thymeleaf, Spring Data JPA, Jakarta Validation, SQL Server, JUnit 5, MockMvc.

## Global Constraints

- Implement PDF sections 3.1-3.8 only.
- Do not use Lombok.
- Keep Microsoft SQL Server as the production database.
- Use constructor injection and explicit Java accessors.
- Follow red-green-refactor for every behavior.

---

### Task 1: Company form and service CRUD

**Files:**
- Create: `src/main/java/com/recruit/recruitmentapplication/dto/CompanyForm.java`
- Create: `src/main/java/com/recruit/recruitmentapplication/service/CompanyService.java`
- Create: `src/test/java/com/recruit/recruitmentapplication/PhaseThreeCompanyServiceTests.java`
- Modify: `src/main/java/com/recruit/recruitmentapplication/entity/Company.java`

**Interfaces:**
- Produces: `CompanyService.findAll()`, `findById(Long)`, `findByIdWithProfile(Long)`, `create(CompanyForm)`, `update(Long, CompanyForm)`, and `delete(Long)`.
- Produces: `CompanyForm.from(Company)` for edit-page binding.

- [ ] **Step 1: Write failing validation and CRUD tests**

Create Spring Boot transactional tests asserting blank `name`, `industry`, and `website` violate validation; `create` cascades a synchronized profile; duplicate names throw `IllegalArgumentException`; `update` changes the existing profile row; and `delete` removes a company.

```java
CompanyForm form = validForm("Phase Three Co.");
Company saved = companyService.create(form);
assertEquals("HCM", saved.getProfile().getHeadquarters());
assertSame(saved, saved.getProfile().getCompany());
assertThrows(IllegalArgumentException.class, () -> companyService.create(form));
```

- [ ] **Step 2: Run the new test and verify RED**

Run: `.\mvnw.cmd -Dtest=PhaseThreeCompanyServiceTests test`

Expected: compilation failure because `CompanyForm` and `CompanyService` do not exist.

- [ ] **Step 3: Implement the form and minimal service**

`CompanyForm` uses `@NotBlank`/`@Size` for company text, `@Size` for profile text, `@PositiveOrZero` for employee count, and year bounds for founded year. `CompanyService` trims text, checks duplicate names, maps both objects, updates an existing profile, and delegates deletion to `CompanyRepository`.

```java
@Transactional
public Company create(CompanyForm form) {
    String name = form.getName().trim();
    if (companyRepository.existsByName(name)) {
        throw new IllegalArgumentException("Tên công ty đã tồn tại");
    }
    Company company = new Company(name, form.getIndustry().trim(), form.getWebsite().trim());
    company.setProfile(new CompanyProfile(form.getDescription(), form.getHeadquarters(),
            form.getEmployeeCount(), form.getFoundedYear()));
    return companyRepository.save(company);
}
```

- [ ] **Step 4: Make `Company.getJobPostings()` unmodifiable as required by the PDF's next relationship contract**

Return `Collections.unmodifiableList(jobPostings)` while retaining `addJobPosting` and internal mutation.

- [ ] **Step 5: Run tests and verify GREEN**

Run: `.\mvnw.cmd -Dtest=PhaseThreeCompanyServiceTests test`

Expected: all PhaseThreeCompanyServiceTests pass.

### Task 2: Company authorization and controller flow

**Files:**
- Create: `src/main/java/com/recruit/recruitmentapplication/controller/CompanyController.java`
- Modify: `src/main/java/com/recruit/recruitmentapplication/security/AuthInterceptor.java`
- Create: `src/test/java/com/recruit/recruitmentapplication/PhaseThreeCompanyWebTests.java`

**Interfaces:**
- Consumes: all `CompanyService` methods and `CompanyForm.from(Company)`.
- Produces: the seven `/companies` routes from the approved design.

- [ ] **Step 1: Write failing MockMvc authorization tests**

Test anonymous redirect, candidate list/detail access, candidate denial for all write routes, and admin/recruiter access to create/edit routes.

```java
mockMvc.perform(get("/companies/new").session(candidateSession()))
        .andExpect(redirectedUrl("/error/403"));
mockMvc.perform(get("/companies/new").session(recruiterSession()))
        .andExpect(status().isOk());
```

- [ ] **Step 2: Run web tests and verify RED**

Run: `.\mvnw.cmd -Dtest=PhaseThreeCompanyWebTests test`

Expected: 404 for missing company controller routes.

- [ ] **Step 3: Extend `AuthInterceptor` for company write routes**

Treat `GET /companies/new`, `GET /companies/{id}/edit`, and every non-GET `/companies` request as write operations. Permit only `ADMIN` and `RECRUITER`; redirect other roles to `/error/403`.

```java
private boolean canManageCompanies(SessionUser user) {
    return Role.ADMIN.equals(user.getRoleName()) || Role.RECRUITER.equals(user.getRoleName());
}
```

- [ ] **Step 4: Implement `CompanyController`**

List and detail use service query methods. Create/edit populate `companyForm` and `editMode`. POST handlers return the form on binding errors, catch duplicate-name `IllegalArgumentException` as a global form error, and redirect to detail after success. Delete uses POST and redirects to list.

- [ ] **Step 5: Run web tests and verify controller behavior**

Run: `.\mvnw.cmd -Dtest=PhaseThreeCompanyWebTests test`

Expected: authorization tests pass; rendering tests may still fail until Task 3 templates exist.

### Task 3: Thymeleaf company pages

**Files:**
- Replace: `src/main/resources/templates/company/list.html`
- Replace: `src/main/resources/templates/company/detail.html`
- Replace: `src/main/resources/templates/company/form.html`
- Modify: `src/main/resources/static/css/style.css`
- Modify: `src/test/java/com/recruit/recruitmentapplication/PhaseThreeCompanyWebTests.java`

**Interfaces:**
- Consumes model attributes `companies`, `company`, `companyForm`, and `editMode`.

- [ ] **Step 1: Add failing rendering assertions**

Assert the list contains seeded company/profile data, detail contains all profile fields, form contains the required inputs, and candidate HTML omits management actions.

```java
mockMvc.perform(get("/companies").session(candidateSession()))
        .andExpect(content().string(containsString("TechCorp Inc.")))
        .andExpect(content().string(not(containsString("Thêm công ty"))));
```

- [ ] **Step 2: Run rendering tests and verify RED**

Run: `.\mvnw.cmd -Dtest=PhaseThreeCompanyWebTests test`

Expected: placeholder templates fail content assertions.

- [ ] **Step 3: Implement list and detail templates**

The list table renders name, industry, employee count, and detail link. The detail page renders every company/profile value. Management controls use exact role checks and delete is a POST form.

- [ ] **Step 4: Implement shared form and focused CSS**

Bind all fields with `th:object`, display `th:errors`, set action based on `editMode`, and use a responsive two-column field grid that collapses on small screens.

- [ ] **Step 5: Run web tests and verify GREEN**

Run: `.\mvnw.cmd -Dtest=PhaseThreeCompanyWebTests test`

Expected: all PhaseThreeCompanyWebTests pass.

### Task 4: Seed verification and regression

**Files:**
- Modify only if tests expose a mismatch: `src/main/java/com/recruit/recruitmentapplication/demo/DataInitializer.java`
- Create: `docs/PHASE3.md`

**Interfaces:**
- Confirms the PDF's three idempotent seeded companies and profiles.

- [ ] **Step 1: Add seed assertions to service tests**

Assert TechCorp Inc., FinanceHub Ltd., and Creative Studio exist with the exact PDF profile values.

- [ ] **Step 2: Run seed assertions and verify their initial state**

Run: `.\mvnw.cmd -Dtest=PhaseThreeCompanyServiceTests test`

Expected: pass if existing seed exactly matches; otherwise fail on the mismatched field before changing production seed code.

- [ ] **Step 3: Correct only mismatched seed values and document Phase 3**

Document routes, permissions, one-to-one ownership, JOIN FETCH, SQL Server, and test command in `docs/PHASE3.md`.

- [ ] **Step 4: Run the complete regression suite**

Run: `.\mvnw.cmd test`

Expected: all Phase 1, Phase 2, and Phase 3 tests pass with zero failures and errors.

- [ ] **Step 5: Run static constraints scan**

Run: `rg -n "import lombok|@Data|@Getter|@Setter|@Builder|postgresql" pom.xml src/main`

Expected: no Lombok match and no PostgreSQL dependency/configuration match. Skill names containing PostgreSQL in seed data are not database configuration violations.
