import { test, expect } from '@playwright/test';
import { assertAuthConfigurationHealthy, KEYCLOAK_URL } from './helpers';

test.describe('Authentication and authorization', () => {
  test('NextAuth and Keycloak discovery are healthy and correctly configured', async ({ page, request }) => {
    await assertAuthConfigurationHealthy(page);
    const discovery = await request.get(`${KEYCLOAK_URL}/realms/qqq-platform/.well-known/openid-configuration`);
    expect(
      discovery.status(),
      `Keycloak discovery returned ${discovery.status()}: ${await discovery.text()}`,
    ).toBe(200);
    const document = await discovery.json();
    expect(document.authorization_endpoint).toContain('/protocol/openid-connect/auth');
    expect(document.token_endpoint).toContain('/protocol/openid-connect/token');
  });

  test('login starts a real OIDC authorization flow without OAuth configuration errors', async ({ page }) => {
    await page.goto('/auth/login');
    await expect(page.getByRole('button', { name: /Continue securely/i })).toBeVisible();
    await Promise.all([
      page.waitForURL(new RegExp(`^${KEYCLOAK_URL.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}/realms/qqq-platform/protocol/openid-connect/auth`)),
      page.getByRole('button', { name: /Continue securely/i }).click(),
    ]);
    expect(page.url(), `Login ended at ${page.url()}`).toMatch(
      new RegExp(`^${KEYCLOAK_URL.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}/realms/qqq-platform/protocol/openid-connect/auth`),
    );
    expect(page.url()).not.toContain('error=Configuration');
    const params = new URL(page.url()).searchParams;
    expect(params.get('client_id')).toBe('platform-web');
    expect(params.get('redirect_uri')).toBe(
      `${process.env.E2E_BASE_URL || 'http://localhost:3004'}/api/auth/callback/keycloak`,
    );
  });

  test('registration starts Keycloak account creation flow', async ({ page }) => {
    await page.goto('/auth/register');
    await Promise.all([
      page.waitForURL(/\/protocol\/openid-connect\/auth/),
      page.getByRole('button', { name: /Begin registration/i }).click(),
    ]);
    expect(page.url(), `Registration ended at ${page.url()}`).toContain('/protocol/openid-connect/auth');
    expect(new URL(page.url()).searchParams.get('prompt')).toBe('create');
  });

  for (const route of ['/member', '/member/profile', '/admin', '/admin/content']) {
    test(`anonymous users cannot access ${route}`, async ({ page }) => {
      await page.context().clearCookies();
      await page.goto(route);
      await page.waitForURL(/\/auth\/login/);
      expect(page.url(), `${route} produced an authentication configuration error`).not.toContain('error=Configuration');
      await expect(page.getByRole('heading', { level: 1, name: /Return to your space/i })).toBeVisible();
    });
  }

  test('credentialed login/logout is exercised when E2E credentials are supplied', async ({ page }) => {
    const username = process.env.E2E_USERNAME;
    const password = process.env.E2E_PASSWORD;
    test.skip(!username || !password, 'Set E2E_USERNAME and E2E_PASSWORD to exercise real login/logout');

    await page.goto('/auth/login');
    await page.getByRole('button', { name: /Continue securely/i }).click();
    await page.getByLabel(/username or email/i).fill(username!);
    await page.getByLabel(/password/i).fill(password!);
    await page.getByRole('button', { name: /sign in/i }).click();
    await expect(page).toHaveURL(/\/member$/);
    await expect(page.getByRole('heading', { level: 1, name: /Welcome/i })).toBeVisible();
    await page.getByRole('button', { name: /Sign out/i }).click();
    await expect(page).toHaveURL(/\/$/);
    const session = await page.request.get('/api/auth/session');
    expect(await session.json()).toEqual({});
  });
});
