<script setup>
import { ref, onMounted } from 'vue'
import { listFileApi } from '@/api/file'
import * as fileApi from '@/api/file'

// 图标（和 home.vue 共用）
import folderIcon from '@/resource/category/文件夹.png'
import videoIcon from '@/resource/category/视频.png'
import audioIcon from '@/resource/category/音乐.png'
import docIcon from '@/resource/category/文本.png'
import archiveIcon from '@/resource/category/压缩包.png'
import unknownIcon from '@/resource/category/未知文件.png'
import imageIcon from '@/resource/category/图片.png'

const fileList = ref([])

const iconMap = { 1: videoIcon, 2: audioIcon, 3: docIcon, 4: archiveIcon, 5: unknownIcon, 6: imageIcon }
function getIcon(file) {
  if (file.fileType === 0) return folderIcon
  if (file.fileCategory === 6) return `/backend/file/thumbnail?fileId=${file.fileId}`
  return iconMap[file.fileCategory] || unknownIcon
}
const loading = ref(false)
const selectedRows = ref([])

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

//加载列表
async function loadRecycleList() {
  loading.value = true
  try {
    const res = await listFileApi('0', '1')
    fileList.value = res.data
  } catch (e) {
    console.log('加载回收站失败：' + e.message)
  } finally {
    loading.value = false
  }
}

//恢复文件
async function recoverFiles(row) {
  const targets = row ? [row] : selectedRows.value
  if (targets.length === 0) { alert('请先选择文件'); return }
  try {
    await Promise.all(targets.map(f => fileApi.recoverApi(f.fileId)))
    loadRecycleList()
  } catch (e) {
    alert('恢复失败：' + e.message)
  }
}

//彻底删除文件
async function deleteForever(row) {
  const targets = row ? [row] : selectedRows.value
  if (targets.length === 0) { alert('请先选择文件'); return }
  if (!confirm(`确定彻底删除 ${targets.length} 个文件？此操作不可撤销！`)) return
  try {
    await Promise.all(targets.map(f => fileApi.deleteApi(f.fileId)))
    loadRecycleList()
  } catch (e) {
    alert('删除失败：' + e.message)
  }
}

onMounted(() => loadRecycleList())
</script>

<template>
  <div id="recycle-container">
    <h2>回收站</h2>

    <div class="toolbar">
      <el-button round @click="recoverFiles()" :disabled="selectedRows.length === 0">恢复</el-button>
      <el-button round type="danger" @click="deleteForever()" :disabled="selectedRows.length === 0">彻底删除</el-button>
    </div>

    <el-table
      :data="fileList"
      v-loading="loading"
      @selection-change="handleSelectionChange"
      style="width: 100%"
    >
      <el-table-column type="selection" width="50" />
      <el-table-column label="文件名" min-width="300">
        <template #default="{ row }">
          <div class="file-name-cell">
            <img :src="getIcon(row)" class="file-icon" @error="e=>e.target.src=unknownIcon" />
            <span>{{ row.fileName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="大小" width="120">
        <template #default="{ row }">
          {{ row.fileType === 0 ? '-' : (row.fileSize / 1024 / 1024).toFixed(2) + ' MB' }}
        </template>
      </el-table-column>
      <el-table-column label="回收时间" width="180">
        <template #default="{ row }">{{ row.recycleTime?.replace('T', ' ') || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="recoverFiles(row)">恢复</el-button>
          <el-button link type="danger" size="small" @click="deleteForever(row)">彻底删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="fileList.length === 0 && !loading" class="empty">
      <el-icon :size="48"><Delete /></el-icon>
      <p>回收站为空</p>
    </div>
  </div>
</template>

<style scoped>
#recycle-container {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  min-height: 100%;
}
#recycle-container h2 { font-size: 18px; margin-bottom: 16px; }
.toolbar { display: flex; gap: 10px; margin-bottom: 16px; }
.file-name-cell { display: flex; align-items: center; gap: 8px; }
.file-icon { width: 28px; height: 28px; object-fit: contain; }
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
  color: #909399;
}
</style>
