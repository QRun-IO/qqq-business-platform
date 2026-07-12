# Database migrations

Liquibase owns the PostgreSQL schema. The application does not mutate schema at startup.

## Contexts

- `baseline` (default): required platform and QBit schema. Safe for every environment.
- `demo`: optional deterministic sample data. Run together with baseline using `baseline,demo`.

A changeset that has no context runs under every context filter, so every changeset in this project **must declare a context**.

## Local commands

From `db/`, set credentials in the environment or override the non-secret local defaults in `liquibase.properties`:

```bash
mvn liquibase:validate
mvn liquibase:update -Dliquibase.contexts=baseline
mvn liquibase:update -Dliquibase.contexts=baseline,demo
```

Compose performs migration as a one-shot dependency before the app starts:

```bash
docker compose -f deploy/docker-compose.yml up --build
LIQUIBASE_CONTEXTS=baseline,demo docker compose -f deploy/docker-compose.yml up --build
```

## Adding a QBit

1. Treat the QBit's physical schema metadata as authoritative; reconcile every table, column, default, constraint, FK, and index.
2. Create `db/changelog/qbits/<qbit>/NNN-description.yaml` with immutable, globally unique IDs and `context: baseline`.
3. Put optional fixed-value demo rows in `db/changelog/demo/<qbit>/NNN-description.yaml` with `context: demo`.
4. Append both includes explicitly to `db.changelog-master.yaml`; do not use `includeAll`.
5. Extend a Testcontainers inventory assertion, then run `mvn -pl db verify` against a clean PostgreSQL container.

## Forward-only policy

Released changesets are append-only: never edit, reorder, delete, bypass checksums, use `runOnChange`/`runAlways`, or add rollback blocks. Corrections are new changesets. Production rollback means stop application traffic, restore a verified pre-deployment database backup/PITR point, and redeploy the previous application. `ForwardOnlyPolicyTest` enforces the file-level rules; Liquibase checksums enforce immutability after deployment.

Before release: back up, run `liquibase:validate`, review `liquibase:updateSQL`, migrate staging from an empty database and a production-like snapshot, then verify table inventory and row counts.
