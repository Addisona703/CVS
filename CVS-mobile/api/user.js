/**
 * 用户API服务
 * 提供用户相关的接口
 */
import http from '@/utils/request'

/**
 * 获取用户信息
 */
export const getUserById = (id) => {
  return http.get(`/users/${id}`)
}

/**
 * 获取当前用户信息
 * 需要从 store 中获取当前用户 ID
 */
export const getUserInfo = () => {
  // 从本地存储获取用户信息（uni.getStorageSync 会自动反序列化）
  const userInfo = uni.getStorageSync('userInfo')
  if (!userInfo) {
    return Promise.reject(new Error('未登录'))
  }
  
  const userId = userInfo.id || userInfo.userId
  if (!userId) {
    return Promise.reject(new Error('用户信息不完整'))
  }
  
  return http.get(`/users/${userId}`)
}

/**
 * 更新用户信息
 */
export const updateUser = (data) => {
  return http.put('/users', data)
}

/**
 * 更新当前用户信息
 */
export const updateUserInfo = (data) => {
  // 从本地存储获取用户 ID（uni.getStorageSync 会自动反序列化）
  const userInfo = uni.getStorageSync('userInfo')
  if (!userInfo) {
    return Promise.reject(new Error('未登录'))
  }
  
  const userId = userInfo.id || userInfo.userId
  if (!userId) {
    return Promise.reject(new Error('用户信息不完整'))
  }
  
  // 确保 data 中包含 id
  return http.put('/users', { ...data, id: userId })
}

/**
 * 分页查询用户列表（管理员端）
 */
export const getUserList = (params) => {
  return http.post('/users/search', params)
}

/**
 * 删除用户
 */
export const deleteUser = (id) => {
  return http.delete(`/users/${id}`)
}

/**
 * 上传头像
 */
export const uploadAvatar = (data) => {
  return http.post('/users/avatar', data.filePath, {
    header: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 修改密码
 */
export const changePassword = (data) => {
  return http.post('/users/change-password', data)
}
