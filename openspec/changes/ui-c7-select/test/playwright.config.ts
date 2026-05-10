import {defineConfig, devices} from '@playwright/test'
import * as path from 'path'

const authFile = path.join(__dirname, '.auth', 'user.json')

export default defineConfig({
  testDir: './',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: 1,
  reporter: [['list'], ['html', {outputFolder: 'playwright-report'}]],
  use: {
    baseURL: 'http://localhost:8800',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    // 本地有头：Chrome；CI/自动化环境优先 Edge（通常免额外下载 Chromium zip）
    channel: (process.env.CI ? 'msedge' : 'chrome') as 'chrome' | 'msedge',
    headless: !!process.env.CI,
  },
  projects: [
    {
      name: 'setup',
      testMatch: 'auth.setup.ts',
    },
    {
      name: 'login-only',
      testMatch: 'TC_C7SEL_001/test.spec.ts',
    },
    {
      name: 'e2e',
      dependencies: ['setup'],
      testMatch: 'TC_C7SEL_*/test.spec.ts',
      testIgnore: ['TC_C7SEL_001/**'],
      use: {
        ...devices['Desktop Chrome'],
        storageState: authFile,
        launchOptions: {
          args: ['--start-maximized'],
        },
        viewport: {width: 1280, height: 900},
      },
    },
  ],
})
