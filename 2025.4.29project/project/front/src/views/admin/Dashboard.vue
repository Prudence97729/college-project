<template>
  <div class="dashboard-container">
    <!-- 系统概览卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-label">用户总数</div>
            <div class="stat-number">{{ statistics.userCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-label">水闸总数</div>
            <div class="stat-number">{{ statistics.gateCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-label">今日访问量</div>
            <div class="stat-number">{{ statistics.todayVisits || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-label">系统运行天数</div>
            <div class="stat-number">{{ statistics.runningDays || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 系统状态图表 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <h3>访问统计</h3>
            </div>
          </template>
          <div id="visitChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <h3>水闸状态分布</h3>
            </div>
          </template>
          <div id="gateStatusChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 最近活动 -->
    <el-card shadow="hover" class="recent-activities">
      <template #header>
        <div class="card-header">
          <h3>最近活动</h3>
          <el-button type="primary" size="small" @click="fetchRecentActivities">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <el-table :data="recentActivities" style="width: 100%" v-loading="activitiesLoading">
        <el-table-column prop="time" label="时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.time) }}
          </template>
        </el-table-column>
        <el-table-column prop="user" label="用户" width="120" />
        <el-table-column prop="action" label="操作" />
        <el-table-column prop="ip" label="IP地址" width="150" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { adminApi } from '@/api/admin'

// 统计数据
const statistics = ref({
  userCount: 0,
  gateCount: 0,
  todayVisits: 0,
  runningDays: 0
})

// 最近活动
const recentActivities = ref([])
const activitiesLoading = ref(false)

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const res = await adminApi.getStatistics()
    if (res.code === 200) {
      statistics.value = res.data
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取最近活动
const fetchRecentActivities = async () => {
  activitiesLoading.value = true
  try {
    const res = await adminApi.getRecentActivities()
    if (res.code === 200) {
      recentActivities.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取最近活动失败')
    }
  } catch (error) {
    console.error('获取最近活动失败:', error)
    ElMessage.error('获取最近活动失败')
  } finally {
    activitiesLoading.value = false
  }
}

// 初始化访问统计图表
const initVisitChart = () => {
  const chartDom = document.getElementById('visitChart')
  const myChart = echarts.init(chartDom)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '访问量',
        type: 'line',
        data: [120, 132, 101, 134, 90, 230, 210],
        smooth: true
      }
    ]
  }
  
  myChart.setOption(option)
  
  // 窗口大小变化时重绘图表
  window.addEventListener('resize', () => {
    myChart.resize()
  })
}

// 初始化水闸状态图表
const initGateStatusChart = async () => {
  const chartDom = document.getElementById('gateStatusChart')
  const myChart = echarts.init(chartDom)
  
  try {
    // 获取水闸列表
    const res = await adminApi.getGateList()
    
    if (res.code === 200) {
      const gates = res.data || []
      
      // 统计不同状态的水闸数量
      const statusCount = {
        open: 0,
        closed: 0
      }
      
      gates.forEach(gate => {
        if (gate.status === 'open') {
          statusCount.open++
        } else {
          statusCount.closed++
        }
      })
      
      const option = {
        tooltip: {
          trigger: 'item'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
            name: '水闸状态',
            type: 'pie',
            radius: '50%',
            data: [
              { value: statusCount.open, name: '开启' },
              { value: statusCount.closed, name: '关闭' }
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      }
      
      myChart.setOption(option)
    } else {
      // 使用默认数据
      const option = {
        tooltip: {
          trigger: 'item'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
            name: '水闸状态',
            type: 'pie',
            radius: '50%',
            data: [
              { value: 5, name: '开启' },
              { value: 3, name: '关闭' }
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      }
      myChart.setOption(option)
    }
  } catch (error) {
    console.error('初始化水闸状态图表失败:', error)
    // 使用默认数据
    const option = {
      tooltip: {
        trigger: 'item'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '水闸状态',
          type: 'pie',
          radius: '50%',
          data: [
            { value: 5, name: '开启' },
            { value: 3, name: '关闭' }
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    }
    myChart.setOption(option)
  }
  
  // 窗口大小变化时重绘图表
  window.addEventListener('resize', () => {
    myChart.resize()
  })
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '';
  // 如果时间字符串包含 'T'，则进行格式化
  if (timeStr.includes('T')) {
    const date = new Date(timeStr);
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  }
  // 否则直接返回原始字符串
  return timeStr;
};

onMounted(() => {
  fetchStatistics()
  fetchRecentActivities()
  initVisitChart()
  initGateStatusChart()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.stat-card {
  text-align: center;
}

.stat-content {
  padding: 20px;
}

.stat-label {
  font-size: 16px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-number {
  font-size: 28px;
  color: #303133;
  font-weight: bold;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}
</style> 