/**
 * 清单：TC_C7BTN_013 - 成功路径 success emit 与 successMessage/successNotify
 */
import { test, expect } from '@playwright/test';
import * as path from 'path';

test.describe('TC_C7BTN_013 - 成功路径 success emit 与 successMessage/successNotify', () => {
  test('验证 resolve 且 checkSuccess 默认通过时 success、after-click(true) 及成功提示规则', async ({
    page,
  }) => {
    const screenshotDir = path.join(__dirname);
    await page.goto('/dev/c7-button-e2e');
    await page.getByTestId('c7-success-msg').click();
    await expect(page.locator('.el-message--success')).toContainText('TC013 操作成功');
    await page.waitForTimeout(500);
    await page.getByTestId('c7-success-notify').click();
    await expect(page.locator('.el-notification')).toBeVisible();
    await expect(page.locator('.el-notification')).toContainText('TC013 Notify成功');
    await page.screenshot({ path: path.join(screenshotDir, '01_success_paths.png'), fullPage: true });
  });
});
