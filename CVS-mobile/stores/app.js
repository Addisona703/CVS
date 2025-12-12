import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    theme: 'light',
    systemInfo: {},
    networkType: 'unknown',
    tabBarIndex: 0
  }),
  
  getters: {
    isOnline: (state) => state.networkType !== 'none'
  },
  
  actions: {
    // 初始化应用
    async init() {
      // 获取系统信息
      try {
        const systemInfo = uni.getSystemInfoSync()
        this.systemInfo = systemInfo
        console.log('系统信息:', systemInfo)
      } catch (error) {
        console.error('获取系统信息失败:', error)
      }
      
      // 获取网络类型
      try {
        uni.getNetworkType({
          success: (res) => {
            this.networkType = res.networkType
            console.log('网络类型:', res.networkType)
          }
        })
      } catch (error) {
        console.error('获取网络类型失败:', error)
      }
      
      // 监听网络状态变化
      uni.onNetworkStatusChange((res) => {
        this.networkType = res.networkType
        console.log('网络状态变化:', res.networkType)
        
        if (res.isConnected) {
          uni.showToast({
            title: '网络已连接',
            icon: 'success',
            duration: 1500
          })
        } else {
          uni.showToast({
            title: '网络已断开',
            icon: 'none',
            duration: 1500
          })
        }
      })
    },
    
    // 设置主题
    setTheme(theme) {
      this.theme = theme
      uni.setStorageSync('theme', theme)
    },
    
    // 设置TabBar索引
    setTabBarIndex(index) {
      this.tabBarIndex = index
    }
  }
})
