import { expect, Page, TestInfo } from '@playwright/test';

export const API_URL = process.env.E2E_API_URL || 'http://localhost:18082';
export const KEYCLOAK_URL = process.env.E2E_KEYCLOAK_URL || 'http://localhost:18081';

export function captureDiagnostics(page: Page, testInfo: TestInfo) {
  const consoleErrors: string[] = [];
  const failedRequests: string[] = [];
  const serverErrors: string[] = [];

  page.on('console', message => {
    if (message.type() === 'error') consoleErrors.push(message.text());
  });
  page.on('pageerror', error => consoleErrors.push(`PAGE ERROR: ${error.message}`));
  page.on('requestfailed', request =>
    failedRequests.push(`${request.method()} ${request.url()} — ${request.failure()?.errorText}`),
  );
  page.on('response', response => {
    if (response.status() >= 500) serverErrors.push(`${response.status()} ${response.url()}`);
  });

  return async () => {
    await testInfo.attach('browser-diagnostics', {
      contentType: 'application/json',
      body: Buffer.from(JSON.stringify({ consoleErrors, failedRequests, serverErrors }, null, 2)),
    });
  };
}

export async function expectNoHorizontalOverflow(page: Page) {
  const dimensions = await page.evaluate(() => ({
    viewport: document.documentElement.clientWidth,
    content: document.documentElement.scrollWidth,
  }));
  expect(dimensions.content, `Page overflows viewport: ${JSON.stringify(dimensions)}`).toBeLessThanOrEqual(
    dimensions.viewport + 1,
  );
}

export async function expectSuccessfulDocument(page: Page, path: string) {
  const response = await page.goto(path, { waitUntil: 'domcontentloaded' });
  expect(response, `No document response for ${path}`).not.toBeNull();
  expect(response!.status(), `${path} returned HTTP ${response!.status()}`).toBeLessThan(400);
}

export function uniqueEmail(prefix = 'playwright') {
  return `${prefix}.${Date.now()}.${Math.random().toString(16).slice(2)}@example.com`;
}

export async function assertAuthConfigurationHealthy(page: Page) {
  const providers = await page.request.get('/api/auth/providers');
  expect(providers.status(), 'NextAuth providers endpoint must respond successfully').toBe(200);
  const body = await providers.json();
  expect(body.keycloak, 'Keycloak provider is not configured').toBeTruthy();
  expect(
    new URL(body.keycloak.callbackUrl).port,
    `NextAuth callback URL points at ${body.keycloak.callbackUrl}; it must use the tested frontend port`,
  ).toBe(new URL(process.env.E2E_BASE_URL || 'http://localhost:3004').port);
}
