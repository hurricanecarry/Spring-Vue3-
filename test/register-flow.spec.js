import { test, expect } from '@playwright/test'
import path from 'node:path'
import fs from 'node:fs'

// ⚠️ 本文件针对【本地开发环境】：覆盖 baseURL，不影响其他测试（它们仍走 config 里的远程地址）
// 运行前提：后端 8980 + 前端 5173 都在跑
test.use({ baseURL: 'http://localhost:5173' })

const PASSWORD = 'daisy123'

// 完整业务流冒烟测试：注册 → 建文件夹 → 上传 → 下载 → 分享
// 用 test.step 把流程切成命名步骤，报告里一目了然
test('注册 → 建文件夹 → 上传 → 下载 → 分享', async ({ page }) => {
  // 每次运行生成唯一账号，避免二次运行撞邮箱
  const stamp = Date.now()
  const nickname = `用户${stamp}`
  const email = `test${stamp}@qq.com`

  // 每次运行生成唯一内容的上传文件 → MD5 不同
  // 原因：后端按 MD5 去重（check-then-act，无锁），两个 worker 并发上传相同内容会竞态失败
  // 这是测试隔离原则：测试数据必须互不影响、可并行、可重复跑
  const uploadName = `upload-${stamp}-${Math.random().toString(36).slice(2, 8)}.txt`
  const uploadPath = path.join(process.cwd(), 'test-results', uploadName)
  fs.mkdirSync(path.dirname(uploadPath), { recursive: true })
  fs.writeFileSync(uploadPath, `自动化测试上传文件-${stamp}\n`)

  try {
    await test.step('1. 注册新账号并进入主页', async () => {
      await page.goto('/login')

      // 登录面板和注册面板同时在 DOM 里（都有"密码"输入框），
      // 所以用 .register-panel 限定范围，避免定位器撞车
      const registerPanel = page.locator('.register-panel')
      await registerPanel.getByRole('textbox', { name: '昵称' }).fill(nickname)
      await registerPanel.getByRole('textbox', { name: '邮箱' }).fill(email)
      await registerPanel.getByRole('textbox', { name: '密码', exact: true }).fill(PASSWORD)
      await registerPanel.getByRole('textbox', { name: '确认密码' }).fill(PASSWORD)
      await registerPanel.getByRole('button', { name: '注 册' }).click()

      // 注册成功 → 自动进主页，工具栏出现（自动等待，不用 sleep）
      await expect(page.getByRole('button', { name: '新建文件夹' })).toBeVisible()
    })

    await test.step('2. 批量创建文件夹', async () => {
      const names = [`目录A_${stamp}`, `目录B_${stamp}`]
      for (const name of names) {
        await page.getByRole('button', { name: '新建文件夹' }).click()
        await page.getByRole('textbox', { name: '输入文件夹名称' }).fill(name)
        await page.keyboard.press('Enter') // 回车确认，等价于点"确认"按钮
        await expect(page.getByText(name, { exact: true })).toBeVisible()
      }
    })

    await test.step('3. 上传文件', async () => {
      // 直接对隐藏的 file input 设置文件（最稳，官方推荐）
      // 注：走 UI 菜单（上传 → 上传文件 → 系统文件框）存在 Element Plus 下拉开合竞态，
      // 实测偶发点不到菜单项导致 filechooser 不触发（flaky）；
      // setInputFiles 会触发同样的 change 事件 → 同一上传逻辑，测试价值等价
      await page.locator('input[type="file"]').first().setInputFiles(uploadPath)

      // 上传完成 → 文件名出现在列表
      await expect(page.getByText(uploadName, { exact: true })).toBeVisible()
    })

    await test.step('4. 下载文件并校验', async () => {
      // 悬停该行 → 行内小工具栏出现（分享/下载/删除）
      const row = page.locator('tr', { hasText: uploadName })
      await row.hover()

      // 第 2 个图标 = 下载；Promise.all 保证"监听事件"先于"点击"注册，不漏接
      const [download] = await Promise.all([
        page.waitForEvent('download'),
        row.locator('.mini-toolbar-item').nth(1).click(),
      ])

      // 断言①：文件名正确
      expect(download.suggestedFilename()).toContain(uploadName)

      // 断言②：下载没有失败（注意：新版 failure() 返回 Promise，要 await）
      expect(await download.failure()).toBeNull()

      // 断言③（硬核）：读内容，确认是"真文件"而不是错误 JSON
      // createReadStream 直接从内存流读，不落盘、免清理
      const stream = await download.createReadStream()
      let content = ''
      for await (const chunk of stream) content += chunk.toString()
      expect(content).toContain(`自动化测试上传文件-${stamp}`)

      // 另存一份到 test-results，方便你手动打开检查
      await download.saveAs(path.join('test-results', download.suggestedFilename()))
    })

    await test.step('5. 分享文件并生成链接', async () => {
      const row = page.locator('tr', { hasText: uploadName })
      await row.hover()
      await row.locator('.mini-toolbar-item').nth(0).click() // 第 1 个图标 = 分享

      const dialog = page.locator('.el-dialog')
      await expect(dialog).toBeVisible()
      await expect(dialog.getByText('分享文件:')).toBeVisible()

      await dialog.getByRole('button', { name: '生成链接' }).click()
      // 生成成功 → 出现分享链接 + 复制按钮
      await expect(dialog.getByRole('button', { name: '复制链接' })).toBeVisible()
      await expect(dialog.locator('.link-row input')).toHaveValue(/\/share\//)
    })
  } finally {
    // 不清理 uploadPath：它和 saveAs 的下载文件同名同路径，删了会把下载结果也删掉
    // 而且 test-results 每次运行开始会被 Playwright 清空（outputDir），无需手动清理
  }
})
