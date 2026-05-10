/**
 * 清单：TC_C7BTN_004 - 显式 props 覆盖预设字段
 */
import { test, expect } from '@playwright/test';
import * as path from 'path';

test.describe('TC_C7BTN_004 - 显式 props 覆盖预设字段', () => {
  test('验证显式传入的 label、type、plain、size 覆盖对应预设', async ({ page }) => {
    const screenshotDir = path.join(__dirname);
    await page.goto('/dev/c7-button-e2e');
    const btn = page.getByTestId('c7-override');
    await expect(btn).toHaveText('自定义');
    await expect(btn).toHaveClass(/el-button--warning/);
    await expect(btn).toHaveClass(/is-plain/);
    await expect(btn).toHaveClass(/el-button--small/);
    await page.screenshot({ path: path.join(screenshotDir, '01_override.png'), fullPage: true });
  });
});
