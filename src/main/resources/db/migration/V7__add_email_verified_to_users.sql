-- Adds an email verification flag so new users must verify their email before authenticating.
ALTER TABLE users
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;
