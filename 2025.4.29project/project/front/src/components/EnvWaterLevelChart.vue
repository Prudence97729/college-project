<template>
  <el-card class="water-level-card">
    <template #header>
      <div class="card-header">
        <h3>{{ stationLabel }}水位监测</h3>
        <div class="controls">
          <el-input-number
            v-model="hours"
            :min="1"
            :max="72"
            controls-position="right"
            placeholder="小时数"
            @change="loadData"
          /><!--@change="loadData":当值变化时触发 loadData 方法-->
          <span class="unit">小时</span>
        </div>
      </div>
    </template>
    
    <div class="chart-wrapper">
      <div ref="chartRef" class="water-level-chart"></div>
    </div>
    
    <div class="current-level">
      最新水位: {{ latestValue }}<span class="unit">m</span>
      <span v-if="loading" class="loading-text">(加载中...)</span>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { envWaterApi } from '@/api/hydro'
import { debounce } from 'lodash-es'
import { ElMessage } from 'element-plus'

const props = defineProps({
  stationName: {
    type: String,
    required: true
  },
  stationLabel: {
    type: String,
    required: true
  }
})

const chartRef = ref(null)
const hours = ref(6)//该子组件定义的小时
const latestValue = ref('--')
const loading = ref(false)
let chart = null

//暴露方法让父组件获取到当前的hours
const getHours =()=>hours.value;
defineExpose({getHours});//暴露给父组件

// 确保图表容器有正确尺寸
const ensureChartSize = () => {
  if (chartRef.value) {
    chartRef.value.style.height = '300px'
    chartRef.value.style.width = '100%'
  }
}

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return
  
  ensureChartSize()
  
  chart = echarts.init(chartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: params => {
        const data = params[0].data
        return `
          <div style="margin-bottom:5px;font-weight:bold">
            ${new Date(data[0]).toLocaleString()}
          </div>
          <div>水位: ${data[1].toFixed(2)}m</div>
        `
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'time',
      axisLabel: {
        formatter: value => {
          const date = new Date(value)
          return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '水位(m)'
    },
    series: [{
      name: '水位',
      type: 'line',
      showSymbol: true,
      symbolSize: 6,
      lineStyle: {
        width: 2,
        color: '#1890ff'
      },
      data: []
    }]
  }
  
  chart.setOption(option)
}

// 加载数据
const loadData = async () => {
  loading.value = true;
  try {
    const response = await envWaterApi.getWaterLevelData({
      stationName: props.stationName,
      hours: hours.value
    });

    // 调试：打印完整响应结构
    console.log('环境水位完整API响应:', response);

    // 获取实际数据和状态码
    const res = response.data; // 注意这里改为 response.data
    const apiData = res.data; // 实际数据数组
    const apiCode = res.code;  // 状态码

    console.log('处理前的数据结构检查:', {
      code: apiCode,
      dataLength: apiData?.length,
      firstItem: apiData?.[0],
      availableFields: apiData?.[0] ? Object.keys(apiData[0]) : null
    });

    if (apiCode === 200 && apiData?.length > 0) {
      // 使用固定的字段名，而不是props.stationName
      const timeField = 'timestamp'; // 时间字段名
      const valueField = 'waterLevel'; // 水位值字段名
      
      // 更健壮的数据验证
      const validData = apiData.filter(item => {
        try {
          return (
            item &&
            item[valueField] !== undefined &&
            item[valueField] !== null &&
            item[timeField] &&
            !isNaN(new Date(item[timeField]).getTime())
          );
        } catch (e) {
          console.warn('数据项验证失败:', item, e);
          return false;
        }
      });

      if (validData.length > 0) {
        const chartData = validData.map(item => [
          new Date(item[timeField]).getTime(),
          parseFloat(item[valueField])
        ]);
        
        latestValue.value = validData[0][valueField].toFixed(2);//第一项才是最晚时间
        updateChart(chartData);
      } else {
        console.error('有效数据为空，可能原因:', {
          requiredFields: [timeField, valueField],
          sampleItem: apiData[0],
          availableFields: Object.keys(apiData[0])
        });
        showNoDataTip();
        latestValue.value = '--';
        ElMessage.warning(`${props.stationLabel}没有有效数据`);
      }
    } else {
      showNoDataTip();
      latestValue.value = '--';
      ElMessage.error(res.message || `获取${props.stationLabel}数据失败`);
    }
  } catch (error) {
    console.error('数据加载失败:', {
      requestParams: {
        stationName: props.stationName,
        hours: hours.value
      },
      error: error.message,
      stack: error.stack
    });
    showNoDataTip();
    latestValue.value = '--';
    ElMessage.error(`加载${props.stationLabel}数据失败: ${error.message}`);
  } finally {
    loading.value = false;
  }
};

// 显示无数据提示
const showNoDataTip = () => {
  if (!chart) return
  
  chart.setOption({
    graphic: {
      type: 'text',
      left: 'center',
      top: 'middle',
      style: {
        text: '暂无监测数据',
        fontSize: 16,
        fill: '#999'
      }
    }
  })
}

// 更新图表
const updateChart = (data) => {
  if (!chart) {
    initChart()
  }
  
  nextTick(() => {
    chart.setOption({
      series: [{
        data: data
      }]
    })
    
    // 确保图表重新渲染
    chart.resize()
  })
}

// 处理窗口大小变化
const handleResize = debounce(() => {
  chart?.resize()
}, 300)

onMounted(() => {
  initChart()
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.water-level-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.controls {
  display: flex;
  align-items: center;
}

.unit {
  margin-left: 5px;
  font-size: 0.9em;
  color: #666;
}

.chart-wrapper {
  width: 100%;
  position: relative;
  margin: 20px 0;
}

.water-level-chart {
  width: 100%;
  height: 300px !important;
  min-height: 300px !important;
}

.current-level {
  margin-top: 15px;
  text-align: right;
  font-size: 16px;
  font-weight: bold;
}

.loading-text {
  font-size: 14px;
  color: #999;
  margin-left: 10px;
}
</style>