<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api/user'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const formRef = ref(null)

const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const rules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 修改密码
const handleSubmit = async (formEl) => {
  if (!formEl) return
  
  await formEl.validate(async (valid, fields) => {
    if (valid) {
      loading.value = true
      try {
        // 从 store 中获取用户 ID
        const userId = userStore.userInfo.id
        
        const res = await userApi.updatePassword({
          userId: userId,
          oldPassword: form.value.oldPassword,
          newPassword: form.value.newPassword
        })
        
        if (res.data.code === 200) {
          ElMessage.success('密码修改成功，请重新登录')
          // 清除用户信息和token
          userStore.clearUserInfo()
          // 跳转到登录页
          router.push('/login')
        } else {
          ElMessage.error(res.data.message || '修改失败')
        }
      } catch (error) {
        ElMessage.error('修改失败，请检查网络连接')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<template>
  <div class="password-container">
    <el-card class="password-card">
      <template #header>
        <h3>修改密码</h3>
      </template>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="form.oldPassword"
            type="password"
            show-password
            placeholder="请输入原密码"
          />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            placeholder="请确认新密码"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleSubmit(formRef)"
          >
            确认修改
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.password-container {
  padding: 20px;
}

.password-card {
  max-width: 500px;
  margin: 0 auto;
}

.password-card :deep(.el-card__header) {
  padding: 15px 20px;
}

.password-card :deep(.el-card__header h3) {
  margin: 0;
  font-size: 18px;
  color: #303133;
}
</style> 