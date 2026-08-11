<script setup>
import { ref, onMounted ,nextTick,computed} from 'vue'
import * as fileApi from '@/api/file'
import * as shareApi from '@/api/share'
import { Document, Folder, MoreFilled } from '@element-plus/icons-vue';

//资源
import folderIcon from '@/resource/category/文件夹.png'
import videoIcon from '@/resource/category/视频.png'
import audioIcon from '@/resource/category/音乐.png'
import docIcon from '@/resource/category/文本.png'
import archiveIcon from '@/resource/category/压缩包.png'
import unknownIcon from '@/resource/category/未知文件.png'
import imageIcon from '@/resource/category/图片.png'
//小工具栏额外选项图标
import reNameIcon from '@/resource/重命名.png'
import moveIcon from '@/resource/移动.png'
import copyIcon from '@/resource/复制.png'
//粘贴
import pasteIcon from '@/resource/粘贴.png'

import createFolderIcon from '@/resource/新建文件夹.png'
//小工具栏额外选择中
const moreVisable=ref(false);

const fileList = ref([])
const loading = ref(false)
const error=ref('')
//创建dom计数器
const tableKey = ref(0)





//搜索内容
const searchText=ref('')
const usedText=ref('')
const filteredList=computed(()=>{
   if(!usedText.value)  return fileList.value;
   return fileList.value.filter(f=>f.fileName.includes(usedText.value))
})

function doSearch(){
   usedText.value=searchText.value;
}

//路径栈
const pathStack = ref([{ id: '0', name: '全部文件' }])

const isCreating = ref(false)
const tempName = ref('')

// 勾选变化
const selectedRows = ref([])
function handleSelectionChange(rows) {
  selectedRows.value = rows
}

//上传框
const uploadFilesInput=ref(null)
const uploadFolderInput=ref(null)

//图片预览
const previewVisible=ref(false)
const previewUrl=ref('')

//分享信息
const shareCreating=ref(false)
const shareVisable=ref(false)
const shareTarget=ref(null)
const shareExpiration=ref('365')
const shareCodeType = ref('none')    // 'none'=无提取码, 'custom'=自定义
const shareCodeInput = ref('')       // 自定义提取码
const shareLink = ref('')



function startCreateFolder() {
  if (isCreating.value) return
  isCreating.value = true
  tempName.value = ''
  // 在数组头部插入假数据行，自动对齐表格文件名列
  fileList.value.unshift({
    fileId: 'temp_new',
    fileName: '',
    fileType: 0,
    isCreating: true,   // 标记，模板据此显示输入框
    fileSize: 0,
  })
  nextTick(() => {
    document.querySelector('.create-input')?.focus()
  })
}
//创建文件夹
async function confirmCreate() {
  if (!tempName.value.trim()) return
  const parentId = pathStack.value[pathStack.value.length - 1].id
  await fileApi.newFolderApi(parentId, tempName.value)
  // 移除假行，重新加载
  fileList.value = fileList.value.filter(f => !f.isCreating)
  isCreating.value = false
  tempName.value = ''
  loadFiles()
}

function cancelCreate() {
  fileList.value = fileList.value.filter(f => !f.isCreating)
  isCreating.value = false
  tempName.value = ''
}


//回收文件
async function recycleFile(row){
  const targets=row?[row]:selectedRows.value
  if(targets.length===0) {
     alert('未选择文件')
     return;
  }
  try{
     await Promise.all(targets.map(f=>fileApi.recycleApi(f.fileId)));
     loadFiles();
  }catch(e){
     alert('回收失败');
     console.log('回收出错'+e.message);
  }
}


//上传文件/文件夹
function triggerFilesUpload(){
   uploadFilesInput.value?.click()
}
function triggerFolderUpload(){
   uploadFolderInput.value?.click();
}
async function onFilesUpload(event) {
  const files=event.target.files;
  if(!files.length) return;
  const parentId=pathStack.value[pathStack.value.length-1].id;
  try{
  await Promise.all([...files].map(f=>fileApi.uploadFileApi(f,parentId)));
  loadFiles();
  }catch(e){
     alert('上传失败');
     console.log('上传失败:'+e.message);
     return;
  }
  event.target.value='';
}

async function onFolderUpload(event){
    const files=[...event.target.files]
    if(!files.length) return
    const parentId=pathStack.value[pathStack.value.length-1].id;
    try{
       await fileApi.uploadFolderApi(files,parentId);
    }catch(e){
       alert('上传文件夹失败')
       console.log('上传文件夹失败:'+e.message);
    }
    event.target.value=''
}


