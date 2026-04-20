-- Seed users for local development.
MERGE INTO users (email, password, created_at) KEY(email)
    VALUES ('user1@example.com', '{noop}password1', CURRENT_TIMESTAMP),
    ('user2@example.com', '{noop}password2', CURRENT_TIMESTAMP);
