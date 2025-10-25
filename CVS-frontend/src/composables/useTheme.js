/**
 * 主题管理 Composable
 * 基于 design.md 第 11 章节规范
 */

import { ref, computed, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'

export function useTheme() {
  const authStore = useAuthStore()
  
  // 深色模式状态
  const isDarkMode = ref(localStorage.getItem('cvs-theme') === 'dark')
  
  // 当前用户角色
  const currentRole = computed(() => {
    const role = authStore.userRole
    // 将后端角色映射到主题角色
    switch (role) {
      case 'ADMIN':
      case 'admin':
        return 'admin'
      case 'TEACHER':
      case 'teacher':
        return 'teacher'
      case 'STUDENT':
      case 'student':
        return 'student'
      default:
        return null
    }
  })
  
  // 应用主题到 DOM
  const applyTheme = (role, darkMode = false) => {
    const body = document.body
    
    // 设置角色主题
    if (role) {
      body.setAttribute('data-role', role)
    } else {
      body.removeAttribute('data-role')
    }
    
    // 设置深色模式
    if (darkMode) {
      body.setAttribute('data-theme', 'dark')
    } else {
      body.removeAttribute('data-theme')
    }
    
    // 更新 Element Plus 主题变量
    updateElementPlusTheme(role, darkMode)
  }
  
  // 更新 Element Plus 主题变量
  const updateElementPlusTheme = (role, darkMode) => {
    const root = document.documentElement
    
    // 根据角色设置主题色配置
    const themeConfigs = {
      admin: {
        primary: '#409eff',
        lightVariants: {
          'light-3': '#79bbff',
          'light-5': '#a0cfff',
          'light-7': '#c6e2ff',
          'light-8': '#d9ecff',
          'light-9': '#ecf5ff'
        },
        dark: '#337ecc'
      },
      teacher: {
        primary: '#67c23a',
        lightVariants: {
          'light-3': '#85ce61',
          'light-5': '#a4da89',
          'light-7': '#c2e7b0',
          'light-8': '#d1edc4',
          'light-9': '#e1f3d8'
        },
        dark: '#529b2e'
      },
      student: {
        primary: '#1f6bff',
        lightVariants: {
          'light-3': '#4d85ff',
          'light-5': '#7aa0ff',
          'light-7': '#a6baff',
          'light-8': '#bfccff',
          'light-9': '#d9ddff'
        },
        dark: '#1956cc'
      }
    }
    
    const config = themeConfigs[role] || themeConfigs.admin
    
    // 设置 Element Plus 主色调
    root.style.setProperty('--el-color-primary', config.primary)
    
    // 设置主色调的浅色变体
    Object.entries(config.lightVariants).forEach(([variant, color]) => {
      root.style.setProperty(`--el-color-primary-${variant}`, color)
    })
    
    // 设置主色调的深色变体
    root.style.setProperty('--el-color-primary-dark-2', config.dark)
    
    // 设置其他 Element Plus 组件相关的颜色变量
    // 按钮相关
    root.style.setProperty('--el-button-hover-bg-color', config.lightVariants['light-3'])
    root.style.setProperty('--el-button-active-bg-color', config.dark)
    
    // 输入框相关
    root.style.setProperty('--el-input-focus-border-color', config.primary)
    root.style.setProperty('--el-input-hover-border-color', config.lightVariants['light-5'])
    
    // 菜单相关
    root.style.setProperty('--el-menu-active-color', config.primary)
    root.style.setProperty('--el-menu-hover-bg-color', config.lightVariants['light-9'])
    
    // 链接相关
    root.style.setProperty('--el-color-primary-light-hover', config.lightVariants['light-3'])
    
    // 边框圆角 - 统一设置为 6px
    root.style.setProperty('--el-border-radius-base', '6px')
    root.style.setProperty('--el-border-radius-small', '4px')
    root.style.setProperty('--el-border-radius-large', '8px')
    
    // 过渡动画时长
    root.style.setProperty('--el-transition-duration', '0.3s')
    root.style.setProperty('--el-transition-duration-fast', '0.2s')
  }
  
  // 切换深色模式
  const toggleDarkMode = () => {
    isDarkMode.value = !isDarkMode.value
    localStorage.setItem('cvs-theme', isDarkMode.value ? 'dark' : 'light')
    applyTheme(currentRole.value, isDarkMode.value)
  }
  
  // 设置深色模式
  const setDarkMode = (dark) => {
    isDarkMode.value = dark
    localStorage.setItem('cvs-theme', dark ? 'dark' : 'light')
    applyTheme(currentRole.value, dark)
  }
  
  // 监听角色变化
  watch(currentRole, (newRole) => {
    applyTheme(newRole, isDarkMode.value)
  }, { immediate: true })
  
  // 初始化主题
  const initTheme = () => {
    // 检查系统偏好
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    const savedTheme = localStorage.getItem('cvs-theme')
    
    if (!savedTheme) {
      isDarkMode.value = prefersDark
      localStorage.setItem('cvs-theme', prefersDark ? 'dark' : 'light')
    }
    
    applyTheme(currentRole.value, isDarkMode.value)
  }
  
  return {
    isDarkMode: computed(() => isDarkMode.value),
    currentRole,
    toggleDarkMode,
    setDarkMode,
    initTheme,
    applyTheme
  }
}

// 工具函数：将 hex 颜色转换为 rgba
function hexToRgba(hex, alpha = 1) {
  const r = parseInt(hex.slice(1, 3), 16)
  const g = parseInt(hex.slice(3, 5), 16)
  const b = parseInt(hex.slice(5, 7), 16)
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

// 工具函数：加深颜色
function darkenColor(hex, amount) {
  const r = Math.max(0, parseInt(hex.slice(1, 3), 16) - Math.round(255 * amount))
  const g = Math.max(0, parseInt(hex.slice(3, 5), 16) - Math.round(255 * amount))
  const b = Math.max(0, parseInt(hex.slice(5, 7), 16) - Math.round(255 * amount))
  
  return `#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}`
}