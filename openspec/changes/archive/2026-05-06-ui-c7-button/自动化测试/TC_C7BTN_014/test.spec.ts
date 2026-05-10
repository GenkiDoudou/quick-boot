/**
 * 清单：TC_C7BTN_014 - confirmFn 返回 false 时中止且无错误 toast
 */
import { test, expect } from '@playwright/test';
import * as path from 'path';

test.describe('TC_C7BTN_014 - confirmFn 返回 false 时中止且无错误 toast', () => {
  test('验证 confirmFn 返回 false 时中止且不弹出错误类 toast', async ({ page }) => {
    const screenshotDir = path.join(__dirname);
    await page.goto('/dev/c7-button-e2e');
    await page.getByTestId('c7-confirmfn-no').click();
    await expect(page.getByTestId('tc014-count')).toHaveText('0');
    await expect(page.locator('.el-message--error')).toHaveCount(0);
    await page.screenshot({ path: path.join(screenshotDir, '01_confirm_fn_false.png'), fullPage: true });
  });
});
