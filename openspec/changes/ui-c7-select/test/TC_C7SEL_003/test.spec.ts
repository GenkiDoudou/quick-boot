/**
 * 对应清单：TC_C7SEL_003 - 静态 dataList 选择与 v-model 同步
 */
import {test, expect} from '@playwright/test'
import * as path from 'path'

const shotDir = __dirname

test.describe('TC_C7SEL_003 - 静态 dataList 选择与 v-model 同步', () => {
  test('选择香蕉后 model 展示 banana', async ({page}) => {
    await page.goto('/dev/c7-select-e2e')
    const sel = page.getByTestId('c7-sel-static')
    await sel.click()
    await page.screenshot({path: path.join(shotDir, '01_dropdown_open.png'), fullPage: true})

    await page.getByRole('option', {name: '香蕉'}).click()
    await page.screenshot({path: path.join(shotDir, '02_selected.png'), fullPage: true})

    await expect(page.getByTestId('c7-sel-static-model')).toContainText('banana')
  })
})
