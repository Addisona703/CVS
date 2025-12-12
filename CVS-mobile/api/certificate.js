/**
 * 证明API服务
 * 提供志愿服务证明相关的接口
 */
import http from '@/utils/request'

/**
 * 申请志愿证明
 */
export const createCertificate = (data) => {
  return http.post('/certificates', data)
}

/**
 * 获取证明详情
 */
export const getCertificateById = (id) => {
  return http.get(`/certificates/${id}`)
}

/**
 * 根据证明编号查询证明
 */
export const getCertificateByNumber = (certificateNumber) => {
  return http.get(`/certificates/number/${certificateNumber}`)
}

/**
 * 获取当前用户的证明列表
 */
export const getMyCertificates = (params) => {
  return http.get('/certificates/my', params)
}

/**
 * 获取所有证明列表（学工处）
 */
export const getCertificateList = (params) => {
  return http.get('/certificates/all', params)
}

/**
 * 获取待审核的证明列表（学工处）
 */
export const getPendingCertificates = (params) => {
  return http.get('/certificates/pending', params)
}

/**
 * 审批证明申请（学工处）
 */
export const approveCertificate = (data) => {
  return http.post('/certificates/approve', data)
}

/**
 * 删除证明申请
 */
export const deleteCertificate = (id) => {
  return http.delete(`/certificates/${id}`)
}

/**
 * 统计用户的证明数量
 */
export const countCertificatesByUser = (userId) => {
  return http.get(`/certificates/count/${userId}`)
}

/**
 * 预览证书PDF
 */
export const previewCertificate = (id) => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://192.168.155.104:8000/api'
  return `${baseUrl}/certificates/${id}/preview`
}

/**
 * 下载证书PDF
 * 注意：此函数已废弃，下载逻辑直接在页面中处理
 */
export const downloadCertificate = (id) => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://192.168.155.104:8000/api'
  return `${baseUrl}/certificates/${id}/download`
}
