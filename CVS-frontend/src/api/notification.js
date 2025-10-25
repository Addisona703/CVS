import request from '@/utils/request'
import { ElMessage } from 'element-plus'

// 重试配置
const RETRY_CONFIG = {
  maxRetries: 3,
  baseDelay: 1000, // 基础延迟 1秒
  maxDelay: 30000, // 最大延迟 30秒
  backoffFactor: 2 // 指数退避因子
}

// 轮询重试配置
const POLLING_RETRY_CONFIG = {
  maxRetries: 5,
  baseDelay: 2000, // 基础延迟 2秒
  maxDelay: 60000, // 最大延迟 60秒
  backoffFactor: 1.5 // 指数退避因子
}

/**
 * 计算指数退避延迟时间
 * @param {number} attempt 当前重试次数
 * @param {object} config 重试配置
 * @returns {number} 延迟时间（毫秒）
 */
const calculateBackoffDelay = (attempt, config) => {
  const delay = config.baseDelay * Math.pow(config.backoffFactor, attempt - 1)
  return Math.min(delay, config.maxDelay)
}

/**
 * 延迟函数
 * @param {number} ms 延迟时间（毫秒）
 * @returns {Promise}
 */
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms))

/**
 * 带重试的API调用
 * @param {Function} apiCall API调用函数
 * @param {object} config 重试配置
 * @param {string} errorMessage 错误提示消息
 * @returns {Promise}
 */
const withRetry = async (apiCall, config = RETRY_CONFIG, errorMessage = '操作失败') => {
  let lastError = null
  
  for (let attempt = 1; attempt <= config.maxRetries; attempt++) {
    try {
      return await apiCall()
    } catch (error) {
      lastError = error
      
      // 如果是最后一次尝试，直接抛出错误
      if (attempt === config.maxRetries) {
        break
      }
      
      // 检查是否应该重试
      if (!shouldRetry(error)) {
        break
      }
      
      // 计算延迟时间并等待
      const delayTime = calculateBackoffDelay(attempt, config)
      console.warn(`API调用失败，${delayTime}ms后进行第${attempt + 1}次重试:`, error.message)
      await delay(delayTime)
    }
  }
  
  // 所有重试都失败了，抛出最后的错误
  console.error(`${errorMessage}，已重试${config.maxRetries}次:`, lastError)
  throw lastError
}

/**
 * 判断是否应该重试
 * @param {Error} error 错误对象
 * @returns {boolean} 是否应该重试
 */
const shouldRetry = (error) => {
  // 网络错误或超时错误应该重试
  if (!error.response) {
    return true
  }
  
  const status = error.response.status
  
  // 5xx 服务器错误应该重试
  if (status >= 500) {
    return true
  }
  
  // 429 请求过多应该重试
  if (status === 429) {
    return true
  }
  
  // 408 请求超时应该重试
  if (status === 408) {
    return true
  }
  
  // 其他错误不重试（如 4xx 客户端错误）
  return false
}

/**
 * 轮询专用的重试机制（静默失败，不显示错误消息）
 * @param {Function} apiCall API调用函数
 * @returns {Promise}
 */
const withPollingRetry = async (apiCall) => {
  let lastError = null
  
  for (let attempt = 1; attempt <= POLLING_RETRY_CONFIG.maxRetries; attempt++) {
    try {
      return await apiCall()
    } catch (error) {
      lastError = error
      
      // 如果是最后一次尝试，静默失败
      if (attempt === POLLING_RETRY_CONFIG.maxRetries) {
        break
      }
      
      // 检查是否应该重试
      if (!shouldRetry(error)) {
        break
      }
      
      // 计算延迟时间并等待
      const delayTime = calculateBackoffDelay(attempt, POLLING_RETRY_CONFIG)
      console.warn(`轮询失败，${delayTime}ms后进行第${attempt + 1}次重试:`, error.message)
      await delay(delayTime)
    }
  }
  
  // 轮询失败时静默处理，只在控制台记录
  console.error('轮询失败，将在下次轮询时重试:', lastError)
  return null
}

export const notificationAPI = {
  // 获取未读消息数量
  getUnreadCount() {
    return request.get('/notifications/unread-count')
  },

  // 获取未读消息数量（轮询专用，带重试机制）
  getUnreadCountWithRetry() {
    return withPollingRetry(() => request.get('/notifications/unread-count'))
  },

  // 获取消息列表（分页）
  getNotifications(params) {
    return withRetry(
      () => request.get('/notifications', { params }),
      RETRY_CONFIG,
      '获取通知列表失败'
    )
  },

  // 获取未读消息列表
  getUnreadNotifications() {
    return withRetry(
      () => request.get('/notifications/unread'),
      RETRY_CONFIG,
      '获取未读通知失败'
    )
  },

  // 标记消息已读
  markAsRead(id) {
    return withRetry(
      () => request.put(`/notifications/${id}/read`),
      RETRY_CONFIG,
      '标记已读失败'
    )
  },

  // 标记所有消息已读
  markAllAsRead() {
    return withRetry(
      () => request.put('/notifications/mark-all-read'),
      RETRY_CONFIG,
      '标记全部已读失败'
    )
  },

  // 删除通知
  deleteNotification(id) {
    return withRetry(
      () => request.delete(`/notifications/${id}`),
      RETRY_CONFIG,
      '删除通知失败'
    )
  },

  // 清空已读通知
  clearReadNotifications() {
    return withRetry(
      () => request.delete('/notifications/clear-read'),
      RETRY_CONFIG,
      '清空已读通知失败'
    )
  },

  // 网络状态检查
  checkNetworkStatus() {
    return navigator.onLine
  },

  // 显示网络异常提示
  showNetworkError() {
    if (!navigator.onLine) {
      ElMessage.error('网络连接已断开，请检查网络设置')
      return true
    }
    return false
  }
}
