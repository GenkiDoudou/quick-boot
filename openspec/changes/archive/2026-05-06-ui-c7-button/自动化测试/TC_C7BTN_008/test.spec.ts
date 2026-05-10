/**
 * 清单：TC_C7BTN_008 - delete 预设且 confirm 时先确认再执行 clickFunction
 */
import { test, expect } from '@playwright/test';
import * as path from 'path';

test.describe('TC_C7BTN_008 - delete 预设且 confirm 时先确认再执行 clickFunction', () => {
  test('验证 btnType=delete 且 confirm=true 时顺序为先确认再执行异步逻辑', async ({ page }) => {
    const screenshotDir = path.join(__dirname);
    await page.goto('/dev/c7-button-e2e');
    await expect(page.getByTestId('tc008-count')).toHaveText('0');
    await page.getByTestId('c7-delete-confirm').click();
    await expect(page.getByRole('dialog')).toBeVisible();
    await expect(page.getByTestId('tc008-count')).toHaveText('0');
    await page.getByRole('button', { name: '确定' }).click();
    await expect(page.getByTestId('tc008-count')).toHaveText('1');
    await page.screenshot({ path: path.join(screenshotDir, '01_delete_confirm_order.png'), fullPage: true });
  });
});
