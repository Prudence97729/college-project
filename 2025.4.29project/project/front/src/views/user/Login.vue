<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, ArrowRight } from '@element-plus/icons-vue'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loginForm = ref({
  username: '',
  password: ''
})
const loading = ref(false)
const loginFormRef = ref(null)

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async (formEl) => {
  if (!formEl) return
  
  await formEl.validate(async (valid, fields) => {
    if (valid) {
      loading.value = true
      try {
        const res = await userApi.login({
          username: loginForm.value.username,
          password: loginForm.value.password
        })
        
        if (res.data.code === 200) {
          localStorage.setItem('token', res.data.token)
          localStorage.setItem('user', JSON.stringify(res.data.data))
          userStore.updateUserInfo(res.data.data)
          
          ElMessage.success('登录成功')
          router.push('/monitor')
        } else {
          ElMessage.error(res.data.message || '登录失败')
        }
      } catch (error) {
        ElMessage.error('登录失败，请检查网络连接')
      } finally {
        loading.value = false
      }
    }
  })
}

const goToAdminLogin = () => {
  router.push('/admin/login')
}
</script>

<template>
  <div class="login-container">
    <!-- 整合背景图和登录框的容器 -->
    <div class="background-wrapper">
      <!-- 背景图片层 -->
      <div class="background-image"></div>
      
      <!-- 半透明遮罩层 -->
      <div class="background-overlay"></div>
      
      <!-- 系统标题文字 -->
      <div class="system-title">
        <h1>水闸群调度策略推荐系统</h1>
        <p>基于知识图谱的平原河网智能调度平台</p>
      </div>
      
      <!-- 登录表单卡片 -->
      <div class="login-card-wrapper">
        <el-card class="login-card">
          <template #header>
            <div class="card-header">
              <h2>用户登录</h2>
              <div class="admin-link" @click="goToAdminLogin">
                <span>管理员入口</span>
                <el-icon><ArrowRight /></el-icon>
              </div>
            </div>
          </template>
          
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="rules"
            label-position="top"
          >
            <el-form-item prop="username">
              <el-input 
                v-model="loginForm.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
                size="large"
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input 
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                show-password
                :prefix-icon="Lock"
                size="large"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button 
                type="primary" 
                class="login-button" 
                :loading="loading"
                @click="handleLogin(loginFormRef)"
                size="large"
              >
                登录
              </el-button>
            </el-form-item>
            
            <div class="bottom-links">
              <router-link to="/register">注册账号</router-link>
              <router-link to="/forget-password">忘记密码？</router-link>
            </div>
          </el-form>
        </el-card>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 基础容器 */
.login-container {
  position: relative;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.background-wrapper {
  position: relative;
  height: 100%;
  width: 100%;
}

/* 背景图片样式 */
.background-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('@/assets/images/login-bg.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  z-index: 0;
}

/* 半透明遮罩 */
.background-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.3);
  z-index: 1;
}

/* 系统标题样式 */
.system-title {
  position: absolute;
  top: 50%;
  left: 10%;
  transform: translateY(-50%);
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  max-width: 500px;
  z-index: 2;
}

.system-title h1 {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 1rem;
  line-height: 1.3;
}

.system-title p {
  font-size: 1.2rem;
  opacity: 0.9;
}

/* 登录卡片容器 */
.login-card-wrapper {
  position: absolute;
  top: 50%;
  right: 10%;
  transform: translateY(-50%);
  width: 420px;
  z-index: 3;
}

/* 登录卡片样式 */
.login-card {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 6px 30px rgba(0, 0, 0, 0.2);
  background-color: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
}

.login-card :deep(.el-card__header) {
  border-bottom: 1px solid var(--el-border-color-light);
  padding: 24px;
  background-color: transparent;
}

.login-card :deep(.el-card__body) {
  padding: 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 1.5rem;
  color: var(--el-text-color-primary);
}

/* 管理员入口链接 */
.admin-link {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--el-color-info);
  font-size: 14px;
  cursor: pointer;
  transition: color 0.3s;
}

.admin-link:hover {
  color: var(--el-color-primary);
}

/* 登录按钮 */
.login-button {
  width: 100%;
  margin-top: 10px;
  font-size: 16px;
  letter-spacing: 2px;
}

/* 底部链接 */
.bottom-links {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
  font-size: 14px;
}

.bottom-links a {
  color: var(--el-color-info);
  text-decoration: none;
  transition: color 0.3s;
}

.bottom-links a:hover {
  color: var(--el-color-primary);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .system-title {
    left: 5%;
  }
  
  .login-card-wrapper {
    right: 5%;
  }
}

@media (max-width: 992px) {
  .system-title {
    left: 50%;
    top: 20%;
    transform: translateX(-50%);
    text-align: center;
    width: 90%;
  }
  
  .login-card-wrapper {
    top: 50%;
    left: 50%;
    right: auto;
    transform: translate(-50%, -50%);
  }
  
  .system-title h1 {
    font-size: 2rem;
  }
}

@media (max-width: 576px) {
  .login-card-wrapper {
    width: 90%;
  }
  
  .login-card :deep(.el-card__header) {
    padding: 18px;
  }
  
  .login-card :deep(.el-card__body) {
    padding: 20px;
  }
  
  .system-title h1 {
    font-size: 1.8rem;
  }
  
  .system-title p {
    font-size: 1rem;
  }
}

@media (max-height: 700px) {
  .system-title {
    top: 15%;
  }
  
  .login-card-wrapper {
    top: 60%;
  }
}
</style>