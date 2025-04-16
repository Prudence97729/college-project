<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { gateApi } from '@/api/gate'  // 导入水闸API
import { getStrategies, simulateStrategy as apiSimulateStrategy } from '@/api/strategy' //导入策略API

// 水闸列表数据
const gates = ref([])
const loading = ref(false)
const strategies = ref([]) //从后端获取策略数据
// 获取水闸列表数据
const fetchGateList = async () => {
  loading.value = true
  try {
    const res = await gateApi.getGateList()
    if (res.code === 200) {
      gates.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取水闸列表失败')
    }
  } catch (error) {
    console.error('获取水闸列表失败:', error)
    ElMessage.error('获取水闸列表失败')
  } finally {
    loading.value = false
  }
}
// 获取策略列表数据
const fetchStrategies = async () => {
  try {
    const res = await getStrategies()
    if (res.code === 200) {
      strategies.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取策略列表失败')
    }
  } catch (error) {
    console.error('获取策略列表失败:', error)
    ElMessage.error('获取策略列表失败')
  }
}


// 在组件挂载时获取数据
onMounted(() => {
  fetchGateList()
  fetchStrategies()
})



// 模拟策略
const simulateStrategy = async (strategy) => {
  strategy.simulating = true
  try {
    const res = await apiSimulateStrategy(strategy.id)
    if (res.code === 200) {
      strategy.prediction = res.data
      ElMessage.success(`策略"${strategy.title}"模拟完成！`)
    } else {
      ElMessage.error(res.message || '模拟策略失败')
    }
  } catch (error) {
    console.error('模拟策略失败:', error)
    ElMessage.error('模拟策略失败')
  } finally {
    strategy.simulating = false
  }
}

// 应用策略
const applyStrategy = (strategy) => {
  if (!strategy.prediction) {
    ElMessage.warning('请先进行模拟预测，确认结果后再应用策略')
    return
  }
  
  // 这里应该调用后台API应用策略
  ElMessage.success(`成功应用策略：${strategy.title}`)
  
  // 更新水闸状态
  strategy.actions.forEach(action => {
    const gate = gates.value.find(g => g.id === action.gateId)
    if (gate) {
      gate.status = action.action
    }
  })
}

// 获取策略类型对应的标签样式
const getTypeTagType = (type) => {
  const typeMap = {
    '防洪': 'danger',
    '引水': 'primary',
    '平衡': 'warning',
    '应急': 'danger',
    '保水': 'success'
  }
  return typeMap[type] || 'info'
}

// 获取优先级对应的标签样式
const getPriorityTagType = (priority) => {
  const priorityMap = {
    '高': 'danger',
    '中': 'warning',
    '低': 'info'
  }
  return priorityMap[priority] || 'info'
}

