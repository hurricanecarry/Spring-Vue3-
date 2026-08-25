import { test, expect } from '@playwright/test'

test.describe('性能测试', () => {

  test('登录 + 创建文件夹 + 上传图片 + 刷新重进', async ({ page }) => {
    test.setTimeout(90000) // 给 90 秒
    const perf = {} // 记录各步骤耗时

    // ① 登录
    const t0 = Date.now()
    await page.goto('/login')
    await page.evaluate(async () => {
      const res = await fetch('/backend/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'account=limore&password=daisy',
      })
      const data = await res.json()
      if (data.code === 200) {
        localStorage.setItem('token', data.data.token)
        localStorage.setItem('user', JSON.stringify(data.data.userInfo))
      }
    })
    await page.goto('/home')
    await page.waitForTimeout(1500)
    perf.login = Date.now() - t0

    // ② 页面加载
    const t1 = Date.now()
    await page.reload()
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)
    perf.pageReload = Date.now() - t1

    // ③ 创建 3 个文件夹
    const t2 = Date.now()
    const names = []
    for (let i = 0; i < 3; i++) {
      const name = `perf_${Date.now()}_${i}`
      names.push(name)
      await page.locator('text=新建文件夹').click()
      await page.locator('.create-input input').fill(name)
      await page.locator('.create-input input').press('Enter')
      // 等表格重建完成 + loading 结束
      await page.waitForTimeout(2000)
    }
    perf.createFolders = Date.now() - t2

    // ④ 上传图片 — 精确计时（等文件出现在列表为止）
    const uploadName = '屏幕截图 2026-01-17 121502.png'
    const t3 = Date.now()
    const fileInput = page.locator('input[type="file"]').first()
    await fileInput.setInputFiles(
      'C:/Users/hurri/Pictures/Screenshots/屏幕截图 2026-01-17 121502.png',
    )
    // 等文件名真正出现在表格中
    await expect(page.locator(`text=${uploadName}`)).toBeVisible({ timeout: 20000 })
    perf.upload = Date.now() - t3

    // ⑤ 退出重进（模拟关闭浏览器再打开）
    const t4 = Date.now()
    await page.goto('/home')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(2000)
    perf.revisit = Date.now() - t4

    // ⑥ 看文件列表是否正常
    const fileCount = await page.locator('.el-table__row').count()

    console.log(`
    ╔══════════════════════╦══════════╗
    ║        步骤          ║  耗时    ║
    ╠══════════════════════╬══════════╣
    ║ 登录                 ║ ${String(perf.login).padStart(5)}ms ║
    ║ 页面刷新             ║ ${String(perf.pageReload).padStart(5)}ms ║
    ║ 创建 3 个文件夹      ║ ${String(perf.createFolders).padStart(5)}ms ║
    ║ 上传 1 张图 (957KB)  ║ ${String(perf.upload).padStart(5)}ms ║
    ║ 退出重进             ║ ${String(perf.revisit).padStart(5)}ms ║
    ╠══════════════════════╬══════════╣
    ║ 文件列表行数         ║ ${fileCount} 个文件  ║
    ╚══════════════════════╩══════════╝
    `)

    expect(fileCount).toBeGreaterThan(0)
  })
})
