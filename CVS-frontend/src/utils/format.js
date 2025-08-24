import dayjs from 'dayjs'
import { DATE_FORMATS } from './constants'

// 格式化日期
export function formatDate(date, format = DATE_FORMATS.DATE) {
  if (!date) return ''
  return dayjs(date).format(format)
}

// 格式化日期时间
export function formatDateTime(date) {
  return formatDate(date, DATE_FORMATS.DATETIME)
}

// 格式化时间
export function formatTime(date) {
  return formatDate(date, DATE_FORMATS.TIME)
}

// 格式化相对时间
export function formatRelativeTime(date) {
  if (!date) return ''
  return dayjs(date).fromNow()
}

// 格式化文件大小
export function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 格式化数字
export function formatNumber(num, precision = 2) {
  if (typeof num !== 'number') return num
  return num.toFixed(precision)
}

// 格式化百分比
export function formatPercent(num, precision = 1) {
  if (typeof num !== 'number') return num
  return (num * 100).toFixed(precision) + '%'
}

// 格式化手机号
export function formatPhone(phone) {
  if (!phone) return ''
  return phone.replace(/(\d{3})(\d{4})(\d{4})/, '$1****$3')
}

// 格式化邮箱
export function formatEmail(email) {
  if (!email) return ''
  const [name, domain] = email.split('@')
  if (name.length <= 2) return email
  return name.substring(0, 2) + '***@' + domain
}

// 截断文本
export function truncateText(text, length = 50) {
  if (!text) return ''
  if (text.length <= length) return text
  return text.substring(0, length) + '...'
}