//分享文件
function startShare(row){
   shareTarget.value=row||selectedRows.value[0];
   if(!shareTarget.value){
     alert('未选择文件')
     return 
   }
   shareLink.value=''
   shareExpiration.value='365'
   shareCodeType.value='none'
   shareCodeInput.value=''
   shareVisable.value=true 
}
async function generateShare(){
   if(shareCreating.value) return;
   shareCreating.value=true;
   const f=shareTarget.value;
   const expireMap={'1':1,'7':7,'30':30,'365':365,'0':0}
   const shareType=shareCodeType.value=='custom'?1:0;
   const shareCode=shareCodeType.value=='custom'?(shareCodeInput.value||undefined):undefined;
   try{
      const res=await shareApi.createShareApi(f.fileId,shareType,expireMap[shareExpiration.value],shareCode);
      shareLink.value=window.location.origin+'/share/'+res.data;
   }catch(e){
     alert('分享创建失败');
     console.log('分享创建失败:'+e.message);
   }
   finally{
      shareCreating.value=false;
   }
}
function copyShare(){
   if(!shareLink.value) return
   copyToClipboard(shareLink.value)
}

// 通用复制（兼容 HTTP 环境）
function copyToClipboard(text) {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(text).then(() => alert('链接已复制'))
    return
  }
  // HTTP fallback：用 textarea + execCommand
  const ta = document.createElement('textarea')
  ta.value = text
  ta.style.position = 'fixed'
  ta.style.left = '-9999px'
  document.body.appendChild(ta)
  ta.select()
  document.execCommand('copy')
  document.body.removeChild(ta)
  alert('链接已复制')
}



//下载文件/文件夹（打包zip）
function downloadFiles(row){
   const targets=row?[row]:selectedRows.value;
   if(targets.length===0){
     alert('未选择下载对象')
     return
   }
   targets.forEach(f=>{
      const a=document.createElement('a')
      a.href=fileApi.getdownloadUrl(f.fileId);
      a.download=f.fileName
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
   })
}

//文件路径跳转以及文件载入
async function loadFiles(){
  loading.value=true;
  error.value='';
  try{
    const currentId=pathStack.value[pathStack.value.length-1].id;
    fileList.value=(await fileApi.listFileApi(currentId)).data;
  }catch(e){
    error.value=e.message || '加载文件失败';
  }finally{
    //每次加载计数器自增，让el-table重新渲染，避免dom复用导致的输入框无法聚焦问题
     loading.value=false;
     tableKey.value++
  }
}

function jumpTo(index){
   pathStack.value=pathStack.value.slice(0,index+1);
   loadFiles();
}
function goback(){
  if(pathStack.value.length<=1) return;
  pathStack.value.pop();
  loadFiles();
}
function clickFile(file){
  if(file.fileType===0){
    pathStack.value.push({id:file.fileId,name:file.fileName});
    loadFiles();
  }else if(file.fileCategory===6){
    // 图片 → 弹窗预览
    previewUrl.value = `/backend/file/thumbnail?fileId=${file.fileId}`
    previewVisible.value = true
  }else{
    // 其他文件 → 下载
    const a = document.createElement('a')
    a.href = fileApi.getdownloadUrl(file.fileId)
    a.download = file.fileName
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
  }
  // 清除表格高亮
  nextTick(() => {
    const table = document.querySelector('.el-table__body')
    if (table) {
      const rows = table.querySelectorAll('.el-table__row')
      rows.forEach(r => r.classList.remove('current-row'))
    }
  })
}



//图标  略缩图获取
const iconMap={
  1: videoIcon,      // 视频
  2: audioIcon,      // 音频
  3: docIcon,        // 文档
  4: archiveIcon,    // 压缩包
  5: unknownIcon,    // 其他
  6: imageIcon,      // 图片
}
function getIcon(file){
  if(file.fileType===0) return folderIcon;
  if (file.fileCategory === 6) return `/backend/file/thumbnail?fileId=${file.fileId}`
  return iconMap[file.fileCategory] || unknownIcon;
}

//格式化文件大小
function formatSize(bytes) {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(2) + ' MB'
}


//下拉菜单显隐
function handleDropdownVisible(show) {
  moreVisable.value = show
}

//额外工具操作
function handleMore(choice,row){
   if(choice==='copy'){
      copyFIle(row);
   }else if(choice==='move'){
      cutFile(row);
   }else if(choice==='reName'){
      startReName(row);
   }
}


