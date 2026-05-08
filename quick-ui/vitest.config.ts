import { defineConfig } from 'vitest/config'

export default defineConfig({
  test: {
    include: ['src/packages/C7Preview/**/*.test.ts'],
    environment: 'node',
  },
})
