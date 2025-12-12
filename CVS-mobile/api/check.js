/**
 * 签到API服务
 * 提供签到签退相关的接口
 */
import http from '@/utils/request'

/**
 * 生成签到令牌
 */
export const generateCheckinToken = (activityId) => {
  return http.post(`/checkin/token/${activityId}`)
}

/**
 * 生成签退令牌
 */
export const generateCheckoutToken = (activityId) => {
  return http.post(`/checkout/token/${activityId}`)
}

/**
 * 学生签到
 */
export const studentCheckin = (payload, config = {}) => {
  return http.post('/checkin', payload, config)
}

/**
 * 学生签退
 */
export const studentCheckout = (payload, config = {}) => {
  return http.post('/checkout', payload, config)
}

/**
 * 获取待签到学生列表
 */
export const getPendingCheckinStudents = (activityId) => {
  return http.get(`/checkin/${activityId}/pending`)
}

/**
 * 获取待签退学生列表
 */
export const getPendingCheckoutStudents = (activityId) => {
  return http.get(`/checkout/${activityId}/pending`)
}

/**
 * 查询待审核的签退列表
 */
export const searchReviews = (data) => {
  return http.post('/review/search', data)
}

/**
 * 教师调整评分与评价
 */
export const reviewSignup = (signupId, data) => {
  return http.patch(`/review/${signupId}`, data)
}

/**
 * 审核通过签退
 */
export const approveCheckout = (signupId, data) => {
  return http.post(`/checkout/${signupId}/approve`, data)
}

/**
 * 拒绝签退
 */
export const rejectCheckout = (signupId, data) => {
  return http.post(`/checkout/${signupId}/reject`, data)
}
