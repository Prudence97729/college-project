<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

// 路由实例
const router = useRouter()

onMounted(() => {
  // 初始化应用程序
  console.log('应用程序已加载')
  
  // 全局事件监听或初始化代码可以放在这里
})
</script>

<template>
  <!-- 只需要 router-view -->
  <router-view></router-view>
</template>

<style>
/* 可以添加一些全局样式 */
body {
  margin: 0;
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: #f5f7fa;
}

#app {
  height: 100vh;
  width: 100%;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

