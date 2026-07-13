import { test, expect } from '@playwright/test';
import { captureDiagnostics, expectSuccessfulDocument } from './helpers';

test.describe('Public pages and navigation', () => {
  test.afterEach(async ({ page }, testInfo) => {
    // Failure artifacts are configured globally; this adds a readable URL to every result.
    await testInfo.attach('final-url', { body: Buffer.from(page.url()), contentType: 'text/plain' });
  });

  test('homepage renders its real critical content', async ({ page }) => {
    const attach = captureDiagnostics(page, test.info());
    await expectSuccessfulDocument(page, '/');
    await expect(page).toHaveTitle(/QQQ/);
    await expect(page.getByRole('heading', { level: 1, name: /Run the work.*Keep the clarity/i })).toBeVisible();
    await expect(page.getByRole('navigation', { name: 'Primary navigation' })).toBeVisible();
    await expect(page.getByLabel('QQQ platform dashboard preview')).toBeVisible();
    await expect(page.getByRole('heading', { name: /Everything important/i })).toBeVisible();
    await expect(page.getByRole('heading', { name: /Less noise.*Better signals/i })).toBeVisible();
    await expect(page.getByLabel('Work email')).toBeVisible();
    await attach();
  });

  for (const pageCase of [
    { path: '/blog', heading: /Ideas for a more considered business/i },
    { path: '/auth/login', heading: /Return to your space/i },
    { path: '/auth/register', heading: /Bring your business into focus/i },
  ]) {
    test(`${pageCase.path} returns a usable page`, async ({ page }) => {
      await expectSuccessfulDocument(page, pageCase.path);
      await expect(page.getByRole('heading', { level: 1, name: pageCase.heading })).toBeVisible();
    });
  }

  test('primary navigation links reach their intended destinations', async ({ page }) => {
    await page.goto('/');
    const nav = page.getByRole('navigation', { name: 'Primary navigation' });
    await nav.getByRole('link', { name: 'Platform', exact: true }).click();
    await expect(page).toHaveURL(/\/#platform$/);
    await expect(page.locator('#platform')).toBeVisible();

    await nav.getByRole('link', { name: 'Pricing', exact: true }).click();
    await expect(page).toHaveURL(/\/#pricing$/);
    await expect(page.locator('#pricing')).toBeVisible();

    await nav.getByRole('link', { name: 'Journal', exact: true }).click();
    await expect(page).toHaveURL(/\/blog$/);
    await expect(page.getByRole('heading', { level: 1, name: /Ideas for a more considered business/i })).toBeVisible();

    await page.getByRole('navigation', { name: 'Primary navigation' }).getByRole('link', { name: 'Sign in', exact: true }).click();
    await expect(page).toHaveURL(/\/auth\/login$/);
  });

  test('critical Next.js assets load so the application can hydrate', async ({ page }) => {
    const failures: string[] = [];
    page.on('response', response => {
      if (response.url().includes('/_next/static/') && response.status() >= 400) failures.push(`${response.status()} ${response.url()}`);
    });
    page.on('requestfailed', request => {
      if (request.url().includes('/_next/static/')) failures.push(`FAILED ${request.url()} — ${request.failure()?.errorText}`);
    });
    await page.goto('/');
    await page.waitForLoadState('networkidle');
    expect(failures, `Frontend cannot hydrate because static assets failed:\n${failures.join('\n')}`).toEqual([]);
  });

  test('all same-origin public links avoid 404/5xx responses', async ({ page, request, baseURL }) => {
    await page.goto('/');
    const hrefs = await page.locator('a[href]').evaluateAll(links =>
      [...new Set(links.map(link => (link as HTMLAnchorElement).href))],
    );
    const origin = new URL(baseURL!);
    const paths = hrefs
      .map(href => new URL(href))
      .filter(url => url.origin === origin.origin && !url.pathname.startsWith('/member') && !url.pathname.startsWith('/admin'))
      .map(url => `${url.pathname}${url.search}`);

    for (const path of paths) {
      const response = await request.get(path, { maxRedirects: 5 });
      expect(response.status(), `${path} returned ${response.status()}`).toBeLessThan(400);
    }
  });
});
