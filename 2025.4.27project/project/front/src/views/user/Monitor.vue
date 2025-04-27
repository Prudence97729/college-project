<script setup>
import { ref, onMounted } from 'vue'
import WaterLevelChart from '@/components/WaterLevelChart.vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const waterLevelData = ref([])
const rainfallData = ref([])

// 模拟获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // 模拟API请求
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟水位数据
    waterLevelData.value = [
      { time: '00:00', value: 3.2 },
      { time: '02:00', value: 3.3 },
      { time: '04:00', value: 3.4 },
      { time: '06:00', value: 3.5 },
      { time: '08:00', value: 3.6 },
      { time: '10:00', value: 3.5 },
      { time: '12:00', value: 3.4 },
      { time: '14:00', value: 3.3 },
      { time: '16:00', value: 3.2 },
      { time: '18:00', value: 3.1 },
      { time: '20:00', value: 3.0 },
      { time: '22:00', value: 3.1 }
    ]
    
    // 模拟降雨数据
    rainfallData.value = [
      { time: '00:00', value: 0 },
      { time: '02:00', value: 0 },
      { time: '04:00', value: 2.5 },
      { time: '06:00', value: 5.8 },
      { time: '08:00', value: 8.2 },
      { time: '10:00', value: 4.5 },
      { time: '12:00', value: 2.1 },
      { time: '14:00', value: 0.5 },
      { time: '16:00', value: 0 },
      { time: '18:00', value: 0 },
      { time: '20:00', value: 0 },
      { time: '22:00', value: 0 }
    ]
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="monitor-container">
    <el-row :gutter="20">
      <!-- 水位图表 -->
      <el-col :span="24">
        <WaterLevelChart />
      </el-col>
      
      <!-- 其他图表和数据可以在这里添加 -->
    </el-row>
  </div>
</template>

<style scoped>
.monitor-container {
  padding: 20px;
}
</style> 