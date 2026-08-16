import { defineConfig } from 'vite'

export default defineConfig({
  build: {
    lib: {
      entry: 'src/cdn.js',
      name: 'LiteRum',
      formats: ['iife'],
      fileName: () => 'lite-rum.min.js'
    },
    outDir: 'dist',
    emptyOutDir: true,
    minify: false,
    sourcemap: false,
    target: 'es2018'
  }
})
