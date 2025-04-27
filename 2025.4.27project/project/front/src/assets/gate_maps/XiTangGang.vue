<template>
  <div class="map-container">
    <!-- 左侧信息面板 -->
    <div class="info-panel">
      <h3>{{ gateInfo?.gateName }}</h3>
      <div class="info-section">
        <h4>水闸信息</h4>
        <p><strong>闸门编号：</strong>{{ gateInfo?.gateCode }}</p>
        <p><strong>闸门类型：</strong>{{ gateInfo?.deviceType }}</p>
        <p><strong>闸门数量：</strong>{{ gateInfo?.gateCount }}个</p>
        <p><strong>闸门宽度：</strong>{{ gateInfo?.width }}m</p>
        <p><strong>闸底高程：</strong>{{ gateInfo?.sillElevation }}m</p>
        <p><strong>闸门高度：</strong>{{ gateInfo?.gateHeight }}m</p>
        <p><strong>流量系数：</strong>{{ gateInfo?.flowCoefficient }}</p>
      </div>

      <div class="info-section">
        <h4>相关水位测站</h4>
        <div v-for="station in stationInfo" :key="station.id" class="station-item">
          <h5>{{ station.name }}</h5>
          <p><strong>当前水位：</strong>{{ station.waterLevel }}</p>
        </div>
      </div>
    </div>

    <!-- 优化后的SVG地图 -->
    <svg width="100%" height="100%" viewBox="0 0 800 506" xmlns="http://www.w3.org/2000/svg">
      <!-- 背景 -->
      <rect x="0" y="0" width="800" height="506" fill="#f5f7fa" />
      
      <!-- 渐变定义 -->
      <defs>
        <linearGradient id="riverGradient" x1="0%" y1="0%" x2="100%" y2="0%">
          <stop offset="0%" stop-color="#a5d7f7"/>
          <stop offset="100%" stop-color="#3da0db"/>
        </linearGradient>
        <filter id="dropShadow">
          <feGaussianBlur in="SourceAlpha" stdDeviation="2"/>
          <feOffset dx="1" dy="1"/>
          <feComponentTransfer>
            <feFuncA type="linear" slope="0.3"/>
          </feComponentTransfer>
          <feMerge>
            <feMergeNode/>
            <feMergeNode in="SourceGraphic"/>
          </feMerge>
        </filter>
      </defs>

      <!-- 主河道 -->
      <path 
        d="m349,224c40,0 80,-5 120,0c40,5 80,-5 120,0c40,5 80,-5 120,0l20,0l0,12c-60,0 -100,-5 -140,0c-40,5 -80,-5 -120,0c-40,5 -80,-5 -120,0l0,-12z"
        transform="rotate(-8.82589 539 229.611)"
        fill="url(#riverGradient)"
        stroke="#2a7fcc"
        stroke-width="1"
        filter="url(#dropShadow)"
      />

      <!-- 支流 -->
      <path 
        d="m175.88958,224.71665c34.73502,0 69.47004,-4.51735 104.20506,0c34.73502,4.51735 69.47004,-4.51735 104.20506,0c34.73502,4.51735 69.47004,-4.51735 104.20506,0l17.36751,0l0,10.84165c-52.10253,0 -86.83755,-4.51735 -121.57258,0c-34.73502,4.51735 -69.47004,-4.51735 -104.20506,0c-34.73502,4.51735 -69.47004,-4.51735 -104.20506,0l0,-10.84165z"
        transform="rotate(-98.893 340.881 229.786)"
        fill="url(#riverGradient)"
        stroke="#2a7fcc"
        stroke-width="1"
        filter="url(#dropShadow)"
      />

      <!-- 水闸 -->
      <g>
        <circle 
          cx="356.5" 
          cy="244.9" 
          r="8.5"
          fill="#E6A23C"
          stroke="#fff"
          stroke-width="2"
        />
        <text 
          x="370" 
          y="234.4"
          font-family="Microsoft YaHei"
          font-size="14"
          fill="#303133"
          font-weight="bold"
        >西塘港闸站改建（二期）</text>
      </g>

      <!-- 水位站 -->
      <g>
        <!-- 天凝水位站 -->
        <circle 
          cx="372" 
          cy="363.9" 
          r="8"
          fill="#409eff"
          stroke="#fff"
          stroke-width="2"
        />
        <text 
          x="385" 
          y="365.4"
          font-family="Microsoft YaHei"
          font-size="12"
          fill="#409eff"
        >天凝</text>
      </g>

      <!-- 河流名称 -->
      <text 
        x="514" 
        y="250.4"
        font-family="Microsoft YaHei"
        font-size="14"
        fill="#3da0db"
        font-weight="bold"
      >西塘港</text>

      <!-- 图例 -->
      <g transform="translate(20, 440)">
        <circle cx="10" cy="0" r="8" fill="#E6A23C" stroke="#fff" stroke-width="1.5" />
        <text x="30" y="4" fill="#606266" font-size="14" font-family="Microsoft YaHei">水闸</text>
        
        <circle cx="10" cy="30" r="6" fill="#409eff" stroke="#fff" stroke-width="1.5" />
        <text x="30" y="34" fill="#606266" font-size="14" font-family="Microsoft YaHei">水位测站</text>

        <rect x="5" y="55" width="20" height="8" fill="url(#riverGradient)" />
        <text x="30" y="64" fill="#606266" font-size="14" font-family="Microsoft YaHei">河道</text>
      </g>
    </svg>
  </div>
</template>

<script setup>
defineProps({
  gateInfo: {
    type: Object,
    required: true
  },
  stationInfo: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
.map-container {
  display: flex;
  gap: 20px;
  height: 100%;
}

.info-panel {
  width: 280px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
  overflow-y: auto;
}

.info-section {
  margin-bottom: 20px;
  padding: 15px;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

h3 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 18px;
  font-weight: bold;
}

h4 {
  margin: 0 0 15px 0;
  color: #409EFF;
  font-size: 16px;
}

h5 {
  margin: 0 0 10px 0;
  color: #409EFF;
  font-size: 14px;
}

p {
  margin: 8px 0;
  color: #606266;
  font-size: 14px;
}

strong {
  color: #303133;
}

.station-item {
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.station-item:last-child {
  margin-bottom: 0;
}

svg {
  flex: 1;
  max-width: calc(100% - 300px);
  height: auto;
  background-color: #f5f7fa;
  border-radius: 4px;
}
</style> 