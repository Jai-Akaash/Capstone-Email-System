import { test, expect } from '@playwright/test';

test('should allow a user to send an email and verify it in history', async ({ page }) => {
  // 1. Navigate to the app
  await page.goto('http://localhost:5173'); 
  await page.waitForLoadState('networkidle');

  // 2. Click EXACTLY on the "Compose" link from your sidebar
  await page.getByRole('link', { name: 'Compose', exact: true }).click();
  
  // Wait a moment for the form page to render by checking the URL
  await expect(page).toHaveURL(/.*compose/);

  // 3. Fill out the email form using your EXACT placeholders
  const recipientInput = page.getByPlaceholder('user@example.com');
  await expect(recipientInput).toBeVisible({ timeout: 10000 });
  await recipientInput.fill('playwright-test@example.com');

  const subjectInput = page.getByPlaceholder('What is this email about?');
  await subjectInput.fill('Automated Capstone Test');

  const bodyInput = page.getByPlaceholder('Type your message here...');
  await bodyInput.fill('This email was triggered automatically by our E2E testing suite.');

  // 4. Wait for the API call to succeed when we click send
  const sendPromise = page.waitForResponse(response => 
    response.request().method() === 'POST' && response.status() === 200
  );
  
  // Click the "Send Email" button
  const sendButton = page.getByRole('button', { name: 'Send Email' });
  await expect(sendButton).toBeVisible();
  await sendButton.click();
  await sendPromise;

  // 5. Navigate to the "All Mails" page to verify it was recorded
  await page.getByRole('link', { name: 'All Mails', exact: true }).click();
  await page.waitForLoadState('networkidle');

  // 6. Assert: Verify the email address appears in the table
  await expect(page.getByText('playwright-test@example.com').first()).toBeVisible({ timeout: 15000 });
});