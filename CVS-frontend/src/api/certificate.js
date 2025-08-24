import request from '@/utils/request'

export const certificateAPI = {
  // 申请志愿证明
  applyCertificate: (data) => {
    return request.post('/certificates/apply', data)
  },

  // 获取证明详情
  getCertificateById: (id) => {
    return request.get(`/certificates/${id}`)
  },

  // 获取用户的证明列表（按文档路径）
  getUserCertificates: (userId, params) => {
    return request.get(`/certificates/user/${userId}`, { params })
  },
  // 获取当前用户的证明列表
  getMyCertificates: (params) => {
    return request.get('/certificates/my', { params })
  },

  // 审核证明（通过）— 不带请求体
  approveCertificate: (id) => {
    return request.put(`/certificates/${id}/approve`)
  },
  // 审核证明（拒绝）— 可选传拒绝原因（若后端不支持将自动回退）
  rejectCertificate: (id, reason) => {
    if (reason) {
      return request.put(`/certificates/${id}/reject`, { reason })
    }
    return request.put(`/certificates/${id}/reject`)
  },

  // 获取所有证明列表
  getAllCertificates: (params) => {
    return request.get('/certificates', { params })
  }
}
