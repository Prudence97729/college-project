
<template>
  <div class="double-map-display">
    <!-- 第一张地图（防洪工程总览） -->
    <div class="map-card">
      <img 
        src="@/assets/images/水闸位置图.png" 
        alt="防洪工程总览"
        class="map-image"
      >
      <div class="map-label">嘉善县防洪工程总布局</div>
    </div>

    <!-- 第二张地图 监测站测点图 -->
    <div class="map-card">
      <img 
        src="@/assets/images/监测站测点图.png" 
        alt="水闸细节"
        class="map-image"
      >
      <div class="map-label">水位测站位置分布图</div>
    </div>
  </div>
</template>

<style scoped>
.double-map-display {
  display: flex;
  gap: 20px;
  padding: 20px;
  flex-wrap: wrap;
  justify-content: center;
}

.map-card {
  width: 100%;
  max-width: 600px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.map-image {
  width: 100%;
  height: auto;
  display: block;
}

.map-label {
  padding: 12px;
  text-align: center;
  font-weight: 500;
  background: #f5f9ff;
  color: #005a80;
}
</style>