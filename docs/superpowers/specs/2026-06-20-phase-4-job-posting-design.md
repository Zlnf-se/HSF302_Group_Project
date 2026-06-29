# Phase 4 Job Posting Design

## Goal

Implement PDF Phase 4: complete the `Company` to `JobPosting` one-to-many relationship and provide authenticated job-posting web CRUD. Production remains Microsoft SQL Server and no Java class uses Lombok.

## Scope

The implementation covers PDF sections 4.1-4.7:

- Preserve the existing `JobPosting` fields, enums, defaults, explicit constructors, accessors, `toString`, `equals`, and `hashCode`.
- Preserve `JobPosting.company` as the lazy many-to-one owning side with non-null `company_id`.
- Preserve `Company.jobPostings` as the lazy one-to-many inverse side with cascade all and orphan removal.
- Return an unmodifiable list from `Company.getJobPostings()` and synchronize both sides through `addJobPosting`.
- Complete repository derived queries and JPQL `JOIN FETCH` queries.
- Add a validated `JobPostingForm`.
- Add service and controller operations for list, search, detail, create, update, close, and delete.
- Replace all three placeholder Thymeleaf job pages.

Phase 5 skill relationships and Phase 6 application relationships already present in the source are preserved but not expanded.

## Authorization Decision

The detailed PDF code is authoritative: `ADMIN` and `RECRUITER` may select any company from the form dropdown and manage its postings. No recruiter-to-company relation is added to `User` or `SessionUser`.

Any authenticated role may view `/jobs` and `/jobs/{id}`. Only `ADMIN` and `RECRUITER` may access create, edit, close, or delete operations. The interceptor enforces this at the HTTP boundary; template conditions only control visibility.

## DTO and Validation

`JobPostingForm` contains `title`, `description`, `location`, `jobType`, `salaryMin`, `salaryMax`, `deadline`, and `companyId`.

- `title`, `jobType`, and `companyId` are required.
- Text lengths match entity columns.
- Salaries must be zero or positive.
- A class-level service check rejects `salaryMin > salaryMax`.
- `jobType` is a string in the form and is converted with `JobType.valueOf()` in the service.
- The edit form is populated through `JobPostingForm.from(JobPosting)`.

## Service Data Flow

`findOpenJobs(keyword)` calls `findOpenJobsWithCompany()` when the keyword is blank. With a keyword it uses a repository query that filters title case-insensitively, restricts status to `OPEN`, joins the company, and orders newest first.

`findById(id)` calls `findByIdWithCompany()` so the detail view can access the company after the transaction ends without an extra query.

Create loads the selected company, constructs the posting, parses `jobType`, calls `company.addJobPosting(posting)`, and saves the posting. The constructor supplies `postedDate = LocalDate.now()` and status `OPEN`.

Update loads posting and selected company, validates salaries and job type, moves the posting between company collections when the selected company changes, updates editable fields, and preserves status and posted date.

Close changes status to `CLOSED`. Delete verifies the posting exists and deletes it.

## Routes

| Method | Route | Access | Result |
| --- | --- | --- | --- |
| GET | `/jobs?keyword=` | Logged in | OPEN jobs, optionally filtered |
| GET | `/jobs/{id}` | Logged in | Posting detail with company |
| GET | `/jobs/new` | ADMIN/RECRUITER | Create form |
| POST | `/jobs` | ADMIN/RECRUITER | Create posting |
| GET | `/jobs/{id}/edit` | ADMIN/RECRUITER | Edit form |
| POST | `/jobs/{id}/edit` | ADMIN/RECRUITER | Update posting |
| POST | `/jobs/{id}/close` | ADMIN/RECRUITER | Set status CLOSED |
| POST | `/jobs/{id}/delete` | ADMIN/RECRUITER | Delete posting |

Invalid form input returns the shared form with field errors, companies, job types, edit mode, and posting ID restored. Missing IDs and invalid enum values become visible form/service errors rather than raw persistence errors.

## User Interface

The list page contains a title search form and rows showing title, company, job type, location, and salary range. Authorized roles see a create button. An empty state is shown when no OPEN job matches.

The detail page displays every Phase 4 field. Authorized roles see edit, close, and delete POST controls. Closed postings remain directly viewable by ID but do not appear in the OPEN list.

The shared form renders company and job-type selects plus all editable fields. It switches its action, title, and submit label based on create/edit mode.

## Testing

Red-green-refactor tests cover:

- Entity defaults and synchronized company relationship.
- Unmodifiable `Company.jobPostings` exposure.
- `JOIN FETCH` list/detail behavior and case-insensitive OPEN-title search.
- Form validation and salary-range rejection.
- Create, update including company reassignment, close, and delete.
- Anonymous redirect, candidate read access, candidate write denial, and admin/recruiter write access.
- List, detail, search, and shared form rendering.
- Full Phase 1-4 regression.

Final verification runs `.\mvnw.cmd test`, scans for Lombok, and confirms the SQL Server JDBC driver, URL, driver class, and dialect remain configured.
