# Recruitment Platform • API

> JWT authentication, RBAC, company and vacancy management, candidate applications, recruiter review flow

**Recruitment Platform API** is a monolithic REST backend for a simple hiring workflow. It handles user
registration and login, JWT-based authentication, role-based access control, company and vacancy management,
candidate job applications, recruiter-side application review, PostgreSQL persistence via Flyway migrations, and
Swagger UI for interactive API testing.

It is an all-in-one backend service that covers:

- authentication
- recruiter company management
- vacancy publishing and filtering
- candidate applications and recruiter review

---

## Project overview - Recruitment Platform

**Recruitment Platform** models a simple job marketplace where recruiters publish openings and candidates apply to
them.

**Core workflow:**

* users register as `RECRUITER` or `CANDIDATE`
* recruiters create and manage their own companies
* recruiters publish and manage vacancies for those companies
* candidates browse public companies and vacancies
* candidates apply to a vacancy once
* recruiters review incoming applications and update statuses

**Main modules:**

* **auth**: registration, login, JWT issuance, request authentication
* **company**: recruiter-owned company CRUD
* **vacancy**: vacancy CRUD with public listing and title filter
* **application**: candidate applications, recruiter review, status updates
* **common**: OpenAPI config, global exception handling, API error responses

---

## Service scope - Recruitment Platform API

* **Authentication** with `register` and `login` endpoints returning JWT bearer tokens.
* **Authorization** with `ADMIN`, `RECRUITER`, and `CANDIDATE` roles.
* **Company management** where only the owning recruiter can update or delete a company.
* **Vacancy management** where recruiters can create vacancies only for their own company.
* **Application flow** where candidates apply and recruiters review applications tied to their vacancies.
* **Validation and error handling** using Jakarta Validation and centralized exception responses.
* **Swagger / OpenAPI** documentation for local testing.
* **Flyway migrations** executed automatically at startup.

---

## Tech stack & versions

* **Java** 21
* **Spring Boot** 3.5.13
* **Spring Web**
* **Spring Security**
* **Spring Data JPA**
* **Spring Validation**
* **PostgreSQL**
* **Flyway**
* **JJWT** 0.12.5
* **Springdoc OpenAPI** 2.8.16
* **JUnit 5**
* **Mockito**
* **Testcontainers (PostgreSQL)**
* **Docker** + **Docker Compose**
* **Maven**

All versions are aligned with this service's `pom.xml`.

---

## API documentation

* **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
* **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

> Public by security config: `/api/auth/**`, `/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs/**`,
> `GET /api/companies`, `GET /api/companies/{id}`, `GET /api/vacancies`, and `GET /api/vacancies/{id}`

---

## Main routes

| Path | Methods | Access | Notes |
|-------------------------------|----------------------------|-------------------------------------------|----------------------------------------------------------------|
| `/api/auth/register` | `POST` | **Public** | Register a user with role `RECRUITER` or `CANDIDATE` |
| `/api/auth/login` | `POST` | **Public** | Returns a JWT bearer token |
| `/api/companies` | `GET`, `POST` | `GET` public, `POST` authenticated recruiter | Create and list companies |
| `/api/companies/{id}` | `GET`, `PUT`, `DELETE` | `GET` public, write owner-only | Only the company owner can update or delete |
| `/api/vacancies` | `GET`, `POST` | `GET` public, `POST` authenticated recruiter | Supports optional `title` filter on `GET` |
| `/api/vacancies/{id}` | `GET`, `PUT`, `DELETE` | `GET` public, write owner-only | Recruiter can manage only vacancies tied to their company |
| `/api/applications` | `POST` | **Authenticated candidate** | Candidate can apply only once per vacancy |
| `/api/applications/my` | `GET` | **Authenticated user** | Returns applications for the current user |
| `/api/applications/recruiter` | `GET` | **Authenticated recruiter** | Returns applications for recruiter-owned vacancies |
| `/api/applications/{id}/status` | `PATCH` | **Authenticated recruiter** | Status values: `PENDING`, `REVIEWING`, `ACCEPTED`, `REJECTED` |
| `/api/test/hello` | `GET` | **Authenticated user** | Simple protected test endpoint |

---

## Build & run

### A) Local JVM (no container for the app)

Prereqs: Java 21, Docker or PostgreSQL, and a database named `recruitment_platform`

```bash
# start only PostgreSQL from docker-compose
docker compose up -d postgres

# start the API
./mvnw spring-boot:run
```

If you prefer a local PostgreSQL instance instead of Docker, update
[`src/main/resources/application.yml`](src/main/resources/application.yml) as needed.

### B) Local Docker

This repo includes [Dockerfile](Dockerfile) and [docker-compose.yml](docker-compose.yml).

```bash
docker compose up --build
```

After startup:

* API base URL: `http://localhost:8080`
* Swagger UI: `http://localhost:8080/swagger-ui/index.html`

> Flyway runs automatically on startup using scripts in `src/main/resources/db/migration`.
> Local defaults use database `recruitment_platform`, username `postgres`, password `postgres`, and port `5432`.

---

## Testing

* **JUnit 5** is used for both unit and integration tests.
* **Mockito** is used in unit tests to mock repositories, `PasswordEncoder`, `JwtService`, and other service dependencies.
* Unit tests focus on the service layer: `AuthService`, `CompanyService`, `VacancyService`, and `ApplicationService`.
* Integration tests use **Spring Boot Test**, **MockMvc**, and **Testcontainers with PostgreSQL** for a production-like database setup.
* Integration coverage includes authentication, public/private access rules, company flow, vacancy flow, application flow, and JWT-protected requests.

Run the unit test suite with:

```bash
mvn test
```

Run unit + integration tests with:

```bash
mvn verify
```

---

## Ports (defaults)

* Recruitment Platform API: **8080**
* PostgreSQL: **5432**
