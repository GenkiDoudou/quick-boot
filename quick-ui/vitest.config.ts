import { defineConfig } from 'vitest/config'
import path from 'path'

export default defineConfig({
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  test: {
    include: [
      'src/test/**/*.test.{js,ts}',
      'src/packages/C7Preview/**/*.test.ts',
      'src/utils/**/*.test.{js,ts}',
      'src/directive/**/__tests__/**/*.test.{js,ts}'
    ],
    environment: 'node',
  },
})
