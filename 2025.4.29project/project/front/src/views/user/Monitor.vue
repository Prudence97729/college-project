<script setup>
import WaterLevelChart from '@/components/WaterLevelChart.vue'

</script>

<template>
  <div class="monitor-container">
    <el-row :gutter="20">
      <!-- 水位图表 -->
      <el-col :span="24">
        <WaterLevelChart />
      </el-col>
      
    </el-row>
  </div>
</template>

<style scoped>
.monitor-container {
  padding: 20px;
}
</style> 