/**
 * 商城API服务
 * 提供积分商城相关的接口
 */
import http from '@/utils/request'

/**
 * 获取商品列表（分页、筛选）
 */
export const getProductList = (data) => {
  return http.post('/products/list', data)
}

/**
 * 获取商品详情
 */
export const getProductById = (id) => {
  return http.get(`/products/${id}`)
}

/**
 * 创建商品（学工处）
 */
export const createProduct = (data) => {
  return http.post('/products', data)
}

/**
 * 更新商品（学工处）
 */
export const updateProduct = (id, data) => {
  return http.put(`/products/${id}`, data)
}

/**
 * 删除商品（学工处）
 */
export const deleteProduct = (id) => {
  return http.delete(`/products/${id}`)
}

/**
 * 更新商品状态（学工处）
 */
export const updateProductStatus = (id, status) => {
  return http.put(`/products/${id}/status?status=${status}`)
}

/**
 * 兑换商品（学生）
 */
export const createRedemption = (data) => {
  return http.post('/redemptions', data)
}

/**
 * 获取我的兑换记录（学生）
 */
export const getMyRedemptions = (data) => {
  return http.post('/redemptions/my', data)
}

/**
 * 获取兑换详情
 */
export const getRedemptionById = (id) => {
  return http.get(`/redemptions/${id}`)
}

/**
 * 核销凭证（学工处）
 * @param {Object} data - { voucherCode: string }
 */
export const verifyRedemption = (data) => {
  console.log('核销API调用，参数:', data)
  console.log('核销API调用，参数类型:', typeof data)
  console.log('核销API调用，参数JSON:', JSON.stringify(data))
  console.log('voucherCode值:', data.voucherCode)
  return http.post('/redemptions/verify', data)
}

/**
 * 取消兑换（学生）
 */
export const cancelRedemption = (id) => {
  return http.put(`/redemptions/${id}/cancel`)
}

/**
 * 获取所有兑换记录（学工处）
 */
export const getRedemptionList = (data) => {
  return http.post('/redemptions/all', data)
}

/**
 * 根据状态获取兑换记录列表（学工处）
 * @param {Object} data - 分页和查询参数
 * @param {String} statusType - 状态类型：TODAY(今日核销)、VERIFIED(累计核销)、PENDING(待核销)
 */
export const getRedemptionsByStatus = (data, statusType) => {
  return http.post(`/redemptions/by-status?statusType=${statusType}`, data)
}

/**
 * 根据凭证编号查询兑换记录（学工处）
 */
export const getRedemptionByVoucher = (voucherCode) => {
  return http.get(`/redemptions/voucher/${voucherCode}`)
}

/**
 * 获取所有分类
 */
export const getCategoryList = () => {
  return http.get('/categories')
}

/**
 * 创建分类（学工处）
 */
export const createCategory = (data) => {
  return http.post('/categories', data)
}

/**
 * 更新分类（学工处）
 */
export const updateCategory = (id, data) => {
  return http.put(`/categories/${id}`, data)
}

/**
 * 删除分类（学工处）
 */
export const deleteCategory = (id) => {
  return http.delete(`/categories/${id}`)
}

/**
 * 获取兑换统计（学工处）
 */
export const getRedemptionStatistics = (params) => {
  return http.get('/statistics/redemptions', params)
}

/**
 * 获取商品排行（学工处）
 */
export const getProductRanking = (params) => {
  return http.get('/statistics/ranking', params)
}

/**
 * 获取库存预警（学工处）
 */
export const getLowStockProducts = () => {
  return http.get('/statistics/low-stock')
}

/**
 * 上传图片
 */
export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/files/upload/image', formData, {
    header: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
