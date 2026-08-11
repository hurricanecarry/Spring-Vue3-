<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

// 默认显示注册面板（isLogin=false → right-active 激活 → 注册在右、覆盖层在左）
const isLogin = ref(false)

// 登录表单
const loginForm = ref({ account: '', password: '' })
const loginLoading = ref(false)
const loginError = ref('')

async function doLogin() {
  loginError.value = ''
  if (!loginForm.value.account || !loginForm.value.password) {
    loginError.value = '请填写完整'
    return
  }
  loginLoading.value = true
  try {
    await auth.login(loginForm.value.account, loginForm.value.password)
    router.replace('/')
  } catch (e) {
    loginError.value = e.message || '登录失败'
  } finally {
    loginLoading.value = false
  }
}

// 注册表单
const registerForm = ref({ nickname: '', email: '', password: '', confirm: '' })
const registerLoading = ref(false)
const registerError = ref('')

async function doRegister() {
  registerError.value = ''
  const f = registerForm.value
  if (!f.nickname || !f.email || !f.password) {
    registerError.value = '请填写完整'
    return
  }
  if (f.password !== f.confirm) {
    registerError.value = '两次密码不一致'
    return
  }
  registerLoading.value = true
  try {
    await auth.register(f.nickname, f.email, f.password)
    router.replace('/')
  } catch (e) {
    registerError.value = e.message || '注册失败'
  } finally {
    registerLoading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <!-- 卡片容器 -->
    <div class="card" :class="{ 'right-active': !isLogin }">
      <!-- === 左侧：登录表单 === -->
      <div class="form-panel login-panel">
        <h2>登录</h2>
        <el-form @submit.prevent>
          <el-form-item>
            <el-input
              v-model="loginForm.account"
              placeholder="邮箱 / 昵称"
              prefix-icon="User"
              size="large"
              @keyup.enter="doLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="loginForm.password"
              type="password"
              show-password
              placeholder="密码"
              prefix-icon="Lock"
              size="large"
              @keyup.enter="doLogin"
            />
          </el-form-item>
          <p v-if="loginError" class="error-msg">{{ loginError }}</p>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              style="width: 100%"
              :loading="loginLoading"
              @click="doLogin"
            >登 录</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- === 右侧：注册表单 === -->
      <div class="form-panel register-panel">
        <h2>注册</h2>
        <el-form @submit.prevent>
          <el-form-item>
            <el-input
              v-model="registerForm.nickname"
              placeholder="昵称"
              prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="registerForm.email"
              placeholder="邮箱"
              prefix-icon="Message"
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="registerForm.password"
              type="password"
              show-password
              placeholder="密码"
              prefix-icon="Lock"
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="registerForm.confirm"
              type="password"
              show-password
              placeholder="确认密码"
              prefix-icon="Lock"
              size="large"
              @keyup.enter="doRegister"
            />
          </el-form-item>
          <el-form-item>
          <p v-if="registerError" class="error-msg">{{ registerError }}</p>
            <el-button
              type="primary"
              size="large"
              style="width: 100%"
              :loading="registerLoading"
              @click="doRegister"
            >注 册</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- === 中间滑动覆盖层 === -->
      <div class="overlay-panel">
        <div class="overlay-inner">
          <!-- 文字 A：覆盖层在左侧（注册模式）→ 引导去登录 -->
          <div class="overlay-text">
            <h2>已有账号？</h2>
            <p>使用已有账号登录</p>
            <el-button round size="large" @click="isLogin = true">去登录</el-button>
          </div>
          <!-- 文字 B：覆盖层在右侧（登录模式）→ 引导去注册 -->
          <div class="overlay-text">
            <h2>还没有账号？</h2>
            <p>立即注册，开始使用云盘</p>
            <el-button round size="large" @click="isLogin = false">去注册</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-image: url('@/resource/【哲风壁纸】云朵-山脉-日落.png');
  background-size: cover;
  background-position: center;
}

/* ---- 卡片 ---- */
.card {
  width: 720px;
  height: 440px;
  background: transparent;                      /* 卡片背景透明，透出壁纸 */
  border-radius: 16px;
  box-shadow: 0 14px 40px rgba(0, 0, 0, 0.15);
  position: relative;
  overflow: hidden;
}

/* ---- 表单面板（两个并排） ---- */
.form-panel {
  position: absolute;
  top: 0;
  height: 100%;
  width: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 40px;
  background: #fff;             /* 白色背景 */
  transition: all 0.6s ease-in-out;
}
.form-panel h2 {
  text-align: center;
  margin-bottom: 20px;
}

/* 登录面板在左 */
.login-panel {
  left: 0;
  z-index: 2;
}
.card.right-active .login-panel {
  transform: translateX(100%);
}

/* 注册面板在右（初始隐藏） */
.register-panel {
  left: 0;
  opacity: 0;
  z-index: 1;
}
.card.right-active .register-panel {
  opacity: 1;
  transform: translateX(100%);
  z-index: 5;
}

/* ---- 覆盖层 ---- */
.overlay-panel {
  position: absolute;
  top: 0;
  left: 50%;
  height: 100%;
  width: 50%;
  overflow: hidden;
  z-index: 100;
  transition: transform 0.6s ease-in-out;
}
.card.right-active .overlay-panel {
  transform: translateX(-100%);
}

/* 内部的滑动背景 */
.overlay-inner {
  width: 200%;
  height: 100%;
  position: relative;
  left: -100%;
  background: transparent;                       /* 覆盖层透明 */
  transition: transform 0.6s ease-in-out;
  display: flex;
}
.card.right-active .overlay-inner {
  transform: translateX(50%);
}

/* 文字区域 */
.overlay-text {
  width: 50%;
  flex-shrink: 0;        /* 禁止 flex 压缩 */
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  text-align: center;
  padding: 0 30px;
}
.overlay-text h2 { margin-bottom: 10px; }
.overlay-text p  { margin-bottom: 20px; font-size: 14px; opacity: 0.85; }

.error-msg {
  color: #f56c6c;
  font-size: 13px;
  text-align: center;
  margin-bottom: 8px;
}
</style>
