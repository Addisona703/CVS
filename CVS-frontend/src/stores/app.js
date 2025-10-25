import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 侧边栏状态
  const sidebarCollapsed = ref(false)
  const sidebarVisible = ref(true) // 移动端侧边栏显示状态
  
  // 响应式状态
  const isMobile = ref(false)
  const isTablet = ref(false)
  const isDesktop = ref(false)
  
  // 主题设置
  const theme = ref('light')
  
  // 语言设置
  const locale = ref('zh-CN')
  
  // 页面加载状态
  const pageLoading = ref(false)
  
  // 计算属性：根据屏幕尺寸自动决定侧边栏状态
  const shouldCollapseSidebar = computed(() => {
    return isMobile.value || (isTablet.value && sidebarCollapsed.value)
  })
  
  // 动作
  const toggleSidebar = () => {
    if (isMobile.value) {
      // 移动端切换侧边栏显示/隐藏
      sidebarVisible.value = !sidebarVisible.value
    } else {
      // 桌面端和平板端切换折叠状态
      sidebarCollapsed.value = !sidebarCollapsed.value
    }
  }
  
  const setSidebarVisible = (visible) => {
    sidebarVisible.value = visible
  }
  
  const updateBreakpoints = (mobile, tablet, desktop) => {
    isMobile.value = mobile
    isTablet.value = tablet
    isDesktop.value = desktop
    
    // 移动端自动隐藏侧边栏
    if (mobile) {
      sidebarVisible.value = false
    } else {
      sidebarVisible.value = true
    }
  }
  
  const setTheme = (newTheme) => {
    theme.value = newTheme
    localStorage.setItem('theme', newTheme)
  }
  
  const setLocale = (newLocale) => {
    locale.value = newLocale
    localStorage.setItem('locale', newLocale)
  }
  
  const setPageLoading = (loading) => {
    pageLoading.value = loading
  }
  
  return {
    sidebarCollapsed,
    sidebarVisible,
    isMobile,
    isTablet,
    isDesktop,
    shouldCollapseSidebar,
    theme,
    locale,
    pageLoading,
    toggleSidebar,
    setSidebarVisible,
    updateBreakpoints,
    setTheme,
    setLocale,
    setPageLoading
  }
})
