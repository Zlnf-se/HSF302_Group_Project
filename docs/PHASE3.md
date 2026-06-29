# Phase 3 - Company and CompanyProfile

## One-to-one relationship

`Company` is the owning side because it contains `@JoinColumn(name = "profile_id")`.
`CompanyProfile` is the inverse side and uses `mappedBy = "profile"`.

The relationship is lazy and optional. Cascade all allows saving or deleting a `Company`
to propagate to its profile. `Company.setProfile` synchronizes both Java object references.
`CompanyRepository.findByIdWithProfile` uses `LEFT JOIN FETCH` to load detail data in one query.

## Web routes

| Method | Route | Access |
| --- | --- | --- |
| GET | `/companies` | Any logged-in user |
| GET | `/companies/{id}` | Any logged-in user |
| GET | `/companies/new` | ADMIN or RECRUITER |
| POST | `/companies` | ADMIN or RECRUITER |
| GET | `/companies/{id}/edit` | ADMIN or RECRUITER |
| POST | `/companies/{id}/edit` | ADMIN or RECRUITER |
| POST | `/companies/{id}/delete` | ADMIN or RECRUITER |

`CompanyForm` combines the fields of `Company` and `CompanyProfile`. Controllers validate
the DTO while `CompanyService` handles duplicate names and persistence.

## Seed accounts

```text
ADMIN
Username: admin
Password: Admin@123

RECRUITER
Username: recruiter
Password: Recruiter@123
```

Both passwords are stored as BCrypt hashes.

## SQL Server and tests

Production uses the Microsoft SQL Server JDBC driver and database `recruitment_web_db`.
Lombok is not used.

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```
