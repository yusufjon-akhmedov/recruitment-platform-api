CREATE TABLE vacancies
(
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255)     NOT NULL,
    description TEXT,
    location    VARCHAR(255)     NOT NULL,
    salary_from DOUBLE PRECISION NOT NULL,
    salary_to   DOUBLE PRECISION NOT NULL,
    company_id  BIGINT           NOT NULL,
    created_at  TIMESTAMP        NOT NULL,
    CONSTRAINT fk_vacancies_company
        FOREIGN KEY (company_id)
            REFERENCES companies (id)
);