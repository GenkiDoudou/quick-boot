/**
 * 对应清单：TC_C7SEL_004 - 静态 clearable 清空
 */
import {test, expect} from '@playwright/test'
import * as path from 'path'

const shotDir = __dirname

test.describe('TC_C7SEL_004 - 静态 clearable 清空', () => {
  test('选中后点击清除，model 为空语义', async ({page}) => {
    await page.goto('/dev/c7-select-e2e')
    const sel = page.getByTestId('c7-sel-static')
    await sel.click()
    await page.getByRole('option', {name: '苹果'}).click()
    await expect(page.getByTestId('c7-sel-static-model')).toContainText('apple')
    await page.screenshot({path: path.join(shotDir, '01_before_clear.png'), fullPage: true})

    const clearBtn = sel.locator('.el-select__clear')
    await expect(clearBtn).toBeVisible({timeout: 5000})
    await clearBtn.click()
    await page.screenshot({path: path.join(shotDir, '02_after_clear.png'), fullPage: true})

    const modelText = (await page.getByTestId('c7-sel-static-model').innerText()).trim()
    expect(modelText).not.toContain('apple')
    expect(/""|''|null|undefined|^$/i.test(modelText) || modelText === '[]').toBeTruthy()
  })
})
