<template>
  <div class="responsive-table-container" :class="{ 'mobile': isMobile }">
    <!-- 桌面端和平板端表格 -->
    <div 
      v-if="!isMobile" 
      ref="tableWrapper"
      class="table-wrapper"
      :class="{ 
        'scroll-left': showLeftShadow, 
        'scroll-right': showRightShadow 
      }"
    >
      <slot name="table"></slot>
    </div>
    
    <!-- 移动端卡片布局 -->
    <div v-else class="mobile-cards">
      <slot name="mobile-cards"></slot>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useResponsive } from '@/composables/useResponsive'
import { useTableScroll } from '@/composables/useTableScroll'

const { isMobile } = useResponsive()
const tableWrapper = ref(null)
const { showLeftShadow, showRightShadow, initScrollCheck } = useTableScroll(tableWrapper)

onMounted(() => {
  if (!isMobile.value) {
    initScrollCheck()
  }
})
</script>

<style lang="scss" scoped>
/**
 * 响应式表格组件
 * 基于 design.md 第 5 章节规范
 */

.responsive-table-container {
  position: relative;
  
  .table-wrapper {
    position: relative;
    overflow-x: auto;
    
    // 左侧滚动阴影
    &.scroll-left::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 10px;
      background: linear-gradient(to right, rgba(0, 0, 0, 0.1), transparent);
      pointer-events: none;
      z-index: 2;
    }
    
    // 右侧滚动阴影
    &.scroll-right::after {
      content: '';
      position: absolute;
      right: 0;
      top: 0;
      bottom: 0;
      width: 10px;
      background: linear-gradient(to left, rgba(0, 0, 0, 0.1), transparent);
      pointer-events: none;
      z-index: 2;
    }
    
    // 自定义滚动条样式
    &::-webkit-scrollbar {
      height: 6px;
    }
    
    &::-webkit-scrollbar-track {
      background: #f1f1f1;
      border-radius: 3px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: #c1c1c1;
      border-radius: 3px;
      
      &:hover {
        background: #a8a8a8;
      }
    }
  }
  
  // 移动端卡片布局
  .mobile-cards {
    display: flex;
    flex-direction: column;
    gap: var(--space-3, 12px);
  }
}

// 角色主题的滚动条颜色
body[data-role="admin"] .responsive-table-container {
  .table-wrapper {
    &.scroll-left::before,
    &.scroll-right::after {
      background: linear-gradient(to right, rgba(64, 158, 255, 0.1), transparent);
    }
    
    &.scroll-right::after {
      background: linear-gradient(to left, rgba(64, 158, 255, 0.1), transparent);
    }
  }
}

body[data-role="teacher"] .responsive-table-container {
  .table-wrapper {
    &.scroll-left::before,
    &.scroll-right::after {
      background: linear-gradient(to right, rgba(103, 194, 58, 0.1), transparent);
    }
    
    &.scroll-right::after {
      background: linear-gradient(to left, rgba(103, 194, 58, 0.1), transparent);
    }
  }
}

body[data-role="student"] .responsive-table-container {
  .table-wrapper {
    &.scroll-left::before,
    &.scroll-right::after {
      background: linear-gradient(to right, rgba(31, 107, 255, 0.1), transparent);
    }
    
    &.scroll-right::after {
      background: linear-gradient(to left, rgba(31, 107, 255, 0.1), transparent);
    }
  }
}
</style>