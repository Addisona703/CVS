/**
 * 存储工具函数
 * 提供缓存管理功能
 */

import {
  CACHE_EXPIRY
} from '@/config'

/**
 * 缓存类型
 */
export const CacheType = {
  PERMANENT: 'permanent', // 永久缓存
  SHORT_TERM: 'short_term', // 短期缓存（30分钟）
  TEMPORARY: 'temporary' // 临时缓存（5分钟）
}

/**
 * 缓存过期时间配置
 */
const EXPIRY_CONFIG = {
  [CacheType.PERMANENT]: 0, // 永不过期
  [CacheType.SHORT_TERM]: CACHE_EXPIRY, // 30分钟
  [CacheType.TEMPORARY]: 5 * 60 * 1000 // 5分钟
}

/**
 * 缓存管理器
 */
export class CacheManager {
  /**
   * 设置缓存
   * @param {String} key 缓存键
   * @param {any} data 缓存数据
   * @param {String} type 缓存类型
   */
  static set(key, data, type = CacheType.SHORT_TERM) {
    try {
      const cacheData = {
        data,
        timestamp: Date.now(),
        type,
        expiry: EXPIRY_CONFIG[type]
      }

      uni.setStorageSync(key, JSON.stringify(cacheData))
      return true
    } catch (error) {
      console.error('设置缓存失败:', error)
      return false
    }
  }

  /**
   * 获取缓存
   * @param {String} key 缓存键
   * @param {Boolean} checkExpiry 是否检查过期
   * @returns {any} 缓存数据
   */
  static get(key, checkExpiry = true) {
    try {
      const cacheStr = uni.getStorageSync(key)
      if (!cacheStr) return null

      const cacheData = JSON.parse(cacheStr)

      // 检查是否过期
      if (checkExpiry && this.isExpired(cacheData)) {
        this.remove(key)
        return null
      }

      return cacheData.data
    } catch (error) {
      console.error('获取缓存失败:', error)
      return null
    }
  }

  /**
   * 检查缓存是否过期
   * @param {Object} cacheData 缓存数据对象
   * @returns {Boolean} 是否过期
   */
  static isExpired(cacheData) {
    if (!cacheData || !cacheData.timestamp) return true

    // 永久缓存不过期
    if (cacheData.type === CacheType.PERMANENT) return false

    const expiry = cacheData.expiry || EXPIRY_CONFIG[cacheData.type] || CACHE_EXPIRY
    return Date.now() - cacheData.timestamp > expiry
  }

  /**
   * 移除缓存
   * @param {String} key 缓存键
   */
  static remove(key) {
    try {
      uni.removeStorageSync(key)
      return true
    } catch (error) {
      console.error('移除缓存失败:', error)
      return false
    }
  }

  /**
   * 清除所有缓存
   * @param {String} type 缓存类型（可选，不传则清除所有）
   */
  static clear(type = null) {
    try {
      if (!type) {
        // 清除所有缓存
        uni.clearStorageSync()
        return true
      }

      // 清除指定类型的缓存
      const info = uni.getStorageInfoSync()
      const keys = info.keys || []

      keys.forEach((key) => {
        try {
          const cacheStr = uni.getStorageSync(key)
          if (cacheStr) {
            const cacheData = JSON.parse(cacheStr)
            if (cacheData.type === type) {
              uni.removeStorageSync(key)
            }
          }
        } catch (error) {
          // 忽略解析错误
        }
      })

      return true
    } catch (error) {
      console.error('清除缓存失败:', error)
      return false
    }
  }

  /**
   * 清理过期缓存
   */
  static clearExpired() {
    try {
      const info = uni.getStorageInfoSync()
      const keys = info.keys || []
      let clearedCount = 0

      keys.forEach((key) => {
        try {
          const cacheStr = uni.getStorageSync(key)
          if (cacheStr) {
            const cacheData = JSON.parse(cacheStr)
            if (this.isExpired(cacheData)) {
              uni.removeStorageSync(key)
              clearedCount++
            }
          }
        } catch (error) {
          // 忽略解析错误
        }
      })

      console.log(`清理了${clearedCount}个过期缓存`)
      return clearedCount
    } catch (error) {
      console.error('清理过期缓存失败:', error)
      return 0
    }
  }

  /**
   * 获取缓存信息
   * @returns {Object} 缓存信息
   */
  static getInfo() {
    try {
      const info = uni.getStorageInfoSync()
      return {
        keys: info.keys || [],
        currentSize: info.currentSize || 0,
        limitSize: info.limitSize || 0
      }
    } catch (error) {
      console.error('获取缓存信息失败:', error)
      return {
        keys: [],
        currentSize: 0,
        limitSize: 0
      }
    }
  }

