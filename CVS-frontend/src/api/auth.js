import request from '@/utils/request'

export const authAPI = {
  // 用户登录
  login: (data) => {
    return request.post('/auth/login', data)
  },

  // 用户注册
  register: (data) => {
    return request.post('/auth/register', data)
  },

  // 用户登出
  logout: () => {
    return request.post('/auth/logout')
  },

  // 发送验证码
  sendCode: (data) => {
    return request.post('/auth/send-code', data)
  },

  // 验证验证码
  verifyCode: (data) => {
    return request.post('/auth/verify-code', data)
  },

  // 重置密码（已登录用户修改密码）
  resetPassword: (data) => {
    return request.post('/auth/reset-password', data)
  },

  // 忘记密码（未登录用户通过邮箱重置）
  forgotPassword: (data) => {
    return request.post('/auth/forgot-password', data)
  }
}
