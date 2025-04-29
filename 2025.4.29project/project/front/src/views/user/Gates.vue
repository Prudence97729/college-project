<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search, Location } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { gateApi } from '@/api/gate'  // 导入gateApi
import { getGateMap } from '@/assets/gate_maps'

// 数据和加载状态
const gates = ref([])
const loading = ref(false)
const mapDialogVisible = ref(false)
const currentGate = ref(null)

// 获取水闸数据
const fetchGates = async () => {
  loading.value = true
  try {
    const res = await gateApi.getGateList()  // 使用gateApi
    if (res.code === 200) {
      gates.value = res.data
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
  fetchGates()
  // 调试水位测站位置
  setTimeout(() => {
    console.log("===== 调试水位测站位置 =====")
    debugStations()
  }, 1000)
})

// 搜索和筛选
const searchQuery = ref('')
const statusFilter = ref('')
const statusOptions = [
  { value: '', label: '全部状态' },
  { value: 'open', label: '开启' },
  { value: 'closed', label: '关闭' }
]

// 分页相关
const currentPage = ref(1)
const pageSize = ref(9)

const filteredGates = computed(() => {
  if (!gates.value) return []
  return gates.value.filter(gate => {
    const matchesSearch = gate.gateName?.toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchesStatus = statusFilter.value ? gate.status === statusFilter.value : true
    return matchesSearch && matchesStatus
  })
})

const paginatedGates = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredGates.value.slice(start, end)
})

const total = computed(() => filteredGates.value.length)

const handlePageChange = (page) => {
  currentPage.value = page
}

// 当前地图组件
const currentMapComponent = ref(null)

// 修改handleViewDetails函数
const handleViewDetails = async (gate) => {
  try {
    const res = await gateApi.getGateDetail(gate.id)
    if (res.code === 200) {
      currentGate.value = res.data
      currentMapComponent.value = getGateMap(gate.gateName)
      mapDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取水闸详情失败')
    }
  } catch (error) {
    console.error('获取水闸详情失败:', error)
    ElMessage.error('获取水闸详情失败')
  }
}

// 修改水闸和测站数据，只保留6个测站
const waterStations = ref([
  { id: 1, name: '大舜', location: [120.31, 31.58], waterLevel: '3.2m' },
  { id: 2, name: '陶庄', location: [120.32, 31.57], waterLevel: '2.8m' },
  { id: 3, name: '池家滨', location: [120.33, 31.59], waterLevel: '2.6m' },
  { id: 4, name: '天凝', location: [120.30, 31.56], waterLevel: '2.4m' },
  { id: 5, name: '西塘', location: [120.29, 31.55], waterLevel: '2.5m' },
  { id: 6, name: '横港大桥', location: [120.31, 31.53], waterLevel: '2.3m' }
])

// 定义水闸与测站的关联关系
const gateStationRelations = {
  '马斜湖枢纽': ['大舜', '池家滨'],
  '西塘港闸站': ['天凝'],
  '后场港闸': ['池家滨', '横港大桥'],
  '北祥符荡东口水闸': ['池家滨'],
  '港南浜水闸': ['陶庄'],
  '红菱塘水闸': ['陶庄']
}

// 获取水闸位置坐标（根据地图信息调整坐标）
const getGatePosition = (gateName) => {
  const positions = {
    '马斜湖枢纽': { x: 610, y: 200 },
    '西塘港闸站': { x: 290, y: 400 },
    '后场港闸': { x: 545, y: 230 },
    '北祥符荡东口水闸': { x: 485, y: 200 },
    '港南浜水闸': { x: 240, y: 330 },
    '红菱塘水闸': { x: 270, y: 340 }
  }
  
  // 模糊匹配水闸名称
  const normalizedName = gateName?.toLowerCase()
  for (const [key, pos] of Object.entries(positions)) {
    if (normalizedName?.includes(key.toLowerCase())) {
      return pos
    }
  }
  return { x: 400, y: 300 } // 默认位置
}

// 获取测站位置坐标（根据你提供的地图调整坐标）
const getStationPosition = (stationName) => {
  const positions = {
    '大舜': { x: 580, y: 180 },      // 位于地图右上方
    '陶庄': { x: 450, y: 200 },      // 位于中上方偏右
    '池家滨': { x: 520, y: 150 },    // 位于右上方
    '天凝': { x: 200, y: 450 },      // 位于左下方
    '西塘': { x: 290, y: 400 },      // 位于中下方偏左
    '横港大桥': { x: 350, y: 420 }   // 位于中下方
  }
  return positions[stationName] || { x: 400, y: 300 }
}

// 获取与当前水闸相关的测站
const getRelatedStations = (gateName) => {
  for (const [gate, stations] of Object.entries(gateStationRelations)) {
    if (gateName?.includes(gate)) {
      return waterStations.value.filter(station => 
        stations.includes(station.name)
      )
    }
  }
  return []
}

// 调试辅助函数 - 添加到控制台
const debugStations = () => {
  waterStations.value.forEach(station => {
    console.log(`测站: ${station.name}, 位置:`, getStationPosition(station.name))
  })
}
</script>

