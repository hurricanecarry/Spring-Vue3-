import {test as base,expect}  from '@playwright/test'



base.use({ baseURL: 'http://localhost:5173',
   browserName:'chromium',
   channel:'msedge',
   headless:false,
 })


const test =base.extend({
    storageState:async({request},use)=>{
          const res=await request.post('http://localhost:8980/backend/auth/login',{
              form:{
                 account:'我的世界',password:'daisy'
              },
          })
          const {data}=await res.json()
          const state={
              cookies:[],
              origins:[{
                 origin:'http://localhost:5173',
                 localStorage:[
                    {name:'token',value:data.token},
                    {name:'user',value:JSON.stringify(data.userInfo)},
                 ]
              }]
          }
          await use(state);
    }
})



test("登录态存储化访问",async({page})=>{
    await page.goto('/');
    await expect(page.getByRole('button',{name:'新建文件夹',exact:true})).toBeVisible();
})
