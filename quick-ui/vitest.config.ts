import { defineConfig } from 'vitest/config'
import path from 'path'

export default defineConfig({
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  test: {
    include: ['src/**/*.test.{js,ts}', 'src/packages/C7Preview/**/*.test.ts'],
    environment: 'node',
  },
})
