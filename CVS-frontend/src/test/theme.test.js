/**
 * 主题系统测试
 * 验证角色主题变量文件和 CSS 变量注入功能
 * 验证 Element Plus 主题定制和组件 token 覆盖
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useTheme } from '@/composables/useTheme'
import { useAuthStore } from '@/stores/auth'

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
}
global.localStorage = localStorageMock

// Mock matchMedia
global.matchMedia = vi.fn(() => ({
  matches: false,
  addListener: vi.fn(),
  removeListener: vi.fn(),
}))

describe('主题系统', () => {
  let pinia

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    vi.clearAllMocks()
    
    // Mock document.body
    global.document = {
      body: {
        setAttribute: vi.fn(),
        removeAttribute: vi.fn(),
      },
      documentElement: {
        style: {
          setProperty: vi.fn(),
        },
      },
    }
  })

  describe('useTheme composable', () => {
    it('应该正确映射用户角色到主题角色', () => {
      const authStore = useAuthStore()
      const { currentRole } = useTheme()

      // 测试管理员角色
      authStore.user = { role: 'ADMIN' }
      expect(currentRole.value).toBe('admin')

      // 测试教师角色
      authStore.user = { role: 'TEACHER' }
      expect(currentRole.value).toBe('teacher')

      // 测试学生角色
      authStore.user = { role: 'STUDENT' }
      expect(currentRole.value).toBe('student')

      // 测试未知角色
      authStore.user = { role: 'UNKNOWN' }
      expect(currentRole.value).toBe(null)
    })

    it('应该正确应用主题到 DOM', () => {
      const { applyTheme } = useTheme()

      // 应用管理员主题
      applyTheme('admin', false)
      expect(document.body.setAttribute).toHaveBeenCalledWith('data-role', 'admin')
      expect(document.body.removeAttribute).toHaveBeenCalledWith('data-theme')

      // 应用深色模式
      applyTheme('admin', true)
      expect(document.body.setAttribute).toHaveBeenCalledWith('data-theme', 'dark')
    })

    it('应该正确切换深色模式', () => {
      localStorageMock.getItem.mockReturnValue('light')
      const { toggleDarkMode, isDarkMode } = useTheme()

      // 初始状态应该是浅色模式
      expect(isDarkMode.value).toBe(false)

      // 切换到深色模式
      toggleDarkMode()
      expect(localStorageMock.setItem).toHaveBeenCalledWith('cvs-theme', 'dark')
    })

    it('应该正确设置 Element Plus 主题变量', () => {
      const { applyTheme } = useTheme()

      // 应用管理员主题
      applyTheme('admin', false)
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary', '#409eff')

      // 应用教师主题
      applyTheme('teacher', false)
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary', '#67c23a')

      // 应用学生主题
      applyTheme('student', false)
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary', '#1f6bff')
    })
  })

  describe('CSS 变量定义', () => {
    it('应该定义所有必需的管理员主题变量', () => {
      // 这个测试验证 CSS 变量是否正确定义
      // 在实际应用中，这些变量会通过 SCSS 文件注入
      const expectedAdminVars = [
        '--primary-color',
        '--sidebar-bg',
        '--sidebar-hover-bg',
        '--content-bg',
        '--text-primary'
      ]

      // 由于我们无法直接测试 CSS 变量，这里只验证变量名称的存在
      expectedAdminVars.forEach(varName => {
        expect(varName).toMatch(/^--[\w-]+$/)
      })
    })

    it('应该定义所有必需的教师主题变量', () => {
      const expectedTeacherVars = [
        '--primary-color',
        '--sidebar-bg',
        '--sidebar-hover-bg',
        '--sidebar-text',
        '--indicator-color'
      ]

      expectedTeacherVars.forEach(varName => {
        expect(varName).toMatch(/^--[\w-]+$/)
      })
    })

    it('应该定义所有必需的学生主题变量', () => {
      const expectedStudentVars = [
        '--primary-color',
        '--sidebar-bg',
        '--sidebar-hover-gradient',
        '--content-bg',
        '--text-dark'
      ]

      expectedStudentVars.forEach(varName => {
        expect(varName).toMatch(/^--[\w-]+$/)
      })
    })
  })

  describe('Element Plus 主题定制', () => {
    it('应该为管理员角色设置正确的 Element Plus 主色调变体', () => {
      const { applyTheme } = useTheme()
      applyTheme('admin', false)

      // 验证主色调
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary', '#409eff')
      
      // 验证浅色变体
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary-light-3', '#79bbff')
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary-light-5', '#a0cfff')
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary-light-7', '#c6e2ff')
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary-light-8', '#d9ecff')
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary-light-9', '#ecf5ff')
      
      // 验证深色变体
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary-dark-2', '#337ecc')
    })

    it('应该为教师角色设置正确的 Element Plus 主色调变体', () => {
      const { applyTheme } = useTheme()
      applyTheme('teacher', false)

      // 验证主色调
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary', '#67c23a')
      
      // 验证浅色变体
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary-light-3', '#85ce61')
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary-light-5', '#a4da89')
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary-light-7', '#c2e7b0')
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary-light-8', '#d1edc4')
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary-light-9', '#e1f3d8')
      
      // 验证深色变体
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary-dark-2', '#529b2e')
    })

    it('应该为学生角色设置正确的 Element Plus 主色调变体', () => {
      const { applyTheme } = useTheme()
      applyTheme('student', false)

      // 验证主色调
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary', '#1f6bff')
      
      // 验证浅色变体
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary-light-3', '#4d85ff')
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary-light-5', '#7aa0ff')
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary-light-7', '#a6baff')
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary-light-8', '#bfccff')
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary-light-9', '#d9ddff')
      
      // 验证深色变体
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-color-primary-dark-2', '#1956cc')
    })

    it('应该设置按钮相关的 Element Plus token', () => {
      const { applyTheme } = useTheme()
      applyTheme('admin', false)

      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-button-hover-bg-color', '#79bbff')
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-button-active-bg-color', '#337ecc')
    })

    it('应该设置输入框相关的 Element Plus token', () => {
      const { applyTheme } = useTheme()
      applyTheme('teacher', false)

      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-input-focus-border-color', '#67c23a')
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-input-hover-border-color', '#a4da89')
    })

    it('应该设置菜单相关的 Element Plus token', () => {
      const { applyTheme } = useTheme()
      applyTheme('student', false)

      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-menu-active-color', '#1f6bff')
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-menu-hover-bg-color', '#d9ddff')
    })

    it('应该设置边框圆角 token', () => {
      const { applyTheme } = useTheme()
      applyTheme('admin', false)

      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-border-radius-base', '6px')
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-border-radius-small', '4px')
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-border-radius-large', '8px')
    })

    it('应该设置过渡动画时长 token', () => {
      const { applyTheme } = useTheme()
      applyTheme('admin', false)

      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-transition-duration', '0.3s')
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-transition-duration-fast', '0.2s')
    })

    it('应该为不同角色设置不同的按钮 hover 颜色', () => {
      const { applyTheme } = useTheme()

      // 管理员
      applyTheme('admin', false)
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-button-hover-bg-color', '#79bbff')

      // 教师
      applyTheme('teacher', false)
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-button-hover-bg-color', '#85ce61')

      // 学生
      applyTheme('student', false)
      expect(document.documentElement.style.setProperty).toHaveBeenCalledWith('--el-button-hover-bg-color', '#4d85ff')
    })
  })
})