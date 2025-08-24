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
  updateActivity: (id, data) => {
    return request.put(`/activities/${id}`, data)
  },

  // 分页查询活动列表
  getActivityList: (params) => {
    return request.get('/activities', { params })
  },

  // 发布活动
  publishActivity: (id) => {
    return request.post(`/activities/${id}/publish`)
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
    return request.get('/activities/my', { params })
  }
}
