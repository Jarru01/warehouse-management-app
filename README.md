# Warehouse Management App

[![build](https://github.com/Jarru01/warehouse-management-app/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/Jarru01/warehouse-management-app/actions/workflows/build.yml)

A Spring Boot web application for role-based warehouse management. Three roles
use the system through server-rendered pages, while a documented REST API
exposes the same operations: the **director** hires and dismisses workers and
manages racks, the **worker** moves between rooms and handles goods, and the
**customer** deposits items and later picks them up.

## 🚀 Features

- **Role-based areas** — session login for director, worker and customer, with URL-pattern access control.
- **REST API** — versionless JSON endpoints under `/api` with request DTOs, Bean Validation and RFC 9457 `ProblemDetail` errors.
- **Warehouse rules** — racks with fixed capacity and numbered slots, hands-full workers, room-bound picking, recipient-checked pickup, safe dismissal of workers holding goods.
- **PostgreSQL persistence** — Spring Data JPA entities and repositories, seeded with a director and three rooms on first start.
- **Tests** — domain services, repositories and both web layers covered by integration tests running against a real PostgreSQL via Testcontainers.

## 🧱 Tech Stack

| Component | Choice |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 4.1 (Spring MVC, Spring Data JPA) |
| View layer | Thymeleaf |
| Database | PostgreSQL 17 |
| Build | Gradle 9 (wrapper included) |
| Tests | JUnit 5, MockMvc, Testcontainers |

## ✅ Requirements

- JDK 21
- Docker Desktop installed and running (provides the PostgreSQL container)

## ▶️ Quick Start

1. **Start Docker Desktop** and wait until it is running — the database container needs it.
2. **Start PostgreSQL** from the project folder:

   ```bash
   docker compose up -d
   docker compose ps          # expect warehouse-db with status "Up (healthy)"
   ```

3. **Start the application** — pick one:

   ```bash
   # Development mode (recommended while working on the code)
   ./gradlew bootRun

   # Or from the built jar
   ./gradlew bootJar
   java -jar build/libs/warehouse-management-app-2.0.0.jar
   ```

4. **Open <http://localhost:8080>** and log in (see [Login](#-login)).

On Windows use `gradlew.bat` instead of `./gradlew`. Both `bootRun` and
`java -jar` keep running until you stop them with `Ctrl+C` in the terminal.

Stop and reset:

| Command | Effect |
|---|---|
| `Ctrl+C` in the app terminal | Stops the application |
| `docker compose down` | Stops the database and **keeps data** |
| `docker compose down -v` | Stops the database and **deletes all data** (fresh start next run) |
| `docker compose ps` | Shows container status |
| `docker compose logs -f db` | Follows database logs |

## 🔑 Login

| Role | Credentials |
|---|---|
| Director (`Riaditel`) | ID `123` (seeded on first start) |
| Worker (`Pracovnik`) | ID created by the director |
| Customer (`Zakaznik`) | ID, first name and last name (identity is asserted, no password) |

Session login is intentionally simple for this project; Spring Security with
passwords and CSRF protection is a possible future step.

## 🔌 REST API

| Method | Path | Description |
|---|---|---|
| GET | `/api/status` | Application and database status |
| GET | `/api/miestnosti` | List rooms |
| GET | `/api/miestnosti/{kluc}/regale` | Racks and their items in a room |
| GET | `/api/riaditel/{id}` | Director profile |
| GET | `/api/riaditel/pracovnici` | List workers |
| POST | `/api/riaditel/pracovnici` | Hire a worker (`id`, `meno`, `priezvisko`) |
| DELETE | `/api/riaditel/pracovnici/{id}` | Dismiss a worker |
| POST | `/api/riaditel/regale` | Add a rack to the main warehouse (`kapacita`) |
| DELETE | `/api/riaditel/regale/{id}` | Remove an empty rack |
| GET | `/api/pracovnik/{id}` | Worker state (room, held item) |
| POST | `/api/pracovnik/{id}/presun` | Move worker to a room (`miestnost`) |
| POST | `/api/pracovnik/{id}/zober` | Pick an item from the current room (`tovarId`) |
| POST | `/api/pracovnik/{id}/uloz` | Store the held item into a free slot |
| POST | `/api/zakaznik/tovar` | Deposit an item (`id`, `nazov`, `vaha`, `odosielatel`, `prijemca`) |
| POST | `/api/zakaznik/{zakaznikId}/vyzdvihnutie/{tovarId}` | Pick up an item ready for release |

Errors use `ProblemDetail`: `400` for invalid input, `404` for unknown
entities, `409` for state conflicts (duplicate ID, full rack, hands full,
wrong room, wrong recipient).

Example:

```bash
curl -X POST http://localhost:8080/api/riaditel/pracovnici \
  -H "Content-Type: application/json" \
  -d '{"id":"5","meno":"Peter","priezvisko":"Novak"}'
```

## 🧰 Common Commands

| Task | Command |
|---|---|
| Start database | `docker compose up -d` |
| Stop database (keep data) | `docker compose down` |
| Reset database | `docker compose down -v` |
| Run app in development mode | `./gradlew bootRun` |
| Build runnable jar | `./gradlew bootJar` |
| Build and run all tests | `./gradlew build` |
| Run tests only | `./gradlew test` |
| Generate API documentation | `./gradlew javadoc` |

## 🛠️ Build & Test

```bash
./gradlew build      # compile, run all tests, build the jar
./gradlew test       # tests only (Testcontainers starts PostgreSQL automatically)
./gradlew bootJar    # runnable jar: build/libs/warehouse-management-app-2.0.0.jar
./gradlew javadoc    # API documentation
```

The test suite does not need the database from Quick Start: Testcontainers
starts its own disposable PostgreSQL instance.

## ❗ Troubleshooting

- **`Connection refused` or `"database":"chyba"` from `/api/status`** — Docker Desktop is not running or the container is not up yet. Start Docker Desktop, then run `docker compose up -d`.
- **Container reported as `starting` or `unhealthy`** — first start takes a few seconds; check `docker compose logs db` if it persists.
- **Port 8080 already in use** — another process or an older app instance is using it. Stop it or change `server.port` in `src/main/resources/application.yml`.
- **Port 5432 already in use** — a locally installed PostgreSQL is running. Stop it or change the published port in `compose.yaml`.
- **Stale or broken data** — reset with `docker compose down -v` followed by `docker compose up -d`.
- **`./gradlew` not recognized on Windows** — use `.\gradlew.bat`.

## 📁 Architecture

| Package | Responsibility |
|---|---|
| `warehouse.domain` | JPA entities with the warehouse model (`Miestnost`/`VelkySklad`/`MalySklad`, `Regal`, `Tovar`, `Pracovnik`, `Riaditel`, `ZakaznikInfo`) |
| `warehouse.repository` | Spring Data JPA repositories with fetch-join queries for read models |
| `warehouse.service` | `SkladService` — all transactional operations and business rules |
| `warehouse.web.api` | REST controllers, DTOs, mapper and the `ProblemDetail` exception handler |
| `warehouse.web.view` | Thymeleaf controllers, session login and role interceptors |
| `warehouse.config` | Seed data (`DataInitializer`) |

The class diagram is in [`docs/uml.puml`](docs/uml.puml).

## ⚙️ Configuration

`src/main/resources/application.yml` holds the datasource (default
`jdbc:postgresql://localhost:5432/sklad`, user and password `warehouse`),
Hibernate schema handling (`ddl-auto: update`) and the server port (`8080`).
Every property can be overridden with environment variables, for example
`SPRING_DATASOURCE_URL` or `SERVER_PORT`.

Development credentials are placeholders for local use; do not reuse them in
other environments. Database migrations (Flyway) are a future step.
