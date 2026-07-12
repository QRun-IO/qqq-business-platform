# QQQ Business Platform

Apache-2.0 licensed, Java 21 business platform distribution assembled as a Maven multi-module project.

## Build and test

```bash
./mvnw verify
```

## Run locally

```bash
./mvnw -pl platform-app -am spring-boot:run
```

Health endpoint: `http://localhost:8080/actuator/health`.

## Container image

```bash
./mvnw -pl platform-app -am package jib:dockerBuild
```

Or launch the packaged service and PostgreSQL with `docker compose -f deploy/docker-compose.yml up --build`.

## Modules

- `platform-bom` — dependency management
- `platform-app` — Spring Boot application and empty material-dashboard QInstance assembly
- `qbits/*` — identity, content, newsletter, and CRM extension points
- `db` — Liquibase migrations and seed data
- `web` — Next.js frontend placeholder
- `deploy` — Docker Compose and Helm assets
- `tests` — integration/golden-path test foundation