//剪切板操作+粘贴
const clipboard=ref(null)
function  copyFIle(row){
  clipboard.value={fileId:row.fileId,fileName:row.fileName,action:'copy'}
}
function cutFile(row){
  clipboard.value={fileId:row.fileId,fileName:row.fileName,action:"cut"}
}
async function paste(){
   if(!clipboard.value) return;
   //会按顺序赋值，如果没有指定的话
   const {fileId,fileName}=clipboard.value;
   const parentId=pathStack.value[pathStack.value.length-1].id;
  if(clipboard.value.action==='copy'){
     try{
        await fileApi.copyApi(fileId,parentId);
        loadFiles();
     }catch(e){
        alert('复制失败');
        console.log('复制失败:'+e.message);
     }
  }else if(clipboard.value.action==='cut'){
      try{
         await fileApi.moveApi(fileId,parentId);
         loadFiles();
      }catch(e){
         alert('移动失败');
         console.log('移动失败:'+e.message);
      }
  }
}

//重命名
const reNameId=ref('')
const newName=ref('')
const reNameInputRef=ref(null)
function startReName(row){
   reNameId.value=row.fileId;
   newName.value=row.fileName;
   nextTick(()=>{
     const el = reNameInputRef.value?.$el?.querySelector('input');
     if(el) {
       el.focus();
       el.setSelectionRange(el.value.length, el.value.length);
     }
   })
}
//confirm和cancel都会最后清空记录---正常行和重命名行用不同div
async function confirmRename(row){
   if(!newName.value.trim())  {
     alert("文件名不能为空");
     return;
   }
   await fileApi.renameApi(row.fileId,newName.value);
   reNameId.value='';
   newName.value='';
   loadFiles();
}
function cancelReName(){
   reNameId.value = '';
   newName.value = '';
}
 

onMounted(() => {
   loadFiles();
})
</script>

