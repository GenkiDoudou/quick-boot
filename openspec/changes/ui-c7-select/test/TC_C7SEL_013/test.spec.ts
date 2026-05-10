/**
 * 对应清单：TC_C7SEL_013 - expose reload() 手动重拉
 */
import {test, expect} from '@playwright/test'
import * as path from 'path'

const shotDir = __dirname

test.describe('TC_C7SEL_013 - expose reload() 手动重拉', () => {
  test('先 reload 再展开下拉可见 mock 城市', async ({page}) => {
    await page.goto('/dev/c7-select-e2e')
    await page.getByTestId('c7-sel-reload-btn').click()
    await page.waitForTimeout(600)
    await page.screenshot({path: path.join(shotDir, '01_after_reload.png'), fullPage: true})

    const sel = page.getByTestId('c7-sel-reload')
    await sel.click()
    await expect(page.getByRole('option', {name: '上海'})).toBeVisible({timeout: 15000})
    await expect(page.getByRole('option', {name: '北京'})).toBeVisible()
  })
})
