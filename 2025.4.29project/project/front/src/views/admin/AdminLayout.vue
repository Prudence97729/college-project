<template>
  <div class="admin-layout">
    <!-- 左侧边栏 -->
    <div class="sidebar">
      <!-- 系统标志 -->
      <div class="logo">
        <h2>管理后台</h2>
      </div>
      
      <!-- 管理员信息 - 调整样式使其与背景协调 -->
      <div class="admin-profile">
        <el-avatar 
          :size="50" 
          :src="adminInfo.userPic || '/api/upload/default.png'" 
          @error="(e) => e.target.src = '/api/upload/default.png'"
        />
        <div class="admin-info-text">
          <h3>{{ adminInfo.nickname || adminInfo.username || '管理员' }}</h3>
          <el-tag size="small" effect="dark" type="info">系统管理员</el-tag>
        </div>
      </div>
      
      <!-- 导航菜单 - 修正URL -->
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/gates">
          <el-icon><Setting /></el-icon>
          <span>水闸控制</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/logs">
          <el-icon><Timer /></el-icon>
          <span>访问记录</span>
        </el-menu-item>
        
        <el-sub-menu index="settings">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>个人设置</span>
          </template>
          <el-menu-item index="/admin/profile">
            <el-icon><User /></el-icon>
            <span>个人资料</span>
          </el-menu-item>
          <el-menu-item index="/admin/password">
            <el-icon><Lock /></el-icon>
            <span>修改密码</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </div>
    
    <!-- 右侧内容区 -->
    <div class="main-content">
      <!-- 顶部标题栏 -->
      <div class="content-header">
        <h2>{{ getPageTitle }}</h2>
        
        <div class="header-actions">
          <!-- 退出按钮 -->
          <el-button type="danger" size="small" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出系统
          </el-button>
        </div>
      </div>
      
      <!-- 内容区域 - 路由视图 -->
      <div class="content-body">
        <router-view></router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { HomeFilled, Setting, User, Timer, SwitchButton, Lock } from '@element-plus/icons-vue'
import { useAdminStore } from '@/stores/admin'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

// 管理员信息
const adminInfo = computed(() => {
  return adminStore.adminInfo || {}
})

// 当前激活的菜单项
const activeMenu = computed(() => route.path)

// 获取页面标题
const getPageTitle = computed(() => {
  const titles = {
    '/admin/dashboard': '系统概览',
    '/admin/gates': '水闸控制',
    '/admin/users': '用户管理',
    '/admin/logs': '访问记录',
    '/admin/profile': '个人资料',
    '/admin/password': '修改密码'
  }
  return titles[route.path] || '管理后台'
})

// 初始化管理员信息
onMounted(() => {
  const adminInfoStr = localStorage.getItem('adminInfo')
  if (adminInfoStr) {
    try {
      const adminData = JSON.parse(adminInfoStr)
      adminStore.updateAdminInfo(adminData)
    } catch (e) {
      console.error('解析管理员信息失败:', e)
    }
  }
})

// 处理退出登录
const handleLogout = () => {
  ElMessageBox.confirm(
    '确定要退出系统吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 清除管理员信息
    adminStore.clearAdminInfo()
    ElMessage.success('已安全退出系统')
    router.push('/admin/login')
  }).catch(() => {})
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: 220px;
  background-color: #304156;
  color: white;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #1f2d3d;
}

.logo h2 {
  color: white;
  margin: 0;
  font-size: 18px;
}

/* 管理员信息样式 - 调整为与背景协调 */
.admin-profile {
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-bottom: 1px solid #1f2d3d;
}

.admin-info-text {
  margin-top: 10px;
  text-align: center;
}

.admin-info-text h3 {
  margin: 0 0 5px 0;
  color: #dcdfe6;
  font-size: 16px;
}

/* 菜单样式 */
.sidebar-menu {
  flex: 1;
  border-right: none;
}

.sidebar-menu :deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
}

.sidebar-menu :deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content-header {
  height: 60px;
  background-color: white;
  border-bottom: 1px solid #dcdfe6;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.content-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.content-body {
  flex: 1;
  padding: 20px;
  background-color: #f0f2f5;
  overflow-y: auto;
}

.header-actions {
  display: flex;
  gap: 10px;
}
</style> 