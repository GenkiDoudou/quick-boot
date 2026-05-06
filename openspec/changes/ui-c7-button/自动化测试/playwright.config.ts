import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: 1,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:8800',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    channel: 'chrome',
    headless: true,
  },
  projects: [
    {
      name: 'setup',
      testMatch: 'auth.setup.ts',
    },
    {
      name: 'login-only',
      testMatch: '**/TC_C7BTN_001/**/*.spec.ts',
    },
    {
      name: 'authenticated',
      dependencies: ['setup'],
      testIgnore: ['**/TC_C7BTN_001/**', 'auth.setup.ts'],
      use: {
        ...devices['Desktop Chrome'],
        storageState: '.auth/user.json',
        launchOptions: {
          args: ['--start-maximized'],
        },
        viewport: { width: 1280, height: 720 },
      },
    },
  ],
});
