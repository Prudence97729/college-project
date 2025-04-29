<template>
  <div class="user-management">
    <el-card class="user-table-card">
      <template #header>
        <div class="card-header">
          <h3>用户管理</h3>
          <el-button type="primary" size="small" @click="refreshUsers">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <el-table
        :data="users"
        stripe
        border
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column type="index" label="#" width="60" />
        
        <el-table-column prop="username" label="用户名" width="120" />
        
        <el-table-column prop="nickname" label="昵称" width="120" />
        
        <el-table-column prop="email" label="邮箱" />
        
        <el-table-column label="头像" width="100">
          <template #default="scope">
            <el-avatar
              v-if="scope.row.userPic"
              :src="scope.row.userPic"
              :size="40"
            />
            <el-avatar v-else :size="40" icon="el-icon-user" />
          </template>
        </el-table-column>
        
        <el-table-column label="注册时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="handleEditUser(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDeleteUser(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 编辑用户对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑用户"
      width="500px"
    >
      <el-form
        :model="editForm"
        :rules="editRules"
        ref="editFormRef"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="editForm.password"
            type="password"
            placeholder="留空表示不修改密码"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEditForm" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'

// 用户列表数据
const users = ref([])
const loading = ref(false)

// 编辑用户相关
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const editForm = reactive({
  id: '',
  username: '',
  nickname: '',
  email: '',
  password: ''
})
const submitting = ref(false)

// 表单验证规则
const editRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符之间', trigger: 'blur' }
  ]
}

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await adminApi.getUserList()
    
    if (res.code === 200) {
      users.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取用户列表失败')
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 刷新用户列表
const refreshUsers = () => {
  fetchUsers()
}

// 编辑用户
const handleEditUser = (user) => {
  editForm.id = user.id
  editForm.username = user.username
  editForm.nickname = user.nickname
  editForm.email = user.email
  editForm.password = '' // 清空密码字段
  
  editDialogVisible.value = true
}

// 提交编辑表单
const submitEditForm = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      // 创建要提交的数据对象
      const userData = {
        id: editForm.id,
        nickname: editForm.nickname,
        email: editForm.email
      }
      
      // 如果密码不为空，则添加到提交数据中
      if (editForm.password) {
        userData.password = editForm.password
      }
      
      const res = await adminApi.updateUser(userData)
      
      if (res.code === 200) {
        ElMessage.success('更新用户成功')
        editDialogVisible.value = false
        
        // 更新本地用户数据
        const index = users.value.findIndex(u => u.id === editForm.id)
        if (index !== -1) {
          users.value[index].nickname = editForm.nickname
          users.value[index].email = editForm.email
        }
      } else {
        ElMessage.error(res.message || '更新用户失败')
      }
    } catch (error) {
      console.error('更新用户失败:', error)
      ElMessage.error('更新用户失败')
    } finally {
      submitting.value = false
    }
  })
}

// 删除用户
const handleDeleteUser = (user) => {
  ElMessageBox.confirm(
    `确定要删除用户 "${user.username}" 吗？此操作不可恢复！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await adminApi.deleteUser(user.id)
      
      if (res.code === 200) {
        // 更新本地状态
        users.value = users.value.filter(u => u.id !== user.id)
        ElMessage.success('删除用户成功')
      } else {
        ElMessage.error(res.message || '删除用户失败')
      }
    } catch (error) {
      console.error('删除用户失败:', error)
      ElMessage.error('删除用户失败')
    }
  }).catch(() => {})
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.user-table-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 