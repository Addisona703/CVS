import request from '@/utils/request'

export const signupAPI = {
  // 学生报名活动
  signupActivity: (activityId) => {
    return request.post(`/activities/${activityId}/signup`)
  },

  // 取消报名
  cancelSignup: (activityId) => {
    return request.delete(`/activities/${activityId}/signup`)
  },

  // 获取某活动的报名列表
  getMyActivitySignups: (activityId, params) => {
    return request.get(`/signups/activity/${activityId}`, { params })
  },

  // 获取当前用户的报名列表
  getMySignups: (params) => {
    return request.get('/signups/my', { params })
  },
  // 兼容旧方法：忽略 userId，转调当前用户列表
  getUserSignups: (userId, params) => {
    return request.get('/signups/my', { params })
  },

  // 审核报名（兼容旧签名：approved=false => reject）
  approveSignup: (signupId, approved) => {
    if (approved === false) {
      return request.put(`/signups/${signupId}/reject`)
    }
    return request.put(`/signups/${signupId}/approve`)
  },
  // 审核通过
  approve: (signupId) => {
    return request.put(`/signups/${signupId}/approve`)
  },
  // 审核拒绝
  reject: (signupId) => {
    return request.put(`/signups/${signupId}/reject`)
  },

  // 签到
  checkIn: (signupId) => {
    return request.post(`/signups/${signupId}/signin`)
  },

  // 签退
  checkOut: (signupId) => {
    return request.post(`/signups/${signupId}/signout`)
  }
}
