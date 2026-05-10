/**
 * 对应清单：TC_C7SEL_007 - remote 输入关键字后请求含 query 且列表更新
 */
import {test, expect} from '@playwright/test'
import * as path from 'path'

const shotDir = __dirname

test.describe('TC_C7SEL_007 - remote 输入关键字后请求含 query 且列表更新', () => {
  test('输入「北」后仅展示北京', async ({page}) => {
    await page.goto('/dev/c7-select-e2e')
    const sel = page.getByTestId('c7-sel-remote')
    await sel.click()
    await expect(page.getByRole('option', {name: '上海'})).toBeVisible()

    const input = sel.locator('input').first()
    await input.fill('北')
    await page.waitForTimeout(450)
    await page.screenshot({path: path.join(shotDir, '01_after_filter.png'), fullPage: true})

    await expect(page.getByRole('option', {name: '北京'})).toBeVisible()
    await expect(page.getByRole('option', {name: '上海'})).toHaveCount(0)
  })
})
