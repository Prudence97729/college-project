<template>
  <div class="access-logs">
    <el-card class="log-table-card">
      <template #header>
        <div class="card-header">
          <h3>访问记录</h3>
          <div class="header-actions">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              :shortcuts="dateShortcuts"
              style="margin-right: 10px;"
            />
            <el-button type="primary" size="small" @click="fetchLogs">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button size="small" @click="resetFilter">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table
        :data="logs"
        stripe
        border
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column type="index" label="#" width="60" />
        
        <el-table-column prop="username" label="用户名" width="120" />
        
        <el-table-column prop="ip" label="IP地址" width="140" />
        
        <el-table-column label="操作" width="120">
          <template #default="scope">
            {{ scope.row.status === 'success' ? '登录系统' : '登录失败' }}
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'success' ? 'success' : 'danger'">
              {{ scope.row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="loginTime" label="访问时间" width="180" />
      </el-table>
      
      <!-- 如果没有数据，显示空状态 -->
      <el-empty v-if="logs.length === 0 && !loading" description="暂无数据">
        <el-button @click="fetchLogs">重新加载</el-button>
      </el-empty>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'

// 日志列表数据
const logs = ref([])
const loading = ref(false)
const dateRange = ref([])

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    },
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    },
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    },
  },
]

// 获取日志列表
const fetchLogs = async () => {
  loading.value = true
  try {
    const res = await adminApi.getLoginLogs({
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1]
    })
    
    if (res.code === 200) {
      if (res.data && Array.isArray(res.data)) {
        logs.value = res.data
      } else {
        logs.value = []
        ElMessage.warning('数据格式不符合预期')
      }
    } else {
      ElMessage.error(res.message || '获取访问记录失败')
    }
  } catch (error) {
    console.error('获取访问记录失败:', error)
    ElMessage.error('获取访问记录失败')
  } finally {
    loading.value = false
  }
}

// 重置筛选条件
const resetFilter = () => {
  dateRange.value = []
  fetchLogs()
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped>
.access-logs {
  padding: 20px;
}

.log-table-card {
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

.header-actions {
  display: flex;
  align-items: center;
}
</style> 