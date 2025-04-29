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
    <!-- 左侧图片区域 -->
    <div class="image-section">
      <div class="overlay">
        <h1>水闸群调度策略推荐系统</h1>
        <p>基于知识图谱的平原河网智能调度平台</p>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-section">
      <el-card class="login-box">
        <template #header>
          <div class="card-header">
            <h2>登录</h2>
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
              placeholder="用户名"
              :prefix-icon="User"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              class="login-button" 
              :loading="loading"
              @click="handleLogin(loginFormRef)"
            >
              登录
            </el-button>
          </el-form-item>
          
          <div class="register-link">
            还没有账号？<router-link to="/register">立即注册</router-link>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.image-section {
  flex: 1.2;
  background-image: url('@/assets/images/login-bg.jpg');
  background-size: cover;
  background-position: center;
  position: relative;
}

.overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.4);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 2rem;
  z-index: 1;
}

.overlay h1 {
  font-size: 2.5rem;
  margin-bottom: 1rem;
  color: white;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.overlay p {
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
}

.login-section {
  flex: 1;
  background-color: #d9dcdf;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem;
}

.login-box {
  width: 100%;
  max-width: 400px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 1rem;
}

.login-box :deep(.el-card__header) {
  background-color: white;
  border-bottom: none;
  padding-bottom: 0;
}

.login-box :deep(h2) {
  text-align: center;
  color: #1890ff;
  margin: 0;
  font-size: 1.8rem;
}

.login-button {
  width: 100%;
  height: 40px;
}

.register-link {
  margin-top: 1rem;
  text-align: center;
  color: #666;
}

.register-link a {
  color: #1890ff;
  text-decoration: none;
  margin-left: 5px;
}

.register-link a:hover {
  color: #40a9ff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }
  
  .image-section {
    display: none;
  }
  
  .login-section {
    padding: 1rem;
    background-color: #d9dcdf;
  }
  
  .login-box {
    background-color: white;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #303133;
}

/* 添加管理员入口按钮样式 */
.admin-link {
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  transition: color 0.3s;
}

.admin-link:hover {
  color: #409EFF;
}
</style> 