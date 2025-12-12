/**
 * 积分API服务
 * 提供积分相关的接口
 * 与 PC 端保持一致
 */
import http from '@/utils/request'

/**
 * 获取当前用户积分统计信息（包括总积分和排名）
 */
export const getCurrentUserPointsStats = () => {
  return http.get('/points/stats/my')
}

/**
 * 获取积分排行榜（分页显示）
 */
export const getPointsRankingPage = (params) => {
  return http.get('/points/ranking/page', params)
}

/**
 * 获取所有用户的积分记录（主要体现服务记录积分）
 */
export const getAllPointsRecords = (data) => {
  return http.post('/points/records/all', data)
}

/**
 * 发放积分（管理员功能）
 */
export const awardPoints = (userId, points) => {
  return http.post('/points/award', null, { params: { userId, points } })
}

/**
 * 获取积分记录列表（别名函数）
 */
export const getPointsRecords = (data) => {
  return getAllPointsRecords(data)
}

/**
 * 调整用户积分（管理员功能）
 */
export const adjustPoints = (userId, points) => {
  return awardPoints(userId, points)
}
