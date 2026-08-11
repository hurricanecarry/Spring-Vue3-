import axios from 'axios'

const request = axios.create({
  baseURL: '/backend', // vite proxy 转发到后端
  timeout: 10000,
})

// 请求拦截器 —— 自动带 token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// 响应拦截器 —— 统一解包 & 401 跳登录
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 后端 Result 结构: { code, message, data }
    if (res.code !== 200) {
      if (res.code === 401) {
        localStorage.removeItem('token')
        window.location.href = '/login'
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res // 返回 { code, message, data } ，调用方直接 .data 取值
  },
  (error) => {
    // HTTP 401 → 后端 token 过期/无效
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export default request
