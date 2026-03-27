-- Creates the companies table and connects each company to the recruiter who owns it.
CREATE TABLE companies
(
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    description  TEXT,
    location     VARCHAR(255) NOT NULL,
    recruiter_id BIGINT       NOT NULL,
    CONSTRAINT fk_companies_recruiter
        FOREIGN KEY (recruiter_id)
            REFERENCES users (id)
);