/**
 * 格式化工具函数
 * 提供日期、时间、数字等格式化功能
 */

/**
 * 格式化日期
 * @param {String|Date} date 日期
 * @param {String} format 格式 (YYYY-MM-DD, YYYY-MM-DD HH:mm:ss等)
 * @returns {String} 格式化后的日期
 */
export function formatDate(date, format = 'YYYY-MM-DD') {
  if (!date) return ''

  const d = new Date(date)
  if (isNaN(d.getTime())) return ''

  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化日期时间
 * @param {String|Date} date 日期
 * @returns {String} 格式化后的日期时间
 */
export function formatDateTime(date) {
  return formatDate(date, 'YYYY-MM-DD HH:mm')
}

/**
 * 格式化时间（formatTime别名，为了兼容性）
 * @param {String|Date} date 日期
 * @returns {String} 格式化后的日期时间
 */
export function formatTime(date) {
  return formatDateTime(date)
}

/**
 * 格式化时间（相对时间）
 * @param {String|Date} date 日期
 * @returns {String} 相对时间描述
 */
export function formatRelativeTime(date) {
  if (!date) return ''

  const d = new Date(date)
  if (isNaN(d.getTime())) return ''

  const now = new Date()
  const diff = now.getTime() - d.getTime()

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  const week = 7 * day
  const month = 30 * day

  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < week) {
    return `${Math.floor(diff / day)}天前`
  } else if (diff < month) {
    return `${Math.floor(diff / week)}周前`
  } else {
    return formatDate(date, 'YYYY-MM-DD')
  }
}

/**
 * 格式化时长（分钟转为小时分钟）
 * @param {Number} minutes 分钟数
 * @returns {String} 格式化后的时长
 */
export function formatDuration(minutes) {
  if (!minutes || minutes < 0) return '0分钟'

  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60

  if (hours === 0) {
    return `${mins}分钟`
  } else if (mins === 0) {
    return `${hours}小时`
  } else {
    return `${hours}小时${mins}分钟`
  }
}

/**
 * 格式化数字（千分位）
 * @param {Number} num 数字
 * @param {Number} decimals 小数位数
 * @returns {String} 格式化后的数字
 */
export function formatNumber(num, decimals = 0) {
  if (num === null || num === undefined || isNaN(num)) return '0'

  const parts = Number(num).toFixed(decimals).split('.')
  parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',')

  return parts.join('.')
}

/**
 * 格式化文件大小
 * @param {Number} bytes 字节数
 * @returns {String} 格式化后的文件大小
 */
export function formatFileSize(bytes) {
  if (!bytes || bytes < 0) return '0 B'

  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let size = bytes
  let unitIndex = 0

  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }

  return `${size.toFixed(2)} ${units[unitIndex]}`
}

/**
 * 格式化手机号（隐藏中间4位）
 * @param {String} phone 手机号
 * @returns {String} 格式化后的手机号
 */
export function formatPhone(phone) {
  if (!phone) return ''

  const phoneStr = String(phone)
  if (phoneStr.length !== 11) return phoneStr

  return phoneStr.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 格式化邮箱（隐藏部分字符）
 * @param {String} email 邮箱
 * @returns {String} 格式化后的邮箱
 */
export function formatEmail(email) {
  if (!email) return ''

  const parts = email.split('@')
  if (parts.length !== 2) return email

  const username = parts[0]
  const domain = parts[1]

  if (username.length <= 3) {
    return `${username[0]}***@${domain}`
  }

  const visibleChars = Math.ceil(username.length / 3)
  const hiddenChars = username.length - visibleChars * 2

  return `${username.substring(0, visibleChars)}${'*'.repeat(hiddenChars)}${username.substring(username.length - visibleChars)}@${domain}`
}

/**
 * 格式化百分比
 * @param {Number} value 数值
 * @param {Number} total 总数
 * @param {Number} decimals 小数位数
 * @returns {String} 百分比字符串
 */
export function formatPercentage(value, total, decimals = 0) {
  if (!total || total === 0) return '0%'

  const percentage = (value / total) * 100
  return `${percentage.toFixed(decimals)}%`
}

/**
 * 格式化金额（人民币）
 * @param {Number} amount 金额
 * @param {Boolean} showSymbol 是否显示货币符号
 * @returns {String} 格式化后的金额
 */
export function formatCurrency(amount, showSymbol = true) {
  if (amount === null || amount === undefined || isNaN(amount)) return showSymbol ? '¥0.00' : '0.00'

  const formatted = formatNumber(amount, 2)
  return showSymbol ? `¥${formatted}` : formatted
}

/**
 * 格式化积分
 * @param {Number} points 积分
 * @returns {String} 格式化后的积分
 */
export function formatPoints(points) {
  if (!points || points < 0) return '0'

  return formatNumber(points)
}

/**
 * 格式化活动状态
 * @param {String} status 状态
 * @returns {String} 状态描述
 */
export function formatActivityStatus(status) {
  const statusMap = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    ONGOING: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }

  return statusMap[status] || status
}

/**
 * 格式化报名状态
 * @param {String} status 状态
 * @returns {String} 状态描述
 */
export function formatSignupStatus(status) {
  const statusMap = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝'
  }

  return statusMap[status] || status
}

/**
 * 格式化兑换状态
 * @param {String} status 状态
 * @returns {String} 状态描述
 */
export function formatRedemptionStatus(status) {
  const statusMap = {
    PENDING: '待核销',
    VERIFIED: '已核销',
    EXPIRED: '已过期'
  }

  return statusMap[status] || status
}

/**
 * 格式化证明状态
 * @param {String} status 状态
 * @returns {String} 状态描述
 */
export function formatCertificateStatus(status) {
  const statusMap = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝'
  }

  return statusMap[status] || status
}

/**
 * 格式化用户角色
 * @param {String} role 角色
 * @returns {String} 角色描述
 */
export function formatUserRole(role) {
  const roleMap = {
    STUDENT: '学生',
    TEACHER: '教师',
    ADMIN: '学工处'
  }

  return roleMap[role] || role
}

export default {
  formatDate,
  formatDateTime,
  formatTime,
  formatRelativeTime,
  formatDuration,
  formatNumber,
  formatFileSize,
  formatPhone,
  formatEmail,
  formatPercentage,
  formatCurrency,
  formatPoints,
  formatActivityStatus,
  formatSignupStatus,
  formatRedemptionStatus,
  formatCertificateStatus,
  formatUserRole
}
