// Token管理工具

// 获取Token
export function getToken() {
  return localStorage.getItem('token')
}

// 设置Token
export function setToken(token) {
  localStorage.setItem('token', token)
}

// 移除Token
export function removeToken() {
  localStorage.removeItem('token')
}

// 检查Token是否有效
export function isTokenValid() {
  const token = getToken()
  if (!token) return false
  
  try {
    // 解析JWT token (简单检查，实际应该验证签名)
    const payload = JSON.parse(atob(token.split('.')[1]))
    const currentTime = Date.now() / 1000
    
    return payload.exp > currentTime
  } catch (error) {
    console.error('Token解析失败:', error)
    return false
  }
}

// 获取用户信息
export function getUserInfo() {
  const userInfo = localStorage.getItem('user')
  return userInfo ? JSON.parse(userInfo) : null
}

// 设置用户信息
export function setUserInfo(userInfo) {
  localStorage.setItem('user', JSON.stringify(userInfo))
}

// 移除用户信息
export function removeUserInfo() {
  localStorage.removeItem('user')
}

// 清除所有认证信息
export function clearAuth() {
  removeToken()
  removeUserInfo()
}

// 检查用户是否已登录
export function isAuthenticated() {
  return !!getToken() && !!getUserInfo()
}
