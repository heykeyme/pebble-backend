# to-do-list-app

A to-do list application built as a set of independently deployable microservices. Every service is its own Spring Boot Maven module under `svc/`, sharing one parent POM for dependency and version management, but each is meant to be built, containerized, and deployed on its own.

## Status

Early scaffold. Three services exist so far:

- **`svc/acc/emc`** — email checker service. `GET /api/v1/auth/email/check` checks whether an email is already registered.
- **`svc/acc/rqc`** — request code service. `POST /api/v1/auth/send-code` issues a 4-digit OTP for pre-registration email verification: rejects emails that are already registered, otherwise generates a code, caches it in Redis (5 min TTL), and publishes a `SendOtpEmailEvent` to RabbitMQ for delivery.
- **`svc/notif/nwk`** — notification worker. No REST API; consumes `SendOtpEmailEvent` off the `notification.email.otp.queue` queue and sends the OTP by email via SMTP (`JavaMailSender`).

### OTP send-code flow

```
[Client]
   │
   ▼ POST /api/v1/auth/send-code
[rqc Service]
   │ 1. Reject if email already registered (Postgres users table)
   │ 2. Generate 4-digit OTP & save to Redis (key "otp:<email>", TTL 5 min)
   │ 3. Publish SendOtpEmailEvent to RabbitMQ exchange
   ▼
[RabbitMQ Exchange: notification.exchange (topic)]
   │
   ▼ Routing Key: notification.email.otp
[Queue: notification.email.otp.queue]
   │
   ▼ @RabbitListener
[nwk: Notification Worker Service]
   │ Sends email via JavaMailSender (SMTP)
   ▼
[User Inbox]
```

The exchange/queue/routing-key names and the `SendOtpEmailEvent` payload live in `libs/common` (`com.todo.common.messaging.NotificationRouting`, `com.todo.common.events.SendOtpEmailEvent`) so `rqc` and `nwk` share the same contract instead of duplicating it.

## Project structure

```
to-do-list-app/
├── pom.xml                  # parent aggregator (packaging=pom) — Spring Boot version mgmt, lists modules
├── mvnw, mvnw.cmd           # Maven wrapper (no local Maven install required)
├── .mvn/wrapper/            # wrapper config
├── libs/                    # shared library modules, depended on by services
│   └── common/              # shared response/error envelopes, event & messaging contracts
│       ├── pom.xml              # plain jar module, spring-boot-maven-plugin repackaging skipped
│       └── src/main/java/com/todo/common/
│           ├── dtos/            # ApiResponse.java
│           ├── errors/          # ApiError.java
│           ├── events/          # SendOtpEmailEvent.java
│           └── messaging/       # NotificationRouting.java — exchange/queue/routing-key names
└── svc/                     # one Maven module per microservice, grouped by domain
    └── acc/
        ├── emc/             # email checker service
        │   ├── pom.xml          # module POM, inherits from the root parent, depends on `common`
        │   ├── Dockerfile
        │   ├── .env.example     # reference env vars; copy to .env for local dev
        │   └── src/
        │       ├── main/
        │       │   ├── java/com/todo/emc/
        │       │   │   ├── EmailCheckerApplication.java
        │       │   │   ├── controllers/     # EmailCheckController.java
        │       │   │   ├── dtos/            # EmailCheckResponse.java
        │       │   │   ├── entity/          # User.java
        │       │   │   ├── repositories/    # EmailCheckerRepository.java
        │       │   │   └── services/        # EmailCheckService.java
        │       │   └── resources/application.yaml
        │       └── test/
        │           └── java/com/todo/emc/EmailCheckerApplicationTests.java
        └── rqc/             # request code service
            ├── pom.xml          # module POM, inherits from the root parent, depends on `common`
            ├── Dockerfile
            ├── .env.example     # reference env vars; copy to .env for local dev
            └── src/
                ├── main/
                │   ├── java/com/todo/rqc/
                │   │   ├── RequestCodeApplication.java
                │   │   ├── config/          # RabbitMQConfig.java
                │   │   ├── controllers/     # SendCodeController.java
                │   │   ├── dtos/            # SendCodeRequest.java, SendCodeResponse.java
                │   │   ├── entity/          # User.java (read-only lookup against the shared users table)
                │   │   ├── exceptions/      # EmailAlreadyRegisteredException.java, GlobalExceptionHandler.java
                │   │   ├── repositories/    # UserRepository.java
                │   │   └── services/        # OtpService.java
                │   └── resources/application.yaml
                └── test/
                    └── java/com/todo/rqc/RequestCodeApplicationTests.java
        └── lgo/             # logout service (scaffold only)
            ├── pom.xml          # module POM, inherits from the root parent, depends on `common`
            ├── Dockerfile
            ├── .env.example     # reference env vars; copy to .env for local dev
            └── src/
                ├── main/
                │   ├── java/com/todo/lgo/
                │   │   └── LogoutApplication.java
                │   └── resources/application.yaml
                └── test/
                    └── java/com/todo/lgo/LogoutApplicationTests.java
    └── notif/
        └── nwk/             # notification worker service (RabbitMQ consumer, no REST API)
            ├── pom.xml          # module POM, inherits from the root parent, depends on `common`
            ├── Dockerfile
            ├── .env.example     # reference env vars; copy to .env for local dev
            └── src/
                ├── main/
                │   ├── java/com/todo/nwk/
                │   │   ├── NotificationWorkerApplication.java
                │   │   ├── config/          # RabbitMQConfig.java
                │   │   ├── listeners/       # OtpEmailListener.java
                │   │   └── services/        # OtpMailService.java
                │   └── resources/application.yaml
                └── test/
                    └── java/com/todo/nwk/NotificationWorkerApplicationTests.java
```

