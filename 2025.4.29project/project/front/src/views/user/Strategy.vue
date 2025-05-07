<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { gateApi } from '@/api/gate'  // 导入水闸API
import request from '@/utils/request'
import EnvWaterLevelChart from '@/components/EnvWaterLevelChart.vue'
// 水闸列表数据
const gates = ref([])
const loading = ref(false)
const strategies = ref([]) //从后端获取策略数据

// 水位测站数据
const stations = ref([
  //{ id: 1, name: '大舜' },
  //{ id: 2, name: '陶庄' },//这两个是环境水位信息
  { id: 1, name: '池家浜' },
  { id: 2, name: '天凝' },
  { id: 3, name: '西塘' },
  { id: 4, name: '横港大桥' }
])


// 选中的目标
const selectedStation = ref(null)
const selectedTarget = ref(null)//记录用户设置的目标水位值

//把Env子组件变成变量
const dashunChart = ref(null);
const henggangChart = ref(null);

// 是否已生成策略
const isStrategyGenerated = ref(false)

// 添加水位信息状态
const currentWaterLevel = ref(null)

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

onMounted(() => {
  fetchGateList();
});

// 生成策略的方法
const generateStrategy = async () => {
  if (!selectedStation.value || !selectedTarget.value) {
    ElMessage.warning('请先选择水位测站和目标值')
    return
  }

  try {
    // 获取选中测站的完整信息
    const selectedStationInfo = stations.value.find(s => s.id === selectedStation.value)
    // 调用后端接口生成策略
    const res = await request({
      url: '/strategy/generate',
      method: 'post',
      data: {
        stationId: selectedStationInfo.id,
        stationName: selectedStationInfo.name,  // 添加测站名称
        targetValue: selectedTarget.value ,//把目标水位值直接传入后端
        dashunHours:dashunChart.value?.getHours(),//用户选择的大舜环境水位小时
        henggangHours:henggangChart.value?.getHours()//用户选择的横岗大桥环境水位小时
      }
    })

    if (res.code === 200) {
      strategies.value = res.data
      isStrategyGenerated.value = true
      ElMessage.success('策略生成成功')
    }
  } catch (error) {
    console.error('生成策略失败:', error)
    ElMessage.error('生成策略失败')
  }
}

