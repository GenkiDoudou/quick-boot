/**
 * 清单：TC_C7BTN_002 - 全局注册后模板可直接使用 C7Button
 */
import { test, expect } from '@playwright/test';
import * as path from 'path';

test.describe('TC_C7BTN_002 - 全局注册后模板可直接使用 C7Button', () => {
  test('验证应用入口注册后页面可直接使用 `<C7Button>` 无需局部注册', async ({ page }) => {
    const screenshotDir = path.join(__dirname);
    await page.goto('/dev/c7-button-e2e');
    await expect(page.getByTestId('e2e-title')).toHaveText('C7Button E2E');
    await page.screenshot({ path: path.join(screenshotDir, '01_e2e_page.png'), fullPage: true });
    const basic = page.getByTestId('c7-tc002-basic');
    await expect(basic).toBeVisible();
    // attrs 透传到根节点 el-button，无额外包裹层
    await expect(basic).toHaveClass(/el-button/);
    await basic.click();
    await page.waitForTimeout(300);
    await expect(page.locator('.el-message--error')).toHaveCount(0);
  });
});
