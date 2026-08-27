import { test, expect } from '@playwright/test'

const BASE = 'http://localhost:8980/backend'
const ACCOUNT = 'logintest@qq.com'
const PASSWORD = 'login123456'

// 接口测试（request fixture 直连后端，不走浏览器）
test.describe('登录接口', () => {
  test('正确账号：返回 token（HTTP 层 + 业务层 + 数据层）', async ({ request }) => {
    const res = await request.post(`${BASE}/auth/login`, {
      params: { account: ACCOUNT, password: PASSWORD },   // 与前端 axios params 一致（后端 @RequestParam）
    })
    expect(res).toBeOK()                                  // ① HTTP 层：200
    const body = await res.json()
    expect(body.code).toBe(200)                            // ② 业务层：成功
    expect(body.data.token).toBeTruthy()                   // ③ 数据层：token 在 data 里
  })

  test('错误密码：返回业务码 403「密码错误」', async ({ request }) => {
    const res = await request.post(`${BASE}/auth/login`, {
      params: { account: ACCOUNT, password: 'wrongpass' },
    })
    expect(res.status()).toBe(200)                          // 注意：后端 HTTP 恒 200，真结果在 body.code
    const body = await res.json()
    expect(body.code).toBe(403)
    expect(body.message).toContain('密码错误')
  })

  test('不存在的账号：返回业务码 404「用户不存在」（账号枚举风险）', async ({ request }) => {
    const res = await request.post(`${BASE}/auth/login`, {
      params: { account: `nobody_${Date.now()}@qq.com`, password: 'x' },
    })
    const body = await res.json()
    expect(body.code).toBe(404)
    expect(body.message).toContain('用户不存在')
    // ⚠️ 安全提示：当前实现会区分"用户不存在(404)"与"密码错误(403)"，存在账号枚举漏洞。
    //    按 AGENTS.md 原则应统一返回 403。此用例固化了当前行为，作为已知风险提示。
  })
})
