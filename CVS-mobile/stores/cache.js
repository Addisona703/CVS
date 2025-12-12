import { defineStore } from 'pinia'

export const useCacheStore = defineStore('cache', {
  state: () => ({
    activities: [],
    activitiesTimestamp: 0,
    cacheExpiry: 30 * 60 * 1000 // 30分钟
  }),
  
  getters: {
    isCacheValid: (state) => {
      return Date.now() - state.activitiesTimestamp < state.cacheExpiry
    }
  },
  
  actions: {
    // 缓存活动列表
    cacheActivities(activities) {
      this.activities = activities
      this.activitiesTimestamp = Date.now()
      
      uni.setStorageSync('cachedActivities', {
        data: activities,
        timestamp: this.activitiesTimestamp
      })
    },
    
    // 获取缓存的活动列表
    getCachedActivities() {
      if (this.isCacheValid && this.activities.length > 0) {
        return this.activities
      }
      
      // 尝试从本地存储读取
      const cached = uni.getStorageSync('cachedActivities')
      if (cached && Date.now() - cached.timestamp < this.cacheExpiry) {
        this.activities = cached.data
        this.activitiesTimestamp = cached.timestamp
        return this.activities
      }
      
      return null
    },
    
    // 清除缓存
    clearCache() {
      this.activities = []
      this.activitiesTimestamp = 0
      uni.removeStorageSync('cachedActivities')
    },
    
    // 清除所有缓存
    clearAllCache() {
      this.clearCache()
      // 可以添加其他缓存清除逻辑
    }
  }
})
