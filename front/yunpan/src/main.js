import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'

const app = createApp(App)

//全局use
app.use(createPinia())
app.use(router)
app.use(ElementPlus)

//component(name,component)注册单个组件
// 全局注册所有 Element Plus 图标
// ❌ 不行 — 图标不是一个插件，没有 install 方法
//app.use(ElementPlusIconsVue)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
