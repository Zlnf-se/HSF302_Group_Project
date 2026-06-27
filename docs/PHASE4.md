# Phase 4 - Company and JobPosting

## Relationship

`JobPosting` owns the many-to-one relationship through the non-null `company_id` foreign key.
`Company` is the inverse one-to-many side and uses `mappedBy = "company"`. The collection is
exposed as an unmodifiable list; `addJobPosting` synchronizes both Java object references.

## JOIN FETCH and search

The OPEN-job list and job detail queries join-fetch `Company`, so Thymeleaf can display company
names without one additional SQL query per job. Title search is case-insensitive and returns only
OPEN postings.

## Routes and permissions

All authenticated users can view `/jobs` and `/jobs/{id}`. `ADMIN` and `RECRUITER` can use:

- `GET /jobs/new`, `POST /jobs`
- `GET /jobs/{id}/edit`, `POST /jobs/{id}/edit`
- `POST /jobs/{id}/close`
- `POST /jobs/{id}/delete`

Following the detailed PDF code, either write role may choose any company from the form dropdown.

## Commands

Production uses SQL Server and no Lombok.

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```
