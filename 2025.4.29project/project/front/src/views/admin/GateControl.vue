<template>
  <div class="gate-control">
    <el-card class="gate-table-card">
      <template #header>
        <div class="card-header">
          <h3>水闸管理</h3>
          <el-button type="primary" size="small" @click="fetchGates">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <el-table
        :data="gates"
        stripe
        border
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column type="index" label="#" width="60" />
        
        <el-table-column prop="gateName" label="水闸名称" width="150" />
        
        <el-table-column prop="gateCode" label="闸门编号" width="120" />
        
        <el-table-column prop="deviceType" label="闸门类型" width="120" />
        
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag
              :type="scope.row.status === 'open' ? 'success' : 'danger'"
              effect="dark"
            >
              {{ scope.row.status === 'open' ? '开启' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="更新时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.updateTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button
              :type="scope.row.status === 'open' ? 'danger' : 'success'"
              size="small"
              @click="handleToggleStatus(scope.row)"
              :loading="scope.row.loading"
            >
              {{ scope.row.status === 'open' ? '关闭' : '开启' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { useGateOperation } from '@/composables/useGateOperation'

const { loading, gates, fetchGates, updateGateStatus } = useGateOperation()

// 切换水闸状态
const handleToggleStatus = async (gate) => {
  // 添加局部加载状态
  gate.loading = true
  
  try {
    const success = await updateGateStatus(gate)
    if (success) {
      // 状态已在 updateGateStatus 中更新
    }
  } finally {
    gate.loading = false
  }
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
    minute: '2-digit',
    second: '2-digit'
  })
}

onMounted(() => {
  fetchGates()
})
</script>

<style scoped>
.gate-control {
  padding: 20px;
}

.gate-table-card {
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
</style> 