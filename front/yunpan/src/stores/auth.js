import { defineStore } from 'pinia'
import { ref } from 'vue'
import { loginApi, registerApi, updateAvatarApi, updateSettingApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  // 是否已登录
  const isLogin = () => !!token.value

  // 登录
  async function login(account, password) {
    const res = await loginApi(account, password)
    token.value = res.data.token
    user.value = res.data.userInfo  // LoginRes: { token, userId, nickname, email, ... }
    localStorage.setItem('token', token.value)
    localStorage.setItem('user', JSON.stringify(user.value))
    return res 
  }

  // 注册
  async function register(nickname, email, password) {
    const res = await registerApi(nickname, email, password)
    token.value = res.data.token
    user.value = res.data.userInfo
    localStorage.setItem('token', token.value)
    localStorage.setItem('user', JSON.stringify(user.value))
    return res
  }
  
  async function updateBasic(nickName,email,password) {
     await updateSettingApi(nickName,email,password)
     if(user.value){
       user.value.nickName=nickName
       user.value.email=email
       user.value.password=password
       localStorage.setItem('user',JSON.stringify(user.value))
     }
  }

  // 更新头像：先本地预览（不依赖后端），后台上传
  async function updateAvatar(file) {
    // ① 本地预览 → base64（纯前端，秒显，不依赖后端）
    const dataUrl = await new Promise((resolve) => {
      const reader = new FileReader()
      reader.onload = () => resolve(reader.result)
      reader.readAsDataURL(file)
    })
    // ② 后台上传（失败不影响预览）
    try {
      const res = await updateAvatarApi(file)
      if (user.value) {
        user.value.avatar = res.data
        localStorage.setItem('user', JSON.stringify(user.value))
      }
    } catch (e) {
      console.warn('头像上传失败，后端未启动：', e.message)
    }
    return dataUrl  // 不管上传是否成功，预览先给
  }
  
  

  // 退出
  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, user, isLogin, login, register, updateBasic, updateAvatar, logout }
})
