/**
 * 表格横向滚动提示 Composable
 * 基于 design.md 第 5 章节规范
 */

import { ref, onMounted, onUnmounted, nextTick } from 'vue'

export function useTableScroll(tableRef) {
  const showLeftShadow = ref(false)
  const showRightShadow = ref(false)
  
  // 检查滚动状态
  const checkScrollState = () => {
    if (!tableRef.value) return
    
    const element = tableRef.value
    const { scrollLeft, scrollWidth, clientWidth } = element
    
    // 左侧阴影：当有向左滚动空间时显示
    showLeftShadow.value = scrollLeft > 0
    
    // 右侧阴影：当有向右滚动空间时显示
    showRightShadow.value = scrollLeft < scrollWidth - clientWidth - 1
  }
  
  // 初始化滚动检查
  const initScrollCheck = async () => {
    await nextTick()
    checkScrollState()
  }
  
  // 滚动事件处理
  const handleScroll = () => {
    checkScrollState()
  }
  
  // 窗口大小变化处理
  const handleResize = () => {
    checkScrollState()
  }
  
  onMounted(() => {
    initScrollCheck()
    
    if (tableRef.value) {
      tableRef.value.addEventListener('scroll', handleScroll)
    }
    
    window.addEventListener('resize', handleResize)
  })
  
  onUnmounted(() => {
    if (tableRef.value) {
      tableRef.value.removeEventListener('scroll', handleScroll)
    }
    
    window.removeEventListener('resize', handleResize)
  })
  
  return {
    showLeftShadow,
    showRightShadow,
    checkScrollState,
    initScrollCheck
  }
}