  /**
   * 检查缓存是否存在
   * @param {String} key 缓存键
   * @returns {Boolean} 是否存在
   */
  static has(key) {
    try {
      const cacheStr = uni.getStorageSync(key)
      return !!cacheStr
    } catch (error) {
      return false
    }
  }

  /**
   * 获取缓存的剩余有效时间（毫秒）
   * @param {String} key 缓存键
   * @returns {Number} 剩余时间（-1表示永久，0表示已过期，null表示不存在）
   */
  static getTTL(key) {
    try {
      const cacheStr = uni.getStorageSync(key)
      if (!cacheStr) return null

      const cacheData = JSON.parse(cacheStr)

      // 永久缓存
      if (cacheData.type === CacheType.PERMANENT) return -1

      const expiry = cacheData.expiry || EXPIRY_CONFIG[cacheData.type] || CACHE_EXPIRY
      const elapsed = Date.now() - cacheData.timestamp
      const remaining = expiry - elapsed

      return remaining > 0 ? remaining : 0
    } catch (error) {
      console.error('获取缓存TTL失败:', error)
      return null
    }
  }
}

/**
 * 活动列表缓存键
 */
export const CACHE_KEYS = {
  ACTIVITY_LIST: 'cache_activity_list',
  ACTIVITY_DETAIL: 'cache_activity_detail_',
  SIGNUP_LIST: 'cache_signup_list',
  SERVICE_RECORD_LIST: 'cache_service_record_list',
  POINTS_BALANCE: 'cache_points_balance',
  POINTS_RECORDS: 'cache_points_records',
  PRODUCT_LIST: 'cache_product_list',
  REDEMPTION_LIST: 'cache_redemption_list',
  NOTIFICATION_LIST: 'cache_notification_list',
  USER_INFO: 'cache_user_info'
}

/**
 * 快捷缓存方法
 */

/**
 * 缓存活动列表
 * @param {Array} activities 活动列表
 */
export function cacheActivityList(activities) {
  return CacheManager.set(CACHE_KEYS.ACTIVITY_LIST, activities, CacheType.SHORT_TERM)
}

/**
 * 获取缓存的活动列表
 * @returns {Array|null} 活动列表
 */
export function getCachedActivityList() {
  return CacheManager.get(CACHE_KEYS.ACTIVITY_LIST)
}

/**
 * 缓存活动详情
 * @param {Number} id 活动ID
 * @param {Object} activity 活动详情
 */
export function cacheActivityDetail(id, activity) {
  return CacheManager.set(
    `${CACHE_KEYS.ACTIVITY_DETAIL}${id}`,
    activity,
    CacheType.TEMPORARY
  )
}

/**
 * 获取缓存的活动详情
 * @param {Number} id 活动ID
 * @returns {Object|null} 活动详情
 */
export function getCachedActivityDetail(id) {
  return CacheManager.get(`${CACHE_KEYS.ACTIVITY_DETAIL}${id}`)
}

/**
 * 缓存用户信息
 * @param {Object} userInfo 用户信息
 */
export function cacheUserInfo(userInfo) {
  return CacheManager.set(CACHE_KEYS.USER_INFO, userInfo, CacheType.PERMANENT)
}

/**
 * 获取缓存的用户信息
 * @returns {Object|null} 用户信息
 */
export function getCachedUserInfo() {
  return CacheManager.get(CACHE_KEYS.USER_INFO, false) // 不检查过期
}

/**
 * 清除用户相关缓存
 */
export function clearUserCache() {
  CacheManager.remove(CACHE_KEYS.USER_INFO)
  CacheManager.remove(CACHE_KEYS.POINTS_BALANCE)
  CacheManager.remove(CACHE_KEYS.POINTS_RECORDS)
  CacheManager.remove(CACHE_KEYS.SIGNUP_LIST)
  CacheManager.remove(CACHE_KEYS.SERVICE_RECORD_LIST)
  CacheManager.remove(CACHE_KEYS.REDEMPTION_LIST)
  CacheManager.remove(CACHE_KEYS.NOTIFICATION_LIST)
}

export default {
  CacheManager,
  CacheType,
  CACHE_KEYS,
  cacheActivityList,
  getCachedActivityList,
  cacheActivityDetail,
  getCachedActivityDetail,
  cacheUserInfo,
  getCachedUserInfo,
  clearUserCache
}
