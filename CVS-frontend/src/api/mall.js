import request from '@/utils/request'

export const mallAPI = {
  // 商品相关API
  // 获取商品列表（分页、筛选）
  getProductList: (data) => {
    return request.post('/products/list', data)
  },

  // 获取商品详情
  getProductDetail: (id) => {
    return request.get(`/products/${id}`)
  },

  // 创建商品（学工处）
  createProduct: (data) => {
    return request.post('/products', data)
  },

  // 更新商品（学工处）
  updateProduct: (id, data) => {
    return request.put(`/products/${id}`, data)
  },

  // 删除商品（学工处）
  deleteProduct: (id) => {
    return request.delete(`/products/${id}`)
  },

  // 更新商品状态（学工处）
  updateProductStatus: (id, status) => {
    return request.put(`/products/${id}/status?status=${status}`)
  },

  // 兑换相关API
  // 兑换商品（学生）
  redeemProduct: (data) => {
    return request.post('/redemptions', data)
  },

  // 获取我的兑换记录（学生）
  getMyRedemptions: (data) => {
    return request.post('/redemptions/my', data)
  },

  // 获取兑换详情
  getRedemptionDetail: (id) => {
    return request.get(`/redemptions/${id}`)
  },

  // 核销凭证（学工处）
  verifyRedemption: (data) => {
    return request.post('/redemptions/verify', data)
  },

  // 取消兑换（学生）
  cancelRedemption: (id) => {
    return request.put(`/redemptions/${id}/cancel`)
  },

  // 获取所有兑换记录（学工处）
  getAllRedemptions: (data) => {
    return request.post('/redemptions/all', data)
  },

  // 根据凭证编号查询兑换记录（学工处）
  getRedemptionByVoucher: (voucherCode) => {
    return request.get(`/redemptions/voucher/${voucherCode}`)
  },

  // 根据状态获取兑换记录（学工处）
  getRedemptionsByStatus: (data, statusType) => {
    return request.post(`/redemptions/by-status?statusType=${statusType}`, data)
  },

  // 分类相关API
  // 获取所有分类
  getCategories: () => {
    return request.get('/categories')
  },

  // 创建分类（学工处）
  createCategory: (data) => {
    return request.post('/categories', data)
  },

  // 更新分类（学工处）
  updateCategory: (id, data) => {
    return request.put(`/categories/${id}`, data)
  },

  // 删除分类（学工处）
  deleteCategory: (id) => {
    return request.delete(`/categories/${id}`)
  },

  // 统计相关API
  // 获取兑换统计（学工处）
  getRedemptionStatistics: (params) => {
    return request.get('/statistics/redemptions', { params })
  },

  // 获取商品排行（学工处）
  getProductRanking: (params) => {
    return request.get('/statistics/ranking', { params })
  },

  // 获取库存预警（学工处）
  getLowStockProducts: () => {
    return request.get('/statistics/low-stock')
  },

  // 导出兑换记录（学工处）
  exportRedemptions: (params) => {
    return request.get('/statistics/export', {
      params,
      responseType: 'blob'
    })
  },

  // 获取用户积分余额
  getUserPoints: () => {
    return request.get('/points/stats/my')
  },

  // 文件上传相关API
  // 上传图片
  uploadImage: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/files/upload/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 删除文件
  deleteFile: (fileName) => {
    return request.delete(`/files/${fileName}`)
  }
}