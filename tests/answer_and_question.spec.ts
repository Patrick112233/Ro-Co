import { test, expect } from '@playwright/test';


test('Test Question and Answer', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('button').filter({ hasText: /^$/ }).nth(1).click(); //if unstable replace this with a ID
  await page.getByTestId('subject').click();
  await page.getByTestId('subject').fill('Design and Construction of a Wooden Bench');
  await page.getByTestId('question').fill('I’m a student and completely new to woodworking, and I was hoping to get some help or advice from folks who know what they’re doing 😅\n\nI want to build a simple wooden bench that could seat two or maybe three people. Nothing too fancy—just something sturdy and functional for my room or maybe the balcony. The problem is… I have no idea where to start.\n\nHere are a few questions I’ve been wondering about:\n\nWhat kind of wood should I use? I’ve heard that some wood is better for outdoor use, but I don’t know the difference between pine, oak, etc. Any suggestions for something that’s not too expensive but still strong?\n\nWhat tools do I actually need? I have access to a basic toolbox, but nothing high-tech like a table saw or anything. Is it possible to build a bench with just hand tools?\n\nDo I need to draw a plan first? How do you figure out the right measurements and angles before cutting anything? Are there templates or free plans online that beginners can follow?\n\nHow do you join the wood pieces together? I keep seeing words like "dowel," "pocket hole," and "wood glue" but I don’t know what’s best for a beginner.\n\nHow do I finish it? Do I need to sand it and apply some kind of protective finish or stain, especially if I want to put it outside?\n\nI’d really appreciate any advice, videos, links, or even super basic step-by-step instructions. Just assume I’m starting from zero 😄\n\nThanks in advance!');
  await page.getByRole('button', { name: 'Post' }).click();

  //Check for question visibility and delete and toggle answered btn
  await expect(page.getByRole('heading', { name: 'Design and Construction of a' }).first()).toBeVisible();
  await expect(page.getByText('I’m a student and completely').first()).toBeVisible();
  await expect(page.getByTestId('DeleteQuestionButton')).toBeVisible();
  await expect(page.getByTestId('ToggleAnsweredButton')).toBeVisible();
  //check weather image User Avatar is visible
  await expect(page.getByRole('img', { name: 'User Avatar' }).nth(1)).toBeVisible();


  await page.getByTestId('OpenAnswerButton').first().click();
  await page.getByTestId('comment').click();
  await page.getByTestId('comment').click();
  await page.getByTestId('comment').fill('P.S. If you have pictures of simple benches you’ve built or links to beginner-friendly tutorials, that would be amazing. I learn way better with visuals!');
  await page.getByRole('button', { name: 'Post comment' }).click();

  await expect(page.getByText('P.S. If you have pictures of')).toBeVisible();
  await expect(page.getByRole('img', { name: 'User Avatar' }).nth(2)).toBeVisible();
  await expect(page.getByTestId('deleteAnswerButton')).toBeVisible();

  //Delete the Answer but answer section should still be visible
  await page.getByTestId('deleteAnswerButton').click();
  await expect(page.getByText('P.S. If you have pictures of')).not.toBeVisible();
  await expect(page.getByRole('button', { name: 'Post comment' })).toBeVisible();
  
  //Check if Toggle Worke
  await page.getByTestId('OpenAnswerButton').first().click();
  await expect(page.getByRole('button', { name: 'Post comment' })).not.toBeVisible();
  });


  test('Check Question BTNs and delete', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('button').filter({ hasText: /^$/ }).nth(1).click();
  await page.getByTestId('subject').click();
  await page.getByTestId('subject').fill('Anfängerfrage: Wie baut man einen einfachen Briefkasten aus Metall?');
  await page.getByTestId('question').click();
  await page.getByTestId('question').fill('Hey zusammen,\n\nich bin Student und möchte mich an meinem ersten kleinen Metallbauprojekt versuchen – einem selbstgebauten Briefkasten aus Metall. Ich hab noch nie sowas gemacht, finde die Idee aber spannend und würde mich über Tipps oder Anleitungen total freuen 😅\n\nIch stell mir einen schlichten, wetterfesten Briefkasten vor, den man draußen an die Wand montieren kann. Kein High-End-Design, einfach funktional und stabil. Aber ich hab echt viele Fragen und wenig Plan.');
  await page.getByRole('button', { name: 'Post' }).click();

  //check if question is visible
  await expect(page.getByRole('heading', { name: 'Anfängerfrage: Wie baut man' }).first()).toBeVisible();
  await expect(page.getByText('Hey zusammen,\n\nich bin Student und möchte mich an meinem ersten kleinen Metallbauprojekt versuchen – einem selbstgebauten Briefkasten aus Metall. Ich hab noch nie sowas gemacht, finde die Idee aber spannend und würde mich über Tipps oder Anleitungen total freuen').first()).toBeVisible();
  await expect(page.getByTestId('DeleteQuestionButton')).toBeVisible();
  await expect(page.getByTestId('ToggleAnsweredButton').first()).toBeVisible();

  //check weather toggle answered button works
  await page.getByTestId('ToggleAnsweredButton').first().click();
  // Accept both 'green' and 'rgb(0, 128, 0)' for the color
  const color = await page.locator('.svg-inline--fa.fa-circle-check').first().evaluate(
    (el) => getComputedStyle(el).color
  );
  expect(['green', 'rgb(0, 128, 0)']).toContain(color);
  await page.getByTestId('ToggleAnsweredButton').first().click();
  const color_gray = await page.locator('.svg-inline--fa.fa-circle-check').first().evaluate(
    (el) => getComputedStyle(el).color
  );
  expect(['green', 'rgb(0, 128, 0)']).not.toContain(color_gray);

  await page.getByTestId('DeleteQuestionButton').first().click();
  await expect(page.getByRole('heading', { name: 'Anfängerfrage: Wie baut man' })).not.toBeVisible();

}
);
