/**
 * 对应清单：TC_C7SEL_002 - Dev 演示页可访问且 C7Select 渲染
 * 依赖 `auth.setup.ts` 写入的 storageState（`e2e` project）。
 */
import {test, expect} from '@playwright/test'
import * as path from 'path'

const shotDir = __dirname

test.describe('TC_C7SEL_002 - Dev 演示页可访问且 C7Select 渲染', () => {
  test('登录态下访问 Dev 页，标题与分区可见', async ({page}) => {
    await page.goto('/dev/c7-select-e2e')
    await page.screenshot({path: path.join(shotDir, '01_dev_page.png'), fullPage: true})

    await expect(page.getByTestId('c7-select-title')).toHaveText('C7Select Dev')
    await expect(page.getByTestId('tc-static')).toBeVisible()
    await expect(page.getByTestId('tc-autoload')).toBeVisible()
    await expect(page.getByTestId('tc-remote')).toBeVisible()
    await expect(page.getByTestId('tc-separator')).toBeVisible()
    await expect(page.getByTestId('tc-reload')).toBeVisible()

    await page.screenshot({path: path.join(shotDir, '02_sections_visible.png'), fullPage: true})
  })
})
