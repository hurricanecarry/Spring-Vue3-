const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({
    channel: 'msedge',
    headless: false
  });
  const context = await browser.newContext();
  const page = await context.newPage();
  await page.goto('http://localhost:5173/login');
  await page.getByRole('textbox', { name: '昵称', exact: true }).click();
  await page.getByRole('textbox', { name: '昵称', exact: true }).fill('测试账号new1');
  await page.getByRole('textbox', { name: '邮箱', exact: true }).click();
  await page.getByRole('textbox', { name: '邮箱', exact: true }).fill('cszhnew1@qq.com');
  await page.locator('#el-id-2032-12').click();
  await page.locator('#el-id-2032-12').fill('daisy');
  await page.getByRole('textbox', { name: '确认密码' }).click();
  await page.getByRole('textbox', { name: '确认密码' }).fill('daisy');
  await page.getByRole('button', { name: '注 册' }).click();
  await page.getByRole('button', { name: '新建文件夹' }).click();
  await page.getByRole('textbox', { name: '输入文件夹名称' }).click();
  await page.getByRole('textbox', { name: '输入文件夹名称' }).fill('我的世界111');
  await page.getByRole('button', { name: '确认' }).click();
  await page.getByRole('menuitem', { name: '上传文件', exact: true }).click();
  await page.locator('input[type="file"]').first().setInputFiles('我的简历.pdf');
  const downloadPromise = page.waitForEvent('download');
  await page.locator('tr:nth-child(2) > .el-table_4_column_14 > .cell > .file-name-cell > .mini-toolbar > i > svg > path').first().click();
  const download = await downloadPromise;
  const page1 = await context.newPage();
  await page1.goto('edge://downloads-hub/');
  await page1.close();

  // ---------------------
  await context.close();
  await browser.close();
})();