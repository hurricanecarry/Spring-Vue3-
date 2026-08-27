import { test, expect } from '@playwright/test'

const BASE = 'http://localhost:8980/backend'
const ACCOUNT = 'logintest@qq.com'
const PASSWORD = 'login123456'

test.describe('文件接口', () => {
  test('文件列表：带 token 能访问（数据层），匿名返回空', async ({ request }) => {
    // 先登录拿 token
    const login = await request.post(`${BASE}/auth/login`, {
      params: { account: ACCOUNT, password: PASSWORD },
    })
    const { data } = await login.json()
    const token = data.token

    // ① 带 token → 返回自己的文件
    const withToken = await request.get(`${BASE}/file/list`, {
      params: { parentId: '0', status: 0 },
      headers: { Authorization: `Bearer ${token}` },       // token 走 Authorization 头（后端 TokenAuthFilter 校验）
    })
    expect(withToken).toBeOK()
    const ok = await withToken.json()
    expect(ok.code).toBe(200)
    expect(Array.isArray(ok.data)).toBe(true)              // data 是数组（你的文件）

    // ② 不带 token → 接口未强制鉴权：HTTP 也是 200，但返回空（不泄露你的文件）
    const noToken = await request.get(`${BASE}/file/list`, { params: { parentId: '0', status: 0 } })
    expect(noToken).toBeOK()
    const empty = await noToken.json()
    expect(empty.code).toBe(200)
    expect(empty.data).toEqual([])
    // ⚠️ 安全提示：该接口未强制鉴权（未登录也返回 200），严格做法应返回 401/403。
    //    当前未泄露数据（匿名只能看到空），此用例记录该行为。
  })
})
