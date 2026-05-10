/**
 * 清单：TC_C7BTN_012 - clickFunction reject 时失败提示与 error
 */
import { test, expect } from '@playwright/test';
import * as path from 'path';

test.describe('TC_C7BTN_012 - clickFunction reject 时失败提示与 error', () => {
  test('验证 Promise reject 表示失败，触发 error 与失败提示', async ({ page }) => {
    const screenshotDir = path.join(__dirname);
    await page.goto('/dev/c7-button-e2e');
    await page.getByTestId('c7-reject').click();
    await expect(page.locator('.el-message--error')).toContainText('TC012 失败');
    await page.screenshot({ path: path.join(screenshotDir, '01_reject.png'), fullPage: true });
  });
});
