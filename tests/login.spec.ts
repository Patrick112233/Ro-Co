
import { test, expect } from '@playwright/test';
import Chance from 'chance';

const chance = new Chance();
const baseURL = 'http://localhost:3000';


test('login with unregistered user shows error or stays on login page', async ({ page }) => {
  await page.goto(baseURL+ '/login');
  await expect(page).toHaveURL(baseURL + '/login');
  await expect(page.getByRole('heading', { name: /login/i })).toBeVisible();
  
  await expect( page.getByRole('button', { name: /login/i })).toBeDisabled();
  await page.fill('#email', Chance().email());
  await page.fill('#password', 'anyPassword123!');
  await page.getByRole('button', { name: /login/i }).click();
  await expect(page).toHaveURL(baseURL+ '/login');
  await expect(page).toHaveURL(baseURL + '/login');
});

test('test disable popup', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('button').nth(1).click();
  await page.getByRole('button', { name: 'Close' }).click();
  //Assert that the popup is closed
  await expect (page.getByRole('heading', { name: 'Ask your Question' })).toBeHidden({ timeout: 10000 });
});

test('test login', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('navigation').getByRole('button').click();
  await expect(page.getByRole('heading', { name: /login/i })).toBeVisible();
  await expect(page).toHaveURL(baseURL + '/login');
}
);







