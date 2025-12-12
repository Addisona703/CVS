/**
 * 活动API服务
 * 提供志愿活动相关的接口
 */
import http from '@/utils/request'

/**
 * 创建活动
 */
export const createActivity = (data) => {
  return http.post('/activities', data)
}

/**
 * 获取活动详情
 */
export const getActivityById = (id) => {
  return http.get(`/activities/${id}`)
}

/**
 * 更新活动
 */
export const updateActivity = (data) => {
  return http.put('/activities', data)
}

/**
 * 分页查询活动列表
 */
export const getActivityList = (params) => {
  return http.post('/activities/search', params)
}

/**
 * 发布活动
 */
export const publishActivity = (id) => {
  return http.post(`/activities/${id}/publish`)
}

/**
 * 审核活动（学工处）
 */
export const approveActivity = (id, approved, rejectReason) => {
  return http.post(`/activities/${id}/approve`, null, {
    params: { approved, rejectReason }
  })
}

/**
 * 取消活动
 */
export const cancelActivity = (id) => {
  return http.post(`/activities/${id}/cancel`)
}

/**
 * 删除活动
 */
export const deleteActivity = (id) => {
  return http.delete(`/activities/${id}`)
}

/**
 * 获取我创建的活动列表
 */
export const getMyActivities = (params) => {
  return http.post('/activities/my/search', params)
}

/**
 * 获取活动统计数据
 */
export const getActivityStatistics = (days = 7) => {
  return http.get('/activities/statistics', { days })
}
