<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userForm = ref({
  username: '',
  nickname: '',
  email: '',
})

const loading = ref(false)
const userFormRef = ref(null)

const userStore = useUserStore()

// 表单验证规则
const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// 获取用户信息
const getUserInfo = async () => {
  try {
    console.log('Local storage user:', localStorage.getItem('user'))  // 添加日志
    const res = await userApi.getUserInfo()
    console.log('API response:', res.data)  // 添加日志
    
    if (res.data.code === 200) {
      // 直接使用响应数据
      userForm.value = res.data.data
      // 更新 Pinia store
      userStore.updateUserInfo(res.data.data)
    }
  } catch (error) {
    console.error('Error:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 更新用户信息
const updateProfile = async (formEl) => {
  if (!formEl) {
    console.error('Form element is null or undefined')
    return
  }
  
  try {
    // 使用 await 等待表单验证完成
    const valid = await formEl.validate()
    
    if (valid) {
      loading.value = true
      try {
        const res = await userApi.updateProfile({
          id: userForm.value.id,
          nickname: userForm.value.nickname,
          email: userForm.value.email
        })
        
        if (res.data.code === 200) {
          ElMessage.success('更新成功')
          // 更新本地存储
          const user = JSON.parse(localStorage.getItem('user') || '{}')
          user.nickname = userForm.value.nickname
          user.email = userForm.value.email
          localStorage.setItem('user', JSON.stringify(user))
          // 更新 store
          userStore.updateUserInfo(user)
        } else {
          ElMessage.error(res.data.message || '更新失败')
        }
      } catch (error) {
        console.error('Update error:', error)
        ElMessage.error('更新失败，请检查网络连接')
      } finally {
        loading.value = false
      }
    }
  } catch (error) {
    console.error('Validation error:', error)
    // 表单验证失败
  }
}

onMounted(() => {
  getUserInfo()
})
</script>

<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <h3>个人资料</h3>
      </template>
      
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="rules"
        label-width="100px"
        size="default"
      >
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" disabled />
        </el-form-item>
        
        <el-form-item label="昵称" prop="nickname">
          <el-input 
            v-model="userForm.nickname" 
            placeholder="请输入昵称"
          />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input 
            v-model="userForm.email" 
            placeholder="请输入邮箱"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            @click="updateProfile(userFormRef)"
            :loading="loading"
          >
            保存修改
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.profile-container {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
}

.profile-card {
  max-width: 600px;
  margin: 0 auto;
}

.profile-card :deep(.el-card__header) {
  padding: 15px 20px;
}

.profile-card :deep(.el-card__header h3) {
  margin: 0;
  font-size: 18px;
  color: #303133;
}
</style> 