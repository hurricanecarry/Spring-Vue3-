import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  css: {
    transformer: 'postcss',   // 用 postcss 不用 lightningcss，避免 :deep 误报
  },
  plugins: [vue(), vueDevTools()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    open:true,
    proxy: {
      '/backend': {
        target: 'http://localhost:8980',
        changeOrigin: true,
      },
    },
  },
})
