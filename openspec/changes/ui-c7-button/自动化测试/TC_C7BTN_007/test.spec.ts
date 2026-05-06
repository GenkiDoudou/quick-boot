/**
 * 清单：TC_C7BTN_007 - 确认框用户取消中止流水线且无错误类提示
 */
import { test, expect } from '@playwright/test';
import * as path from 'path';

test.describe('TC_C7BTN_007 - 确认框用户取消中止流水线且无错误类提示', () => {
  test('验证 ElMessageBox.confirm 用户取消时不视为失败 toast', async ({ page }) => {
    const screenshotDir = path.join(__dirname);
    await page.goto('/dev/c7-button-e2e');
    await page.getByTestId('c7-confirm-cancel').click();
    await expect(page.getByRole('dialog')).toBeVisible();
    await page.getByRole('dialog').getByRole('button', { name: '取消' }).click();
    await expect(page.getByTestId('tc007-count')).toHaveText('0');
    await expect(page.locator('.el-message--error')).toHaveCount(0);
    await page.screenshot({ path: path.join(screenshotDir, '01_cancel_confirm.png'), fullPage: true });
  });
});