<template>
  <div id="file-container">
      <!--工具栏-->
      <div id="toolbar">
         <div id="toolbar-left">
            <el-button round @click="startCreateFolder" v-if="!isCreating">
              <img :src="createFolderIcon" class="tool-btn-icon"></img>
              <span>新建文件夹</span>
            </el-button>
            <el-button v-if="isCreating" type="success" @click="confirmCreate">确认</el-button>
            <el-button v-if="isCreating" type="warning" @click="cancelCreate">取消</el-button>
            <el-dropdown>
              <el-button round>
                <el-icon><Upload /></el-icon>
                <span>上传</span>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="triggerFilesUpload">上传文件</el-dropdown-item>
                  <el-dropdown-item @click="triggerFolderUpload">上传文件夹</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button round @click="downloadFiles()">
              <el-icon><Download /></el-icon>
              <span>下载</span>
            </el-button>
            <el-button round @click="recycleFile()">
              <el-icon><Delete /></el-icon>
              <span>放入回收站</span>
            </el-button>
            <!--只能单个分享-->
            <el-button round @click="startShare()" :disabled="selectedRows.length!==1">
              <el-icon><Share /></el-icon>
              <span>分享</span>
            </el-button>
            <el-button round @click="paste" v-if="clipboard">
              <img :src="pasteIcon" class="tool-btn-icon"></img>
                <span>粘贴</span>
            </el-button>
         </div>
         <div id="toolbar-right">
            <el-input placeholder="搜索文件..." id="search"  v-model="searchText"  clearable>
                <template #append>
                   <el-button type="" @click="doSearch"  icon="Search"  id="search-icon"></el-button>
                </template>
            </el-input>
         </div>
      </div>

      <!--导航栏-->
      <div id="path-title-bar">
         <el-icon id="route-arrow" v-if="pathStack.length>1" @click="goback"><ArrowLeft /></el-icon>
         <span id="title-text" v-if="pathStack.length===1">全部文件</span>
         <template v-if="pathStack.length>1" v-for="(item,index) in pathStack" :key="item.id">
            <span v-if="index>0" class="sep" :class="{active:index===pathStack.length-1}"> > </span>
            <!--active类名有生效条件:从第二个开始才生效-->
            <span class="crumb" :class="{active:index===pathStack.length-1}" @click="jumpTo(index)">{{item.name}}</span>
         </template>
       </div>

       
      <!--文件列表栏-->
      <el-table :data="filteredList" :key="tableKey" v-loading="loading"  highlight-current-row
        @selection-change="handleSelectionChange"  style="width: 100%" @row-click="clickFile">
        <el-table-column type="selection" width="50"></el-table-column>
        <el-table-column label="文件名" min-width="350">
            <template #default="{row}">
              <!-- 新建文件夹输入框 -->
              <div v-if="row.isCreating" class="file-name-cell" @click.stop>
                <img :src="folderIcon" class="file-icon" />
                <el-input
                  v-model="tempName"
                  class="create-input"
                  placeholder="输入文件夹名称"
                  @keyup.enter="confirmCreate"
                  @keyup.escape="cancelCreate"
                />
              </div>
              <!-- 重命名中：阻止冒泡 -->
              <div v-else-if="reNameId===row.fileId" class="file-name-cell" @click.stop>
                <img :src="getIcon(row)" class="file-icon" @error="e=>e.target.src=unknownIcon" />
                <el-input ref="reNameInputRef" v-model="newName"
                  class="rename-input"
                  @keyup.enter="confirmRename(row)" @keyup.escape="cancelReName"
                  @blur="cancelReName"
                />
                <!--小工具栏（重命名中不显示） -->
              </div>
              <!-- 正常行 -->
              <div v-else class="file-name-cell">
                <img :src="getIcon(row)" class="file-icon" @error="e=>e.target.src=unknownIcon" />
                <span class="file-text">{{ row.fileName }}</span>
                <!--小工具栏-->
                <span class="mini-toolbar"  :class="{'keep-visable':moreVisable}">
                  <el-tooltip  content="分享">
                    <el-icon class="mini-toolbar-item"  @click.stop="startShare(row)"><Share/></el-icon>
                  </el-tooltip>
                  <el-tooltip content="下载">
                    <el-icon class="mini-toolbar-item" @click.stop="downloadFiles(row)"><Download/></el-icon>
                  </el-tooltip>
                  <el-tooltip content="删除">
                    <el-icon class="mini-toolbar-item" @click.stop="recycleFile(row)"><Delete/></el-icon>
                  </el-tooltip>
                  <el-dropdown  trigger="hover" @visible-change="handleDropdownVisible" @command="(cmd)=>handleMore(cmd,row)">
                      <el-icon  class="mini-tool-moreIcon" @click.stop><MoreFilled/></el-icon>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item command="copy">
                             <img :src="copyIcon" class="tool-btn-icon"></img> 
                             <span>复制</span>
                          </el-dropdown-item>
                          <el-dropdown-item command="move">
                              <img :src="moveIcon" class="tool-btn-icon"></img> 
                              <span>移动</span>
                          </el-dropdown-item>
                          <el-dropdown-item command="reName">
                              <img :src="reNameIcon" class="tool-btn-icon"></img> 
                              <span>重命名</span>
                          </el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                  </el-dropdown>
                </span>
              </div>
            </template>
        </el-table-column>
        <el-table-column label="大小" width="180" prop="fileSize" align="left" style="margin-right:50px" >         
            <template #default="{ row }">{{ row.fileType === 0 ? '-' : formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column label="修改时间" width="200" prop="updateTime" align="left">
             <template #default="{ row }">{{ row.updateTime?.replace('T', ' ') || '-' }}</template>
        </el-table-column>
      </el-table>
       
       
      <!--上传input-->
      <input ref="uploadFilesInput" type="file" multiple hidden @change="onFilesUpload" />
      <input ref="uploadFolderInput" type="file" webkitdirectory hidden @change="onFolderUpload" />
      

      <!--图片预览弹窗--> 
      <el-dialog v-model="previewVisible" title="图片预览" width="80%" :close-on-click-modal="true">
        <div style="text-align:center">
          <img :src="previewUrl" style="max-width:100%;max-height:70vh" @error="e=>e.target.src=unknownIcon" />
        </div>
      </el-dialog>

      <!--分享弹窗-->
      <el-dialog v-model="shareVisable" title="分享" width="560px">
          <div class="share-row">
             <span class="share-row-label" >分享文件:</span>
             <strong>{{shareTarget?.fileName}}</strong>
          </div>
          <div class="share-row">
             <span class="share-row-label">有效期:</span>
             <el-radio-group v-model="shareExpiration" class="card-radio-group">
                 <el-radio-button label="1">1天</el-radio-button>
                 <el-radio-button label="7">7天</el-radio-button>
                 <el-radio-button label="30">30天</el-radio-button>
                 <el-radio-button label="365">365天</el-radio-button>
                 <el-radio-button label="0">永久有效</el-radio-button>
             </el-radio-group>
          </div>
          <div class="share-row">
              <span class="share-row-label">提取码：</span>
              <el-radio-group v-model="shareCodeType" class="card-radio-group">
                  <el-radio-button label="none">无提取码</el-radio-button>
                  <el-radio-button label="custom">自定义</el-radio-button>
              </el-radio-group>
           </div>
           <div class="share-row" v-if="shareCodeType==='custom'">
               <span class="share-row-label"></span>
               <el-input placeholder="输入提取码" v-model="shareCodeInput"></el-input>
           </div>
           <div class="share-action-row">
              <el-button type="primary" @click="generateShare">
                 生成链接
              </el-button>
           </div>
           <div class="link-row" v-if="shareLink">
               <el-input placeholder="" v-model="shareLink" readonly>
                  <template #append>
                     <el-button type="" class="copy-btn" @click="copyShare">复制链接</el-button>
                  </template>
               </el-input>
           </div>
      </el-dialog>
  </div>
