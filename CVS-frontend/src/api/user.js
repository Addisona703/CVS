import request from '@/utils/request'

export const userAPI = {
  // 获取用户信息
  getUserById: (id) => {
    return request.get(`/users/${id}`)
  },

  // 更新用户信息
  updateUser: (id, data) => {
    return request.put(`/users/${id}`, data)
  },

  // 分页查询用户列表
  getUserList: (params) => {
    return request.get('/users', { params })
  },

  // 删除用户
  deleteUser: (id) => {
    return request.delete(`/users/${id}`)
  }
}
