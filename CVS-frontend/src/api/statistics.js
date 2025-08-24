import request from '@/utils/request'

export const statisticsAPI = {
  // 获取管理员仪表板统计
  getAdminDashboardStats() {
    return request({
      url: '/statistics/admin-dashboard',
      method: 'get'
    })
  },

  // 获取教师仪表板统计
  getTeacherDashboardStats() {
    return request({
      url: '/statistics/teacher-dashboard',
      method: 'get'
    })
  },

  // 获取学生仪表板统计
  getStudentDashboardStats() {
    return request({
      url: '/statistics/student-dashboard',
      method: 'get'
    })
  }
}
