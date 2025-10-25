/**
 * 响应式断点管理 Composable
 * 基于 design.md 第 5 章节规范
 */

import { ref, onMounted, onUnmounted } from 'vue'

// 断点定义
const BREAKPOINTS = {
  mobile: 768,
  tablet: 1024
}

export function useResponsive() {
  const windowWidth = ref(0)
  const isMobile = ref(false)
  const isTablet = ref(false)
  const isDesktop = ref(false)
  
  // 更新断点状态
  const updateBreakpoints = () => {
    windowWidth.value = window.innerWidth
    isMobile.value = windowWidth.value < BREAKPOINTS.mobile
    isTablet.value = windowWidth.value >= BREAKPOINTS.mobile && windowWidth.value < BREAKPOINTS.tablet
    isDesktop.value = windowWidth.value >= BREAKPOINTS.tablet
  }
  
  // 监听窗口大小变化
  const handleResize = () => {
    updateBreakpoints()
  }
  
  onMounted(() => {
    updateBreakpoints()
    window.addEventListener('resize', handleResize)
  })
  
  onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
  })
  
  return {
    windowWidth,
    isMobile,
    isTablet,
    isDesktop,
    breakpoints: BREAKPOINTS
  }
}