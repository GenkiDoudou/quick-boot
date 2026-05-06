/**
 * 清单：TC_C7BTN_003 - btnType 预设的文案、类型与图标
 */
import { test, expect } from '@playwright/test';
import * as path from 'path';

const PRESET_EXPECT = {
  add: { label: '新增', primary: true },
  edit: { label: '修改', primary: true },
  delete: { label: '删除', danger: true },
  query: { label: '查询', primary: true },
  refresh: { label: '重置', def: true },
  upload: { label: '上传', primary: true },
  download: { label: '下载', primary: true },
  submit: { label: '提交', primary: true },
  cancel: { label: '取消', def: true },
} as const;

test.describe('TC_C7BTN_003 - btnType 预设的文案、类型与图标', () => {
  test('验证各 btnType 默认 label、type、plain 与图标符合预设表', async ({ page }) => {
    const screenshotDir = path.join(__dirname);
    await page.goto('/dev/c7-button-e2e');
    await expect(page.getByTestId('tc003')).toBeVisible();

    for (const key of Object.keys(PRESET_EXPECT) as (keyof typeof PRESET_EXPECT)[]) {
      const btn = page.getByTestId(`c7-preset-${key}`);
      await expect(btn).toHaveText(PRESET_EXPECT[key].label);
      if ('danger' in PRESET_EXPECT[key]) {
        await expect(btn).toHaveClass(/el-button--danger/);
      } else if ('primary' in PRESET_EXPECT[key]) {
        await expect(btn).toHaveClass(/el-button--primary/);
      } else if ('def' in PRESET_EXPECT[key]) {
        await expect(btn).toHaveClass(/el-button--default/);
      }
    }

    const noPreset = page.getByTestId('c7-no-preset');
    await expect(noPreset).toHaveText('仅文案');
    await expect(noPreset).toHaveClass(/el-button--info/);

    await page.screenshot({ path: path.join(screenshotDir, '01_presets.png'), fullPage: true });
  });
});
