/**
 * 认证工具函数
 * 提供token管理、设备ID生成等功能
 */

import { TOKEN_EXPIRY, REFRESH_TOKEN_EXPIRY } from '@/config'

/**
 * Token管理器
 */
export const tokenManager = {
  /**
   * 保存token
   * @param {String} accessToken 访问令牌
   * @param {String} refreshToken 刷新令牌
   */
  setToken(accessToken, refreshToken) {
    const deviceId = this.getDeviceId()

    // 清理token中的空字符
    const cleanAccessToken = accessToken ? accessToken.replace(/\u0000/g, '').trim() : ''
    const cleanRefreshToken = refreshToken ? refreshToken.replace(/\u0000/g, '').trim() : ''

    // 加密存储token
    const encryptedAccess = this.encrypt(cleanAccessToken)
    const encryptedRefresh = this.encrypt(cleanRefreshToken)

    uni.setStorageSync('accessToken', encryptedAccess)
    uni.setStorageSync('refreshToken', encryptedRefresh)
    uni.setStorageSync('tokenTime', Date.now())
    uni.setStorageSync('deviceId', deviceId)
  },

  /**
   * 获取访问令牌
   * @returns {String|null} 访问令牌
   */
  getAccessToken() {
    const encrypted = uni.getStorageSync('accessToken')
    if (!encrypted) return null
    
    const decrypted = this.decrypt(encrypted)
    // 清除可能存在的空字符和其他不可见字符
    return decrypted ? decrypted.replace(/\u0000/g, '').trim() : null
  },

  /**
   * 获取刷新令牌
   * @returns {String|null} 刷新令牌
   */
  getRefreshToken() {
    const encrypted = uni.getStorageSync('refreshToken')
    if (!encrypted) return null
    
    const decrypted = this.decrypt(encrypted)
    // 清除可能存在的空字符和其他不可见字符
    return decrypted ? decrypted.replace(/\u0000/g, '').trim() : null
  },

  /**
   * 检查访问令牌是否过期
   * @returns {Boolean} 是否过期
   */
  isAccessTokenExpired() {
    const tokenTime = uni.getStorageSync('tokenTime')
    if (!tokenTime) return true

    return Date.now() - tokenTime > TOKEN_EXPIRY
  },

  /**
   * 检查刷新令牌是否过期
   * @returns {Boolean} 是否过期
   */
  isRefreshTokenExpired() {
    const tokenTime = uni.getStorageSync('tokenTime')
    if (!tokenTime) return true

    return Date.now() - tokenTime > REFRESH_TOKEN_EXPIRY
  },

  /**
   * 清除token
   */
  clearToken() {
    uni.removeStorageSync('accessToken')
    uni.removeStorageSync('refreshToken')
    uni.removeStorageSync('tokenTime')
    // 保留deviceId用于下次登录
  },

  /**
   * 获取或生成设备ID
   * @returns {String} 设备ID
   */
  getDeviceId() {
    let deviceId = uni.getStorageSync('deviceId')

    if (!deviceId) {
      deviceId = this.generateDeviceId()
      uni.setStorageSync('deviceId', deviceId)
    }

    return deviceId
  },

  /**
   * 生成设备ID
   * @returns {String} 设备ID
   */
  generateDeviceId() {
    try {
      const systemInfo = uni.getSystemInfoSync()
      const timestamp = Date.now()
      const random = Math.random().toString(36).substring(2, 15)

      // 组合设备信息生成唯一ID
      const deviceString = `${systemInfo.platform}_${systemInfo.model}_${systemInfo.system}_${timestamp}_${random}`

      // Base64编码
      return this.base64Encode(deviceString)
    } catch (error) {
      console.error('生成设备ID失败:', error)
      // 降级方案：使用时间戳和随机数
      return this.base64Encode(`${Date.now()}_${Math.random()}`)
    }
  },

  /**
   * 验证设备ID
   * @param {String} deviceId 设备ID
   * @returns {Boolean} 是否有效
   */
  validateDeviceId(deviceId) {
    const storedDeviceId = this.getDeviceId()
    return deviceId === storedDeviceId
  },

  /**
   * 简单加密（生产环境应使用更强的加密算法如AES）
   * @param {String} data 原始数据
   * @returns {String} 加密后的数据
   */
  encrypt(data) {
    try {
      return this.base64Encode(encodeURIComponent(data))
    } catch (error) {
      console.error('加密失败:', error)
      return data
    }
  },

  /**
   * 简单解密
   * @param {String} encrypted 加密的数据
   * @returns {String} 解密后的数据
   */
  decrypt(encrypted) {
    try {
      return decodeURIComponent(this.base64Decode(encrypted))
    } catch (error) {
      console.error('解密失败:', error)
      return encrypted
    }
  },

  /**
   * Base64编码
   * @param {String} str 原始字符串
   * @returns {String} 编码后的字符串
   */
  base64Encode(str) {
    // uni-app环境下的Base64编码
    // 注意：不同平台可能需要不同的实现
    try {
      // #ifdef H5
      return btoa(str)
      // #endif

      // #ifndef H5
      // 小程序和App环境使用自定义Base64编码
      const base64Chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/'
      let result = ''
      let i = 0

      while (i < str.length) {
        const a = str.charCodeAt(i++)
        const b = i < str.length ? str.charCodeAt(i++) : 0
        const c = i < str.length ? str.charCodeAt(i++) : 0

        const bitmap = (a << 16) | (b << 8) | c

        result += base64Chars.charAt((bitmap >> 18) & 63)
        result += base64Chars.charAt((bitmap >> 12) & 63)
        result += i - 2 < str.length ? base64Chars.charAt((bitmap >> 6) & 63) : '='
        result += i - 1 < str.length ? base64Chars.charAt(bitmap & 63) : '='
      }

      return result
      // #endif
    } catch (error) {
      console.error('Base64编码失败:', error)
      return str
    }
  },

  /**
   * Base64解码
   * @param {String} str 编码的字符串
   * @returns {String} 解码后的字符串
   */
  base64Decode(str) {
    try {
      // #ifdef H5
      return atob(str)
      // #endif

      // #ifndef H5
      // 小程序和App环境使用自定义Base64解码
      const base64Chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/'
      let result = ''
      let i = 0

      // 移除非Base64字符
      str = str.replace(/[^A-Za-z0-9+/=]/g, '')

      while (i < str.length) {
        const a = base64Chars.indexOf(str.charAt(i++))
        const b = base64Chars.indexOf(str.charAt(i++))
        const c = base64Chars.indexOf(str.charAt(i++))
        const d = base64Chars.indexOf(str.charAt(i++))

        const bitmap = (a << 18) | (b << 12) | (c << 6) | d

        result += String.fromCharCode((bitmap >> 16) & 255)
        if (c !== 64) result += String.fromCharCode((bitmap >> 8) & 255)
        if (d !== 64) result += String.fromCharCode(bitmap & 255)
      }

      return result
      // #endif
    } catch (error) {
      console.error('Base64解码失败:', error)
      return str
    }
  }
}

/**
 * 检查是否已登录
 * @returns {Boolean} 是否已登录
 */
export function isLoggedIn() {
  const token = tokenManager.getAccessToken()
  return !!token && !tokenManager.isAccessTokenExpired()
}

/**
 * 需要登录的页面守卫
 * @param {Function} callback 登录后的回调
 */
export function requireAuth(callback) {
  if (isLoggedIn()) {
    callback && callback()
  } else {
    uni.showModal({
      title: '提示',
      content: '请先登录',
      showCancel: false,
      success: () => {
        uni.reLaunch({
          url: '/pages/index/index'
        })
      }
    })
  }
}

export default {
  tokenManager,
  isLoggedIn,
  requireAuth
}
