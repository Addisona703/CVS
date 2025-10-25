import request from '@/utils/request'

export const signupAPI = {
  // 学生报名活动
  signupActivity: (data) => {
    return request.post('/signups', data)
  },

  // 获取报名详情
  getSignupById: (id) => {
    return request.get(`/signups/${id}`)
  },

  // 获取报名列表（分页查询）
  getSignupList: (params) => {
    return request.post('/signups/search', params)
  },

  // 获取当前用户的报名列表
  getMySignups: (data) => {
    return request.post('/signups/my', data)
  },

  // 审核通过单个报名
  approve: (signupId) => {
    return request.put(`/signups/${signupId}/approve`)
  },

  // 拒绝单个报名
  reject: (signupId, data) => {
    return request.put(`/signups/${signupId}/reject`, data)
  },

  // 批量审核通过
  batchApprove: (signupIds) => {
    return request.put('/signups/batch/approve', signupIds)
  },

  // 批量拒绝
  batchReject: (data) => {
    return request.put('/signups/batch/reject', data)
  },

  // 签到 - 注意：签到功能已移至check.js中的checkIn方法
  checkIn: (signupId) => {
    console.warn('signup.checkIn已废弃，请使用checkAPI.studentCheckin')
    return Promise.reject(new Error('请使用checkAPI.studentCheckin方法'))
  },

  // 签退 - 注意：签退功能已移至check.js中的checkOut方法
  checkOut: (signupId, data) => {
    console.warn('signup.checkOut已废弃，请使用checkAPI.studentCheckout')
    return Promise.reject(new Error('请使用checkAPI.studentCheckout方法'))
  },

  // 获取已签到但未签退的人员列表（教师仪表盘用）
  getCheckedInSignups: (data) => {
    return request.post('/signups/checked-in', data)
  },

  // 取消报名
  cancelSignup: (signupId) => {
    return request.delete(`/signups/${signupId}`)
  },

  // 废弃的方法，保留以兼容旧代码
  getMyActivitySignups: (activityId, params) => {
    console.warn('getMyActivitySignups已废弃，请使用getActivitySignups')
    return this.getActivitySignups(activityId, { params: params || {} })
  },

  getUserSignups: (userId, params) => {
    console.warn('getUserSignups已废弃，请使用getMySignups')
    return this.getMySignups({ params: params || {} })
  },

  approveSignup: (signupId, approved) => {
    console.warn('approveSignup已废弃，请使用approve或reject方法')
    if (approved === false) {
      return this.reject(signupId, { rejectReason: '未通过审核' })
    }
    return this.approve(signupId)
  },

  // 获取指定活动的报名列表（新接口）
  getActivitySignups: (activityId, data) => {
    return request.post(`/signups/activity/${activityId}`, data)
  }
}
