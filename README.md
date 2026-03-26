# Recruitment Platform API

A monolithic backend application built with Spring Boot for managing recruitment workflows. The system supports authentication, role-based authorization, company management, vacancy management, job applications, and recruiter review flow.

## Overview

This project simulates a simple recruitment platform where recruiters can create companies and post vacancies, while candidates can browse vacancies and apply for jobs. Recruiters can then review incoming applications and update their statuses.

The application is built as a monolithic REST API and focuses on clean backend fundamentals such as authentication, authorization, layered architecture, validation, exception handling, database migrations, and API documentation.

## Features

- User registration and login
- JWT-based authentication
- Role-based authorization
- Company CRUD operations
- Vacancy CRUD operations
- Apply to a vacancy
- Candidate application listing
- Recruiter application listing
- Recruiter application status update
- Request validation
- Global exception handling
- Flyway database migrations
- Swagger / OpenAPI documentation

## Roles

The system supports the following roles:

- `ADMIN`
- `RECRUITER`
- `CANDIDATE`

### Recruiter

A recruiter can:
- create a company
- update their own company
- delete their own company
- create vacancies for their own company
- update their own vacancies
- delete their own vacancies
- view applications submitted to their vacancies
- update application status

### Candidate

A candidate can:
- register and log in
- view companies
- view vacancies
- apply to a vacancy
- view their own applications

## Tech Stack

- **Java 21**
- **Spring Boot**
- **Spring Web**
- **Spring Security**
- **JWT Authentication**
- **Spring Data JPA**
- **PostgreSQL**
- **Flyway**
- **Swagger / OpenAPI**
- **Maven**

## Architecture

This project is built as a **monolithic REST API** using a layered architecture.

### Main layers

- **Controller layer** – handles HTTP requests and responses
- **Service layer** – contains business logic
- **Repository layer** – communicates with the database
- **Entity layer** – maps Java classes to database tables
- **DTO layer** – handles request and response payloads
- **Security layer** – manages JWT authentication and authorization
- **Exception layer** – handles validation and business rule errors

## Project Structure

```text
src/main/java/com/yusufjon/recruitmentplatform
├── application
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
├── auth
│   ├── controller
│   ├── dto
│   ├── security
│   └── service
├── common
│   ├── config
│   ├── exception
│   └── response
├── company
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
├── shared
│   └── enums
├── user
│   ├── entity
│   └── repository
└── vacancy
    ├── controller
    ├── dto
    ├── entity
    ├── repository
    └── service

Database Schema

The project currently uses the following main tables:

roles
users
companies
vacancies
applications
flyway_schema_history
Relationships
A user belongs to one role
A company belongs to one recruiter
A vacancy belongs to one company
An application belongs to one candidate
An application belongs to one vacancy
Authentication

This project uses JWT (JSON Web Token) authentication.

Public endpoints

These endpoints can be accessed without authentication:

POST /api/auth/register
POST /api/auth/login
GET /api/companies
GET /api/companies/{id}
GET /api/vacancies
GET /api/vacancies/{id}
Swagger endpoints
Protected endpoints

All create, update, delete, and application-related endpoints require a valid JWT token.

How authentication works
A user registers or logs in
The API returns a JWT token
The client sends the token in the Authorization header:

Authorization: Bearer YOUR_TOKEN

API Endpoints
Auth
POST /api/auth/register
POST /api/auth/login
Companies
POST /api/companies
GET /api/companies
GET /api/companies/{id}
PUT /api/companies/{id}
DELETE /api/companies/{id}
Vacancies
POST /api/vacancies
GET /api/vacancies
GET /api/vacancies/{id}
PUT /api/vacancies/{id}
DELETE /api/vacancies/{id}
Applications
POST /api/applications
GET /api/applications/my
GET /api/applications/recruiter
PATCH /api/applications/{id}/status
Sample Requests
Register
POST /api/auth/register
Content-Type: application/json
{
  "fullName": "Ali Valiyev",
  "email": "ali@example.com",
  "password": "123456",
  "role": "RECRUITER"
}
Login
POST /api/auth/login
Content-Type: application/json
{
  "email": "ali@example.com",
  "password": "123456"
}
Create Company
POST /api/companies
Authorization: Bearer YOUR_TOKEN
Content-Type: application/json
{
  "name": "TechNova",
  "description": "Software company",
  "location": "Tashkent"
}
Create Vacancy
POST /api/vacancies
Authorization: Bearer YOUR_TOKEN
Content-Type: application/json
{
  "title": "Java Backend Developer",
  "description": "Spring Boot backend developer needed",
  "location": "Tashkent",
  "salaryFrom": 700.0,
  "salaryTo": 1500.0,
  "companyId": 1
}
Apply to Vacancy
POST /api/applications
Authorization: Bearer YOUR_TOKEN
Content-Type: application/json
{
  "vacancyId": 1
}
Update Application Status
PATCH /api/applications/1/status
Authorization: Bearer YOUR_TOKEN
Content-Type: application/json
{
  "status": "REVIEWING"
}
Validation and Error Handling

The project includes request validation and global exception handling.

Validation examples
empty required fields
invalid email format
short password
missing role
missing vacancy ID
Business rule examples
duplicate email registration
applying to the same vacancy twice
candidate trying to create a company
recruiter trying to edit another recruiter's resources
Example error response
{
  "timestamp": "2026-03-26T23:59:59",
  "status": 403,
  "error": "Forbidden",
  "message": "Only recruiters can create companies",
  "path": "/api/companies"
}
Flyway Migrations

Database schema is managed with Flyway.

Current migrations:

V1__create_roles_table.sql
V2__create_users_table.sql
V3__insert_default_roles.sql
V4__create_companies_table.sql
V5__create_vacancies_table.sql
V6__create_applications_table.sql
Swagger Documentation

Swagger UI is available at:

http://localhost:8080/swagger-ui/index.html

The project includes OpenAPI configuration with JWT bearer authentication support.