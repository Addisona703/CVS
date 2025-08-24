import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 侧边栏状态
  const sidebarCollapsed = ref(false)
  
  // 主题设置
  const theme = ref('light')
  
  // 语言设置
  const locale = ref('zh-CN')
  
  // 页面加载状态
  const pageLoading = ref(false)
  
  // 动作
  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
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
    theme,
    locale,
    pageLoading,
    toggleSidebar,
    setTheme,
    setLocale,
    setPageLoading
  }
})
