/**
 * 清单：TC_C7BTN_016 - validateRef 无效配置时的行为与表单校验失败区分
 */
import { test, expect } from '@playwright/test';
import * as path from 'path';

test.describe('TC_C7BTN_016 - validateRef 无效配置时的行为与表单校验失败区分', () => {
  test('验证 validate=true 但 validateRef 无效时不进入表单 validate，且不弹错误类 toast', async ({
    page,
  }) => {
    const screenshotDir = path.join(__dirname);
    await page.goto('/dev/c7-button-e2e');
    await page.getByTestId('c7-invalid-validate-ref').click();
    await expect(page.getByTestId('tc016-count')).toHaveText('0');
    await expect(page.locator('.el-message--error')).toHaveCount(0);
    await page.screenshot({ path: path.join(screenshotDir, '01_invalid_validate_ref.png'), fullPage: true });
  });
});
