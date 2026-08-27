import { test, expect } from '../fixtures'

// 注册 / 登录相关的 UI 测试（不带登录态，用普通 test）
test.describe('注册与登录', () => {
  test('注册新账号并自动进入主页', async ({ page }) => {
    // 每次用唯一账号，避免二次运行撞邮箱
    const stamp = Date.now()
    const nickname = `用户_${stamp}`
    const email = `user_${stamp}@qq.com`
    const password = 'daisy123'

    await page.goto('/login')
    const registerPanel = page.locator('.register-panel')   // 登录/注册两个面板都在 DOM，用范围避免撞车
    await registerPanel.getByRole('textbox', { name: '昵称' }).fill(nickname)
    await registerPanel.getByRole('textbox', { name: '邮箱' }).fill(email)
    await registerPanel.getByRole('textbox', { name: '密码', exact: true }).fill(password)   // 密码二字会同时命中"确认密码"，要 exact
    await registerPanel.getByRole('textbox', { name: '确认密码' }).fill(password)
    await registerPanel.getByRole('button', { name: '注 册' }).click()

    // 注册成功 → 自动进主页，出现"新建文件夹"（自动等待，不用 sleep）
    await expect(page.getByRole('button', { name: '新建文件夹' })).toBeVisible()
  })

  test('错误密码登录：提示错误且不跳转', async ({ page }) => {
    await page.goto('/login')
    // "去登录"按钮在覆盖层，不在 login-panel 里（用 page 直接找）
    await page.getByRole('button', { name: '去登录', exact: true }).click()

    const loginPanel = page.locator('.login-panel')
    await loginPanel.getByPlaceholder('邮箱 / 昵称').fill('logintest@qq.com')
    await loginPanel.getByPlaceholder('密码').fill('wrongpass')   // 故意错
    await loginPanel.getByRole('button', { name: '登 录', exact: true }).click()

    // ① 断言错误提示出现（正则容错，别写死整句文案）
    await expect(page.getByText(/密码错误|错误/)).toBeVisible()
    // ② 断言还在登录页（没跳走）
    await expect(page).toHaveURL('/login')
  })
})
