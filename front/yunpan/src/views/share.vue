<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { showShareApi, verifyCodeApi, saveShareApi } from '@/api/share'
import { useAuthStore } from '@/stores/auth'

// 图标
import folderIcon from '@/resource/category/文件夹.png'
import unknownIcon from '@/resource/category/未知文件.png'

const route = useRoute()
const auth = useAuthStore()
const shareId = route.params.shareId
const urlCode = route.query.code || ''

// 状态
const loading = ref(true)
const errorMsg = ref('')
const needCode = ref(false)
const codeInput = ref(urlCode)
const codeError = ref('')
const shareToken = ref('')
const shareData = ref(null)

// 面包屑
const breadcrumb = ref([])

const isFolder = computed(() => shareData.value?.rootFile?.fileType === 0)

// 多选
const selectedFiles = ref(new Set())

function toggleFile(fileId) {
  const s = selectedFiles.value
  s.has(fileId) ? s.delete(fileId) : s.add(fileId)
  selectedFiles.value = new Set(s)  // 触发响应式
}

// ========== 加载分享 ==========
async function loadShare(parentId) {
  loading.value = true
  errorMsg.value = ''
  try {
    if (codeInput.value && !shareToken.value) {
      try {
        const verifyRes = await verifyCodeApi(shareId, codeInput.value)
        shareToken.value = verifyRes.data
      } catch {
        needCode.value = true
        loading.value = false
        return
      }
    }
    const res = await showShareApi(shareId, undefined, shareToken.value, parentId)
    shareData.value = res.data
    needCode.value = false
    if (!parentId) breadcrumb.value = []
  } catch (e) {
    if (e.message?.includes('提取') || e.response?.status === 403) {
      needCode.value = true
    } else {
      errorMsg.value = e.message || '分享不存在或已失效'
    }
  } finally {
    loading.value = false
  }
}

// ========== 提取码 ==========
async function submitCode() {
  if (!codeInput.value.trim()) return
  codeError.value = ''
  try {
    const res = await verifyCodeApi(shareId, codeInput.value)
    shareToken.value = res.data.shareToken || res.data
    needCode.value = false
    loadShare()
  } catch (e) {
    codeError.value = e.message || '提取码错误'
  }
}

// ========== 面包屑 ==========
function enterFolder(f) {
  breadcrumb.value.push({ id: f.fileId, name: f.fileName })
  loadShare(f.fileId)
}
function jumpTo(index) {
  breadcrumb.value = breadcrumb.value.slice(0, index + 1)
  loadShare(breadcrumb.value[index]?.id)
}
function goBack() {
  if (breadcrumb.value.length === 0) return
  breadcrumb.value.pop()
  const targetId = breadcrumb.value.length > 0 ? breadcrumb.value[breadcrumb.value.length - 1].id : undefined
  loadShare(targetId)
}
function backToRoot() {
  breadcrumb.value = []
  loadShare()
}

