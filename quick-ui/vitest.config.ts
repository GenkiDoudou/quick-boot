import { defineConfig } from 'vitest/config'
import path from 'path'

export default defineConfig({
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      '@quickboot/lite-rum': path.resolve(__dirname, '../packages/lite-rum/src/index.js'),
    },
  },
  test: {
    include: [
      'src/test/**/*.test.{js,ts}',
      'src/packages/C7Preview/**/*.test.ts',
      'src/utils/**/*.test.{js,ts}',
      'src/packages/__tests__/**/*.test.{js,ts}',
      'src/directive/**/__tests__/**/*.test.{js,ts}'
    ],
    environment: 'node',
  },
})
