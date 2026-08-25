import {test,expect}  from '@playwright/test'



test.use({ baseURL: 'http://localhost:5173',
   browserName:'chromium',
   channel:'msedge',
   headless:false,
 })

 test("基础测试",async ({browser,page,context})=>{
         await  page.goto('/login');
         await expect(page).toHaveTitle('网盘个人空间');
         const registerPanel=page.locator('.register-panel')
         const loginPanel=page.locator('.login-panel')


         await expect(loginPanel.getByRole('heading',{name:'登录'})).toBeVisible();
         await expect(loginPanel.getByRole('textbox',{name:'邮箱 / 昵称'})).toBeVisible();
         await expect(loginPanel.getByRole('textbox',{name:'密码',exact:true})).toBeVisible();
         await expect(loginPanel.getByRole('button',{name:'登 录',exact:true})).toBeVisible();

         await expect(registerPanel.getByRole('heading',{name:'注册'})).toBeVisible();
         await expect(registerPanel.getByRole('textbox',{name:'昵称'})).toBeVisible();
         await expect(registerPanel.getByRole('textbox',{name:'邮箱'})).toBeVisible();
         await expect(registerPanel.getByRole('textbox',{name:'密码',exact:true})).toBeVisible();
         await expect(registerPanel.getByRole('textbox',{name:'确认密码',exact:true})).toBeVisible();
         await expect(registerPanel.getByRole('button',{name:'注 册'})).toBeVisible();
 })