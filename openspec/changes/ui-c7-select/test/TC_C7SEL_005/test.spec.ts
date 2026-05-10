/**
 * 对应清单：TC_C7SEL_005 - autoLoad 非 remote 挂载后自动拉取
 */
import {test, expect} from '@playwright/test'
import * as path from 'path'

const shotDir = __dirname

test.describe('TC_C7SEL_005 - autoLoad 非 remote 挂载后自动拉取', () => {
  test('展开 autoLoad 下拉可见异步项', async ({page}) => {
    await page.goto('/dev/c7-select-e2e')
    const sel = page.getByTestId('c7-sel-autoload')
    await sel.click()
    await page.screenshot({path: path.join(shotDir, '01_autoload_open.png'), fullPage: true})

    await expect(page.getByRole('option', {name: '异步项1'})).toBeVisible()
    await expect(page.getByRole('option', {name: '异步项2'})).toBeVisible()
  })
})
