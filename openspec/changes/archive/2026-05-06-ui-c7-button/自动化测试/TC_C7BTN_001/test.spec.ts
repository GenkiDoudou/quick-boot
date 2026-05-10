/**
 * 清单：TC_C7BTN_001 - 合法账号登录并具备后续页面访问能力
 */
import { test, expect } from '@playwright/test';
import * as path from 'path';

test.describe('TC_C7BTN_001 - 合法账号登录并具备后续页面访问能力', () => {
  test('验证使用合法账号成功登录并进入系统，会话有效', async ({ page, context }) => {
    const screenshotDir = path.join(__dirname);
    await context.clearCookies();
    await page.goto('/login');
    await page.getByPlaceholder('用户名').fill('admin');
    await page.getByPlaceholder('密码').fill('admin');
    await page.screenshot({ path: path.join(screenshotDir, '01_login_form.png'), fullPage: true });
    await page.getByRole('button', { name: /登\s*录/ }).click();
    await page.waitForURL((u) => !u.pathname.endsWith('/login'), { timeout: 60000 });
    await expect(page).not.toHaveURL(/\/login$/);
    const cookies = await context.cookies();
    const token = cookies.find((c) => c.name === 'Admin-Token');
    expect(token?.value, '登录后应存在 Admin-Token Cookie').toBeTruthy();
    await page.screenshot({ path: path.join(screenshotDir, '02_after_login.png'), fullPage: true });
  });
});
