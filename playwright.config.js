import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './test',
  timeout: 30000,
  use: {
    baseURL: ' http://localhost:5173',
    headless: false,           // 显示浏览器窗口，看得到操作过程
    screenshot: 'only-on-failure',
  },
})
