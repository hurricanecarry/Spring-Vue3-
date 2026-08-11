import request from '@/utils/request'

// 登录（后端用 @RequestParam）
export function loginApi(account, password) {
  return request({
    method: 'post',
    url: '/auth/login',
    params: { account, password },
  })
}

// 注册（后端用 @RequestParam，参数拼在 URL 上）
export function registerApi(nickname, email, password) {
  return request({
    method: 'post',
    url: '/auth/register',
    params: { nickName: nickname, email, password },
  })
}

//param当字段名和变量名一样时可用省略
export function updateSettingApi(nickName,email,password){
   return request({
     method:'post',
     url:'/auth/setting/basic/change',
     params:{nickName,email,password},
   })
}

//前端给后端发文件时，必须用 FormData 封装，后端用 @RequestParam 接收
export function updateAvatarApi(avatarFile) {
  const formData = new FormData()
  formData.append('avatarFile', avatarFile)
  return request({
     method:'post',
     url:'/auth/setting/avatar/change',
     data:formData,
  })
}