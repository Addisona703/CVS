/**
 * 日志管理器
 * 提供日志记录、上报和性能监控功能
 */

import { isDev } from '@/config'

/**
 * 日志级别
 */
export const LogLevel = {
  DEBUG: 'DEBUG',
  INFO: 'INFO',
  WARN: 'WARN',
  ERROR: 'ERROR'
}

/**
 * 日志级别优先级
 */
const LOG_LEVEL_PRIORITY = {
  [LogLevel.DEBUG]: 0,
  [LogLevel.INFO]: 1,
  [LogLevel.WARN]: 2,
  [LogLevel.ERROR]: 3
}

/**
 * 日志管理器类
 */
export class Logger {
  constructor(options = {}) {
    this.level = options.level || (isDev ? LogLevel.DEBUG : LogLevel.INFO)
    this.maxLogs = options.maxLogs || 100
    this.logs = []
    this.enableConsole = options.enableConsole !== false
    this.enableStorage = options.enableStorage !== false
    this.enableReport = options.enableReport || false
    this.reportUrl = options.reportUrl || ''
  }

  /**
   * 检查是否应该记录该级别的日志
   * @param {String} level 日志级别
   * @returns {Boolean} 是否记录
   */
  shouldLog(level) {
    return LOG_LEVEL_PRIORITY[level] >= LOG_LEVEL_PRIORITY[this.level]
  }

  /**
   * 记录日志
   * @param {String} level 日志级别
   * @param {String} message 日志消息
   * @param {any} data 附加数据
   */
  log(level, message, data = null) {
    if (!this.shouldLog(level)) return

    const logEntry = {
      level,
      message,
      data,
      timestamp: new Date().toISOString(),
      userAgent: this.getUserAgent()
    }

    // 添加到日志列表
    this.logs.push(logEntry)

    // 限制日志数量
    if (this.logs.length > this.maxLogs) {
      this.logs.shift()
    }

    // 输出到控制台
    if (this.enableConsole) {
      this.logToConsole(logEntry)
    }

    // 存储到本地
    if (this.enableStorage) {
      this.saveToStorage()
    }

    // 上报日志
    if (this.enableReport && level === LogLevel.ERROR) {
      this.reportLog(logEntry)
    }
  }

  /**
   * 输出到控制台
   * @param {Object} logEntry 日志条目
   */
  logToConsole(logEntry) {
    const { level, message, data, timestamp } = logEntry
    const prefix = `[${timestamp}] [${level}]`

    switch (level) {
      case LogLevel.DEBUG:
        console.debug(prefix, message, data)
        break
      case LogLevel.INFO:
        console.info(prefix, message, data)
        break
      case LogLevel.WARN:
        console.warn(prefix, message, data)
        break
      case LogLevel.ERROR:
        console.error(prefix, message, data)
        break
      default:
        console.log(prefix, message, data)
    }
  }

  /**
   * 保存到本地存储
   */
  saveToStorage() {
    try {
      uni.setStorageSync('app_logs', JSON.stringify(this.logs))
    } catch (error) {
      console.error('保存日志失败:', error)
    }
  }

  /**
   * 从本地存储加载日志
   */
  loadFromStorage() {
    try {
      const logsStr = uni.getStorageSync('app_logs')
      if (logsStr) {
        this.logs = JSON.parse(logsStr)
      }
    } catch (error) {
      console.error('加载日志失败:', error)
    }
  }

  /**
   * 上报日志
   * @param {Object} logEntry 日志条目
   */
  async reportLog(logEntry) {
    if (!this.reportUrl) return

    try {
      await uni.request({
        url: this.reportUrl,
        method: 'POST',
        data: logEntry
      })
    } catch (error) {
      console.error('上报日志失败:', error)
    }
  }

  /**
   * 获取用户代理信息
   * @returns {String} 用户代理
   */
  getUserAgent() {
    try {
      const systemInfo = uni.getSystemInfoSync()
      return `${systemInfo.platform} ${systemInfo.system} ${systemInfo.model}`
    } catch (error) {
      return 'Unknown'
    }
  }

  /**
   * DEBUG级别日志
   * @param {String} message 日志消息
   * @param {any} data 附加数据
   */
  debug(message, data = null) {
    this.log(LogLevel.DEBUG, message, data)
  }

  /**
   * INFO级别日志
   * @param {String} message 日志消息
   * @param {any} data 附加数据
   */
  info(message, data = null) {
    this.log(LogLevel.INFO, message, data)
  }

  /**
   * WARN级别日志
   * @param {String} message 日志消息
   * @param {any} data 附加数据
   */
  warn(message, data = null) {
    this.log(LogLevel.WARN, message, data)
  }

  /**
   * ERROR级别日志
   * @param {String} message 日志消息
   * @param {any} data 附加数据
   */
  error(message, data = null) {
    this.log(LogLevel.ERROR, message, data)
  }

  /**
   * 获取所有日志
   * @returns {Array} 日志列表
   */
  getLogs() {
    return this.logs
  }

  /**
   * 清除日志
   */
  clearLogs() {
    this.logs = []
    try {
      uni.removeStorageSync('app_logs')
    } catch (error) {
      console.error('清除日志失败:', error)
    }
  }

  /**
   * 导出日志
   * @returns {String} 日志JSON字符串
   */
  exportLogs() {
    return JSON.stringify(this.logs, null, 2)
  }
}

/**
 * 性能监控器
 */
export class PerformanceMonitor {
  constructor() {
    this.metrics = {}
  }

  /**
   * 开始计时
   * @param {String} name 指标名称
   */
  start(name) {
    this.metrics[name] = {
      startTime: Date.now(),
      endTime: null,
      duration: null
    }
  }

  /**
   * 结束计时
   * @param {String} name 指标名称
   * @returns {Number} 持续时间（毫秒）
   */
  end(name) {
    if (!this.metrics[name]) {
      console.warn(`性能指标 ${name} 未开始`)
      return 0
    }

    this.metrics[name].endTime = Date.now()
    this.metrics[name].duration = this.metrics[name].endTime - this.metrics[name].startTime

    return this.metrics[name].duration
  }

  /**
   * 获取指标
   * @param {String} name 指标名称
   * @returns {Object} 指标数据
   */
  getMetric(name) {
    return this.metrics[name]
  }

  /**
   * 获取所有指标
   * @returns {Object} 所有指标
   */
  getAllMetrics() {
    return this.metrics
  }

  /**
   * 清除指标
   * @param {String} name 指标名称（可选）
   */
  clear(name = null) {
    if (name) {
      delete this.metrics[name]
    } else {
      this.metrics = {}
    }
  }

  /**
   * 记录页面加载时间
   * @param {String} pageName 页面名称
   */
  recordPageLoad(pageName) {
    const loadTime = Date.now() - this.metrics[`${pageName}_start`]?.startTime || 0
    logger.info(`页面加载时间: ${pageName}`, { loadTime })
    return loadTime
  }

  /**
   * 记录API请求时间
   * @param {String} apiName API名称
   * @param {Number} duration 持续时间
   */
  recordApiRequest(apiName, duration) {
    logger.info(`API请求时间: ${apiName}`, { duration })
  }
}

// 创建默认实例
const logger = new Logger({
  level: isDev ? LogLevel.DEBUG : LogLevel.INFO,
  enableConsole: true,
  enableStorage: true,
  enableReport: false
})

const performanceMonitor = new PerformanceMonitor()

// 加载历史日志
logger.loadFromStorage()

export default logger
export { logger, performanceMonitor }