// ========== 下载 ==========
function downloadFile(file) {
  const a = document.createElement('a')
  a.href = `/backend/file/download?fileId=${file.fileId}`
  a.download = file.fileName
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

// ========== 保存 ==========
async function saveToMe() {
  if (!auth.isLogin()) { alert('请先登录后再保存'); return }
  const file = shareData.value?.rootFile
  if (!file) return
  try {
    await saveShareApi(file.fileId, '0', shareId)
    alert('已保存到网盘根目录')
  } catch (e) {
    alert('保存失败：' + e.message)
  }
}

// ========== 图标 ==========
function getIcon(file) {
  if (file.fileType === 0) return folderIcon
  return `/backend/file/thumbnail?fileId=${file.fileId}`
}
function formatSize(bytes) {
  if (!bytes) return '-'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(2) + ' MB'
}

onMounted(() => loadShare())
</script>

<template>
  <div class="share-page">
    <!-- ===== 加载中 ===== -->
    <div v-if="loading" class="state-box">
      <p>加载中...</p>
    </div>

    <!-- ===== 错误 ===== -->
    <div v-else-if="errorMsg" class="state-box">
      <h2>{{ errorMsg }}</h2>
    </div>

    <!-- ===== 提取码 ===== -->
    <div v-else-if="needCode" class="share-card code-card">
      <div class="card-header">
        <h2>🔐 加密分享</h2>
        <p class="card-sub">请输入提取码以访问此分享</p>
      </div>
      <div class="code-section">
        <el-input
          v-model="codeInput"
          placeholder="输入提取码"
          maxlength="10"
          class="dark-input"
          @keyup.enter="submitCode"
        />
        <el-button class="dark-btn" @click="submitCode">确认</el-button>
      </div>
      <p v-if="codeError" class="code-err">{{ codeError }}</p>
    </div>

    <!-- ===== 正常浏览 ===== -->
    <div v-else class="share-card browse-card" :class="{ wide: isFolder }">
      <!-- 分享者信息 -->
      <div class="owner-bar">
        <img
          :src="shareData?.ownerUserId
            ? `/backend/auth/setting/avatar/get?userId=${shareData.ownerUserId}`
            : unknownIcon"
          class="owner-avatar"
          @error="e => e.target.src = unknownIcon"
        />
        <span class="owner-name">来自 {{ shareData?.ownerName || '用户' }} 的分享</span>
      </div>
      <!-- 标题 -->
      <div class="card-header">
        <h2>📎 {{ shareData?.rootFile?.fileName }}</h2>
        <p class="card-sub">
          {{ isFolder ? '文件夹' : '文件' }}
          <span v-if="!isFolder && shareData?.rootFile?.fileSize">
            · {{ formatSize(shareData.rootFile.fileSize) }}
          </span>
          <span v-if="shareData?.shareType === 1"> · 🔒</span>
        </p>
      </div>

      <!-- 面包屑 -->
      <div class="breadcrumb-row" v-if="breadcrumb.length > 0">
        <span class="crumb-back" @click="goBack">← 返回上级</span>
        <span v-for="(item, index) in breadcrumb" :key="item.id">
          <span class="crumb-sep">/</span>
          <span
            class="crumb-link"
            :class="{ cur: index === breadcrumb.length - 1 }"
            @click="jumpTo(index)"
          >{{ item.name }}</span>
        </span>
      </div>

      <!-- 文件夹 -->
      <div v-if="isFolder" class="file-list">
        <!-- 根目录：先展示文件夹本身（信封），双击打开 -->
        <div v-if="breadcrumb.length === 0" class="file-row clickable" @dblclick="enterFolder(shareData.rootFile)">
          <img :src="getIcon(shareData.rootFile)" class="f-icon" @error="e => e.target.src = unknownIcon" />
          <span class="f-name">{{ shareData.rootFile.fileName }}</span>
          <span class="f-size">文件夹</span>
        </div>
        <!-- 子目录内：展示 children -->
        <template v-else>
          <div
            v-for="f in shareData?.children"
            :key="f.fileId"
            class="file-row"
            :class="{ clickable: f.fileType === 0, checked: selectedFiles.has(f.fileId) }"
            @dblclick="f.fileType === 0 ? enterFolder(f) : undefined"
          >
            <span class="f-check" @click.stop="toggleFile(f.fileId)">
              {{ selectedFiles.has(f.fileId) ? '☑' : '☐' }}
            </span>
            <img :src="getIcon(f)" class="f-icon" @error="e => e.target.src = unknownIcon" />
            <span class="f-name">{{ f.fileName }}</span>
            <span class="f-size">{{ f.fileType === 0 ? '' : formatSize(f.fileSize) }}</span>
            <span v-if="f.fileType === 1" class="f-dl" @click.stop="downloadFile(f)">下载</span>
            <span class="f-dl" @click.stop="saveFile(f)">保存</span>
          </div>
          <div v-if="!shareData?.children?.length" class="empty-row">此文件夹为空</div>
        </template>
      </div>

      <!-- 单文件：先展示卡片 -->
      <div v-if="!isFolder" class="file-list">
        <div class="file-row clickable" @dblclick="downloadFile(shareData.rootFile)">
          <img :src="getIcon(shareData.rootFile)" class="f-icon" @error="e => e.target.src = unknownIcon" />
          <span class="f-name">{{ shareData.rootFile.fileName }}</span>
          <span class="f-size">{{ formatSize(shareData.rootFile.fileSize) }}</span>
          <span class="f-dl" @click.stop="downloadFile(shareData.rootFile)">下载</span>
        </div>
      </div>

      <!-- 底部保存按钮 -->
      <div class="card-footer">
        <el-button class="dark-btn" size="large" @click="saveToMe"> 保存到我的空间</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ===== 全局背景 ===== */
.share-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding: 24px;
}

/* ===== 状态 ===== */
.state-box {
  color: rgba(255,255,255,0.7);
  font-size: 16px;
}

