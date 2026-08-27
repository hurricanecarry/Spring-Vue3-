import { test as base, expect } from '@playwright/test'

export { expect }

// 本地库中已存在的测试账号（用于登录态复用）
export const ACCOUNT = 'logintest@qq.com'
export const PASSWORD = 'login123456'

// 普通 test（不带登录态）—— 用于注册/登录相关测试
export const test = base

// 带登录态（storageState 复用）的 test —— 用于"免登录"直接操作页面的测试
// 原理：通过 API 登录一次拿到 token，塞进 localStorage；后续测试浏览器一打开就是"已登录"
export const logintest = base.extend({
  storageState: async ({ request }, use) => {
    const res = await request.post('http://localhost:8980/backend/auth/login', {
      params: { account: ACCOUNT, password: PASSWORD },
    })
    const { data } = await res.json()
    await use({
      cookies: [],
      origins: [
        {
          origin: 'http://localhost:5173',
          localStorage: [
            { name: 'token', value: data.token },
            { name: 'user', value: JSON.stringify(data.userInfo) },
          ],
        },
      ],
    })
  },
})
