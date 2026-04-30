# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build
./gradlew.bat build

# Run all tests
./gradlew.bat test

# Run a single test class
./gradlew.bat test --tests "io.github.jaehyeonhan.project.service.ChatServiceTest"

# Start with Docker Compose (requires .env in project root)
docker compose up

# Local run (requires DB env vars set)
./gradlew.bat bootRun
```

**Environment variables** (set in `.env` or shell):
| Variable | Default | Notes |
|---|---|---|
| `DB_URL` | — | e.g. `jdbc:postgresql://postgres:5432/chat` |
| `DB_USER` | `user` | |
| `DB_PASSWORD` | `pass` | |
| `DB_NAME` | `chat` | |
| `SQL_INIT_MODE` | `never` | Use `always` to re-run `schema.sql` on startup |

## Architecture

Single Gradle module (`app`) with a classic layered architecture:

```
Controller → Service → Repository (interface) → JPA impl → PostgreSQL
```

**Package layout** (`io.github.jaehyeonhan.project`):
- `controller/` — REST endpoints, request/response DTOs under `dto/request` and `dto/response`
- `service/` — business logic (`ChatService`) and validation (`ChatValidationService`)
- `repository/` — repository interfaces; JPA implementations live in `repository/jpa/`
- `entity/` — JPA entities (`Chat`, `Message`, `Participation`, `Block`, `ParticipationRole`)
- `exception/` — domain exceptions + `ChatControllerAdvice` (global `@RestControllerAdvice`)
- `config/` — `ClockConfig` (injects a `Clock` bean for testable time)
- `constant/` — `TimeConstant` (block duration bounds)

**Domain rules:**
- Participation roles: `CREATOR > MANAGER > USER`
- CREATOR/MANAGER can block users for 5–30 minutes or permanently (duration `0` = infinite); only the blocker can retract a block
- Message polling uses a `lastRead` cursor (return messages newer than that ID)

**Infrastructure** (`infra/`): Redis Sentinel (1 master + 2 replicas) with OpenResty for reverse-proxy and Lua-based token-bucket rate limiting. This infra is separate from the main app's `docker-compose.yml`.

## Testing

- **Unit tests** mock `ChatValidationService` and repositories via Mockito
- **Integration tests** (`ChatServiceIntegrationTest`) spin up a real PostgreSQL container via Testcontainers — no mocking of the DB layer
- CI runs tests on PR via GitHub Actions (`.github/workflows/run-tests.,yml`)
- SonarCloud static analysis runs on PR (`.github/workflows/sonarcloud-analyze.yml`)
- method name should follow "given_condition_when_execution_then_verification" pattern

## Database

Schema is managed manually in `app/src/main/resources/schema.sql` (Hibernate DDL is set to `none`). Tables: `chat`, `participation`, `message`, `block`.

Swagger UI is available at `/swagger-ui.html` when the app is running (SpringDoc OpenAPI).