</template>

<style scoped>
.file-icon{
   width: 40px;
}
#file-container{
   padding: 20px;
   background-color: #ffffff;
   border-radius: 8px;
  height: 100%;
  overflow-y: auto
}

/*工具栏*/
#toolbar{
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
#toolbar-left .el-button {
  margin-right: 12px;
}
#toolbar-right .el-input {
  width: 500px;
}
.tool-btn-icon {
  width: 16px;
  height: 16px;
  margin-right: 4px;
  vertical-align: middle;
}
#search-icon:hover{
  background-color: #98adc3;
   color: #409eff;
   opacity: 0.5;
}


/*当前标题*/
#path-title-bar {
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}
#route-arrow {
  cursor: pointer;
  margin-right: 8px;
}
#title-text {
  font-weight: bold;
}
.sep{
  color: #409eff;
}
.sep.active{
  color: #333; font-weight: bold; 
}
.crumb {
 color: #409eff; cursor: pointer;
}
.crumb.active {
 color: #333; font-weight: bold; cursor: default;
}

/*表格中的文件栏*/
.file-name-cell {
  display: flex;
  align-items: center;
}
.file-icon {
  width: 32px;
  height: 32px;
  margin-right: 10px;
  object-fit: contain; /* 防止图标变形 */
  cursor: pointer; /* 鼠标悬停时显示为可点击状态 */
}
.file-text {
  font-size: 14px;
  color: #333;
  cursor: pointer;
}
.file-text:hover {
  color: #409eff;
}




/* 表格内新建文件夹输入框 —— 去边框融入表格 */
:deep(.create-input .el-input__wrapper) {
  box-shadow: none !important;
  padding: 0;
}
:deep(.create-input .el-input__inner) {
  font-size: 14px;
  color: #333;
  caret-color: #409eff;
}


/*表内小工具栏*/
.mini-toolbar{
   gap:35px;
   opacity: 0;
   pointer-events: none;
   transition: opacity 0.15s;
   align-items: center;
   margin-left: 300px;
   display: flex;
}
.file-name-cell:hover .mini-toolbar{
   opacity: 1;
   pointer-events: auto;
}
/* 下拉展开时保持可见 */
.mini-toolbar.keep-visable{
   opacity: 1;
   pointer-events: auto;
}
.mini-toolbar-item{
   font-size: 17px;
}
.mini-toolbar-item:hover{
   cursor: pointer;
   color: #2a75c0;
}
.mini-tool-moreIcon{
   font-size: 17px;
   cursor: pointer;
}
.mini-tool-moreIcon:focus,
.mini-tool-moreIcon:focus-visible{
   outline: none;
}
.mini-toolbar.keep-visable{
   pointer-events: auto;/*显示时允许鼠标事件*/
   opacity: 1;
}



/*重命名*/
.rename-input :deep(.el-input__wrapper) {
  box-shadow: none !important;
  padding: 0;
}
.rename-input :deep(.el-input__inner) {
  font-size: 14px;
  color: #333;
}



/*分享弹窗*/
:deep(.el-dialog) {
  background-color: #f5f7fa;
  padding: 20px 24px;
}
.share-row {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}
.share-row-label { width: 70px; color: #606266; font-size: 14px; }
.share-action-row { display: flex; justify-content: center; margin: 24px 0 16px; }
.link-row { margin-top: 12px; }
.copy-btn { background: #409eff !important; color: #000 !important; border: none; }
.card-radio-group :deep(.el-radio-button__inner) {
  border-radius: 6px !important;
  margin: 0 4px;
}
.card-radio-group :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: #e8f4ff !important;
  color: #409eff !important;
  border-color: #e8f4ff !important;
  box-shadow: none !important;
}

</style>
