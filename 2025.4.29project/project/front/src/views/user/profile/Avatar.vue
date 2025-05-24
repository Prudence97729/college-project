<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const uploadRef = ref()
const selectedFile = ref(null)
const imageUrl = ref('')
const loading = ref(false)

// 获取当前用户头像
const getCurrentAvatar = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    if (user.userPic) {
      imageUrl.value = user.userPic  // 直接使用相对路径
    }
  }
}

// 处理文件选择改变
const handleChange = (uploadFile) => {
  if (!uploadFile.raw.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  
  const maxSize = 2 * 1024 * 1024 // 2MB
  if (uploadFile.raw.size > maxSize) {
    ElMessage.warning('图片大小不能超过 2MB')
    return
  }

  selectedFile.value = uploadFile.raw
  imageUrl.value = URL.createObjectURL(uploadFile.raw)
}

// 处理头像上传
const handleAvatarUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择图片')
    return
  }

  loading.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)

    const res = await userApi.updateAvatar(formData)
    
    if (res.data.code === 200) {
      // 更新本地存储的用户信息
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      user.userPic = res.data.data.userPic
      localStorage.setItem('user', JSON.stringify(user))
      
      // 更新 store
      userStore.updateAvatar(res.data.data.userPic)
      
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(res.data.message || '上传失败')
    }
  } catch (error) {
    console.error('Upload error:', error)
    ElMessage.error('上传失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

// 重置头像选择
const resetAvatar = () => {
  selectedFile.value = null
  getCurrentAvatar()
}

// 组件挂载时获取当前头像
onMounted(() => {
  getCurrentAvatar()
})

// 组件卸载时清理对象URL
onUnmounted(() => {
  if (selectedFile.value && imageUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(imageUrl.value)
  }
})
</script>

<template>
  <div class="avatar-container">
    <el-card class="avatar-card">
      <template #header>
        <h3>修改头像</h3>
      </template>
      
      <div class="avatar-uploader">
        <!--el-upload 组件本质上是一个 ​​透明文件输入框​​，当点击其包裹的内容（如图片或图标）时，会自动触发浏览器原生的文件选择窗口-->
        <el-upload
          ref="uploadRef"
          class="avatar-uploader"
          :auto-upload="false"
          :show-file-list="false"
          :on-change="handleChange"
          accept="image/jpeg,image/png,image/gif"
        >
          <img v-if="imageUrl" :src="imageUrl" class="avatar" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
      </div>
      
      <div class="upload-tip">
        支持 JPG、PNG、GIF 格式，文件大小不超过 2MB
      </div>
      
      <div class="upload-actions">
        <el-button 
          type="primary" 
          :loading="loading"
          @click="handleAvatarUpload"
        >
          <el-icon><Upload /></el-icon>
          上传头像
        </el-button>
        
        <el-button @click="resetAvatar">
          重置
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.avatar-container {
  padding: 20px;
}

.avatar-card {
  max-width: 400px;
  margin: 0 auto;
}

.avatar-uploader {
  text-align: center;
  padding: 20px 0;
}

.avatar {
  width: 278px;
  height: 278px;
  display: block;
  object-fit: cover;
  border-radius: 6px;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 278px;
  height: 278px;
  text-align: center;
  line-height: 278px;
}

.upload-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.upload-tip {
  text-align: center;
  color: #909399;
  font-size: 14px;
  margin-top: 10px;
}

/* 添加图片加载过渡效果 */
img {
  transition: all 0.3s ease;
}
</style> 