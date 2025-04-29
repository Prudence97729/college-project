<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h3>个人资料</h3>
          <el-button type="primary" link @click="goBack">
            <el-icon><ArrowLeft /></el-icon> 返回
          </el-button>
        </div>
      </template>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        v-loading="loading"
      >
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            保存修改
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)

const form = ref({
  id: '',
  username: '',
  nickname: '',
  email: ''
})

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

// 获取管理员信息
const getAdminInfo = () => {
  loading.value = true
  
  // 从本地存储获取管理员信息
  const adminInfoStr = localStorage.getItem('adminInfo')
  if (adminInfoStr) {
    try {
      const adminInfo = JSON.parse(adminInfoStr)
      form.value.id = adminInfo.id
      form.value.username = adminInfo.username
      form.value.nickname = adminInfo.nickname || ''
      form.value.email = adminInfo.email || ''
    } catch (error) {
      console.error('解析管理员信息失败:', error)
      ElMessage.error('获取管理员信息失败')
    }
  } else {
    ElMessage.warning('未找到管理员信息，请重新登录')
    router.push('/admin/login')
  }
  
  loading.value = false
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const res = await adminApi.updateProfile({
          id: form.value.id,
          nickname: form.value.nickname,
          email: form.value.email
        })
        
        if (res.code === 200) {
          ElMessage.success('更新成功')
          
          // 更新本地存储的管理员信息
          const adminInfo = JSON.parse(localStorage.getItem('adminInfo') || '{}')
          adminInfo.nickname = form.value.nickname
          adminInfo.email = form.value.email
          localStorage.setItem('adminInfo', JSON.stringify(adminInfo))
        } else {
          ElMessage.error(res.message || '更新失败')
        }
      } catch (error) {
        console.error('更新失败:', error)
        ElMessage.error('更新失败，请检查网络连接')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 返回上一页
const goBack = () => {
  router.back()
}

onMounted(() => {
  getAdminInfo()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.profile-card {
  max-width: 600px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}
</style> 