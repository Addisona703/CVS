/**
 * 工具函数统一导出
 */

// HTTP请求客户端
export { default as http, HttpClient } from './request'

// 认证工具
export { tokenManager, isLoggedIn, requireAuth } from './auth'

// 格式化工具
export {
  formatDate,
  formatRelativeTime,
  formatDuration,
  formatNumber,
  formatFileSize,
  formatPhone,
  formatEmail,
  formatPercentage,
  formatCurrency,
  formatPoints,
  formatActivityStatus,
  formatSignupStatus,
  formatRedemptionStatus,
  formatCertificateStatus,
  formatUserRole
} from './format'

// 验证工具
export {
  validateEmail,
  validatePhone,
  validatePassword,
  validateUsername,
  validateIdCard,
  validateUrl,
  validateNumber,
  validateDate,
  validateRequired,
  validateLength,
  filterXSS,
  filterSQL,
  validateForm
} from './validate'

// 存储工具
export {
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
} from './storage'

// 错误处理
export { ErrorCode, ErrorMessage, ErrorRecovery, ErrorHandler, createError } from './errorHandler'

// 日志管理
export { logger, performanceMonitor, LogLevel, Logger, PerformanceMonitor } from './logger'
