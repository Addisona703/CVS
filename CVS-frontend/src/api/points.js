import request from '@/utils/request'

export const pointsAPI = {
  // 创建积分记录（按文档：POST /points）
  createPointsRecord: (data) => {
    return request.post('/points', data)
  },

  // 获取用户积分统计（按文档：GET /points/user/{userId}）
  getUserPoints: (userId) => {
    return request.get(`/points/user/${userId}`)
  },
  // 获取当前用户积分统计
  getMyPoints: () => {
    return request.get('/points/my')
  },

  // 获取用户积分记录（按文档：GET /points/records/user/{userId}）
  getUserPointsRecords: (userId, params) => {
    return request.get(`/points/records/user/${userId}`, { params })
  },
  // 获取当前用户积分记录
  getMyPointsRecords: (params) => {
    return request.get('/points/records/my', { params })
  },

  // 获取积分排行榜
  getPointsRanking: (params) => {
    return request.get('/points/ranking', { params })
  },

  // 获取所有积分记录列表
  getAllPointsRecords: (params) => {
    return request.get('/points/records', { params })
  }
}