// 获取状态对应的标签样式
const getStatusTagType = (status) => {
  const statusMap = {
    '推荐': 'success',
    '备选': 'info',
    '紧急': 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取水位变化对应的类名
const getWaterLevelClass = (value) => {
  if (value < 0) return 'decrease'
  if (value > 0) return 'increase'
  return ''
}

// 获取流速变化对应的类名
const getFlowRateClass = (value) => {
  if (value < 0) return 'decrease'
  if (value > 0) return 'increase'
  return ''
}

// 获取风险等级对应的标签样式
const getRiskLevelTagType = (level) => {
  const levelMap = {
    '低': 'success',
    '中': 'warning',
    '高': 'danger',
    '极高': 'danger'
  }
  return levelMap[level] || 'info'
}

// 格式化水位变化显示
const formatWaterLevelChange = (value) => {
  if (value > 0) return `+${value}m`
  return `${value}m`
}

// 格式化流速变化显示
const formatFlowRateChange = (value) => {
  if (value > 0) return `+${value}m/s`
  return `${value}m/s`
}
</script>

<template>
  <div class="strategy-container">
    <!-- 策略列表 -->
    <div class="strategy-list">
      <h2>调度策略方案</h2>
      <el-row :gutter="20">
        <el-col :span="24" v-for="strategy in strategies" :key="strategy.id">
          <el-card class="strategy-card" shadow="hover">
            <template #header>
              <div class="strategy-header">
                <div class="header-left">
                  <span class="strategy-title">{{ strategy.title }}</span>
                  <el-tag size="small" :type="getTypeTagType(strategy.type)">
                    {{ strategy.type }}
                  </el-tag>
                  <el-tag size="small" :type="getPriorityTagType(strategy.priority)">
                    {{ strategy.priority }}优先级
                  </el-tag>
                </div>
                <div class="header-right">
                  <el-tag size="small" :type="getStatusTagType(strategy.status)" effect="dark">
                    {{ strategy.status }}
                  </el-tag>
                </div>
              </div>
            </template>
            
            <div class="strategy-content">
              <div class="description">
                {{ strategy.description }}
              </div>
              
              <div class="actions">
                <h4>水闸调度策略：</h4>
                <el-table :data="strategy.actions" border stripe>
                  <el-table-column label="水闸" width="250">
                    <template #default="scope">
                      {{ gates.find(g => g.id === scope.row.gateId)?.gateName }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="action" label="操作" width="180">
                    <template #default="scope">
                      <el-tag :type="scope.row.action === '开启' ? 'success' : 'danger'">
                        {{ scope.row.action }}
                      </el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
              
              <!-- 操作按钮 -->
              <div class="operations" v-if="!strategy.prediction">
                <el-button 
                  type="primary" 
                  @click="simulateStrategy(strategy)"
                  :loading="strategy.simulating"
                >
                  {{ strategy.simulating ? '模拟中...' : '模拟预测' }}
                </el-button>
              </div>
              
              <!-- 预测结果 -->
              <div class="prediction" v-if="strategy.prediction">
                <h4>预测结果：</h4>
                
                <!-- 水位变化 -->
                <div class="prediction-section">
                  <h5>水位变化预测：</h5>
                  <el-row :gutter="20">
                    <el-col :span="8" v-for="(value, area) in strategy.prediction.waterLevel" :key="area">
                      <div class="prediction-item">
                        <div class="label">{{ area }}水位</div>
                        <div class="value" :class="getWaterLevelClass(value)">
                          {{ formatWaterLevelChange(value) }}
                        </div>
                      </div>
                    </el-col>
                  </el-row>
                </div>
                
                <!-- 流速变化 -->
                <div class="prediction-section">
                  <h5>流速变化预测：</h5>
                  <el-row :gutter="20">
                    <el-col :span="8" v-for="(value, channel) in strategy.prediction.flowRate" :key="channel">
                      <div class="prediction-item">
                        <div class="label">{{ channel }}</div>
                        <div class="value" :class="getFlowRateClass(value)">
                          {{ formatFlowRateChange(value) }}
                        </div>
                      </div>
                    </el-col>
                  </el-row>
                </div>
                
                <!-- 风险评估 -->
                <div class="prediction-section">
                  <h5>风险与评估：</h5>
                  <el-row :gutter="20">
                    <el-col :span="8">
                      <div class="prediction-item">
                        <div class="label">风险等级</div>
                        <div class="risk-level">
                          <el-tag :type="getRiskLevelTagType(strategy.prediction.riskLevel)" effect="dark">
                            {{ strategy.prediction.riskLevel }}风险
                          </el-tag>
                        </div>
                      </div>
                    </el-col>
                    <el-col :span="8">
                      <div class="prediction-item">
                        <div class="label">预计耗时</div>
                        <div class="value">{{ strategy.prediction.timeEstimate }}</div>
                      </div>
                    </el-col>
                    <el-col :span="24" class="evaluation">
                      <div class="label">综合评估</div>
                      <div class="evaluation-content">
                        {{ strategy.prediction.evaluation }}
                      </div>
                    </el-col>
                  </el-row>
                </div>
                
                <!-- 应用策略按钮 -->
                <div class="operations">
                  <el-button 
                    type="success" 
                    @click="applyStrategy(strategy)"
                  >
                    应用此策略
                  </el-button>
                  <el-button 
                    type="info" 
                    @click="simulateStrategy(strategy)"
                    :loading="strategy.simulating"
                  >
                    重新模拟
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<style scoped>
.strategy-container {
  padding: 20px;
}

.strategy-list h2 {
  margin: 0 0 20px; /* 修改顶部外边距，让标题直接显示在页面顶部 */
  color: #303133;
}

.strategy-card {
  margin-bottom: 20px;
}

.strategy-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.strategy-title {
  font-size: 16px;
  font-weight: bold;
}

.strategy-content {
  padding: 20px 0;
}

.description {
  margin-bottom: 20px;
  color: #606266;
  line-height: 1.6;
}

.actions {
  margin-bottom: 20px;
}

.actions h4,
.prediction h4 {
  margin-bottom: 15px;
  color: #303133;
}

.prediction {
  margin: 20px 0;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.prediction-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px dashed #e0e0e0;
}

.prediction-section:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.prediction-section h5 {
  margin: 0 0 15px;
  color: #303133;
  font-size: 14px;
}

.prediction-item {
  text-align: center;
  padding: 15px;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 15px;
}

.prediction-item .label {
  color: #909399;
  margin-bottom: 8px;
  font-size: 13px;
}

.prediction-item .value {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
}

.prediction-item .value.increase {
  color: #f56c6c;
}

.prediction-item .value.decrease {
  color: #67c23a;
}

.risk-level {
  margin-top: 10px;
}

.evaluation {
  margin-top: 15px;
}

.evaluation .label {
  color: #909399;
  margin-bottom: 8px;
  font-size: 13px;
}

.evaluation-content {
  padding: 15px;
  background-color: white;
  border-radius: 4px;
  color: #606266;
  line-height: 1.6;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.operations {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 15px;
}

:deep(.el-tag) {
  margin-right: 5px;
}

:deep(.el-table) {
  margin: 10px 0;
}

.gate-status-card {
  /* 移除此样式，但为了安全保留定义，只是不使用它 */
  display: none;
}
</style> 