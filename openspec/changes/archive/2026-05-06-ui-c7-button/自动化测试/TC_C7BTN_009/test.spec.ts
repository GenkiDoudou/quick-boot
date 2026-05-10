/**
 * 清单：TC_C7BTN_009 - clickFunction 执行期 loading 且 busy 阻止重入
 */
import { test, expect } from '@playwright/test';
import * as path from 'path';

test.describe('TC_C7BTN_009 - clickFunction 执行期 loading 且 busy 阻止重入', () => {
  test('验证 Promise 未完成前按钮 loading，且流水线执行中重复点击不重复执行异步逻辑', async ({
    page,
  }) => {
    const screenshotDir = path.join(__dirname);
    await page.goto('/dev/c7-button-e2e');
    const btn = page.getByTestId('c7-slow');
    await btn.click();
    await expect(btn).toHaveClass(/is-loading/);
    for (let i = 0; i < 5; i++) {
      await btn.click({ force: true });
    }
    await expect(page.getByTestId('tc009-count')).toHaveText('1');
    await expect(btn).not.toHaveClass(/is-loading/, { timeout: 5000 });
    await page.screenshot({ path: path.join(screenshotDir, '01_busy_loading.png'), fullPage: true });
  });
});
