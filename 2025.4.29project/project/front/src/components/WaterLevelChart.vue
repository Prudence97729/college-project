<template>
  <div class="chart-container">
    <el-card v-loading="loading" class="chart-card">
      <template #header>
        <div class="chart-header">
          <h3>水位变化趋势图</h3>
          <div class="chart-controls">
            <el-select 
              v-model="selectedLocation" 
              placeholder="选择监测点" 
              class="location-select"
            >
              <el-option 
                v-for="loc in locations" 
                :key="loc.value" 
                :label="loc.label" 
                :value="loc.value"
              />
            </el-select>
            <el-select 
              v-model="timeRange" 
              placeholder="时间范围"
              class="time-select"
            >
              <el-option label="单日数据" value="1d" />
              <el-option label="三日数据" value="3d" />
              <el-option label="全部数据" value="all" />
            </el-select>
            
            <el-date-picker
              v-if="timeRange === '1d' || timeRange === '3d'"
              v-model="selectedDate"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              :disabled-date="disabledDate"
              class="date-picker"
            />
          </div>
        </div>
      </template>
      <div class="chart" ref="chartRef"></div>
    </el-card>
    
    <!-- 数据表格卡片 -->
    <el-card class="data-table-card">
      <template #header>
        <div class="table-header">
          <h3>水位数据详情</h3>
        </div>
      </template>
      
      <el-table
        :data="tableData"
        stripe
        border
        height="500"
        style="width: 100%"
      >
        <el-table-column
          prop="monitoringTime"
          label="监测时间"
          width="180"
          :formatter="formatTime"
        />
        <el-table-column
          prop="value"
          label="水位(m)"
          width="120"
          :formatter="formatValue"
        />
        <el-table-column
          prop="trend"
          label="变化趋势"
          width="120"
        >
          <template #default="scope">
            <el-tag :type="getTrendType(scope.row.trend)">
              {{ formatTrend(scope.row.trend) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="备注"
          prop="remark"
        />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted, computed } from 'vue'
import * as echarts from 'echarts'
import { hydroApi } from '@/api/hydro'
import { ElMessage } from 'element-plus'
import { debounce } from 'lodash-es'  // 添加 debounce 函数
import { formatTime as formatTimeUtil } from '@/utils/format'

const chartRef = ref(null)
const timeRange = ref('1d')
const loading = ref(false)
const selectedLocation = ref('xitang_main')
const selectedDate = ref(new Date('2024-09-20').toISOString().split('T')[0])
let chart = null

// 数据日期范围
const dataStartDate = new Date('2024-09-13')
const dataEndDate = new Date('2024-09-20')

// 禁用数据范围外的日期
const disabledDate = (time) => {
  const date = new Date(time)
  return date < dataStartDate || date > dataEndDate
}

// 定义监测点列表 - 移到组件外部避免重复创建
const locations = [
  { label: '西塘(主)', value: 'xitang_main' },
  { label: '横港大桥(主)', value: 'henggang_bridge_main' },
  { label: '陶庄(主)', value: 'taozhuang_main' },
  { label: '丁栅(主)', value: 'dingzha_main' },
  { label: '池家浜(主)', value: 'chijia_bang_main' },
  { label: '天凝(主)', value: 'tianning_main' },
  { label: '大舜', value: 'dashun' }
]

// 修改字段映射表
const fieldMapping = {
  'xitang_main': 'xitangMain',
  'henggang_bridge_main': 'henggangBridgeMain',
  'taozhuang_main': 'taozhuangMain',
  'dingzha_main': 'dingshaMain',
  'chijia_bang_main': 'chijiaBangMain',
  'tianning_main': 'tianningMain',
  'dashun': 'dashun'
}

// 使用防抖处理数据加载
const loadData = debounce(async () => {
  if (loading.value) return
  
  loading.value = true
  try {
    const params = {
      timeRange: timeRange.value,//时间范围
      location: selectedLocation.value//所选测站
    }
    
    if ((timeRange.value === '1d' || timeRange.value === '3d') && selectedDate.value) {
      params.date = selectedDate.value//所选具体日期
    }
    
    const res = await hydroApi.getWaterLevelData(params)//获取水位数据
    
    if (res.data.code === 200) {
      updateChart(res.data.data)
    } else {
      ElMessage.error(res.data.message || '获取数据失败')
    }
  } catch (error) {
    console.error('Load data error:', error)
    ElMessage.error('获取数据失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}, 300)  // 300ms 的防抖延迟

// 表格相关的数据
const currentPage = ref(1)
const pageSize = ref(20)
const totalItems = ref(0)
const tableData = ref([])

// 修改 updateChart 函数
const updateChart = (data) => {
  if (!chart) return
  
  const locationLabel = locations.find(loc => loc.value === selectedLocation.value)?.label || ''
  const backendField = fieldMapping[selectedLocation.value]
  
  // 添加调试信息
  if (data.length > 0) {
    console.log('数据示例:', {
      原始数据: data[0],
      字段名: backendField,
      字段值: data[0][backendField]
    })
  }
  
  const chartData = data
    .filter(item => {
      const value = item[backendField]
      const isValid = value != null && !isNaN(parseFloat(value))
      if (!isValid) {
        console.log('无效数据:', {
          时间: item.monitoringTime,
          字段名: backendField,
          值: value
        })
      }
      return isValid
    })
    .map(item => [
      new Date(item.monitoringTime).getTime(),
      Number(parseFloat(item[backendField]).toFixed(2))
    ])
  
  if (chartData.length === 0) {
    ElMessage.warning(`${locationLabel}在所选时间范围内没有数据`)
    chart.setOption({
      title: {
        text: `${locationLabel}水位变化趋势`,
        left: 'center'
      },
      series: [{
        type: 'line',
        data: [],
        name: '水位'
      }]
    }, true)
    return
  }
  
  const values = chartData.map(item => item[1])
  const minValue = Number((Math.floor(Math.min(...values) * 10) / 10).toFixed(1))
  const maxValue = Number((Math.ceil(Math.max(...values) * 10) / 10).toFixed(1))
  
  chart.setOption({
    title: {
      text: `${locationLabel}水位变化趋势`,
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      formatter: params => {
        const data = params[0]
        return `时间：${new Date(data.value[0]).toLocaleString()}<br/>
                水位：${data.value[1].toFixed(2)}m`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'time',
      axisLabel: {
        formatter: function (value) {
          const date = new Date(value)
          const month = (date.getMonth() + 1).toString().padStart(2, '0')
          const day = date.getDate().toString().padStart(2, '0')
          const hours = date.getHours().toString().padStart(2, '0')
          const minutes = date.getMinutes().toString().padStart(2, '0')
          return `${hours}:${minutes}\n${month}-${day}`  // 显示为两行
        },
        interval: 'auto',  // 自动计算间隔
        rotate: 0
      },
      splitLine: {
        show: true,
        lineStyle: { type: 'dashed' }
      }
    },
    yAxis: {
      type: 'value',
      name: '水位(m)',
      min: minValue,
      max: maxValue,
      interval: 0.1,
      axisLabel: { 
        formatter: (value) => value.toFixed(1)
      },
      splitLine: {
        show: true,
        lineStyle: { type: 'dashed' }
      }
    },
    series: [{
      name: '水位',
      type: 'line',
      smooth: true,
      showSymbol: true,
      symbolSize: 6,
      data: chartData,
      animation: false,
      itemStyle: { color: '#1890ff' },
      lineStyle: { width: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(24,144,255,0.3)' },
          { offset: 1, color: 'rgba(24,144,255,0.1)' }
        ])
      }
    }]
  }, true)
  
  // 更新表格数据
  const processedData = data.map((item, index, array) => {
    const value = parseFloat(item[backendField])
    let trend = 0
    if (index > 0) {
      const prevValue = parseFloat(array[index - 1][backendField])
      trend = value - prevValue
    }
    
    return {
      monitoringTime: item.monitoringTime,
      value: value,
      trend: trend,
      remark: getTrendRemark(trend)
    }
  })
  
  tableData.value = processedData
  totalItems.value = processedData.length
}

// 初始化图表
const initChart = () => {
  if (chart) return
  
  chart = echarts.init(chartRef.value)
  chart.setOption({
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'time',
      axisLabel: {
        formatter: function (value) {
          const date = new Date(value)
          const month = (date.getMonth() + 1).toString().padStart(2, '0')
          const day = date.getDate().toString().padStart(2, '0')
          const hours = date.getHours().toString().padStart(2, '0')
          const minutes = date.getMinutes().toString().padStart(2, '0')
          return `${hours}:${minutes}\n${month}-${day}`  // 显示为两行
        },
        interval: 'auto',  // 自动计算间隔
        rotate: 0
      },
      splitLine: {
        show: true,
        lineStyle: { type: 'dashed' }
      }
    },
    yAxis: {
      type: 'value',
      name: '水位(m)',
      interval: 0.1,
      axisLabel: { formatter: '{value}' },
      splitLine: {
        show: true,
        lineStyle: { type: 'dashed' }
      }
    },
    series: [{
      name: '水位',
      type: 'line',
      smooth: true,
      showSymbol: true,
      symbolSize: 6,
      itemStyle: { color: '#1890ff' },
      lineStyle: { width: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(24,144,255,0.3)' },
          { offset: 1, color: 'rgba(24,144,255,0.1)' }
        ])
      }
    }]
  })
  
  // 使用防抖处理窗口调整
  const handleResize = debounce(() => chart?.resize(), 100)
  window.addEventListener('resize', handleResize)
  
  // 组件卸载时清理
  onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
    chart?.dispose()
    chart = null
  })
}

