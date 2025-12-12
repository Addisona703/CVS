/**
 * 应用配置
 */

// 获取环境变量
const env = import.meta.env

// 开发环境
export const isDev = env.MODE === 'development'

// 生产环境
export const isProd = env.MODE === 'production'

// API基础URL
export const API_BASE_URL = env.VITE_API_BASE_URL || 'http://localhost:8080'

// 应用标题
export const APP_TITLE = env.VITE_APP_TITLE || 'CVS志愿服务'

// 请求超时时间（毫秒）
export const REQUEST_TIMEOUT = 30000

// Token过期时间（毫秒）
export const TOKEN_EXPIRY = 2 * 60 * 60 * 1000 // 2小时

// Refresh Token过期时间（毫秒）
export const REFRESH_TOKEN_EXPIRY = 7 * 24 * 60 * 60 * 1000 // 7天

// 缓存过期时间（毫秒）
export const CACHE_EXPIRY = 30 * 60 * 1000 // 30分钟

// 分页配置
export const PAGE_SIZE = 20

// 图片压缩质量
export const IMAGE_QUALITY = 0.8

// 最大上传文件大小（字节）
export const MAX_FILE_SIZE = 10 * 1024 * 1024 // 10MB

// 二维码刷新间隔（毫秒）
export const QR_CODE_REFRESH_INTERVAL = 5 * 60 * 1000 // 5分钟

// 轮询间隔（毫秒）
export const POLLING_INTERVAL = 3000 // 3秒

// 最大重试次数
export const MAX_RETRY_COUNT = 3

// 重试延迟（毫秒）
export const RETRY_DELAY = 1000

export default {
  isDev,
  isProd,
  API_BASE_URL,
  APP_TITLE,
  REQUEST_TIMEOUT,
  TOKEN_EXPIRY,
  REFRESH_TOKEN_EXPIRY,
  CACHE_EXPIRY,
  PAGE_SIZE,
  IMAGE_QUALITY,
  MAX_FILE_SIZE,
  QR_CODE_REFRESH_INTERVAL,
  POLLING_INTERVAL,
  MAX_RETRY_COUNT,
  RETRY_DELAY
}
