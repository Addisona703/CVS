<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useTheme } from '@/composables/useTheme'

// 应用入口组件
const { initTheme } = useTheme()

// 初始化主题系统
onMounted(() => {
  initTheme()
})
</script>

<style>
/* 基础样式已移至主题系统，这里只保留应用特定样式 */
#app {
  height: 100%;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: var(--border-color-lighter, #f1f1f1);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: var(--border-color-base, #c1c1c1);
  border-radius: 3px;
  transition: background var(--transition-duration, 0.3s) var(--transition-timing, ease);
}

::-webkit-scrollbar-thumb:hover {
  background: var(--text-color-secondary, #a8a8a8);
}

/* Element Plus 样式覆盖 - 使用主题变量 */
.el-card {
  border-radius: var(--border-radius-content, 8px);
  box-shadow: var(--card-shadow);
  transition: box-shadow var(--transition-duration, 0.3s) var(--transition-timing, ease);
}

.el-card:hover {
  box-shadow: var(--card-shadow-hover);
}

.el-button {
  border-radius: 6px;
  transition: all var(--transition-duration, 0.3s) var(--transition-timing, ease);
}

.el-button:active {
  transform: scale(0.95);
}

.el-input__wrapper {
  border-radius: 6px;
  transition: all var(--transition-duration, 0.3s) var(--transition-timing, ease);
}

.el-select .el-input__wrapper {
  border-radius: 6px;
}

/* 确保主题切换时的平滑过渡 */
* {
  transition-property: background-color, border-color, color, box-shadow;
  transition-duration: var(--transition-duration, 0.3s);
  transition-timing-function: var(--transition-timing, ease);
}
</style>
