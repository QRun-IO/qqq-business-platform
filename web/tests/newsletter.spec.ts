import { test, expect } from '@playwright/test';
import { API_URL, captureDiagnostics, uniqueEmail } from './helpers';

test.describe('Newsletter signup', () => {
  test('valid signup reaches the real backend and reports success', async ({ page, request }) => {
    const attach = captureDiagnostics(page, test.info());
    const health = await request.get(`${API_URL}/api/public/v1/content/posts`);
    expect(health.status(), `Backend preflight failed with ${health.status()}`).toBeLessThan(500);

    await page.goto('/');
    const email = uniqueEmail('newsletter');
    const requestPromise = page.waitForRequest(req =>
      req.url().includes('/api/newsletter/subscribe') && req.method() === 'POST', { timeout: 5_000 },
    );
    const responsePromise = page.waitForResponse(res =>
      res.url().includes('/api/newsletter/subscribe') && res.request().method() === 'POST', { timeout: 5_000 },
    );

    await page.getByLabel('Work email').fill(email);
    await page.getByRole('button', { name: 'Join the list' }).click();
    const outgoing = await requestPromise;
    expect(outgoing.postDataJSON()).toEqual({ email });
    const response = await responsePromise;
    expect(response.status(), `Newsletter API returned ${response.status()}: ${await response.text()}`).toBeLessThan(300);
    await expect(page.getByText('Check your inbox to confirm your subscription.')).toBeVisible();
    await expect(page.getByLabel('Work email')).toHaveValue('');
    await attach();
  });

  test('invalid email is rejected client-side without an API call', async ({ page }) => {
    let calls = 0;
    page.on('request', req => {
      if (req.url().includes('/api/public/v1/newsletter/subscribe')) calls += 1;
    });
    await page.goto('/');
    await page.getByLabel('Work email').fill('not-an-email');
    await page.getByRole('button', { name: 'Join the list' }).click();
    await expect(page.locator('#newsletter-email-error')).toContainText(/valid email/i);
    expect(calls, 'Invalid form unexpectedly called the API').toBe(0);
  });

  test('backend failures are shown to the user and do not fake success', async ({ page }) => {
    await page.route('**/api/newsletter/subscribe', route =>
      route.fulfill({ status: 500, contentType: 'application/json', body: JSON.stringify({ error: 'Subscription failed. Please try again.' }) }),
    );
    await page.goto('/');
    await page.getByLabel('Work email').fill('failure@example.com');
    await page.getByRole('button', { name: 'Join the list' }).click();
    await expect(page.getByText('Subscription failed. Please try again.')).toBeVisible();
    await expect(page.getByText('Check your inbox to confirm your subscription.')).toHaveCount(0);
    await expect(page.getByLabel('Work email')).toHaveValue('failure@example.com');
  });
});
