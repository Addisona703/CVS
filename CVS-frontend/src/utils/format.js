/**
 * 格式化日期时间
 * @param {string|Date} dateTime 日期时间
 * @param {string} format 格式化模式
 * @returns {string} 格式化后的字符串
 */
export function formatDateTime(dateTime, format = 'YYYY-MM-DD HH:mm') {
  if (!dateTime) return '-'
  
  const date = typeof dateTime === 'string' ? new Date(dateTime) : dateTime
  
  if (isNaN(date.getTime())) return '-'
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化日期
 * @param {string|Date} date 日期
 * @returns {string} 格式化后的字符串
 */
export function formatDate(date) {
  return formatDateTime(date, 'YYYY-MM-DD')
}

/**
 * 格式化时间
 * @param {string|Date} time 时间
 * @returns {string} 格式化后的字符串
 */
export function formatTime(time) {
  return formatDateTime(time, 'HH:mm')
}

/**
 * 格式化文件大小
 * @param {number} bytes 字节数
 * @returns {string} 格式化后的字符串
 */
export function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 格式化数字
 * @param {number} num 数字
 * @param {number} precision 精度
 * @returns {string} 格式化后的字符串
 */
export function formatNumber(num, precision = 2) {
  if (typeof num !== 'number' || isNaN(num)) return '0'
  return num.toFixed(precision)
}

/**
 * 格式化百分比
 * @param {number} num 数字 (0-1 或 0-100)
 * @param {number} precision 精度
 * @returns {string} 格式化后的百分比字符串
 */
export function formatPercent(num, precision = 1) {
  if (typeof num !== 'number' || isNaN(num)) return '0%'
  
  // 如果数字小于等于1，认为是小数形式的百分比
  const percentage = num <= 1 ? num * 100 : num
  
  return percentage.toFixed(precision) + '%'
}