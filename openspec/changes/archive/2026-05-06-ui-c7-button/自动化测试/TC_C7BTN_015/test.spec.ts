/**
 * 清单：TC_C7BTN_015 - showErrorToast=false 时失败不弹出错误 toast
 */
import { test, expect } from '@playwright/test';
import * as path from 'path';

test.describe('TC_C7BTN_015 - showErrorToast=false 时失败不弹出错误 toast', () => {
  test('验证关闭 showErrorToast 时 reject 或 checkSuccess 失败不弹出错误 toast', async ({ page }) => {
    const screenshotDir = path.join(__dirname);
    await page.goto('/dev/c7-button-e2e');
    await page.getByTestId('c7-no-err-toast').click();
    await page.waitForTimeout(400);
    await expect(page.locator('.el-message--error')).toHaveCount(0);
    await page.getByTestId('c7-no-err-toast-checkfail').click();
    await page.waitForTimeout(400);
    await expect(page.locator('.el-message--error')).toHaveCount(0);
    await page.screenshot({ path: path.join(screenshotDir, '01_no_error_toast.png'), fullPage: true });
  });
});
