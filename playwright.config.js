import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './test',
  timeout: 30000,
  retries: 0,
  fullyParallel: false,          // 用例间数据有依赖，先用单 worker 稳妥；CI 再看情况并行
  reporter: 'list',
  use: {
    baseURL: 'http://localhost:5173',   // 注意：之前这里开头多了个空格，会导致相对路径拼出非法 URL
    headless: false,                     // 本地想看过程；CI 上改为 true 或直接删掉
    screenshot: 'only-on-failure',
    trace: 'on-first-retry',             // 失败重试时录 trace，方便排查
  },
})
