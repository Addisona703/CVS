import request from '@/utils/request'

export const serviceRecordAPI = {
  // 创建服务记录
  create: (data) => {
    return request.post('/service-records', data)
  },

  // 更新服务记录
  update: (id, data) => {
    return request.put(`/service-records/${id}`, data)
  },

  // 删除服务记录
  remove: (id) => {
    return request.delete(`/service-records/${id}`)
  },

  // 获取某用户的服务记录列表
  getByUser: (userId, params) => {
    return request.get(`/service-records/user/${userId}`, { params })
  },

  // 获取当前用户的服务记录列表
  getMyRecords: (params) => {
    return request.get('/service-records/my', { params })
  },

  // 获取所有服务记录列表
  getAll: (params) => {
    return request.get('/service-records', { params })
  },

  // 获取用户服务统计
  getUserStats: (userId) => {
    return request.get(`/service-records/stats/user/${userId}`)
  },

  // 获取当前用户服务统计
  getMyStats: () => {
    return request.get('/service-records/stats/my')
  },

  // 根据活动获取服务记录
  getByActivity: (activityId, params) => {
    return request.get(`/service-records/activity/${activityId}`, { params })
  },

  // 自动生成服务记录
  generate: (params) => {
    return request.post('/service-records/generate', null, { params })
  }
}