## Prerequisites

- JDK 17
- A PostgreSQL instance reachable by any service you run
- A Redis instance (used by `rqc` to cache OTPs)
- A RabbitMQ instance (used by `rqc` to publish and `nwk` to consume `SendOtpEmailEvent`) — connect on the AMQP port (default `5672`), not the management UI port (`15672`)
- An SMTP account (used by `nwk` to send OTP emails)
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
| `libs/common` | `common` | plain jar | Shared DTOs, error types, and event/messaging contracts used by every service (`ApiResponse<T>`, `ApiError`, `SendOtpEmailEvent`, `NotificationRouting`). Not repackaged as an executable jar — it exists purely to be depended on. |
| `svc/acc/emc` | `emc` | Spring Boot app | Email checker service. Depends on `common`, `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, and the PostgreSQL driver. |
| `svc/acc/rqc` | `rqc` | Spring Boot app | Request code service. Depends on `common`, `spring-boot-starter-web`, `spring-boot-starter-data-jpa` + PostgreSQL driver (email-registered lookup), `spring-boot-starter-data-redis` (OTP cache), `spring-boot-starter-amqp` (publishes to RabbitMQ), `spring-boot-starter-validation`. |
| `svc/acc/lgo` | `lgo` | Spring Boot app | Logout service (scaffold only, no endpoints yet). Depends on `common` and `spring-boot-starter-web`. |
| `svc/notif/nwk` | `nwk` | Spring Boot app (no web) | Notification worker. Depends on `common`, `spring-boot-starter-amqp` (consumes from RabbitMQ), `spring-boot-starter-mail` (sends via SMTP). |

### `emc` environment variables

| Variable | Description |
|---|---|
| `EMC_DB_URL` | JDBC URL of the PostgreSQL database |
| `EMC_DB_USERNAME` | Database username |
| `EMC_DB_PASSWORD` | Database password |
| `EMC_SERVER_PORT` | Port the service listens on |

### `rqc` environment variables

| Variable | Description |
|---|---|
| `RQC_DB_URL` | JDBC URL of the PostgreSQL database (same `users` table as `emc`) |
| `RQC_DB_USERNAME` | Database username |
| `RQC_DB_PASSWORD` | Database password |
| `RQC_REDIS_HOST` | Redis host |
| `RQC_REDIS_PORT` | Redis port |
| `RQC_REDIS_PASSWORD` | Redis password |
| `RQC_RABBITMQ_HOST` | RabbitMQ host |
| `RQC_RABBITMQ_PORT` | RabbitMQ AMQP port (default `5672`, **not** the `15672` management UI port) |
| `RQC_RABBITMQ_USERNAME` | RabbitMQ username |
| `RQC_RABBITMQ_PASSWORD` | RabbitMQ password |
| `RQC_SERVER_PORT` | Port the service listens on |

### `lgo` environment variables

| Variable | Description |
|---|---|
| `LGO_SERVER_PORT` | Port the service listens on |

### `nwk` environment variables

| Variable | Description |
|---|---|
| `NWK_RABBITMQ_HOST` | RabbitMQ host |
| `NWK_RABBITMQ_PORT` | RabbitMQ AMQP port (default `5672`, **not** the `15672` management UI port) |
| `NWK_RABBITMQ_USERNAME` | RabbitMQ username |
| `NWK_RABBITMQ_PASSWORD` | RabbitMQ password |
| `NWK_MAIL_HOST` | SMTP host |
| `NWK_MAIL_PORT` | SMTP port |
| `NWK_MAIL_USERNAME` | SMTP username |
| `NWK_MAIL_PASSWORD` | SMTP password |

## Adding a new service

1. Create `svc/<domain>/<service>/` with its own `pom.xml`, parented to the root `com.todo:todo-list-app`.
2. Register the new module path in the root `pom.xml`'s `<modules>` list.
3. Add a `.env.example` documenting any required environment variables.
4. Depend on `com.todo:common:${project.version}` to reuse `ApiResponse<T>` / `ApiError` for consistent response shapes.