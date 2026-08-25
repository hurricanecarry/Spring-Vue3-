import {test ,expect}  from '@playwright/test'



test.use({ baseURL: 'http://localhost:5173',
   browserName:'chromium',
   channel:'msedge',
   headless:false,
 })


const backend='http://localhost:8980/backend' 

test("正常成功登录",async({request})=>{
     const res=await request.post(`${backend}/auth/login`,{
          params:{account:'我的世界',password:'daisy'},
     })
     expect(res).toBeOK();
     const body=await res.json();
     
     expect(body.code).toBe(200);
     expect(body.data.token).toBeTruthy();
})

test("失败登录--密码错误",async({request})=>{
     const res=await request.post(`${backend}/auth/login`,{
          params:{account:'我的世界',password:'Daisy'},
     })
     expect(res).toBeOK();
     const body=await res.json();
     expect(body.code).toBe(403);
     expect(body.message).toContain('密码错误')
    
})

test("失败登录--账户错误",async({request})=>{
     const res=await request.post(`${backend}/auth/login`,{
          params:{account:'我*世界',password:'daisy'},
     })
    expect(res).toBeOK();
     const body=await res.json();
     expect(body.code).toBe(404);
     expect(body.message).toContain('用户不存在')
})

test("文件列表查看",async({request})=>{
     const loginRes=await request.post(`${backend}/auth/login`,{
          params:{account:'我的世界',password:'daisy'},
     })
    expect(loginRes).toBeOK();
    const body=await loginRes.json();
     expect(body.code).toBe(200);
     expect(body.data.token).toBeTruthy();
    const token=body.data.token;
    //传入token
    const fileListRes=await request.get(`${backend}/file/list`,{
         params:{parentId:'0',status:0},
         headers:{Authorization: `Bearer ${token}`}
    })
    expect(fileListRes).toBeOK();
    const ListBody=await fileListRes.json();
    expect(ListBody.code).toBe(200);
    expect(ListBody.data).toBeTruthy();
   //不传token
    const fileListRes_utoken=await request.get(`${backend}/file/list`,{
         params:{parentId:'0',status:0},
    })
    expect(fileListRes_utoken).toBeOK(); 
    const nBody=await  fileListRes_utoken.json();
    expect(nBody.code).toBe(200);
    expect(nBody.data).toEqual([]);
   
})