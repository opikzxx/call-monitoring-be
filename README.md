# Call Monitoring — Backend

**Version**: v1.0.0
**Last Updated**: 2026-08-25
**Purpose**: Backend REST API for the Call Monitoring take-home test — serves authentication and the call-monitoring dataset (search, period filter, sentiment filter, sort, pagination) backed by PostgreSQL.

> Related: [Frontend repository](https://github.com/opikzxx/call-monitoring-fe) · User story: `THT-MON-US-001`

---

## 📋 Project Overview

This app implements the backend half of **`THT-MON-US-001`**: a Supervisor logs in via `/api/auth/login`, then the [frontend](https://github.com/opikzxx/call-monitoring-fe) calls `GET /api/call-monitoring` to render a table with search, period filter, sentiment filter, sortable columns, and pagination — all backed by PostgreSQL, no data hardcoded anywhere in this API.

### Technology Stack

- **Java 21** (Eclipse Temurin) — check version with `java -version`
- **Spring Boot 4.1.1** — Web MVC, Data JPA, Security, Validation, Actuator
- **PostgreSQL 16** + **Flyway** — schema migrations and seed data
- **Spring Security** + **JJWT 0.12.6** — stateless JWT access-token auth (no refresh token, no server-side session)
- **Lombok** — compile-time getter/setter generation
- **Gradle** (wrapper) — build tool
- **JUnit 5** + **Mockito** + **AssertJ** — unit tests
- **Testcontainers** — integration test that spins up a real PostgreSQL container
- **Docker** — containerized dev (hot-reload) and production builds

### Documentation

- **Frontend repository**: https://github.com/opikzxx/call-monitoring-fe
- **User story**: `THT-MON-US-001` (see take-home test materials)

---

## 🏗️ Architecture

### Layer-Based Structure

Code is grouped by architectural layer under `src/main/java/com/callmonitoring/backend/`:

```
com.callmonitoring.backend/
├── controller/        # thin REST controllers — delegate to services, no business logic
├── service/           # business logic (validation of query params, orchestration)
├── repository/        # Spring Data JPA repositories (+ JpaSpecificationExecutor)
├── entity/            # JPA entities
│   └── specification/ # dynamic query builders for search/filter predicates
├── dto/
│   ├── request/       # inbound request shapes + enums (e.g. SentimentFilter)
│   └── response/      # outbound response records (e.g. CallMonitoringResponse, PageResponse)
├── security/           # JwtTokenProvider, JwtAuthenticationFilter, JwtAuthenticationEntryPoint, UserDetailsService
├── config/             # SecurityConfig (filter chain, CORS, password encoder, AuthenticationManager)
└── exception/          # GlobalExceptionHandler + shared ErrorResponse record
```

**Reference implementations**: `controller/AuthController.java` (simplest) and `controller/CallMonitoringController.java` + `service/CallMonitoringService.java` + `entity/specification/CallMonitoringSpecification.java` (full search/filter/sort/pagination flow).

**Key rules**:
- Controllers never touch repositories directly — always through a service.
- Entities are never returned from a controller — every endpoint returns a DTO (`record`), mapped via a static `from(entity)` factory.
- Dynamic filtering (search / period / sentiment) is built with JPA `Specification`, not hand-written JPQL or native SQL.
- Error responses share one shape — `exception/ErrorResponse.java` — used by both `GlobalExceptionHandler` (controller-level errors) and `JwtAuthenticationEntryPoint` (filter-level 401s), so the JSON envelope never drifts between the two.
- Auth is intentionally minimal: a signed JWT access token only. No refresh token, no roles/permissions tables, no server-side token store.

---

## 🚀 Development Workflow

### Docker-First Approach

```bash
make quick   # copy .env.example -> .env, run unit tests, then docker compose up
```

Or step by step:

```bash
make env     # copy .env.example -> .env (only if .env doesn't exist yet)
make up      # start db + app (hot-reload via bind mount)
make logs    # follow the app container's logs
make down    # stop and remove the containers
```

The API will be available at `http://localhost:8080`. On first boot, Flyway runs all migrations automatically (schema + seed data), so there's nothing to run manually.

### Running Without Docker

Requires JDK 21 and a local PostgreSQL instance:

```bash
./gradlew bootRun
```

> **Note**: This project targets **JDK 21** (Gradle toolchain). If your machine only has an older JDK and Gradle can't auto-provision one, either install JDK 21 or just use `make up` — the Docker image already bundles JDK 21.

### Pre-Commit Checklist

```bash
make test    # ./gradlew test — unit + integration tests
```

---

## 🧪 Testing

### Unit Tests (JUnit 5 + Mockito + AssertJ)

Fast, no Spring context, no database:

- `JwtTokenProviderTest` — token generation, validation, expiry, wrong-signature rejection
- `AuthServiceTest` — login delegates to `AuthenticationManager` and issues a token
- `CallMonitoringServiceTest` — sort-field whitelist validation, date-range validation, response mapping + pagination

### Integration Test (Testcontainers)

- `BackendApplicationTests` — boots the full Spring context against a real PostgreSQL container to verify wiring end-to-end.

```bash
make test          # runs the full suite
```

> **Note**: The integration test needs Docker socket access to spin up its own Postgres container. Running it *inside* a nested container (e.g. CI-in-CI, sandboxed environments) can fail on the reaper/network layer even though the app itself is fine — if you hit that, run it on a normal Docker host instead.

---

## ⚙️ Configuration

### Environment Variables

| Variable | Description | Default |
|---|---|---|
| `POSTGRES_DB` | Database name | `mydatabase` |
| `POSTGRES_USER` | Database user | `myuser` |
| `POSTGRES_PASSWORD` | Database password | `secret` |
| `DB_PORT` | Host port Postgres is published on | `5432` |
| `APP_PORT` | Host port the API is published on | `8080` |
| `JWT_SECRET` | HMAC secret used to sign/validate access tokens | `dev-only-secret-key-...` *(change in production)* |
| `JWT_EXPIRATION_MS` | Access token lifetime, in milliseconds | `3600000` (1 hour) |
| `CORS_ALLOWED_ORIGINS` | Origin allowed to call this API from a browser | `http://localhost:3000` |

Copy `.env.example` to `.env` and adjust as needed:

```bash
cp .env.example .env
```

All values have safe defaults baked into `application.properties`, so the app also runs without a `.env` file present.

---

## 🔧 Quick Reference

### Essential Commands

| Task | Command |
|---|---|
| Start dev stack (Docker) | `make up` |
| Copy env + test + start, one shot | `make quick` |
| Stop containers | `make down` |
| View logs | `make logs` |
| Run tests | `make test` |
| Copy env template | `make env` |
| Run without Docker | `./gradlew bootRun` |
| Build production jar | `./gradlew bootJar` |
| Clean restart | `make down && make up` |

### API Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/login` | Public | Login with `email` + `password`, returns a JWT access token |
| GET | `/api/call-monitoring` | Bearer token | List call records — `search`, `startDate`, `endDate`, `sentiment` (`BELOW_70`/`AT_LEAST_70`), `sortBy`, `sortDir`, `page` (5 records/page, fixed) |
| GET | `/actuator/health` | Public | Health check |

### Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/callmonitoring/backend/
│   │   │   ├── controller/         # AuthController, CallMonitoringController
│   │   │   ├── service/            # AuthService, CallMonitoringService
│   │   │   ├── repository/         # UserRepository, CallMonitoringRepository
│   │   │   ├── entity/             # User, CallMonitoring, Role
│   │   │   │   └── specification/  # CallMonitoringSpecification
│   │   │   ├── dto/
│   │   │   │   ├── request/        # LoginRequest, SentimentFilter
│   │   │   │   └── response/       # TokenResponse, CallMonitoringResponse, PageResponse
│   │   │   ├── security/           # JwtTokenProvider, JwtAuthenticationFilter, ...
│   │   │   ├── config/             # SecurityConfig
│   │   │   └── exception/          # GlobalExceptionHandler, ErrorResponse
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/       # Flyway: V1 users, V2 call_monitoring, V3 seed data
│   └── test/java/com/callmonitoring/backend/
│       ├── security/                # JwtTokenProviderTest
│       └── service/                  # AuthServiceTest, CallMonitoringServiceTest
├── Dockerfile                       # development / builder / production stages
├── docker-compose.dev.yml           # db + app dev stack, hot reload
├── Makefile                         # quick / env / up / down / logs / test
└── .env.example
```

---

## 💡 Notes for AI Assistants

1. **Follow the layer-based structure**: controller → service → repository/specification, mirrored from `CallMonitoringController` / `CallMonitoringService` / `CallMonitoringSpecification`.
2. **Never return entities from a controller** — always map to a response DTO record.
3. **Auth stays minimal on purpose**: JWT access token only. Don't add refresh tokens, roles/permissions tables, or Redis-backed sessions without being explicitly asked.
4. **`sortBy` is whitelisted** in `CallMonitoringService.SORTABLE_FIELDS` — extend that set when adding a new sortable column, don't remove the check.
5. **This project targets JDK 21** — verify with `java -version` (or `/usr/libexec/java_home -V` on macOS) before assuming `./gradlew` will just work on a given machine; otherwise use `make up`, whose Docker image already has JDK 21.
6. **Error responses go through `ErrorResponse`** — add new exception handlers to `GlobalExceptionHandler` and reuse `ErrorResponse.of(...)`, don't hand-roll a new JSON shape.

---

## 🔗 Additional Resources

- **Frontend repository**: https://github.com/opikzxx/call-monitoring-fe
- **This repository**: https://github.com/opikzxx/call-monitoring-be
