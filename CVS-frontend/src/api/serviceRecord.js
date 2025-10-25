import request from '@/utils/request'

export const serviceRecordAPI = {
  // 获取服务记录详情
  getById: (id) => {
    return request.get(`/service-records/${id}`)
  },

  // 获取我的服务记录（学生端）- 使用POST方法和分页DTO
  getMyRecords: (data) => {
    return request.post('/service-records/my', data)
  },

  // 获取教师管理的服务记录 - 使用POST方法和分页DTO
  getManagedRecords: (data) => {
    return request.post('/service-records/managed', data)
  },

  // 获取用户服务统计数据
  getUserStats: (userId) => {
    return request.get(`/service-records/stats/${userId}`)
  },

  // 获取我的服务统计数据
  getMyStats: () => {
    return request.get('/service-records/stats/my')
  },

  // 获取所有服务记录（管理员端）
  getAllRecords: (data) => {
    return request.post('/service-records/all', data)
  },

  // 以下为兼容旧版本的方法（如果其他地方有使用）
  // 创建服务记录 - 已在Controller中移除，通过签退自动创建
  create: (data) => {
    console.warn('create方法已废弃，服务记录通过签退自动创建')
    return Promise.reject(new Error('服务记录创建已移至签退流程'))
  },

  // 更新服务记录 - 已在Controller中移除
  update: (id, data) => {
    console.warn('update方法已废弃')
    return Promise.reject(new Error('服务记录更新功能已移除'))
  },

  // 删除服务记录 - 已在Controller中移除
  remove: (id) => {
    console.warn('remove方法已废弃')
    return Promise.reject(new Error('服务记录删除功能已移除'))
  },

  // 其他废弃方法
  getByUser: (userId, params) => {
    console.warn('getByUser方法已废弃，请使用getUserStats')
    return this.getUserStats(userId)
  },

  getAll: (params) => {
    console.warn('getAll方法已废弃')
    return Promise.reject(new Error('getAll方法已废弃'))
  },

  getByActivity: (activityId, params) => {
    console.warn('getByActivity方法已废弃，请使用getManagedRecords')
    return Promise.reject(new Error('getByActivity方法已废弃'))
  },

  generate: (params) => {
    console.warn('generate方法已废弃')
    return Promise.reject(new Error('自动生成功能已移除'))
  }
}

