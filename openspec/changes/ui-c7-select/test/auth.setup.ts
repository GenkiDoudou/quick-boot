/**
 * 登录并写入 storageState，供 `e2e` project 复用（与清单 TC_C7SEL_001 默认账号一致：以项目 Dev 常见 admin/admin 为准）。
 */
import {test as setup, expect} from '@playwright/test'
import * as fs from 'fs'
import * as path from 'path'

const authFile = path.join(__dirname, '.auth', 'user.json')

setup('authenticate', async ({page}) => {
  fs.mkdirSync(path.dirname(authFile), {recursive: true})
  await page.goto('/login')
  await page.getByPlaceholder('用户名').fill('admin')
  await page.getByPlaceholder('密码').fill('admin')
  await page.getByRole('button', {name: /登\s*录/}).click()
  await page.waitForURL((u) => !u.pathname.endsWith('/login'), {timeout: 60000})
  await expect(page).not.toHaveURL(/\/login$/)
  await page.context().storageState({path: authFile})
})
