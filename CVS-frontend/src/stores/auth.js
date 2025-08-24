import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authAPI } from '@/api'
import { ElMessage } from 'element-plus'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const user = ref(null)
  const token = ref(localStorage.getItem('token'))
  const loading = ref(false)
  
  // 计算属性
  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const userRole = computed(() => user.value?.role)
  const userName = computed(() => user.value?.name)
  
  // 动作
  const login = async (credentials) => {
    loading.value = true
    try {
      const response = await authAPI.login(credentials)
      if (response.code === 200) {
        token.value = response.data.token
        user.value = {
          id: response.data.userId,
          username: response.data.username,
          name: response.data.name,
          role: response.data.role
        }
        
        // 保存到本地存储
        localStorage.setItem('token', token.value)
        localStorage.setItem('user', JSON.stringify(user.value))
        
        ElMessage.success('登录成功')
        return response.data
      } else {
        throw new Error(response.message)
      }
    } catch (error) {
      ElMessage.error(error.message || '登录失败')
      throw error
    } finally {
      loading.value = false
    }
  }
  
  const logout = async () => {
    try {
      await authAPI.logout()
    } catch (error) {
      console.error('登出请求失败:', error)
    } finally {
      // 清除状态和本地存储
      user.value = null
      token.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      ElMessage.success('已退出登录')
    }
  }
  
  const initAuth = () => {
    const savedUser = localStorage.getItem('user')
    const savedToken = localStorage.getItem('token')
    
    if (savedUser && savedToken) {
      user.value = JSON.parse(savedUser)
      token.value = savedToken
    }
  }
  
  const updateUser = (userData) => {
    user.value = { ...user.value, ...userData }
    localStorage.setItem('user', JSON.stringify(user.value))
  }
  
  return {
    // 状态
    user,
    token,
    loading,
    // 计算属性
    isAuthenticated,
    userRole,
    userName,
    // 动作
    login,
    logout,
    initAuth,
    updateUser
  }
})
