/**
 * 对应清单：TC_C7SEL_009 - 多选 + separator 对外逗号字符串与空选择（自动化覆盖：初始逗号串展示）
 * 清空与多选增删的完整手测见清单步骤 2～4。
 */
import {test, expect} from '@playwright/test'
import * as path from 'path'

const shotDir = __dirname

test.describe('TC_C7SEL_009 - 多选 + separator 对外逗号字符串与空选择', () => {
  test('初始 model 展示含逗号的字符串且含 a、x、b', async ({page}) => {
    await page.goto('/dev/c7-select-e2e')
    const model = page.getByTestId('c7-sel-sep-model')
    await page.screenshot({path: path.join(shotDir, '01_sep_initial.png'), fullPage: true})

    const t = (await model.innerText()).trim()
    expect(t).toContain('x')
    expect(t).toContain('a')
    expect(t).toContain('b')
    expect(t).toMatch(/,/)
  })
})
