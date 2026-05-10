/**
 * 对应清单：TC_C7SEL_006 - remote 首次展开全量请求不含 query（行为侧：全量选项可见）
 */
import {test, expect} from '@playwright/test'
import * as path from 'path'

const shotDir = __dirname

test.describe('TC_C7SEL_006 - remote 首次展开全量请求不含 query', () => {
  test('首次展开 remote 下拉展示全量 mock 城市', async ({page}) => {
    await page.goto('/dev/c7-select-e2e')
    const sel = page.getByTestId('c7-sel-remote')
    await sel.click()
    await page.screenshot({path: path.join(shotDir, '01_remote_first_open.png'), fullPage: true})

    await expect(page.getByRole('option', {name: '上海'})).toBeVisible()
    await expect(page.getByRole('option', {name: '北京'})).toBeVisible()
    await expect(page.getByRole('option', {name: '南京'})).toBeVisible()
  })
})
