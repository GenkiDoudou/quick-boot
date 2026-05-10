/**
 * 对应清单：TC_C7SEL_001 - 合法账号登录并进入可测状态
 */
import {test, expect} from '@playwright/test'
import * as path from 'path'

const shotDir = __dirname

test.describe('TC_C7SEL_001 - 合法账号登录并进入可测状态', () => {
  test('验证使用合法账号成功登录，会话有效', async ({page}) => {
    await page.goto('/login')
    await page.screenshot({path: path.join(shotDir, '01_login_page.png'), fullPage: true})

    await page.getByPlaceholder('用户名').fill('admin')
    await page.getByPlaceholder('密码').fill('admin')
    await page.screenshot({path: path.join(shotDir, '02_filled_form.png'), fullPage: true})

    await page.getByRole('button', {name: /登\s*录/}).click()
    await page.waitForURL((u) => !u.pathname.endsWith('/login'), {timeout: 60000})
    await expect(page).not.toHaveURL(/\/login$/)

    await page.screenshot({path: path.join(shotDir, '03_after_login.png'), fullPage: true})
  })
})
