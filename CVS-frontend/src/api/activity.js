import request from '@/utils/request'

export const activityAPI = {
  // 创建活动
  createActivity: (data) => {
    return request.post('/activities', data)  
  },

  // 获取活动详情
  getActivityById: (id) => {
    return request.get(`/activities/${id}`)
  },

  // 更新活动
  updateActivity: (data) => {
    return request.put(`/activities`, data)
  },

  // 分页查询活动列表
  getActivityList: (params) => {
    return request.post('/activities/search', params)
  },

  // 提交活动审核
  publishActivity: (id) => {
    return request.post(`/activities/${id}/publish`)
  },

  // 审核活动（管理员）
  approveActivity: (id, approved, rejectReason) => {
    return request.post(`/activities/${id}/approve`, null, {
      params: { approved, rejectReason }
    })
  },

  // 取消活动
  cancelActivity: (id) => {
    return request.post(`/activities/${id}/cancel`)
  },

  // 删除活动
  deleteActivity: (id) => {
    return request.delete(`/activities/${id}`)
  },

  // 获取我创建的活动列表
  getMyActivities: (params) => {
    return request.post('/activities/my/search', params)
  },

  // 获取活动统计数据
  getActivityStatistics: (days = 7) => {
    return request.get('/activities/statistics', { params: { days } })
  }
}
