-- Creates the applications table that tracks candidate submissions and recruiter review status.
CREATE TABLE applications
(
    id           BIGSERIAL PRIMARY KEY,
    candidate_id BIGINT      NOT NULL,
    vacancy_id   BIGINT      NOT NULL,
    status       VARCHAR(50) NOT NULL,
    created_at   TIMESTAMP   NOT NULL,
    CONSTRAINT fk_applications_candidate
        FOREIGN KEY (candidate_id)
            REFERENCES users (id),
    CONSTRAINT fk_applications_vacancy
        FOREIGN KEY (vacancy_id)
            REFERENCES vacancies (id)
);