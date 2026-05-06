/**
 * 清单：TC_C7BTN_006 - 表单校验失败时不进入确认与 clickFunction
 */
import { test, expect } from '@playwright/test';
import * as path from 'path';

test.describe('TC_C7BTN_006 - 表单校验失败时不进入确认与 clickFunction', () => {
  test('验证 validate=true 且 ElForm 校验失败时不进入确认与 clickFunction，且无错误类 toast', async ({
    page,
  }) => {
    const screenshotDir = path.join(__dirname);
    await page.goto('/dev/c7-button-e2e');
    await page.getByTestId('tc006-name').fill('');
    await page.getByTestId('c7-validate-fail').click();
    await expect(page.getByRole('dialog')).toHaveCount(0);
    await expect(page.getByTestId('tc006-count')).toHaveText('0');
    await expect(page.locator('.el-message--error')).toHaveCount(0);
    await page.screenshot({ path: path.join(screenshotDir, '01_validate_fail.png'), fullPage: true });
  });
});
