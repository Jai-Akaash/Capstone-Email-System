import { test, expect } from '@playwright/test';

test('should allow a user to create and view a new email template', async ({ page }) => {
  await page.goto('http://localhost:5173'); 
  // Wait for the app to settle down before clicking
  await page.waitForLoadState('networkidle'); 

  // 1. Click Templates
  await page.getByRole('link', { name: /templates/i }).click();

  // 2. Click Create
  const createButton = page.getByRole('button', { name: /create|add|new/i });
  await expect(createButton).toBeVisible();
  // force: true bypasses animation blocks
  await createButton.click({ force: true }); 

  // 3. Fill the form - Added visibility checks so it doesn't type too early
  const firstInput = page.locator('input').first();
  await expect(firstInput).toBeVisible();
  await firstInput.fill('Playwright Capstone Template');

  const textArea = page.locator('textarea').first();
  await expect(textArea).toBeVisible();
  await textArea.fill('E2E testing is successful!');

  // 4. Submit
  // .last() ensures it clicks the form button, not a nav button
  const saveButton = page.getByRole('button', { name: /save|submit|create/i }).last();
  await expect(saveButton).toBeVisible();
  await saveButton.click({ force: true });

  // 5. Final Check - Give it up to 15 seconds for the database to save and UI to update
  await expect(page.getByText('Playwright Capstone Template').first()).toBeVisible({ timeout: 15000 });
});