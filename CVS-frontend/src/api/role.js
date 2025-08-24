import request from '@/utils/request'

export const roleAPI = {
  // 获取所有角色
  getAll: () => {
    return request.get('/roles')
  },

  // 给用户分配角色
  assignToUser: (userId, data) => {
    // data: { role: string } - role should be 'STUDENT', 'TEACHER', or 'ADMIN'
    return request.post(`/roles/users/${userId}`, data)
  },

  // 获取用户的角色
  getUserRole: (userId) => {
    return request.get(`/roles/users/${userId}`)
  }
}

