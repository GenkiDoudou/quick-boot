/**
 * 清单：TC_C7BTN_010 - 防抖 leading 语义下同窗口尾随点击不重复排队
 */
import { test, expect } from '@playwright/test';
import * as path from 'path';

test.describe('TC_C7BTN_010 - 防抖 leading 语义下同窗口尾随点击不重复排队', () => {
  test('验证 debounceDelay 默认语义为首击立即、同窗口内尾随点击不开启新流水线', async ({ page }) => {
    const screenshotDir = path.join(__dirname);
    await page.goto('/dev/c7-button-e2e');
    const btn = page.getByTestId('c7-debounce');
    await btn.click();
    await expect(page.getByTestId('tc010-count')).toHaveText('1');
    await page.waitForTimeout(400);
    await btn.click();
    await btn.click({ delay: 10 });
    await page.waitForTimeout(400);
    await expect(page.getByTestId('tc010-count')).toHaveText('2');
    await page.screenshot({ path: path.join(screenshotDir, '01_debounce.png'), fullPage: true });
  });
});
