/**
 * 清单：TC_C7BTN_011 - checkSuccess 为 false 时走失败分支与错误提示
 */
import { test, expect } from '@playwright/test';
import * as path from 'path';

test.describe('TC_C7BTN_011 - checkSuccess 为 false 时走失败分支与错误提示', () => {
  test('验证 clickFunction resolve 后 checkSuccess 返回 false 时触发 error 且可展示失败提示', async ({
    page,
  }) => {
    const screenshotDir = path.join(__dirname);
    await page.goto('/dev/c7-button-e2e');
    await page.getByTestId('c7-check-fail').click();
    await expect(page.locator('.el-message.el-message--error')).toBeVisible();
    await expect(page.locator('.el-message--error')).toContainText('业务处理未通过');
    await page.screenshot({ path: path.join(screenshotDir, '01_check_fail.png'), fullPage: true });
  });
});
