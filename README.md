# Task Notes API

A production-style REST API for managing personal tasks, built to demonstrate backend fundamentals with Spring Boot: JWT authentication, layered architecture, validation, global error handling, pagination, automated tests (unit + integration with Testcontainers), API documentation, and containerization.

## Why this project

This is the first of a three-project portfolio showing backend engineering fundamentals for junior Java roles:

1. **task-notes-api** (this repo) — clean CRUD + auth fundamentals
2. [ecommerce-orders-api](#) — complex relationships, role-based access
3. [url-shortener-analytics](#) — caching, async processing, performance

## Tech stack

- **Java 21**, **Spring Boot 3.3**
- **Spring Security** with stateless **JWT** authentication
- **Spring Data JPA** + **PostgreSQL** (H2 for local/dev runs)
- **Bean Validation** for request validation
- **springdoc-openapi** for interactive API docs (Swagger UI)
- **JUnit 5**, **Mockito**, **Testcontainers** for testing
- **Docker** / **Docker Compose** for containerized deployment

## Architecture

```
web/            REST controllers, DTOs, global exception handling
service/        Business logic, transaction boundaries
repository/     Spring Data JPA repositories
domain/         JPA entities (task, user)
security/       JWT generation/validation, authentication filter
config/         Security and OpenAPI configuration
```

Each task belongs to exactly one user. All task endpoints are scoped to the authenticated caller — a user can never read, update, or delete another user's tasks (enforced at the repository query level, not just in the controller).

## Running locally (H2, no Docker)

```bash
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui.html`.

## Running with Docker Compose (PostgreSQL)

```bash
cp .env.example .env   # edit JWT_SECRET for anything beyond local testing
docker compose up --build
```

## API overview

| Method | Endpoint             | Auth required | Description                  |
|--------|----------------------|----------------|-------------------------------|
| POST   | `/api/auth/register` | No             | Create an account, returns JWT |
| POST   | `/api/auth/login`    | No             | Authenticate, returns JWT     |
| POST   | `/api/tasks`         | Yes            | Create a task                 |
| GET    | `/api/tasks`         | Yes            | List tasks (paged, filterable by `status`) |
| GET    | `/api/tasks/{id}`    | Yes            | Get a single task             |
| PUT    | `/api/tasks/{id}`    | Yes            | Update a task                 |
| DELETE | `/api/tasks/{id}`    | Yes            | Delete a task                 |

Authenticated requests send `Authorization: Bearer <token>`.

### Example

```bash
curl -X POST localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"cristian","email":"c@example.com","password":"s3cret-password"}'

curl -X POST localhost:8080/api/tasks \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"title":"Ship the portfolio","description":"Push to GitHub"}'
```

## Testing

```bash
./mvnw test
```

- `TaskServiceTest` — unit tests for business logic (Mockito, no Spring context)
- `TaskControllerTest` — web-layer tests (`@WebMvcTest`, mocked service, validates status codes, auth, validation errors)
- `TaskFlowIntegrationTest` — full end-to-end flow (register → login → create task → access control) against a real PostgreSQL instance via **Testcontainers**, requires Docker running

## Design notes worth asking about in an interview

- Passwords are hashed with **BCrypt**; raw passwords never touch the database.
- JWTs are signed with HMAC-SHA; the secret is injected via environment variable, never hardcoded.
- Ownership checks happen in the repository query (`findByIdAndOwnerUsername`) rather than fetching then comparing in the service — this means a non-owner gets a 404, not a 403, which avoids leaking that the resource exists.
- `Page<T>` responses use Spring Data's DTO serialization mode to avoid exposing internal pagination implementation details in the JSON contract.
