import { test as setup, expect } from '@playwright/test';
import path from 'path';
import Chance from 'chance';

const chance = new Chance();

const authFile = path.join(__dirname, '../playwright/.auth/user.json');
const baseURL = 'http://localhost:3000';

setup('authenticate', async ({ page }) => {
  await page.goto(baseURL);
  await page.getByRole('button', { name: /create/i }).click();
  await expect(page.getByRole('heading', { name: /signup/i })).toBeVisible();

  await page.fill('#user_name',chance.name());
  await page.fill('#email',chance.email());
  await page.fill('#password', 'anyPassword123!');
  await page.fill('#confirm_pwd', 'anyPassword123!');
  await page.getByRole('button', { name: /sign up/i }).click();

  // Check if the user is redirected to the home page after signup
  await expect(page).toHaveURL(baseURL);

  await page.context().storageState({ path: authFile });

});