# Phase 5 - Candidate and Skills

## Relationships

- Candidate owns `candidate_skills`; Skill is the inverse side.
- JobPosting owns `job_required_skills`; Skill is the inverse side.
- Candidate owns the optional one-to-one profile and unique User link.
- Skill `toString` excludes both inverse collections to prevent recursion.

The candidate and job forms select existing Skill IDs. Services clear old selections and attach
managed Skill rows, so shared skills such as Java and Docker are not duplicated.

## Security

ADMIN and RECRUITER can use `/candidates` and `/candidates/{id}`. CANDIDATE users are blocked
from those routes and can only use GET/POST `/candidates/me`. The current User ID comes from
`SessionUser`, preventing an IDOR attack through a guessed candidate ID.

## Sample candidate accounts

```text
alice / Alice@123
bob   / Bob@123
```

Passwords are BCrypt hashes. Production remains SQL Server and the project does not use Lombok.

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```
