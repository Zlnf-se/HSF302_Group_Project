# Phase 2 - Authentication and User Management

## Implemented features

- `Role` and `User` entities use plain Java constructors, getters and setters. Lombok is not used.
- Registration validates input, assigns the `CANDIDATE` role and hashes passwords with BCrypt.
- Login stores a small `SessionUser` DTO in `HttpSession` instead of storing a JPA entity.
- `AuthInterceptor` requires login for protected pages and restricts `/admin/**` to `ADMIN`.
- Administrators can list users, change roles, enable or disable accounts, and delete users.
- Thymeleaf pages provide registration, login, access denied and user-management screens.

## Seed data

At startup, `DataInitializer` creates these roles when missing:

- `ADMIN`
- `RECRUITER`
- `CANDIDATE`

It also creates the default administrator:

```text
Username: admin
Password: Admin@123
Email: admin@recruit.com
```

The stored password is a BCrypt hash, not plain text.

## Main routes

| Method | Route | Access |
| --- | --- | --- |
| GET, POST | `/auth/register` | Public |
| GET, POST | `/auth/login` | Public |
| GET | `/auth/logout` | Logged-in users |
| GET | `/admin/users` | ADMIN |
| GET, POST | `/admin/users/{id}/role` | ADMIN |
| GET | `/admin/users/{id}/toggle` | ADMIN |
| GET | `/admin/users/{id}/delete` | ADMIN |

## SQL Server

The application connects to SQL Server database `recruitment_db` through the Microsoft JDBC driver. Create the database first and update the local credentials in `src/main/resources/application.properties` when necessary.

```sql
CREATE DATABASE recruitment_db;
```

Run the application with:

```powershell
.\mvnw.cmd spring-boot:run
```

Run all automated tests with:

```powershell
.\mvnw.cmd test
```
