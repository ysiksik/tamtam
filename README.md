# TamTam Starter

Spring Boot based starter project for user registration, JWT authentication, and a reusable batch module skeleton.

## Modules

- `tamtam-core`: shared JPA domain and repository code
- `tamtam-api`: REST API for signup, login, and token refresh
- `tamtam-batch`: reusable Spring Batch module with a no-op starter job

## Features

- user signup
- login with JWT access and refresh tokens
- refresh-token reissue flow
- reusable batch module scaffold
- H2 database for local development
- Swagger UI support

## API

- `POST /signup`
- `POST /login`
- `POST /refresh-token`

## Local run

```bash
./gradlew :tamtam-api:bootRun
```

```bash
./gradlew :tamtam-batch:bootRun
```

Swagger UI:

- `http://localhost:8080/swagger-ui/index.html`

## Default seed users

- `user1@example.com` / `password1`
- `user2@example.com` / `password2`
