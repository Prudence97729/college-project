<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { userApi } from '@/api/user'

const router = useRouter()
const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: ''
})
const loading = ref(false)
const registerFormRef = ref(null)

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.value.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const handleRegister = async (formEl) => {
  if (!formEl) return
  
  await formEl.validate(async (valid, fields) => {
    if (valid) {
      loading.value = true
      try {
        const res = await userApi.register({
          username: registerForm.value.username,
          password: registerForm.value.password
        })
        
        if (res.data.code === 200) {
          ElMessage.success('注册成功')
          router.push('/login')
        } else {
          ElMessage.error(res.data.message || '注册失败')
        }
      } catch (error) {
        ElMessage.error('注册失败，请检查网络连接')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<template>
  <div class="register-container">
    <!-- 左侧图片区域 -->
    <div class="image-section">
      <div class="overlay">
        <h1>水闸群调度策略推荐系统</h1>
        <p>基于知识图谱的平原河网智能调度平台</p>
      </div>
    </div>

    <!-- 右侧注册表单 -->
    <div class="register-section">
      <el-card class="register-box">
        <template #header>
          <h2>注册</h2>
        </template>
        
        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="rules"
          label-position="top"
        >
          <el-form-item prop="username" label="用户名">
            <el-input
              v-model="registerForm.username"
              :prefix-icon="User"
              placeholder="请输入用户名"
            />
          </el-form-item>
          
          <el-form-item prop="password" label="密码">
            <el-input
              v-model="registerForm.password"
              type="password"
              :prefix-icon="Lock"
              placeholder="请输入密码"
              show-password
            />
          </el-form-item>
          
          <el-form-item prop="confirmPassword" label="确认密码">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              :prefix-icon="Lock"
              placeholder="请再次输入密码"
              show-password
            />
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              class="register-button"
              @click="handleRegister(registerFormRef)"
            >
              {{ loading ? '注册中...' : '注册' }}
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="login-link">
          已有账号？
          <router-link to="/login">立即登录</router-link>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  display: flex;
  min-height: 100vh;
  width: 100%;
}

.image-section {
  flex: 2;
  position: relative;
  overflow: hidden;
}

.image-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url('@/assets/images/water-gate.jpg');
  background-size: cover;
  background-position: center;
  width: 100%;
  height: 100%;
}

.overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
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

.register-section {
  flex: 1;
  background-color: #d9dcdf;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem;
}

.register-box {
  width: 100%;
  max-width: 400px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 1rem;
}

.register-box :deep(.el-card__header) {
  background-color: white;
  border-bottom: none;
  padding-bottom: 0;
}

.register-box :deep(h2) {
  text-align: center;
  color: #1890ff;
  margin: 0;
  font-size: 1.8rem;
}

.register-button {
  width: 100%;
  height: 40px;
}

.login-link {
  margin-top: 1rem;
  text-align: center;
  color: #666;
}

.login-link a {
  color: #1890ff;
  text-decoration: none;
  margin-left: 5px;
}

.login-link a:hover {
  color: #40a9ff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-container {
    flex-direction: column;
  }
  
  .image-section {
    display: none;
  }
  
  .register-section {
    padding: 1rem;
    background-color: #d9dcdf;
  }
  
  .register-box {
    background-color: white;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  }
}
</style> 