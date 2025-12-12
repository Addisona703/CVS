/**
 * 报名API服务
 * 提供活动报名相关的接口
 */
import http from '@/utils/request'

/**
 * 学生报名活动
 */
export const createSignup = (data) => {
  return http.post('/signups', data)
}

/**
 * 获取报名详情
 */
export const getSignupById = (id) => {
  return http.get(`/signups/${id}`)
}

/**
 * 获取报名列表（分页查询）
 */
export const getSignupList = (params) => {
  return http.post('/signups/search', params)
}

/**
 * 获取当前用户的报名列表
 */
export const getMySignups = (data) => {
  return http.post('/signups/my', data)
}

/**
 * 审核通过单个报名
 */
export const approveSignup = (signupId, data = {}) => {
  return http.put(`/signups/${signupId}/approve`, data)
}

/**
 * 拒绝单个报名
 */
export const rejectSignup = (signupId, data = {}) => {
  return http.put(`/signups/${signupId}/reject`, data)
}

/**
 * 批量审核通过
 */
export const batchApprove = (signupIds) => {
  return http.put('/signups/batch/approve', signupIds)
}

/**
 * 批量拒绝
 */
export const batchReject = (data) => {
  return http.put('/signups/batch/reject', data)
}

/**
 * 获取已签到但未签退的人员列表（教师仪表盘用）
 */
export const getCheckedInSignups = (data) => {
  return http.post('/signups/checked-in', data)
}

/**
 * 取消报名
 */
export const cancelSignup = (signupId) => {
  return http.delete(`/signups/${signupId}`)
}

/**
 * 获取指定活动的报名列表
 */
export const getActivitySignups = (activityId, data) => {
  return http.post(`/signups/activity/${activityId}`, data)
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
