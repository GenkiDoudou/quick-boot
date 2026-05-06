/**
 * 清单：TC_C7BTN_005 - beforeClick 否决中止流水线且无错误类提示
 */
import { test, expect } from '@playwright/test';
import * as path from 'path';

test.describe('TC_C7BTN_005 - beforeClick 否决中止流水线且无错误类提示', () => {
  test('验证 beforeClick 返回 false 时中止流水线且不弹出错误类 toast', async ({ page }) => {
    const screenshotDir = path.join(__dirname);
    await page.goto('/dev/c7-button-e2e');
    await page.getByTestId('c7-before-veto').click();
    await expect(page.getByTestId('tc005-count')).toHaveText('0');
    await expect(page.locator('.el-message--error')).toHaveCount(0);
    await page.screenshot({ path: path.join(screenshotDir, '01_veto.png'), fullPage: true });
  });
});