// 模拟策略的方法
const simulateStrategy = async (strategy) => {
  try {
    // 获取选中测站的完整信息，向后端传入名称和目标值信息
    const selectedStationInfo = stations.value.find(s => s.id === selectedStation.value)
    strategy.simulating = true;//标记正在模拟
    const res = await request({
      url: '/strategy/simulate',
      method: 'post',
      data: {
        strategy,
        stationName: selectedStationInfo.name,  // 添加测站名称
        targetValue: selectedTarget.value
      }
    })
    if (res.code === 200) {
      console.log("prediction赋值前 res 对象:", res.data)
      const expectedWaterLevel = res.data.expectedWaterLevel//预计水位
      // 把模拟结果赋值给strategy
      strategy.prediction = {
        waterLevel: {
          [stations.value.find(s => s.id === selectedStation.value)?.name]: 
            res.data.expectedWaterLevel
        },
        riskLevel: res.data.energyCost,
        timeEstimate: res.data.timeToReach,
        evaluation: expectedWaterLevel > 5.0 ? `⚠️ 危险水位：${expectedWaterLevel.toFixed(2)}m (此水位超过防洪警戒线，需警惕此策略)` :
           expectedWaterLevel > 3.5 ? `🔶 较高水位：${expectedWaterLevel.toFixed(2)}m (接近安全上限，建议监测)` :
           expectedWaterLevel < 1.0 ? `⚠️ 缺水水位：${expectedWaterLevel.toFixed(2)}m (低于生态水位，可能影响供水,灌溉效率可能下降)` :
           `✅ 安全水位：${expectedWaterLevel.toFixed(2)}m`
      };
      console.log("prediction赋值后 strategy 对象:", strategy);
      ElMessage.success('模拟完成')
      // TODO: 展示模拟结果
    }
  } catch (error) {
    console.error('策略模拟失败:', error)
    ElMessage.error('策略模拟失败')
  } finally{
    strategy.simulating = false;//表示模拟完成
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
  const num = Number(value).toFixed(2)
  return `${num}m`
}


// 获取当前水位测站水位信息，只传stationName，后端返回最新水位信息
const fetchWaterLevel = async () => {
  if (!selectedStation.value || !selectedTarget.value) {
    ElMessage.warning('请先选择水位测站和目标值')
    return
  }
  //selectedStation.value里面存储的是id，需要根据stations找到id对应的水闸名称
  const selectedStationInfo = stations.value.find(s => s.id === selectedStation.value)
  try {
    const res = await request({
      url: '/water-level/latest',
      method: 'get',
      params: {stationName: selectedStationInfo.name}
    })
    if (res.code === 200) {
      currentWaterLevel.value = res.data.waterLevel
    }
  } catch (error) {
    console.error('获取水位信息失败:', error)
  }
}


</script>

<template>
  <div class="strategy-container">
      <!-- 新增：环境水位信息展示模块 -->
    <el-card class="water-level-display">
      <template #header>
        <div class="card-header">
          <span>环境水位信息</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="12">
          <EnvWaterLevelChart 
            ref="dashunChart"
            station-name="大舜"
            station-label="大舜"
          />
        </el-col>
        <el-col :span="12">
          <EnvWaterLevelChart 
            ref="henggangChart"
            station-name="横港大桥"
            station-label="横港大桥" 
          />
        </el-col>
      </el-row>
    
    </el-card>

    <!-- 目标选择部分 -->
    <el-card class="target-selection">
      <template #header>
        <div class="card-header">
          <span>调度目标设置</span>
        </div>
      </template>
      
      <el-form :model="form" label-width="120px">
        <el-form-item label="选择水位测站">
          <el-select v-model="selectedStation" placeholder="请选择水位测站">
            <el-option
              v-for="station in stations"
              :key="station.id"
              :label="station.name"
              :value="station.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="输入目标值">
          <el-input
            v-model.number="selectedTarget"
            type="number"
            placeholder="请输入目标水位值（单位：米）"
            :min="0"
            :max="10"
            :step="0.1"
          >
          </el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="() => { generateStrategy(); fetchWaterLevel() }">生成策略</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 策略列表部分 -->
    <el-card v-if="isStrategyGenerated" class="strategy-list">
      <template #header>
        <div class="card-header">
          <span>推荐策略</span>
        </div>
      </template>
      
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
                
                <!-- 水位变化预测 -->
                <div class="prediction-section">
                  <h5>水位变化预测：</h5>
                  <el-row :gutter="20">
                    <el-col :span="8">
                      <div class="prediction-item">
                        <div class="label">当前水位</div>
                        <div class="value">
                          {{ currentWaterLevel ? `${currentWaterLevel.toFixed(2)}m` : '暂无数据' }}
                        </div>
                        <div class="water-level-change">
                          <el-tag type="info" size="small">基准值</el-tag>
                        </div>
                      </div>
                    </el-col>
                    <el-col :span="8">
                      <div class="prediction-item">
                        <div class="label">预计水位</div>
                        <div class="value" :class="getWaterLevelClass(strategy.prediction.waterLevel[Object.keys(strategy.prediction.waterLevel)[0]] - currentWaterLevel)">
                          {{ formatWaterLevelChange(strategy.prediction.waterLevel[Object.keys(strategy.prediction.waterLevel)[0]]) }}
                        </div>
                        <div class="water-level-change">
                          <el-tag :type="getWaterLevelClass(strategy.prediction.waterLevel[Object.keys(strategy.prediction.waterLevel)[0]] - currentWaterLevel) === 'increase' ? 'danger' : 'success'" size="small">
                            {{ formatWaterLevelChange(strategy.prediction.waterLevel[Object.keys(strategy.prediction.waterLevel)[0]] - currentWaterLevel) }}
                          </el-tag>
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
                
                <!-- 应用策略按钮，暂时还不用 -->
                <div class="operations">
                  <!-- <el-button 
                    type="success" 
                    @click="applyStrategy(strategy)"
                  >
                    应用此策略
                  </el-button> -->
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
    </el-card>
  </div>
</template>

<style scoped>
.strategy-container {
  padding: 20px;
}

/* 环境水位信息卡片 */
.water-level-display {
  margin-bottom: 20px; /* 与下方卡片的间距 */
}

.target-selection {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-form {
  max-width: 500px;
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
  height: 120px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.prediction-item .label {
  color: #909399;
  margin-bottom: 8px;
  font-size: 13px;
}

.prediction-item .value,
.prediction-item .risk-level {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin: 8px 0;
  flex-grow: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.prediction-item .value.increase {
  color: #f56c6c;
}

.prediction-item .value.decrease {
  color: #67c23a;
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
  display: none;
}

.water-level-change {
  margin-top: 8px;
  font-size: 12px;
  min-height: 24px; /* 确保标签区域高度一致 */
  display: flex;
  justify-content: center;
}
</style>