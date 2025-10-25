import request from '@/utils/request'

export const pointsAPI = {
  // 1. 获取当前用户积分统计信息（包括总积分和排名）
  getCurrentUserPointsStats: () => {
    return request.get('/points/stats/my')
  },

  // 2. 获取积分排行榜（分页显示）
  getPointsRankingPage: (params) => {
    return request.get('/points/ranking/page', { params })
  },

  // 3. 获取所有用户的积分记录（主要体现服务记录积分）
  getAllPointsRecordsWithSearch: (data) => {
    return request.post('/points/records/all', data)
  },

  // 4. 发放积分（管理员功能）
  awardPoints: (data) => {
    return request.post('/points/award', data)
  }
}
