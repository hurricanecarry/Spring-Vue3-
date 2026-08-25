import {test,expect}  from '@playwright/test'



test.use({ baseURL: 'http://localhost:5173',
   browserName:'chromium',
   channel:'msedge',
   headless:false,
 })


test("错误测试",async ({browser,page,context})=>{
    await  page.goto('/login');
    const loginPanel=page.locator('.login-panel');
    await page.getByRole('button',{name:'去登录',exact:true}).click();
    await  loginPanel.getByRole('textbox',{name:'邮箱 / 昵称',exact:true}).fill('我的世界');
    await  loginPanel.getByRole('textbox',{name:'密码',exact:true}).fill('Daisy');
    await loginPanel.getByRole('button',{name:'登 录',exact:true}).click();
     
    await expect(page.getByText(/登录失败|错误|密码错误|用户名错误/)).toBeVisible();
    await  expect(page).toHaveURL('/login');
})
