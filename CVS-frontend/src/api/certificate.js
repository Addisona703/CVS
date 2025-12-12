import request from '@/utils/request'

export const certificateAPI = {
  // 申请志愿证明
  applyCertificate: (data) => {
    return request.post('/certificates', data)
  },

  // 获取证明详情
  getCertificateById: (id) => {
    return request.get(`/certificates/${id}`)
  },

  // 根据证明编号查询证明
  getCertificateByNumber: (certificateNumber) => {
    return request.get(`/certificates/number/${certificateNumber}`)
  },

  // 获取当前用户的证明列表
  getMyCertificates: (params) => {
    return request.get('/certificates/my', { params })
  },

  // 获取所有证明列表（学工处）
  getAllCertificates: (params) => {
    return request.get('/certificates/all', { params })
  },

  // 获取待审核的证明列表（学工处）
  getPendingCertificates: (params) => {
    return request.get('/certificates/pending', { params })
  },

  // 审批证明申请（学工处）
  approveCertificate: (data) => {
    return request.post('/certificates/approve', data)
  },

  // 删除证明申请
  deleteCertificate: (id) => {
    return request.delete(`/certificates/${id}`)
  },

  // 统计用户的证明数量
  countCertificatesByUser: (userId) => {
    return request.get(`/certificates/count/${userId}`)
  },

  // 预览证书PDF
  previewCertificate: (id) => {
    return request.get(`/certificates/${id}/preview`, {
      responseType: 'blob'
    })
  },

  // 下载证书PDF
  downloadCertificate: (id) => {
    return request.get(`/certificates/${id}/download`, {
      responseType: 'blob'
    })
  }
}
