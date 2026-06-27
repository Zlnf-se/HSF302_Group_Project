# Phase 3 Company CRUD Design

## Goal

Implement Phase 3 from the assignment PDF: complete the `Company` and `CompanyProfile` one-to-one domain and provide authenticated web CRUD pages. All Java code uses explicit constructors, getters, setters, `toString`, `equals`, and `hashCode`; Lombok is not allowed. The production database remains Microsoft SQL Server.

## Scope

The implementation covers PDF sections 3.1 through 3.8 only:

- `CompanyProfile` is the inverse side with `mappedBy = "profile"` and lazy loading.
- `Company` is the owning side with `profile_id`, cascade all, lazy loading, and an optional profile.
- `CompanyRepository` supports derived queries and `findByIdWithProfile` using JPQL `LEFT JOIN FETCH`.
- `CompanyForm` combines company and profile fields and validates required company fields.
- `CompanyService` provides list, detail, create, update, and delete operations.
- `CompanyController` exposes the routes required by the PDF.
- Thymeleaf provides company list, detail, and shared create/edit form pages.
- `DataInitializer` preserves the three required companies and their complete profiles.

No additional deletion rule is introduced. `CompanyService.delete(id)` delegates to the repository after verifying that the company exists.

## Domain Model

`Company.profile` owns the relationship and stores the unique nullable `profile_id` foreign key. `CompanyProfile.company` is the inverse reference. `Company.setProfile` keeps both in-memory references synchronized so cascade persistence saves a consistent graph.

Both entities avoid recursive output: `CompanyProfile.toString()` never prints `company`, while `Company.toString()` prints at most the profile identifier. Equality and hash code continue to be based on persistent identity.

## DTO and Validation

`CompanyForm` contains:

- `name`, `industry`, `website` with `@NotBlank` and size limits matching entity columns.
- `description`, `headquarters`, `employeeCount`, and `foundedYear` for the profile.
- A no-argument constructor and explicit accessors.

The service trims required text before persistence. Duplicate company names are rejected on create. During update, retaining the current company's name is allowed, while changing to another existing name is rejected.

## Service and Data Flow

The controller accepts only `CompanyForm`; JPA entities are not used as web form models.

Create flow:

1. Validate form input in the controller.
2. Reject an existing company name.
3. Construct `CompanyProfile` and `Company`.
4. Call `company.setProfile(profile)`.
5. Save `Company`; cascade persists the profile.

Update flow:

1. Fetch the company and profile in one query with `findByIdWithProfile`.
2. Check duplicate name excluding the current company.
3. Update company fields and the existing profile; create a profile only if it is absent.
4. Save and redirect to the detail page.

Delete verifies the company exists, deletes it, and redirects to the company list.

## Routes and Authorization

All company routes require an authenticated session because the existing global interceptor protects every non-public route.

| Method | Route | Authorization | Result |
| --- | --- | --- | --- |
| GET | `/companies` | Any logged-in role | Company list |
| GET | `/companies/{id}` | Any logged-in role | Company detail with profile |
| GET | `/companies/new` | ADMIN or RECRUITER | Create form |
| POST | `/companies` | ADMIN or RECRUITER | Create company |
| GET | `/companies/{id}/edit` | ADMIN or RECRUITER | Edit form |
| POST | `/companies/{id}/edit` | ADMIN or RECRUITER | Update company |
| POST | `/companies/{id}/delete` | ADMIN or RECRUITER | Delete company |

The interceptor recognizes `/companies` write operations by HTTP method and path. Unauthorized authenticated candidates are redirected to `/error/403`. Templates use `SessionUser.roleName` to show write controls only to `ADMIN` and `RECRUITER`.

## User Interface

The list page displays name, industry, employee count, and a detail action. Authorized roles also see the create action.

The detail page displays all company and profile fields. Edit and delete actions are visible only to authorized roles; deletion uses a POST form.

The shared form switches title, action URL, and submit label according to create or edit mode. Every validated field shows its own error message. Service errors such as duplicate names return to the same form with a visible message.

## Error Handling

Missing company IDs raise a domain-facing `IllegalArgumentException` and are handled as a not-found response by the controller layer. Invalid forms return HTTP 200 with field errors. Duplicate names return the form with a global error. Unauthorized access redirects to the existing 403 page.

## Seed Data

Startup keeps these records idempotently by company name:

| Company | Industry | Website | Description | Headquarters | Employees | Founded |
| --- | --- | --- | --- | --- | ---: | ---: |
| TechCorp Inc. | Technology | techcorp.com | Cloud solutions leader | HCM | 500 | 2010 |
| FinanceHub Ltd. | Finance | financehub.com | Digital banking fintech | Ha Noi | 200 | 2015 |
| Creative Studio | Design | creativestudio.com | UX/UI agency | Da Nang | 80 | 2018 |

## Testing

Implementation follows red-green-refactor cycles. Automated tests cover:

- One-to-one owning/inverse mapping and cascade persistence.
- `JOIN FETCH` returning an initialized profile.
- Required form validation.
- Create, duplicate-name rejection, update of the existing profile, and delete.
- Anonymous redirect to login.
- Candidate read access and write denial.
- Admin and recruiter create/edit/delete access.
- Thymeleaf list, detail, and form rendering.
- Existing Phase 1 and Phase 2 regression tests.

The final verification command is `.\mvnw.cmd test`. A static scan confirms no Lombok annotations/imports and confirms the SQL Server JDBC driver and dialect remain configured.