// 监听数据变化
watch([timeRange, selectedLocation, selectedDate], loadData)

// 组件挂载时初始化
onMounted(() => {
  initChart()
  loadData()
})

// 格式化时间
const formatTime = (row, column) => {
  return formatTimeUtil(row.monitoringTime)
}

// 格式化水位值
const formatValue = (row, column) => {
  return row.value.toFixed(2)
}

// 获取趋势类型
const getTrendType = (trend) => {
  if (trend > 0) return 'danger'
  if (trend < 0) return 'success'
  return 'info'
}

// 格式化趋势
const formatTrend = (trend) => {
  if (trend > 0) return `上升 ${trend.toFixed(2)}m`
  if (trend < 0) return `下降 ${Math.abs(trend).toFixed(2)}m`
  return '持平'
}

// 获取趋势备注
const getTrendRemark = (trend) => {
  if (Math.abs(trend) > 0.5) return '水位变化显著'
  if (Math.abs(trend) > 0.2) return '水位变化中等'
  return '水位变化平稳'
}
</script>

<style scoped>
.chart-container {
  padding: 20px;
}

.chart {
  height: 400px;
  width: 100%;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-header h3 {
  margin: 0;
}

.chart-controls {
  display: flex;
  gap: 10px;
  align-items: center;
}

/* 添加固定宽度 */
.location-select {
  width: 150px;  /* 监测点选择器宽度 */
}

.time-select {
  width: 120px;  /* 时间范围选择器宽度 */
}

.date-picker {
  width: 130px;  /* 日期选择器宽度 */
}

/* 确保在小屏幕上也能正常显示 */
@media (max-width: 768px) {
  .chart-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .chart-controls {
    width: 100%;
    flex-wrap: wrap;
  }
  
  .location-select,
  .time-select,
  .date-picker {
    width: 100%;
  }
}

.chart-card {
  margin-bottom: 20px;
}

.data-table-card {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-header h3 {
  margin: 0;
}

/* 确保在小屏幕上表格也能正常显示 */
@media (max-width: 768px) {
  .el-table {
    width: 100%;
    overflow-x: auto;
  }
}
</style> 