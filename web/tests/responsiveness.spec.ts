import { test, expect } from '@playwright/test';
import { expectNoHorizontalOverflow } from './helpers';

test.describe('Mobile responsiveness @mobile', () => {
  test('homepage has no horizontal overflow and mobile navigation works', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByRole('heading', { level: 1, name: /Run the work.*Keep the clarity/i })).toBeVisible();
    await expectNoHorizontalOverflow(page);

    const toggle = page.getByRole('button', { name: 'Toggle navigation' });
    await expect(toggle).toBeVisible();
    await expect(toggle).toHaveAttribute('aria-expanded', 'false');
    await toggle.click();
    await expect(toggle).toHaveAttribute('aria-expanded', 'true');
    const primaryNavigation = page.getByRole('navigation', { name: 'Primary navigation' });
    const journalLink = primaryNavigation.getByRole('link', { name: 'Journal', exact: true });
    await expect(journalLink).toBeVisible();
    await journalLink.click();
    await expect(page).toHaveURL(/\/blog$/);
    await expectNoHorizontalOverflow(page);
  });

  for (const path of ['/auth/login', '/auth/register', '/blog']) {
    test(`${path} fits and remains actionable on a phone`, async ({ page }) => {
      await page.goto(path);
      await expect(page.locator('h1').first()).toBeVisible();
      await expectNoHorizontalOverflow(page);
      const mainBox = await page.locator('main').boundingBox();
      expect(mainBox, `${path} main content has no layout box`).not.toBeNull();
      expect(mainBox!.width).toBeLessThanOrEqual(page.viewportSize()!.width + 1);
    });
  }

  test('newsletter form is usable at phone width', async ({ page }) => {
    await page.goto('/');
    const input = page.getByLabel('Work email');
    await input.scrollIntoViewIfNeeded();
    await input.fill('mobile@example.com');
    await expect(input).toHaveValue('mobile@example.com');
    await expect(page.getByRole('button', { name: 'Join the list' })).toBeVisible();
    await expectNoHorizontalOverflow(page);
  });
});
