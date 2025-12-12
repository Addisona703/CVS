import { defineStore } from 'pinia'
import { login as loginApi } from '@/api/auth'
import { tokenManager } from '@/utils/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: uni.getStorageSync('token') || '',
    userInfo: uni.getStorageSync('userInfo') || null,
    isLoggedIn: false
  }),

  getters: {
    userId: (state) => state.userInfo?.id,
    username: (state) => state.userInfo?.username,
    name: (state) => state.userInfo?.name,
    role: (state) => state.userInfo?.role,
    isStudent: (state) => state.userInfo?.role === 'STUDENT',
    isTeacher: (state) => state.userInfo?.role === 'TEACHER',
    isAdmin: (state) => state.userInfo?.role === 'ADMIN'
  },

  actions: {
    // 根据角色获取首页路由
    getRoleHomePage() {
      const roleRoutes = {
        STUDENT: '/pages/student-sub/dashboard/dashboard',
        TEACHER: '/pages/teacher/dashboard/dashboard',
        ADMIN: '/pages/admin/dashboard/dashboard'
      }
      return roleRoutes[this.role] || roleRoutes.STUDENT
    },

    // 登录
    async login(credentials) {
      try {
        // 调用登录API
        const response = await loginApi(credentials)

        // 处理不同的响应数据结构
        let token, userInfo

        // 情况1: response 直接包含 token
        if (response.token) {
          token = response.token

          // 后端直接返回用户信息字段（userId, username, name, role等）
          if (response.userId || response.username) {
            userInfo = {
              id: response.userId,
              userId: response.userId,
              username: response.username,
              name: response.name,
              realName: response.name,
              role: response.role
            }
          }
          // 或者有独立的 userInfo 字段
          else if (response.userInfo) {
            userInfo = response.userInfo
          }
          // 或者有 user 字段
          else if (response.user) {
            userInfo = response.user
          }
        }
        // 情况2: 数据在 data 字段中
        else if (response.data) {
          token = response.data.token
          userInfo = response.data.userInfo || response.data.user || response.data
        }

        if (!token || !userInfo) {
          throw new Error('登录响应数据格式错误')
        }

        // 确保 userInfo 有 role 字段
        if (!userInfo.role) {
          throw new Error('用户信息缺少角色字段')
        }

        this.token = token
        this.userInfo = userInfo
        this.isLoggedIn = true

        // 持久化存储到 storage
        uni.setStorageSync('token', this.token)
        uni.setStorageSync('userInfo', this.userInfo)

        // 同时保存到 tokenManager（用于请求拦截器）
        tokenManager.setToken(token, token) // 暂时使用同一个token作为refreshToken

        return { token, userInfo }
      } catch (error) {
        console.error('登录失败:', error)
        throw error
      }
    },

    // 退出登录
    async logout() {
      this.token = ''
      this.userInfo = null
      this.isLoggedIn = false

      // 清除所有存储
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
      uni.removeStorageSync('accessToken')
      uni.removeStorageSync('refreshToken')
      uni.removeStorageSync('tokenTime')

      // 清除 tokenManager 中的 token
      tokenManager.clearToken()

      // 跳转到登录页
      uni.reLaunch({
        url: '/pages/index/index'
      })
    },

    // 更新用户信息
    updateUserInfo(userInfo) {
      this.userInfo = { ...this.userInfo, ...userInfo }
      uni.setStorageSync('userInfo', this.userInfo)
    },

    // 检查登录状态
    async checkLoginStatus() {
      const token = uni.getStorageSync('token')
      const userInfo = uni.getStorageSync('userInfo')

      if (token && userInfo) {
        this.token = token
        this.userInfo = userInfo
        this.isLoggedIn = true

        // 同步到 tokenManager
        tokenManager.setToken(token, token)
        
        console.log('Token 已同步到 tokenManager')

        return true
      }

      return false
    }
  }
})
