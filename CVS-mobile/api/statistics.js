/**
 * 统计API服务
 * 提供统计数据相关的接口
 * 与 PC 端保持一致
 */
import http from '@/utils/request'

/**
 * 获取管理员仪表板统计
 */
export const getAdminDashboardStats = () => {
  return http.get('/statistics/admin-dashboard')
}

/**
 * 获取教师仪表板统计
 */
export const getTeacherDashboardStats = () => {
  return http.get('/statistics/teacher-dashboard')
}

/**
 * 获取学生仪表板统计
 */
export const getStudentDashboardStats = () => {
  return http.get('/statistics/student-dashboard')
}

/**
 * 获取兑换统计
 */
export const getRedemptionStatistics = (params) => {
  return http.get('/statistics/redemptions', params)
}

/**
 * 获取商品兑换排行
 */
export const getProductRanking = (limit = 10) => {
  return http.get('/statistics/ranking', { limit })
}

/**
 * 获取库存预警列表
 */
export const getLowStockProducts = () => {
  return http.get('/statistics/low-stock')
}

/**
 * 导出兑换记录Excel
 */
export const exportRedemptions = (params) => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://192.168.155.104:8000/api'
  const token = uni.getStorageSync('token')
  const queryString = params ? `?${new URLSearchParams(params).toString()}` : ''
  return `${baseUrl}/statistics/export${queryString}&token=${token}`
}

/**
 * 获取核销统计数据
 */
export const getVerifyStatistics = () => {
  return http.get('/statistics/verify-stats')
}
