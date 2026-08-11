<script setup>
import { ref, onMounted } from 'vue'
import { listShareApi, cancelShareApi } from '@/api/share'
import * as fileApi from '@/api/file'

const shareList = ref([])
const loading = ref(false)

async function loadShares() {
  loading.value = true
  try {
    const res = await listShareApi()
    shareList.value = res.data
  } catch (e) {
    console.log('加载分享列表失败：' + e.message)
  } finally {
    loading.value = false
  }
}

function shareUrl(shareId) {
  return window.location.origin + '/share/' + shareId
}

function copyLink(shareId) {
  const url = shareUrl(shareId)
  if (navigator.clipboard) {
    navigator.clipboard.writeText(url).then(() => alert('链接已复制'))
    return
  }
  const ta = document.createElement('textarea')
  ta.value = url
  ta.style.position = 'fixed'; ta.style.left = '-9999px'
  document.body.appendChild(ta)
  ta.select()
  document.execCommand('copy')
  document.body.removeChild(ta)
  alert('链接已复制')
}

async function cancelShare(shareId) {
  if (!confirm('确定取消这个分享？')) return
  try {
    await cancelShareApi(shareId)
    loadShares()
  } catch (e) {
    alert('取消失败：' + e.message)
  }
}

function shareTypeLabel(type) {
  return type === 0 ? '公开' : '加密'
}

function statusLabel(status) {
  return status === 0 ? '有效' : status === 1 ? '已取消' : '已过期'
}

onMounted(() => loadShares())
</script>

<template>
  <div id="sharelist-container">
    <h2>我的分享</h2>

    <el-table :data="shareList" v-loading="loading" style="width: 100%">
      <el-table-column label="分享ID" width="130">
        <template #default="{ row }">{{ row.shareId }}</template>
      </el-table-column>
      <el-table-column label="文件名" min-width="150">
        <template #default="{ row }">{{ row.fileName || '-' }}</template>
      </el-table-column>
      <el-table-column label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="row.shareType === 0 ? 'success' : 'warning'" size="small">
            {{ shareTypeLabel(row.shareType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="提取码" width="100">
        <template #default="{ row }">{{ row.shareCode || '-' }}</template>
      </el-table-column>
      <el-table-column label="浏览" width="80" prop="viewCount" />
      <el-table-column label="保存" width="80" prop="downloadCount" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'info'" size="small">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="过期时间" width="180">
        <template #default="{ row }">{{ row.expireTime || '永不过期' }}</template>
      </el-table-column>
      <el-table-column label="创建时间" width="180" prop="createdTime">
        <template #default="{ row }">{{ row.createdTime?.replace('T', ' ') }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="copyLink(row.shareId)">复制链接</el-button>
          <el-button
            v-if="row.status === 0"
            link
            type="danger"
            size="small"
            @click="cancelShare(row.shareId)"
          >取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="shareList.length === 0 && !loading" class="empty">
      <el-icon :size="48"><Share /></el-icon>
      <p>暂无分享记录</p>
    </div>
  </div>
</template>

<style scoped>
#sharelist-container {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  min-height: 100%;
}
#sharelist-container h2 {
  font-size: 18px;
  margin-bottom: 16px;
}
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
  color: #909399;
}
</style>
