import { test, expect } from '@playwright/test';
import { API_URL } from './helpers';

test.describe('Real API integration', () => {
  test('public content API returns a successful JSON contract', async ({ request }) => {
    const response = await request.get(`${API_URL}/api/public/v1/content/posts`);
    expect(response.status(), `Content API returned ${response.status()}: ${await response.text()}`).toBe(200);
    expect(response.headers()['content-type']).toContain('application/json');
    const body = await response.json();
    expect(Array.isArray(body.items || body.posts), `Unexpected content API payload: ${JSON.stringify(body)}`).toBe(true);
  });

  test('blog server rendering consumes live API data without server errors', async ({ page }) => {
    const serverErrors: string[] = [];
    page.on('response', response => {
      if (response.status() >= 500) serverErrors.push(`${response.status()} ${response.url()}`);
    });
    const response = await page.goto('/blog');
    expect(response?.status()).toBe(200);
    await expect(page.getByRole('heading', { level: 1, name: /Ideas for a more considered business/i })).toBeVisible();
    expect(serverErrors, `Server errors while rendering blog: ${serverErrors.join(', ')}`).toEqual([]);
  });

  test('protected member and staff APIs reject anonymous requests', async ({ request }) => {
    for (const path of ['/api/member/v1/profile', '/api/member/v1/subscriptions', '/api/staff/v1/users']) {
      const response = await request.get(`${API_URL}${path}`);
      expect([401, 403], `${path} was not protected; returned ${response.status()}`).toContain(response.status());
    }
  });

  test('unknown frontend routes return a genuine 404', async ({ request }) => {
    const response = await request.get(`/definitely-not-a-real-route-${Date.now()}`);
    expect(response.status()).toBe(404);
  });
});
