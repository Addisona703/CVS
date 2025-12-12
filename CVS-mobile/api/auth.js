/**
 * 认证API服务
 * 提供登录、注册等认证相关的接口
 */
import http from '@/utils/request'

/**
 * 用户登录
 */
export const login = (data) => {
  return http.post('/auth/login', data)
}

/**
 * 用户注册
 */
export const register = (data) => {
  return http.post('/auth/register', data)
}

/**
 * 用户登出
 */
export const logout = () => {
  return http.post('/auth/logout')
}

/**
 * 发送验证码
 */
export const sendCode = (data) => {
  return http.post('/auth/send-code', data)
}

/**
 * 验证验证码
 */
export const verifyCode = (data) => {
  return http.post('/auth/verify-code', data)
}

/**
 * 重置密码（已登录用户修改密码）
 */
export const resetPassword = (data) => {
  return http.post('/auth/reset-password', data)
}

/**
 * 忘记密码（未登录用户通过邮箱重置）
 */
export const forgotPassword = (data) => {
  return http.post('/auth/forgot-password', data)
}

/**
 * 刷新令牌
 */
export const refreshToken = (refreshToken) => {
  return http.post('/auth/refresh', { refreshToken })
}