/* ===== 毛玻璃卡片 ===== */
.share-card {
  width: 480px;
  max-height: 75vh;
  overflow-y: auto;
  padding: 36px 32px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 20px;
  box-shadow: 0 12px 48px rgba(0,0,0,0.25);
  color: #fff;
}
/* 提取码阶段：小尺寸居中 */
.share-card.code-card {
  width: 420px;
  height: auto;
  max-height: none;
}
/* 浏览阶段：撑满 */
.share-card.browse-card {
  height: 75vh;
}
.share-card.wide { width: 560px; }

.share-card::-webkit-scrollbar { width: 4px; }
.share-card::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.15); border-radius: 2px; }

/* ===== 标题 ===== */
.owner-bar { display: flex; align-items: center; gap: 8px; padding-bottom: 14px; margin-bottom: 14px; border-bottom: 1px solid rgba(255,255,255,0.08); }
.owner-avatar { width: 28px; height: 28px; border-radius: 14px; object-fit: cover; }
.owner-name { font-size: 13px; color: rgba(255,255,255,0.6); }

.card-header { text-align: center; margin-bottom: 20px; }
.owner-bar { display: flex; align-items: center; gap: 8px; padding-bottom: 14px; margin-bottom: 14px; border-bottom: 1px solid rgba(255,255,255,0.08); }
.owner-avatar { width: 28px; height: 28px; border-radius: 14px; object-fit: cover; }
.owner-name { font-size: 13px; color: rgba(255,255,255,0.6); }

.card-header h2 { font-size: 22px; font-weight: 600; margin: 0 0 6px; color: #fff; }
.card-sub { font-size: 13px; color: rgba(255,255,255,0.55); margin: 0; }

/* ===== 提取码 ===== */
.code-section { display: flex; gap: 10px; justify-content: center; margin-top: 20px; }
.code-err { color: #ff6b6b; text-align: center; margin-top: 10px; font-size: 13px; }

/* 暗色输入框 & 按钮 */
:deep(.dark-input .el-input__wrapper) {
  background: rgba(255,255,255,0.1) !important;
  border: 1px solid rgba(255,255,255,0.2) !important;
  border-radius: 10px !important;
  box-shadow: none !important;
}
:deep(.dark-input .el-input__inner) {
  color: #fff;
  caret-color: #409eff;
}
:deep(.dark-input .el-input__inner::placeholder) { color: rgba(255,255,255,0.35); }

.dark-btn {
  background: rgba(255,255,255,0.15) !important;
  border: 1px solid rgba(255,255,255,0.25) !important;
  color: #fff !important;
  border-radius: 10px !important;
  transition: all 0.2s;
}
.dark-btn:hover {
  background: rgba(255,255,255,0.25) !important;
  border-color: rgba(255,255,255,0.4) !important;
}

/* ===== 面包屑 ===== */
.breadcrumb-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  font-size: 13px;
  color: rgba(255,255,255,0.5);
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.crumb-back { color: #79bbff; cursor: pointer; margin-right: 6px; }
.crumb-back:hover { color: #a0d0ff; }
.crumb-sep { margin: 0 2px; }
.crumb-link { color: rgba(255,255,255,0.6); cursor: pointer; }
.crumb-link:hover { color: #fff; }
.crumb-link.cur { color: #fff; font-weight: 600; cursor: default; }

/* ===== 文件列表（纵向） ===== */
.file-list { display: flex; flex-direction: column; gap: 2px; }
.file-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  transition: background 0.15s;
  color: #fff;
}
.file-row:hover { background: rgba(255,255,255,0.06); }
.file-row.clickable { cursor: pointer; }
.f-check { font-size: 14px; cursor: pointer; flex-shrink: 0; color: rgba(255,255,255,0.5); transition: color 0.15s; }
.f-check:hover { color: #fff; }
.file-row.checked { background: rgba(255,255,255,0.08); }

.f-icon { width: 28px; height: 28px; object-fit: contain; flex-shrink: 0; }
.f-name { flex: 1; font-size: 14px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.f-size { font-size: 12px; color: rgba(255,255,255,0.45); flex-shrink: 0; }
.f-dl {
  font-size: 12px;
  color: #79bbff;
  cursor: pointer;
  flex-shrink: 0;
  padding: 2px 6px;
  border-radius: 4px;
  transition: background 0.15s;
}
.f-dl:hover { background: rgba(255,255,255,0.1); }

.empty-row { text-align: center; padding: 40px 0; color: rgba(255,255,255,0.35); font-size: 14px; }

/* ===== 底部保存 ===== */
.card-footer { margin-top: 24px; text-align: center; }
</style>
