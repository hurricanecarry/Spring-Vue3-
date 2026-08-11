import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login.vue'),
    },
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/home.vue'),
    },
    {
      path: '/home',
      name: 'home',
      component: () => import('@/views/home.vue'),
    },
    {
      path: '/sharelist',
      name: 'sharelist',
      component: () => import('@/views/sharelist.vue'),
    },
    {
      path: '/share/:shareId',
      name: 'share',
      component: () => import('@/views/share.vue'),
    },
    {
      path: '/recycle',
      name: 'recycle',
      component: () => import('@/views/recycle.vue'),
    },
     {
      path: '/setting',
      name: 'setting',
      component: () => import('@/views/setting.vue'),
    },
  ],
})

// 全局路由守卫 —— 统一检查 token
const whiteList = ['/login', '/share'] // 无需登录的白名单（前缀匹配）

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const isPublic = whiteList.some((p) => to.path.startsWith(p))

  if (!token && !isPublic) {
    // 没有 token 且不是公开页面 → 去登录
    next('/login')
  } else {
    next()
  }
})

export default router
