<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Monitor, Setting, Switch, SwitchButton, User, ArrowDown, Picture, Lock, Operation, SetUp } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const activeMenu = ref('')
const isAuthenticated = computed(() => localStorage.getItem('token'))

// 获取用户信息
const userInfo = computed(() => {
  console.log('userStore中的userInfo:', userStore.userInfo)
  return userStore.userInfo
})

// 判断是否是管理员路由
const isAdminRoute = computed(() => {
  return route.path.startsWith('/admin')
})

// 判断是否已登录
const isLoggedIn = computed(() => {
  return userStore.isLoggedIn
})

// 初始化用户信息
onMounted(() => {
  // 检查本地存储中是否有用户信息
  const userStr = localStorage.getItem('user')
  console.log('localStorage中的user:', userStr)
  
  if (userStr) {
    try {
      const userData = JSON.parse(userStr)
      console.log('解析后的userData:', userData)
      userStore.updateUserInfo(userData.user || userData)  
      console.log('设置后的userStore.userInfo:', userStore.userInfo)
    } catch (e) {
      console.error('解析用户信息失败:', e)
    }
  }
})

const handleLogout = () => {
  userStore.clearUserInfo()  // 使用 store 的方法清除用户信息
  ElMessage.success('退出成功')
  router.push('/login')
}

// 获取头像URL的函数
const getAvatarUrl = (user) => {
  // 检查用户对象中可能的头像字段
  if (!user) return '/api/upload/default.png'
  
  // 检查常见的头像字段名
  if (user.avatar) return user.avatar
  if (user.userPic) return user.userPic
  if (user.avatarUrl) return user.avatarUrl
  if (user.headImg) return user.headImg
  
  // 如果都没有，返回默认头像
  return '/api/upload/default.png'
}

// 处理头像加载失败
const handleAvatarError = (e) => {
  console.log('头像加载失败，使用默认头像')
  e.target.src = '/api/upload/default.png'
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'basic':
      router.push('/profile')
      break
    case 'avatar':
      router.push('/profile/avatar')
      break
    case 'password':
      router.push('/profile/password')
      break
    case 'logout':
      userStore.clearUserInfo()
      ElMessage.success('退出成功')
      router.push('/login')
      break
  }
}
</script>

<template>
  <el-container class="layout-container">
    <!--el-container:Element Plus 提供的布局容器组件，用于构建整体页面框架-->
    <el-header>
      <div class="header-content">
        <div class="logo">
          <el-text class="logo-text" size="large">水闸调度策略推荐系统</el-text>
        </div>
        
        <!-- 导航链接 调用Element-Plus里面的el-link链接组件，
        href定义了点击后跳转的路径，type 是样式设置-->
        <div class="nav-links">
          <el-link 
            :type="route.path === '/monitor' ? 'primary' : 'info'"
            :underline="false"
            href="/monitor" 
            class="nav-link"
          >
            <el-icon><monitor /></el-icon>
            水文信息
          </el-link>
          <el-link 
            :type="route.path === '/gates' ? 'primary' : 'info'"
            :underline="false"
            href="/gates"
            class="nav-link"
          >
            <el-icon><set-up /></el-icon>
            水闸状态
          </el-link>
          <el-link 
            :type="route.path === '/strategy' ? 'primary' : 'info'"
            :underline="false"
            href="/strategy"
            class="nav-link"
          >
            <el-icon><operation /></el-icon>
            调度策略
          </el-link>
        </div>
        
        <!-- 用户操作区域，右侧个人资料 如果userInfo有数据，就展示个人资料，否则展示登录、注册按钮-->
        <div class="user-actions">
          <template v-if="Object.keys(userInfo || {}).length > 0">
            <el-dropdown trigger="click" @command="handleCommand"><!--点击触发下拉菜单-->
              <span class="el-dropdown-link">
                <el-avatar 
                  :size="32" 
                  :src="getAvatarUrl(userInfo)" 
                  @error="handleAvatarError"
                />
                <span class="username">{{ userInfo.nickname || userInfo.username }}</span>
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="basic">
                    <el-icon><user /></el-icon>
                    基本资料
                  </el-dropdown-item>
                  <el-dropdown-item command="avatar">
                    <el-icon><picture /></el-icon>
                    更换头像
                  </el-dropdown-item>
                  <el-dropdown-item command="password">
                    <el-icon><lock /></el-icon>
                    修改密码
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><switch-button /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" @click="$router.push('/login')">登录</el-button>
            <el-button @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>
    
    <el-main>
      <router-view></router-view><!-- ​​Vue Router​​ 提供的核心组件，根据当前URL自动渲染对应的页面内容-->
    </el-main>

    <!-- 页脚 -->
    <el-footer height="60px">
      <div class="footer-content">
        基于知识图谱的平原河网水闸群调度策略推荐系统
      </div>
    </el-footer>
  </el-container>
</template>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
  padding: 0;
  position: fixed;
  width: 100%;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
}

.logo-text {
  font-size: 20px;
  font-weight: bold;
  color: var(--el-color-primary);
}

.nav-links {
  flex: 1;
  display: flex;
  justify-content: center;
  gap: 40px;
}

.nav-link {
  font-size: 16px;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-link:hover {
  transform: translateY(-2px);
}

.nav-link .el-icon {
  margin-right: 4px;
}

.user-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.el-main {
  padding-top: 80px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.el-footer {
  background-color: white;
  border-top: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.footer-content {
  text-align: center;
  color: #666;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 15px;
  }
  
  .nav-links {
    gap: 20px;
  }
  
  .nav-link {
    font-size: 14px;
  }
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 2px 8px;
  border-radius: 4px;
  transition: all 0.3s;
}

.el-dropdown-link:hover {
  background-color: var(--el-fill-color-light);
}

.username {
  font-size: 14px;
  color: var(--el-text-color-primary);
}

:deep(.el-dropdown-menu__item) {
  display: flex !important;
  align-items: center !important;
  gap: 8px !important;
  padding: 8px 16px !important;
}

:deep(.el-dropdown-menu__item .el-icon) {
  margin-right: 4px !important;
}

:deep(.el-dropdown-menu__item.is-divided) {
  border-top: 1px solid var(--el-border-color-lighter);
  margin: 6px 0;
}
</style> 