# Recruitment Platform API

> Auth, RBAC, Companies, Vacancies, Applications, and Recruiter Review Flow

Recruitment Platform API is a monolithic backend application for managing a simple recruitment workflow. It provides JWT-based authentication, role-based access control, company and vacancy management, candidate job applications, recruiter review flow, PostgreSQL persistence with Flyway migrations, Swagger documentation, and Docker-based local setup.

---

## Project Overview

Recruitment Platform API simulates a job marketplace where:

- recruiters create companies
- recruiters publish vacancies
- candidates browse vacancies
- candidates apply to vacancies
- recruiters review incoming applications
- recruiters update application statuses

This project is designed as a **monolithic REST API** and focuses on practical backend development concepts such as authentication, authorization, layered architecture, validation, exception handling, database migrations, and API documentation.

---

## Service Scope

### Authentication
- user registration
- user login
- JWT generation
- secured endpoints with bearer token authentication

### Authorization
- role-based access control
- roles: `ADMIN`, `RECRUITER`, `CANDIDATE`
- protected recruiter-only and candidate-only operations

### Company Management
- create company
- update own company
- delete own company
- list companies
- get company by ID

### Vacancy Management
- create vacancy
- update own vacancy
- delete own vacancy
- list vacancies
- get vacancy by ID
- filter vacancies by title

### Application Flow
- apply to vacancy
- candidate can view own applications
- recruiter can view applications submitted to their vacancies
- recruiter can update application status

### API Documentation
- Swagger / OpenAPI UI available for testing endpoints

---

## Tech Stack & Versions

- Java 21
- Spring Boot 3.x
- Spring Security
- JWT (`jjwt`)
- Spring Web
- Spring Data JPA
- PostgreSQL
- Flyway
- Swagger / OpenAPI (`springdoc-openapi`)
- Docker
- Docker Compose
- Maven

> All versions are aligned with this project’s `pom.xml`.

---

## API Documentation

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## Build & Run

### A) Local JVM

Prerequisites: Java 21, Maven, PostgreSQL

1. Create a PostgreSQL database named `recruitment_platform`
2. Update `src/main/resources/application.yml` if needed
3. Start the application

```bash
./mvnw spring-boot:run

For Windows:
mvnw.cmd spring-boot:run
```

B) Local Docker

This repository includes Dockerfile and docker-compose.yml.

Run the project with:

docker compose up --build

After startup, open:

http://localhost:8080/swagger-ui/index.html

This starts:

PostgreSQL
the Spring Boot application

Flyway migrations run automatically at startup.

Data & Migrations

Flyway runs automatically when the application starts.

Migration scripts are located under:

src/main/resources/db/migration

Current migrations:

V1__create_roles_table.sql
V2__create_users_table.sql
V3__insert_default_roles.sql
V4__create_companies_table.sql
V5__create_vacancies_table.sql
V6__create_applications_table.sql
Testing

This project is ready to be extended with:

unit testing using JUnit 5
mocking with Mockito
integration testing with Spring Boot Test
CI/CD pipeline integration

Run tests with:

./mvnw test
Roles & Permissions
Recruiter

A recruiter can:

create a company
update own company
delete own company
create vacancies for own company
update own vacancies
delete own vacancies
view recruiter-side applications
update application statuses
Candidate

A candidate can:

register and log in
browse companies
browse vacancies
apply to vacancies
view own applications
Admin

Admin role exists in the domain model and can be extended further in future improvements.

Example Workflow
Recruiter Flow
Register as RECRUITER
Log in and get JWT token
Create a company
Create a vacancy
View applications submitted to recruiter-owned vacancies
Update application status
Candidate Flow
Register as CANDIDATE
Log in and get JWT token
Browse vacancies
Apply to a vacancy
View own applications
Validation & Error Handling

The project includes:

request body validation
business rule validation
global exception handling
structured error responses

Example error response:

{
  "timestamp": "2026-03-26T23:59:59",
  "status": 403,
  "error": "Forbidden",
  "message": "Only recruiters can create companies",
  "path": "/api/companies"
}
Ports (Defaults)
Recruitment Platform API: 8080
PostgreSQL: 5432
Current Status

Implemented MVP features:

registration
login
JWT authentication
role-based authorization
company CRUD
vacancy CRUD
apply to vacancy
candidate application listing
recruiter application listing
recruiter application status update
validation
exception handling
Flyway migrations
Swagger documentation
Docker support
Future Improvements
pagination
filtering by location
sorting
richer validation error details
unit tests with JUnit and Mockito
integration tests
CI/CD pipeline
production deployment
Docker image publishing
admin-specific endpoints
Author

Yusufjon Axmedov

GitHub: https://github.com/yusufjon-akhmedov