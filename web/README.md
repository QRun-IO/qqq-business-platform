# QQQ Business Platform Web

Next.js 14 App Router frontend for the QQQ platform.

## Run

```bash
cp .env.example .env.local
npm install
npm run dev
```

Requires Keycloak on `localhost:18081` and the platform API on `localhost:18080`. The configured Keycloak realm/client are `qqq-platform` / `platform-web`. Test users are documented in the compose realm import.

## Workflows

- Keycloak registration, login and logout
- Protected member portal and editable profile
- Public blog list/details
- Staff content library and TipTap post editor
- Staff media uploads (`POST /api/staff/v1/files`)
- Double opt-in newsletter signup and confirmation
- CRM, campaign, user and content administration views

## Mandatory validation

- `npm run test:e2e` runs Playwright against the real API and Keycloak services; it never mocks them. Playwright starts the frontend on port 3004 when needed.
- `npm run build` automatically runs type checks, unit tests, and the complete E2E suite first. A failed or unavailable live service blocks the build.
- `npm install`/`npm ci` enables the repository pre-commit hook. Commits touching platform code, infrastructure, tests, or workflows run the E2E suite and are rejected on failure.
- `.github/workflows/e2e.yml` starts the actual Compose services on every pull request, main push, nightly schedule, and manual dispatch. Its `Deployment gate / E2E passed` status cannot succeed when E2E fails.
- HTML, JUnit, traces, screenshots, and videos are retained as workflow artifacts. Failures appear in the Actions summary and can be sent to a JSON incoming webhook (including Slack-compatible receivers) by defining the `E2E_ALERT_WEBHOOK_URL` Actions secret.

Repository administrators should make **Required E2E validation / E2E / live services** and **Deployment gate / E2E passed** required branch-protection checks. Deployment workflows must depend on the deployment-gate job (or this workflow's successful conclusion); do not permit manual success overrides.

Local hooks require the live stack at the defaults below. Override these variables only to point to another real environment:

```bash
E2E_API_URL=http://localhost:18082 \
E2E_KEYCLOAK_URL=http://localhost:18081 \
npm run test:e2e
```
