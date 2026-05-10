/**
 * 对应清单：TC_C7SEL_011 - 逗号字符串回显与缺 option 的 value 保留
 */
import {test, expect} from '@playwright/test'
import * as path from 'path'

const shotDir = __dirname

test.describe('TC_C7SEL_011 - 逗号字符串回显与缺 option 的 value 保留', () => {
  test('model 文案仍包含无 option 的 x', async ({page}) => {
    await page.goto('/dev/c7-select-e2e')
    await page.screenshot({path: path.join(shotDir, '01_sep_model.png'), fullPage: true})
    await expect(page.getByTestId('c7-sel-sep-model')).toContainText('x')
  })
})
