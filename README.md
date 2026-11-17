# Warehouse Management App

[![build](https://github.com/Jarru01/warehouse-management-app/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/Jarru01/warehouse-management-app/actions/workflows/build.yml)

A Spring Boot web application for role-based warehouse management. Three roles
use the system through server-rendered pages, while a documented REST API
exposes the same operations: the **director** hires and dismisses workers and
manages racks, the **worker** moves between rooms and handles goods, and the
**customer** deposits items and later picks them up.

## 🚀 Features

- **Secure login** — Spring Security with BCrypt-hashed passwords, CSRF protection and session fixation protection; customers register their own accounts and can recover a forgotten password by e-mail.
- **HTTPS** — local HTTPS with a self-signed certificate, or production behind an HTTPS reverse proxy with trusted `X-Forwarded-*` headers, `Secure` cookies and HSTS.
- **Multi-instance ready** — sessions (Spring Session JDBC) and login attempt counters live in PostgreSQL, so they survive restarts and are shared by every instance.
- **Role-based areas** — director, worker and customer areas; pages and REST API check the role, and workers and customers can act only on their own ID.
- **REST API** — versionless JSON endpoints under `/api` with request DTOs, Bean Validation and RFC 9457 `ProblemDetail` errors; interactive OpenAPI documentation (Swagger UI) for the director outside production.
- **Performance** — screens load with a constant number of SQL queries regardless of the number of racks (director dashboard: 5), free slots are found with one SQL query, foreign keys and lookups are indexed, and CSS/JS are served under content-hashed URLs cached for a year.
- **Warehouse rules** — racks with fixed capacity and numbered slots, hands-full workers, room-bound picking, recipient-checked pickup, safe dismissal of workers holding goods, and protected single racks in the receiving and dispatch rooms.
- **PostgreSQL persistence** — Spring Data JPA entities with optimistic locking, Flyway schema migrations, seeded with a director and three rooms on first start.
- **Clear errors** — conflicting simultaneous changes return `409` (API) or a message on the page instead of a crash; friendly error pages without stack traces.
- **Brute-force protection** — after 5 failed logins per account or 20 per IP address within 15 minutes, further attempts are refused until the window ends.
- **Audit log** — every login, failed or blocked login, logout and change is stored with who, when and what; the director sees the latest events on the dashboard and through the API. Records older than 365 days are deleted by a nightly maintenance job.
- **Health checks** — `/actuator/health`, `liveness` (process) and `readiness` (includes the database) for monitoring; strict security headers including a Content-Security-Policy.
- **Docker** — multi-stage, layered image running as a non-root user with a built-in health check; one command starts the whole stack behind an HTTPS reverse proxy.
- **Backups** — nightly verified `pg_dump` with daily/weekly/monthly rotation, optional off-site copy to S3-compatible storage, heartbeat for monitoring, and a one-command restore.
- **Deployment guide** — server setup with a real domain and automatic Let's Encrypt certificate, plus monitoring setup, in [`docs/DEPLOYMENT.md`](docs/DEPLOYMENT.md).
- **Tests** — domain services, repositories and both web layers covered by integration tests running against a real PostgreSQL via Testcontainers.

## 🧱 Tech Stack

| Component | Choice |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 4.1 (Spring MVC, Spring Data JPA, Spring Security 7) |
| View layer | Thymeleaf |
| Database | PostgreSQL 17, Flyway migrations |
| Build | Gradle 9 (wrapper included) |
| Tests | JUnit 5, MockMvc, Testcontainers |
| Packaging | Docker (Eclipse Temurin 21 JRE), Caddy as HTTPS reverse proxy |

## ✅ Requirements

- JDK 21
- Docker Desktop installed and running (provides the PostgreSQL container)

## ▶️ Quick Start

1. **Start Docker Desktop** and wait until it is running — the database container needs it.
2. **Start PostgreSQL and the test mail server** from the project folder:

   ```bash
   docker compose up -d
   docker compose ps          # expect warehouse-db "Up (healthy)" and warehouse-mail "Up"
   ```

   E-mails the app sends (password recovery) never leave your machine; read
   them in the Mailpit inbox at <http://localhost:8025>.

3. **Start the application** — pick one:

   ```bash
   # Development mode (recommended while working on the code)
   ./gradlew bootRun

   # Or from the built jar
   ./gradlew bootJar
   java -jar build/libs/warehouse-management-app-2.0.0.jar
   ```

4. **Open <http://localhost:8080>** and log in (see [Login](#-login)). For HTTPS see [HTTPS](#-https).

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

Every role logs in with an ID and a password on the start page.

| Role | Account |
|---|---|
| Director (`Riaditel`) | ID `123`, seeded on first start. Its temporary password comes from `warehouse.riaditel.heslo` (local value in `application-dev.yml`). |
| Worker (`Pracovnik`) | Created by the director with a temporary password. |
| Customer (`Zakaznik`) | Registers on the customer tab with an ID, name, e-mail and password. |

Passwords must have 8 to 72 characters and are stored only as BCrypt hashes.
A temporary password (seeded director, new worker, password reset by the
director) must be changed right after logging in; until then every page leads
to **Zmeniť heslo** and the API answers `403`. Everyone can change their own
password from the top bar, and the director can set a new temporary password
for any worker in the workers table.

**Forgotten password (customers):** *Zabudol si heslo?* on the customer tab
asks for the registration e-mail and sends a one-time link that is valid for
30 minutes. The answer is the same whether the e-mail exists or not, only a
hash of the link's token is stored, and at most one e-mail per customer is sent
every 2 minutes. Links point to `warehouse.verejna-adresa`, never to the
request's `Host` header.

Goods can be sent only to a registered customer whose ID, first name and last
name match.

**Upgrading an existing database:** Flyway upgrades the schema on the next
start. Workers hired before passwords existed cannot log in until the director
sets a temporary password for them in the workers table. Customers registered
before e-mails existed can still log in but cannot use password recovery.

## 🔒 HTTPS

**Locally** — create a self-signed certificate once (stored in `certs/`, which
Git ignores), then start with the `https` profile:

```bash
mkdir certs
keytool -genkeypair -alias warehouse -keyalg RSA -keysize 2048 -validity 825 -storetype PKCS12 -keystore certs/localhost.p12 -storepass changeit -dname "CN=localhost" -ext "SAN=dns:localhost,ip:127.0.0.1"
```

```bash
SPRING_PROFILES_ACTIVE=dev,https ./gradlew bootRun
```

Open <https://localhost:8443>. The browser warns that the certificate is not
trusted, because it is self-signed; continue to the site. The session cookie
is then sent only over HTTPS.

**In production** — run with the `prod` profile behind a reverse proxy that
terminates TLS (nginx, Traefik, a cloud load balancer). The app trusts the
proxy's `X-Forwarded-For` and `X-Forwarded-Proto` headers only from internal
addresses (`TRUSTED_PROXIES`), so login limits and the audit log see the real
client IP, cookies are `Secure` and responses carry HSTS. Minimal nginx site:

```nginx
server {
    listen 443 ssl;
    server_name sklad.example.com;
    ssl_certificate     /etc/letsencrypt/live/sklad.example.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/sklad.example.com/privkey.pem;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

server {
    listen 80;
    server_name sklad.example.com;
    return 301 https://$host$request_uri;
}
```

## 🔌 REST API

**Interactive documentation:** log in as director and open
<http://localhost:8080/swagger-ui.html> (or `/v3/api-docs` for the raw
OpenAPI JSON). Requests use your login session; for `POST` and `DELETE` get a
token from `GET /api/csrf` and enter it under **Authorize** as `X-CSRF-TOKEN`.
The documentation is switched off in the `prod` profile.

| Method | Path | Description |
|---|---|---|
| GET | `/api/status` | Application and database status (`503` when the database is down) |
| GET | `/actuator/health` | Health check (`/liveness`, `/readiness` sub-paths); public |
| GET | `/api/csrf` | CSRF token for the current session |
| POST | `/api/heslo` | Change your own password (`sucasneHeslo`, `noveHeslo`) |
| GET | `/api/miestnosti` | List rooms |
| GET | `/api/miestnosti/{kluc}/regale` | Racks and their items in a room |
| GET | `/api/riaditel/{id}` | Director profile |
| GET | `/api/riaditel/pracovnici` | List workers |
| GET | `/api/riaditel/audit` | Latest audit events (`pocet` up to 200, optional `akcia`, e.g. `NEUSPESNE_PRIHLASENIE`) |
| POST | `/api/riaditel/pracovnici` | Hire a worker (`id`, `meno`, `priezvisko`, `heslo`) |
| DELETE | `/api/riaditel/pracovnici/{id}` | Dismiss a worker |
| POST | `/api/riaditel/pracovnici/{id}/heslo` | Set a new temporary password for a worker (`heslo`) |
| POST | `/api/riaditel/regale` | Add a rack to the main warehouse (`kapacita`) |
| DELETE | `/api/riaditel/regale/{id}` | Remove an empty rack |
| GET | `/api/pracovnik/{id}` | Worker state (room, held item) |
| POST | `/api/pracovnik/{id}/presun` | Move worker to a room (`miestnost`) |
| POST | `/api/pracovnik/{id}/zober` | Pick an item from the current room (`tovarId`) |
| POST | `/api/pracovnik/{id}/uloz` | Store the held item into a free slot |
| POST | `/api/zakaznik/tovar` | Deposit an item (`id`, `nazov`, `vaha`, `odosielatel`, `prijemca`) |
| POST | `/api/zakaznik/{zakaznikId}/vyzdvihnutie/{tovarId}` | Pick up an item ready for release |

The API uses the same login session as the pages. Log in with
`POST /prihlasenie` (form fields `rola`, `id`, `heslo`), keep the session
cookie, and send the CSRF token in the `X-CSRF-TOKEN` header with every
`POST` and `DELETE`. Fetch a fresh token from `/api/csrf` after logging in,
because login rotates the session. Access rules:

| Path | Allowed |
|---|---|
| `/api/status`, `/api/csrf`, `/actuator/health/**`, `/actuator/info` | everyone |
| `/api/heslo` | any logged-in user |
| `/api/riaditel/**` | director |
| `/api/pracovnik/{id}/**` | the worker whose ID is `{id}` |
| `/api/zakaznik/tovar` | a customer, and only as the sender (`odosielatel.id`) |
| `/api/zakaznik/{zakaznikId}/**` | the customer whose ID is `{zakaznikId}` |
| `/api/miestnosti/**` | director and workers |

Errors use `ProblemDetail`: `400` for invalid input, `401` without login,
`403` for the wrong role, someone else's ID or a missing CSRF token, `404` for unknown entities,
`409` for state conflicts (duplicate ID, full rack, hands full, wrong room,
wrong recipient, removing the rack of a small room).

Example:

```bash
# 1. CSRF token for the login form (requires jq)
TOKEN=$(curl -s -c cookies.txt -b cookies.txt http://localhost:8080/api/csrf | jq -r .token)

# 2. Log in as the director (the password is the one from warehouse.riaditel.heslo)
curl -s -c cookies.txt -b cookies.txt -o /dev/null \
  -d rola=RIADITEL -d id=123 --data-urlencode "heslo=$RIADITEL_HESLO" -d "_csrf=$TOKEN" \
  http://localhost:8080/prihlasenie

# 3. New token for the logged-in session, then call the API
#    (a temporary password must first be changed with POST /api/heslo)
TOKEN=$(curl -s -c cookies.txt -b cookies.txt http://localhost:8080/api/csrf | jq -r .token)
curl -b cookies.txt -X POST http://localhost:8080/api/riaditel/pracovnici \
  -H "Content-Type: application/json" -H "X-CSRF-TOKEN: $TOKEN" \
  -d '{"id":"5","meno":"Peter","priezvisko":"Novak","heslo":"zmen-ma-123"}'
```

## 🐳 Docker

The whole application runs in containers with one command: PostgreSQL,
Mailpit, the application (production profile), Caddy as the HTTPS reverse
proxy in front of it, and nightly database backups into `./backups`.

1. Create the local secrets file once and set the director's temporary password:

   ```bash
   cp .env.example .env
   ```

2. Build the image and start everything (stop a locally running app first,
   both use port 8443):

   ```bash
   docker compose --profile app up -d --build
   docker compose --profile app ps     # app and proxy become "healthy"/"Up"
   ```

3. Open <https://localhost:8443>. Caddy signs the certificate with its own local
   authority, so the browser shows a warning; continue to the site. E-mails are
   in Mailpit at <http://localhost:8025>.

The application container publishes no port of its own; only the proxy is
reachable. It runs as the unprivileged `warehouse` user, refuses to start
without its secrets, and reports its state through the Docker health check
(`/actuator/health/liveness`). The image build skips tests; CI runs them and
then builds the image.

| Task | Command |
|---|---|
| Start the full stack | `docker compose --profile app up -d --build` |
| Follow application logs | `docker compose logs -f app` |
| Stop the full stack (keep data) | `docker compose --profile app down` |
| Back up the database now | `docker compose --profile app run --rm backup zaloha` |
| List backups | `docker compose --profile app run --rm backup zoznam` |
| Restore a backup (stop `app` first) | `docker compose --profile app run --rm backup obnov daily/sklad-….dump` |
| Build only the image | `docker build -t warehouse-management-app:2.0.0 .` |

## 🌍 Production server

[`docs/DEPLOYMENT.md`](docs/DEPLOYMENT.md) is the step-by-step guide for a
Linux server with a public domain: DNS and firewall, `.env` from
[`.env.prod.example`](.env.prod.example), starting with
`docker compose -f compose.yaml -f compose.prod.yaml --profile app up -d --build`
(Let's Encrypt certificate, ports 80/443 only, no exposed database, log
rotation), updates, backups with off-site copies and tested restore, and how to
connect uptime and backup monitoring to the health endpoints.

## 🧰 Common Commands

| Task | Command |
|---|---|
| Start database and mail server | `docker compose up -d` |
| Stop them (keep data) | `docker compose down` |
| Reset database | `docker compose down -v` |
| Start the full Docker stack | `docker compose --profile app up -d --build` |
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
- **`Schema-validation` or Flyway error on start** — the database schema does not match the migrations, typically an old database created before passwords existed. Reset it with `docker compose down -v`.
- **Port 5432 already in use** — a locally installed PostgreSQL is running. Stop it or change the published port in `compose.yaml`.
- **Stale or broken data** — reset with `docker compose down -v` followed by `docker compose up -d`.
- **`./gradlew` not recognized on Windows** — use `.\gradlew.bat`.
- **`warehouse-app` keeps restarting** — usually a missing or too short `WAREHOUSE_RIADITEL_HESLO` in `.env`; `docker compose logs app` shows the reason.
- **Port 8443 already in use** — the app is still running locally with the `https` profile; stop it before starting the Docker stack.

## 📁 Architecture

| Package | Responsibility |
|---|---|
| `warehouse.domain` | JPA entities with the warehouse model (`Miestnost`/`VelkySklad`/`MalySklad`, `Regal`, `Tovar`, `Pracovnik`, `Riaditel`, `Zakaznik`, `ZakaznikInfo`), with optimistic locking |
| `warehouse.repository` | Spring Data JPA repositories with fetch-join queries for read models |
| `warehouse.service` | `SkladService` — transactional operations and business rules; `HeslaService` — password rules and hashing; `AuditService` — audit log |
| `warehouse.web.api` | REST controllers, DTOs, mapper and the `ProblemDetail` exception handler |
| `warehouse.web.view` | Thymeleaf controllers, registration, password change and form validation (same DTO rules as the API) |
| `warehouse.security` | Spring Security configuration, login by role, ID and password, login attempt limits, accounts, forced change of temporary passwords |
| `src/main/resources/db/migration` | Flyway migrations (`V1__…` to `V5__…`) |
| `warehouse.config` | Seed data (`DataInitializer`) |

The class diagram is in [`docs/uml.puml`](docs/uml.puml).

## ⚙️ Configuration

Configuration is split into Spring profiles:

| File | Used for | Contents |
|---|---|---|
| `application.yml` | every environment | port, Flyway, `ddl-auto: validate`, error pages without stack traces, session cookie, Actuator, login attempt limits (`warehouse.prihlasenie.*`) |
| `application-dev.yml` | local development (the default profile) | database from `compose.yaml`, director's temporary password, template cache off |
| `application-https.yml` | local HTTPS (`SPRING_PROFILES_ACTIVE=dev,https`) | port 8443 with the keystore from `certs/`, `Secure` session cookie |
| `application-prod.yml` | production behind a reverse proxy (`SPRING_PROFILES_ACTIVE=prod`) | secrets only from environment variables, trusted proxy headers, `Secure` cookie |

Production needs these environment variables; the app refuses to start
without them:

| Variable | Meaning |
|---|---|
| `DB_URL` | JDBC URL, e.g. `jdbc:postgresql://db-host:5432/sklad` |
| `DB_USERNAME` | Database user |
| `DB_PASSWORD` | Database password |
| `WAREHOUSE_RIADITEL_HESLO` | Director's temporary password for the first start |
| `PUBLIC_URL` | Public address used in e-mail links, e.g. `https://sklad.example.com` |
| `MAIL_HOST`, `MAIL_FROM` | SMTP server and sender address for password recovery e-mails |

Optional: `MAIL_PORT` (587), `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_SMTP_AUTH`
and `MAIL_STARTTLS` (both `true`), and `TRUSTED_PROXIES` (a regular expression
of proxy addresses whose `X-Forwarded-*` headers are trusted; private networks
and localhost by default).

The schema is managed by Flyway (`src/main/resources/db/migration`); Hibernate
only validates it. Databases created before Flyway are baselined at version 1
automatically. Change the schema only through a new `V<n>__description.sql`
file. The dev credentials are placeholders for local use; never reuse them.
