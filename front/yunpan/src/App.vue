<script setup>
import { useRoute } from 'vue-router'
import { computed,ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import defaultAvatar from '@/resource/默认头像.png'

const route = useRoute()
const auth = useAuthStore()

// /login 和 /share/xxx 不需要侧边栏
const hideSidebar = computed(() =>
  route.path === '/login' || route.path.startsWith('/share/')
)

const avatarUrl=ref(
   auth.user?.avatar?
   `/backend/auth/setting/avatar/get?userId=${auth.user.userId}`:
    defaultAvatar
)
</script>

<template>
  <!-- 路 A：全屏页（登录、分享展示） -->
  <router-view v-if="hideSidebar" />

  <!-- 路 B：侧边栏布局 -->
  <el-container v-else id="total-container">
      <el-aside id="total-aside">
        <img src="@/resource/skydrive.png" alt="" id="sky-logo">
        <img :src="avatarUrl" alt="" id="avatar">
        <el-menu id="total-menu" :default-active="route.path" router>
            <el-menu-item class="total-item" index="/home">
              <el-icon class="total-item-icon"><HomeFilled /></el-icon>
              <span class="menu-item-text">首页</span>
            </el-menu-item>
            <el-menu-item class="total-item" index="/sharelist">
             <el-icon class="total-item-icon"><Share /></el-icon>
              <span class="menu-item-text">分享</span>
            </el-menu-item>
            <el-menu-item class="total-item" index="/recycle">
               <el-icon class="total-item-icon"><Delete /></el-icon>
              <span class="menu-item-text">回收站</span>
            </el-menu-item>
            <el-menu-item class="total-item" index="/setting">
              <el-icon class="total-item-icon"><Tools /></el-icon>
              <span class="menu-item-text">设置</span>
            </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
           <router-view />
      </el-main>
   </el-container>
</template>
<!--el-menu设置flex-direction不影响，因为内部有隐含的mode-->
<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
  background: #f5f7fa;
  color: #333;
  height: 100%;
}
#total-container{
   height: 100vh;
   width:100vw;
}
#total-aside {
  /* 保持原样 */
  width: 100px;
  border-right: 1px solid #ebeef5; 
  background-color: #fff;
  height: 100vh; 
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 20px; /* 整体顶部留 20px 即可 */
}

#sky-logo {
   height: 50px;
   width: 50px;
   margin-bottom: 20px; /* 用 margin-bottom 控制间距 */
}

#avatar {
   height: 54px;
   width: 54px;
   border-radius: 50%;
   border: 2px solid #f0f0f0; /* 给头像加个浅色边框更好看 */
   margin-bottom: 10px; /* 头像和菜单之间的间距 */
}

#total-menu {
   height: 100%;
   width: 100%;
   display: flex;
   flex-direction: column;
   align-items: center;
   padding-top: 10px ; /* 由很大的 60px 改为 10px，紧凑起来 */
}

.total-item {
  width: 60px;
  /* 删掉 margin-top: 25px;，改用 margin-bottom 控制菜单项之间的间距 */
  margin-bottom: 8px; 
  display: flex;
  flex-direction: column;
  justify-content: center; /* 核心修复：让图标和文字在垂直方向上完美居中 */
  align-items: center;
  gap: 5px; /* 核心参数：修改这个数字，就能精确调整图标和文字的距离 */
  height: 60px; /* 建议设一个固定高度，让灰色方块更规整 */
  border-radius: 8px; /* 四角为弧的方形 */
  transition: all 0.2s; /* 增加平滑过渡 */
}

.menu-item-text {
  /* 删掉 height: 20%; */
  height: auto; 
  line-height: 1.2; /* 防止文字自身的行高撑开多余的间距 */
  font-size: 13px;
  color: #606266;
}

/* 顺便加上悬停和选中的样式，满足你之前的需求 */
:deep(.total-item:hover) {
  background-color: #f5f6f8;
}
:deep(.total-item.is-active) {
  background-color: #eef6ff;
  color: #409eff;
}
:deep(.total-item.is-active .el-icon) {
  color: #409eff;
}
.total-item.is-active .menu-item-text{
   color: #409eff;
}

</style>
