import { test, expect } from '@playwright/test'

// 辅助函数：通过 API 登录，绕过双面板 DOM 遮挡
async function loginViaAPI(page) {
  await page.goto('/login')
  // 直接调后端 login，拿 token 存 localStorage
  const response = await page.evaluate(async () => {
    const res = await fetch('/backend/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: 'account=limore&password=daisy'
    })
    const data = await res.json()
    if (data.code === 200) {
      localStorage.setItem('token', data.data.token)
      localStorage.setItem('user', JSON.stringify(data.data.userInfo))
      return true
    }
    return false
  })
  if (response) {
    await page.goto('/')
    // 点一下侧边栏"首页"让内容加载出来
    await page.locator('text=首页').click()
    await page.waitForTimeout(1000)
  } else {
    throw new Error('API 登录失败')
  }
}

test.describe('页面基本功能', () => {

  test('打开网站 → 看到登录/注册页面', async ({ page }) => {
    await page.goto('/')
    await expect(page).toHaveTitle('网盘个人空间')
    await expect(page.locator('text=登 录')).toBeVisible()
    await expect(page.locator('text=注 册')).toBeVisible()
  })

  test('已有账号登录 → 进入主页', async ({ page }) => {
    await loginViaAPI(page)
    await expect(page.locator('text=全部文件')).toBeVisible()
  })

  test('新建文件夹', async ({ page }) => {
    await loginViaAPI(page)

    const folderName = 'test_' + Date.now()
    await page.locator('text=新建文件夹').click()
    await page.locator('.create-input input').fill(folderName)
    await page.locator('.create-input input').press('Enter')
    await page.waitForTimeout(2000)

    await expect(page.locator(`text=${folderName}`)).toBeVisible({ timeout: 10000 })
  })

  test('搜索过滤', async ({ page }) => {
    await loginViaAPI(page)

    const searchName = 'search_' + Date.now()
    await page.locator('text=新建文件夹').click()
    await page.locator('.create-input input').fill(searchName)
    await page.locator('.create-input input').press('Enter')
    await page.waitForTimeout(2000)

    await page.locator('input[placeholder="搜索文件..."]').fill(searchName)
    await page.locator('#search-icon').click()
    await page.waitForTimeout(1000)

    await expect(page.locator(`text=${searchName}`)).toBeVisible()
  })

})

test.describe('并发性能', () => {
  test('5 个文件夹并发创建 → 刷新后全部可见', async ({ page }) => {
    await loginViaAPI(page)

    const start = Date.now()
    const names = Array.from({ length: 5 }, (_, i) => `concurrent_${Date.now()}_${i}`)

    // 并发调 API 创建文件夹（不走 UI，模拟真实并发）
    await page.evaluate(async (names) => {
      const token = localStorage.getItem('token')
      await Promise.all(names.map(name =>
        fetch('/backend/file/NewFolder?parentId=0&fileName=' + encodeURIComponent(name), {
          method: 'PUT',
          headers: { 'Authorization': `Bearer ${token}` }
        })
      ))
    }, names)

    // 强制刷新页面
    await page.reload()
    await page.waitForTimeout(2000)
    await page.locator('text=首页').click()
    await page.waitForTimeout(1000)

    // 验证 5 个全部出现
    for (const name of names) {
      await expect(page.locator(`text=${name}`)).toBeVisible({ timeout: 5000 })
    }

    const elapsed = Date.now() - start
    console.log(`✅ 并发创建 5 个文件夹：${elapsed}ms（含刷新）`)
  })
})
