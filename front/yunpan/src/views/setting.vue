<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import defaultAvatar from '@/resource/默认头像.png'

const auth = useAuthStore()
const fileInput = ref(null)
const editing = ref(false)

// === 头像 ===
const previewUrl = ref(
  auth.user?.avatar
    ? `/backend/auth/setting/avatar/get?userId=${auth.user.userId}`
    : defaultAvatar,
)

function openPicker() {
  fileInput.value?.click()
}

async function onFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  previewUrl.value = await auth.updateAvatar(file)
}

// === 基本资料 ===
const form = ref({
  nickName: auth.user?.nickname || '',
  email: auth.user?.email || '',
  password: '',
})

async function saveBasic() {
  await auth.updateBasic(form.value.nickName, form.value.email, form.value.password)
  form.value.password = ''
  editing.value = false
}

function startEdit() {
  editing.value = true
}

function cancel() {
  editing.value = false
  form.value.nickName = auth.user?.nickname || ''
  form.value.email = auth.user?.email || ''
  form.value.password = ''
}
</script>

<template>
  <div id="main-setting">
    <!-- 头像（hover 显示文字） -->
  <div id="avatar-card" class="card">
    <div class="avatar-wrap" @click="openPicker">
      <img :src="previewUrl" id="avatar" />
      <div class="avatar-overlay"><span>更换头像</span></div>
    </div>
    <input ref="fileInput" type="file" accept="image/*" hidden @change="onFileChange" />
    <h2>{{ auth.user?.nickname || '名字加载失败' }}</h2>
  </div>
  
    <!-- 基本资料 -->
  <div id="basic-card" class="card">
      <h3>用户名</h3>
      <el-input class="input" v-model="form.nickName" :disabled="!editing" />
      <h3>邮箱</h3>
      <el-input class="input" v-model="form.email" :disabled="!editing" />
      <h3>密码（留空不修改）</h3>
      <el-input class="input" v-model="form.password" :disabled="!editing" type="password" show-password placeholder="留空则不修改密码" />

      <div class="btn-row">
        <el-button v-if="!editing" type="primary" @click="startEdit">修改</el-button>
        <template v-else>
          <el-button type="primary" @click="saveBasic">保存</el-button>
          <el-button @click="cancel">取消</el-button>
        </template>
      </div>
    </div>

    <div class="card">
        <el-button type="danger" @click="auth.logout">退出登录</el-button>
    </div>
  </div>
</template>

<style scoped>
#main-setting {
  width: 100%;
  padding: 40px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 头像 hover 效果 */
.avatar-wrap {
  width: 100px;
  height: 100px;
  border-radius: 50px;
  position: relative;
  cursor: pointer;
  overflow: hidden;
}
#avatar {
  width: 100%;
  height: 100%;
  border-radius: 50px;
  object-fit: cover;
  display: block;
}
.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  color: #fff;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50px;
  opacity: 0;
  transition: opacity 0.2s;
}
.avatar-wrap:hover .avatar-overlay {
  opacity: 1;
}

h2 {
  margin: 16px 0 8px;
}

.card {
  width: 100%;
  margin-top: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #5a5d64;   /* 分隔线 */
}
.card:last-child{
   border-bottom: none;  /* 最后一个卡片不显示分隔线 */
}
.card h3 {
  margin-bottom: 6px;
  margin-top: 14px;
  font-size: 14px;
  color: #606266;
}
.btn-row {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}
.input{
   width:500px;
}
</style>
