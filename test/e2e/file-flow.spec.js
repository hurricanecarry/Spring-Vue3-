import path from 'node:path'
import fs from 'node:fs'
import { logintest as test, expect } from '../fixtures'

// 用带登录态的 logintest（storageState 复用），直接"免登录"进入主页操作文件
test('登录态下文件全流程：建夹 → 上传 → 下载 → 分享', async ({ page }) => {
  const stamp = Date.now()
  const folder = `目录_${stamp}`

  // 每次运行用唯一内容的上传文件（内容不同 → MD5 不同），避免并发上传触发后端 MD5 去重竞态
  const uploadName = `upload_${stamp}.txt`
  const uploadPath = path.join(process.cwd(), 'test-results', uploadName)
  fs.mkdirSync(path.dirname(uploadPath), { recursive: true })
  fs.writeFileSync(uploadPath, `playwright 自动化上传测试文件 ${stamp}\n`)

  await page.goto('/')
  // 已登录 → 主页工具栏出现
  await expect(page.getByRole('button', { name: '新建文件夹' })).toBeVisible()

  // ① 建文件夹
  await page.getByRole('button', { name: '新建文件夹' }).click()
  await page.getByRole('textbox', { name: '输入文件夹名称' }).fill(folder)
  await page.keyboard.press('Enter')   // 回车确认
  await expect(page.getByText(folder, { exact: true })).toBeVisible()

  // ② 上传（直接对隐藏 file input 设置，最稳；走 UI 菜单有 Element Plus 下拉开合竞态）
  await page.locator('input[type="file"]').first().setInputFiles(uploadPath)
  await expect(page.getByText(uploadName, { exact: true })).toBeVisible()

  // ③ 下载并校验（三层断言：文件名 / 未失败 / 内容）
  const row = page.locator('tr', { hasText: uploadName })
  await row.hover()   // 行内小工具栏悬停才显示
  const [download] = await Promise.all([
    page.waitForEvent('download'),
    row.locator('.mini-toolbar-item').nth(1).click(),   // 第 2 个图标 = 下载
  ])
  expect(download.suggestedFilename()).toContain(uploadName)
  expect(await download.failure()).toBeNull()
  const stream = await download.createReadStream()
  let content = ''
  for await (const chunk of stream) content += chunk.toString()
  expect(content).toContain(`playwright 自动化上传测试文件 ${stamp}`)

  // ④ 分享并生成链接
  await row.hover()
  await row.locator('.mini-toolbar-item').nth(0).click()   // 第 1 个图标 = 分享
  const dialog = page.locator('.el-dialog')
  await expect(dialog).toBeVisible()
  await dialog.getByRole('button', { name: '生成链接' }).click()
  await expect(dialog.locator('.link-row input')).toHaveValue(/\/share\//)
})