<template>
  <div class="gates-container" v-loading="loading">
    <!-- 搜索和筛选区域 -->
    <div class="search-filter">
      <el-input
        v-model="searchQuery"
        placeholder="搜索水闸名称"
        :prefix-icon="Search"
        clearable
        class="search-input"
      />
      <el-select 
        v-model="statusFilter" 
        placeholder="状态筛选"
        clearable
        class="status-filter"
      >
        <el-option
          v-for="option in statusOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
    </div>

    <!-- 水闸列表 -->
    <el-row :gutter="20">
      <el-col 
        :span="8" 
        v-for="gate in paginatedGates" 
        :key="gate.id"
        class="gate-col"
      >
        <el-card class="gate-card" shadow="hover">
          <template #header>
            <div class="gate-header">
              <span class="gate-name">{{ gate.gateName }}</span>
              <el-tag :type="gate.status === 'open' ? 'success' : 'danger'">
                {{ gate.status === 'open' ? '开启' : '关闭' }}
              </el-tag>
            </div>
          </template>
          
          <div class="gate-info">
            <!-- 基本信息 -->
            <div class="info-section">
              <div class="info-item">
                <span class="label">闸门编号：</span>
                <span class="value">{{ gate.gateCode }}</span>
              </div>
              <div class="info-item">
                <span class="label">闸门类型：</span>
                <span class="value">{{ gate.deviceType }}</span>
              </div>
              <div class="info-item">
                <span class="label">闸门数量：</span>
                <span class="value">{{ gate.gateCount }}个</span>
              </div>
              <div class="info-item">
                <span class="label">闸门宽度：</span>
                <span class="value">{{ gate.width }}m</span>
              </div>
              <div class="info-item">
                <span class="label">闸底高程：</span>
                <span class="value">{{ gate.sillElevation }}m</span>
              </div>
              <div class="info-item">
                <span class="label">闸门高度：</span>
                <span class="value">{{ gate.gateHeight }}m</span>
              </div>
              <div class="info-item">
                <span class="label">流量系数：</span>
                <span class="value">{{ gate.flowCoefficient }}</span>
              </div>
            </div>
            
            <!-- 添加详情按钮 -->
            <div class="action-section">
              <el-button 
                type="primary" 
                :icon="Location"
                @click="handleViewDetails(gate)"
              >
                查看地图位置
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分页器 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[6, 9, 12, 15]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="currentPage = 1"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 地图对话框 -->
    <el-dialog
      v-model="mapDialogVisible"
      :title="currentGate?.gateName + '位置信息'"
      width="80%"
      :close-on-click-modal="false"
    >
      <div class="map-container">
        <component 
          v-if="currentMapComponent" 
          :is="currentMapComponent"
          :gate-info="currentGate"
          :station-info="getRelatedStations(currentGate?.gateName)"
        />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.gates-container {
  padding: 20px;
  min-height: 400px;
}

.search-filter {
  margin-bottom: 20px;
  display: flex;
  gap: 20px;
}

.search-input {
  width: 300px;
}

.status-filter {
  width: 150px;
}

.gate-col {
  margin-bottom: 20px;
}

.gate-card {
  height: 100%;
  transition: all 0.3s;
}

.gate-card:hover {
  transform: translateY(-2px);
}

.gate-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.gate-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.gate-info {
  padding: 10px 0;
}

.info-section {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.info-item {
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  color: #909399;
}

.value {
  color: #409EFF;
  font-weight: 500;
}

.action-section {
  display: flex;
  justify-content: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.map-container {
  display: flex;
  gap: 20px;
  height: 500px;
}

.map-info {
  flex: 1;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
  overflow-y: auto;
}

.map-view {
  flex: 2;
  background-color: #f8f9fa;
  border-radius: 4px;
  overflow: hidden;
}

.map-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.simple-map {
  width: 100%;
  height: 100%;
  background-color: #f5f9fd;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.1);
}

.location-details {
  margin: 15px 0;
  padding: 15px;
  background-color: white;
  border-radius: 4px;
}

.stations-list {
  margin-top: 15px;
}

.station-item {
  margin-bottom: 15px;
  padding: 15px;
  background-color: white;
  border-radius: 4px;
}

.station-item:last-child {
  margin-bottom: 0;
}

.station-item h4 {
  margin: 0 0 10px 0;
  color: #409EFF;
}

.station-item p {
  margin: 5px 0;
  color: #606266;
}

h3 {
  margin: 20px 0 10px;
  color: #303133;
}

h3:first-child {
  margin-top: 0;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 响应式布局 */
@media (max-width: 1400px) {
  .el-col {
    width: 50% !important;
  }
}

@media (max-width: 768px) {
  .el-col {
    width: 100% !important;
  }
  
  .search-filter {
    flex-direction: column;
    gap: 10px;
  }
  
  .search-input,
  .status-filter {
    width: 100%;
  }

  .map-container {
    flex-direction: column;
  }
  
  .map-info,
  .map-view {
    flex: none;
  }
  
  .map-view {
    height: 300px;
  }
}
</style> 