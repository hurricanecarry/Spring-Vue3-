import { test, expect } from '@playwright/test'

test.use({ baseURL: 'http://localhost:5173',
   browserName:'chromium',
   channel:'msedge',
   headless:false,
 })

test("登录加载测试",async({page})=>{
     await page.goto('http://localhost:5173/login',{waitUntil:'load'});
     const m=await page.evaluate(()=>{
        //获得最近一次页面完整加载的数据
         const nav=performance.getEntriesByType('navigation')[0];
         return{
            //第一个字节时间
             ttfb:nav.responseStart-nav.requestStart,
             //dom树构建完成
             domContentLoaded:nav.domContentLoadedEventEnd-nav.requestStart,
             //资源全部加载
             load:nav.loadEventEnd-nav.requestStart,
             //服务器实际传输数据
             transferKB:Math.round(nav.transferSize/1024),
         }
     })
     console.log("登录页载入:",m);
     //首字节到达时间<1s
     expect(m.ttfb).toBeLessThan(1000);
     expect(m.load).toBeLessThan(3000);
})