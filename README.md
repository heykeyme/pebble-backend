# to-do-list-app

A to-do list application built as a set of independently deployable microservices. Every service is its own Spring Boot Maven module under `svc/`, sharing one parent POM for dependency and version management, but each is meant to be built, containerized, and deployed on its own.

## Status

Early scaffold. One service exists so far:

- **`svc/acc/emc`** — email checker service. Has a Spring Boot app, a Postgres datasource wired up via env vars, and a placeholder test. No REST endpoints or business logic yet.

## Project structure

```
to-do-list-app/
├── pom.xml                  # parent aggregator (packaging=pom) — Spring Boot version mgmt, lists modules
├── mvnw, mvnw.cmd           # Maven wrapper (no local Maven install required)
├── .mvn/wrapper/            # wrapper config
├── libs/                    # shared library modules, depended on by services
│   └── common/              # ApiResponse<T> / ApiError — shared response & error envelopes
│       ├── pom.xml              # plain jar module, spring-boot-maven-plugin repackaging skipped
│       └── src/main/java/com/todo/common/
│           ├── dtos/            # ApiResponse.java
│           └── errors/          # ApiError.java
└── svc/                     # one Maven module per microservice, grouped by domain
    └── acc/
        └── emc/             # email checker service
            ├── pom.xml          # module POM, inherits from the root parent, depends on `common`
            ├── Dockerfile       # placeholder — not yet written
            ├── .env.example     # reference env vars; copy to .env for local dev
            └── src/
                ├── main/
                │   ├── java/com/todo/emc/
                │   │   ├── EmailCheckerApplication.java
                │   │   ├── controllers/     # EmailCheckController.java
                │   │   ├── dtos/            # EmailCheckResponse.java
                │   │   ├── entity/          # User.java
                │   │   ├── repositories/    # EmailCheckerRepository.java
                │   │   └── services/        # EmailCheckService.java
                │   └── resources/application.yaml
                └── test/
                    └── java/com/todo/app/AppApplicationTests.java
```

## Prerequisites

- JDK 17
- A PostgreSQL instance reachable by any service you run
- No local Maven install needed — use the bundled `./mvnw` / `mvnw.cmd`

## Getting started

Build everything from the repo root:

```
./mvnw clean install
```

Configure a service before running it — copy its `.env.example` to `.env` and fill in real values:

```
cp svc/acc/emc/.env.example svc/acc/emc/.env
```

`.env` is loaded automatically at startup via `springboot4-dotenv` and is git-ignored — never commit it.

Run a single service:

```
./mvnw -pl svc/acc/emc spring-boot:run
```

## Modules

| Path | Module | Type | Purpose |
|---|---|---|---|
| `libs/common` | `common` | plain jar | Shared DTOs and error types used by every service (`ApiResponse<T>`, `ApiError`). Not repackaged as an executable jar — it exists purely to be depended on. |
| `svc/acc/emc` | `emc` | Spring Boot app | Email checker service. Depends on `common`, `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, and the PostgreSQL driver. |

### `emc` environment variables

| Variable | Description |
|---|---|
| `EMC_DB_URL` | JDBC URL of the PostgreSQL database |
| `EMC_DB_USERNAME` | Database username |
| `EMC_DB_PASSWORD` | Database password |

## Adding a new service

1. Create `svc/<domain>/<service>/` with its own `pom.xml`, parented to the root `com.todo:todo-list-app`.
2. Register the new module path in the root `pom.xml`'s `<modules>` list.
3. Add a `.env.example` documenting any required environment variables.
4. Depend on `com.todo:common:${project.version}` to reuse `ApiResponse<T>` / `ApiError` for consistent response shapes.