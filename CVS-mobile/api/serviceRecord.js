/**
 * 服务记录API服务
 * 提供志愿服务记录相关的接口
 */
import http from '@/utils/request'

/**
 * 获取服务记录详情
 */
export const getServiceRecordById = (id) => {
  return http.get(`/service-records/${id}`)
}

/**
 * 获取我的服务记录（学生端）
 */
export const getMyServiceRecords = (data) => {
  return http.post('/service-records/my', data)
}

/**
 * 获取教师管理的服务记录
 */
export const getManagedServiceRecords = (data) => {
  return http.post('/service-records/managed', data)
}

/**
 * 获取用户服务统计数据
 */
export const getUserServiceStats = (userId) => {
  return http.get(`/service-records/stats/${userId}`)
}

/**
 * 获取我的服务统计数据
 */
export const getMyServiceStats = () => {
  return http.get('/service-records/stats/my')
}

/**
 * 获取所有服务记录（管理员端）
 */
export const getAllServiceRecords = (data) => {
  return http.post('/service-records/all', data)
}

/**
 * 获取服务记录列表（管理员端）
 * 别名函数，为了兼容性
 */
export const getServiceRecordList = (data) => {
  return getAllServiceRecords(data)
}
