/**
 * 验证工具函数
 * 提供各种数据验证功能
 */

/**
 * 验证邮箱
 * @param {String} email 邮箱地址
 * @returns {Boolean} 是否有效
 */
export function validateEmail(email) {
  if (!email) return false

  const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  return emailRegex.test(email)
}

/**
 * 验证手机号（中国大陆）
 * @param {String} phone 手机号
 * @returns {Boolean} 是否有效
 */
export function validatePhone(phone) {
  if (!phone) return false

  const phoneRegex = /^1[3-9]\d{9}$/
  return phoneRegex.test(phone)
}

/**
 * 验证密码（简单版本）
 * @param {String} password 密码
 * @returns {Boolean} 是否有效
 */
export function validatePassword(password) {
  if (!password) return false
  
  const minLength = 6
  const maxLength = 20
  
  return password.length >= minLength && password.length <= maxLength
}

/**
 * 检查密码强度
 * @param {String} password 密码
 * @returns {Number} 强度等级 0-3
 */
export function checkPasswordStrength(password) {
  if (!password) return 0
  
  let strength = 0
  
  // 长度检查
  if (password.length >= 8) strength++
  
  // 包含数字
  if (/\d/.test(password)) strength++
  
  // 包含字母
  if (/[a-zA-Z]/.test(password)) strength++
  
  // 包含大小写字母或特殊字符
  if (/[A-Z]/.test(password) && /[a-z]/.test(password)) {
    strength = Math.min(3, strength + 1)
  } else if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
    strength = Math.min(3, strength + 1)
  }
  
  return Math.min(3, strength)
}

/**
 * 验证密码强度（详细版本）
 * @param {String} password 密码
 * @returns {Object} 验证结果 { valid, message, strength }
 */
export function validatePasswordStrength(password) {
  if (!password) {
    return { valid: false, message: '密码不能为空', strength: 0 }
  }

  const minLength = 8
  const maxLength = 20

  if (password.length < minLength) {
    return { valid: false, message: `密码长度至少${minLength}位`, strength: 0 }
  }

  if (password.length > maxLength) {
    return { valid: false, message: `密码长度不能超过${maxLength}位`, strength: 0 }
  }

  // 检查密码组成
  const hasUpperCase = /[A-Z]/.test(password)
  const hasLowerCase = /[a-z]/.test(password)
  const hasNumber = /\d/.test(password)
  const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(password)

  const strength = [hasUpperCase, hasLowerCase, hasNumber, hasSpecialChar].filter(Boolean).length

  if (strength < 3) {
    return {
      valid: false,
      message: '密码必须包含大小写字母、数字和特殊字符中的至少3种',
      strength
    }
  }

  return { valid: true, message: '密码强度良好', strength }
}

/**
 * 验证用户名
 * @param {String} username 用户名
 * @returns {Boolean} 是否有效
 */
export function validateUsername(username) {
  if (!username) return false
  
  const minLength = 4
  const maxLength = 20
  
  if (username.length < minLength || username.length > maxLength) {
    return false
  }
  
  // 只允许字母、数字、下划线
  const usernameRegex = /^[a-zA-Z0-9_]+$/
  return usernameRegex.test(username)
}

/**
 * 验证用户名（详细版本）
 * @param {String} username 用户名
 * @returns {Object} 验证结果 { valid, message }
 */
export function validateUsernameDetail(username) {
  if (!username) {
    return { valid: false, message: '用户名不能为空' }
  }

  const minLength = 4
  const maxLength = 20

  if (username.length < minLength) {
    return { valid: false, message: `用户名长度至少${minLength}位` }
  }

  if (username.length > maxLength) {
    return { valid: false, message: `用户名长度不能超过${maxLength}位` }
  }

  // 只允许字母、数字、下划线
  const usernameRegex = /^[a-zA-Z0-9_]+$/
  if (!usernameRegex.test(username)) {
    return { valid: false, message: '用户名只能包含字母、数字和下划线' }
  }

  return { valid: true, message: '用户名格式正确' }
}

/**
 * 验证身份证号
 * @param {String} idCard 身份证号
 * @returns {Boolean} 是否有效
 */
export function validateIdCard(idCard) {
  if (!idCard) return false

  // 18位身份证号正则
  const idCardRegex = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/

  if (!idCardRegex.test(idCard)) return false

  // 验证校验码
  const weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
  const checkCodes = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2']

  let sum = 0
  for (let i = 0; i < 17; i++) {
    sum += parseInt(idCard[i]) * weights[i]
  }

  const checkCode = checkCodes[sum % 11]
  return idCard[17].toUpperCase() === checkCode
}

/**
 * 验证URL
 * @param {String} url URL地址
 * @returns {Boolean} 是否有效
 */
export function validateUrl(url) {
  if (!url) return false

  const urlRegex = /^(https?:\/\/)?([\da-z.-]+)\.([a-z.]{2,6})([/\w .-]*)*\/?$/
  return urlRegex.test(url)
}

/**
 * 验证数字
 * @param {any} value 值
 * @param {Object} options 选项 { min, max, integer }
 * @returns {Object} 验证结果 { valid, message }
 */
export function validateNumber(value, options = {}) {
  const { min, max, integer = false } = options

  if (value === null || value === undefined || value === '') {
    return { valid: false, message: '请输入数字' }
  }

  const num = Number(value)

  if (isNaN(num)) {
    return { valid: false, message: '请输入有效的数字' }
  }

  if (integer && !Number.isInteger(num)) {
    return { valid: false, message: '请输入整数' }
  }

  if (min !== undefined && num < min) {
    return { valid: false, message: `数字不能小于${min}` }
  }

  if (max !== undefined && num > max) {
    return { valid: false, message: `数字不能大于${max}` }
  }

  return { valid: true, message: '数字格式正确' }
}

/**
 * 验证日期
 * @param {String|Date} date 日期
 * @param {Object} options 选项 { min, max }
 * @returns {Object} 验证结果 { valid, message }
 */
export function validateDate(date, options = {}) {
  const { min, max } = options

  if (!date) {
    return { valid: false, message: '请选择日期' }
  }

  const d = new Date(date)

  if (isNaN(d.getTime())) {
    return { valid: false, message: '日期格式不正确' }
  }

  if (min) {
    const minDate = new Date(min)
    if (d < minDate) {
      return { valid: false, message: `日期不能早于${min}` }
    }
  }

  if (max) {
    const maxDate = new Date(max)
    if (d > maxDate) {
      return { valid: false, message: `日期不能晚于${max}` }
    }
  }

  return { valid: true, message: '日期格式正确' }
}

/**
 * 验证必填项
 * @param {any} value 值
 * @param {String} fieldName 字段名称
 * @returns {Object} 验证结果 { valid, message }
 */
export function validateRequired(value, fieldName = '此项') {
  if (value === null || value === undefined || value === '') {
    return { valid: false, message: `${fieldName}不能为空` }
  }

  if (typeof value === 'string' && value.trim() === '') {
    return { valid: false, message: `${fieldName}不能为空` }
  }

  if (Array.isArray(value) && value.length === 0) {
    return { valid: false, message: `${fieldName}不能为空` }
  }

  return { valid: true, message: '' }
}

/**
 * 验证字符串长度
 * @param {String} value 字符串
 * @param {Object} options 选项 { min, max }
 * @returns {Object} 验证结果 { valid, message }
 */
export function validateLength(value, options = {}) {
  const { min, max } = options

  if (!value) {
    return { valid: false, message: '请输入内容' }
  }

  const length = String(value).length

  if (min !== undefined && length < min) {
    return { valid: false, message: `长度不能少于${min}个字符` }
  }

  if (max !== undefined && length > max) {
    return { valid: false, message: `长度不能超过${max}个字符` }
  }

  return { valid: true, message: '长度符合要求' }
}

/**
 * 过滤XSS攻击字符
 * @param {String} str 输入字符串
 * @returns {String} 过滤后的字符串
 */
export function filterXSS(str) {
  if (!str) return ''

  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#x27;')
    .replace(/\//g, '&#x2F;')
}

/**
 * 过滤SQL注入字符
 * @param {String} str 输入字符串
 * @returns {String} 过滤后的字符串
 */
export function filterSQL(str) {
  if (!str) return ''

  // 移除常见的SQL注入关键字和特殊字符
  const sqlKeywords = [
    'select',
    'insert',
    'update',
    'delete',
    'drop',
    'create',
    'alter',
    'exec',
    'execute',
    'script',
    'union'
  ]

  let filtered = String(str)

  sqlKeywords.forEach((keyword) => {
    const regex = new RegExp(keyword, 'gi')
    filtered = filtered.replace(regex, '')
  })

  // 移除特殊字符
  filtered = filtered.replace(/[';\\]/g, '')

  return filtered
}

/**
 * 验证表单
 * @param {Object} formData 表单数据
 * @param {Object} rules 验证规则
 * @returns {Object} 验证结果 { valid, errors }
 */
export function validateForm(formData, rules) {
  const errors = {}
  let valid = true

  Object.keys(rules).forEach((field) => {
    const value = formData[field]
    const fieldRules = rules[field]

    for (const rule of fieldRules) {
      let result

      switch (rule.type) {
        case 'required':
          result = validateRequired(value, rule.message || field)
          break
        case 'email':
          if (value) {
            result = validateEmail(value)
              ? { valid: true }
              : { valid: false, message: rule.message || '邮箱格式不正确' }
          } else {
            result = { valid: true }
          }
          break
        case 'phone':
          if (value) {
            result = validatePhone(value)
              ? { valid: true }
              : { valid: false, message: rule.message || '手机号格式不正确' }
          } else {
            result = { valid: true }
          }
          break
        case 'length':
          result = validateLength(value, rule)
          break
        case 'number':
          result = validateNumber(value, rule)
          break
        case 'date':
          result = validateDate(value, rule)
          break
        case 'custom':
          result = rule.validator(value, formData)
          break
        default:
          result = { valid: true }
      }

      if (!result.valid) {
        errors[field] = result.message
        valid = false
        break
      }
    }
  })

  return { valid, errors }
}

export default {
  validateEmail,
  validatePhone,
  validatePassword,
  checkPasswordStrength,
  validatePasswordStrength,
  validateUsername,
  validateUsernameDetail,
  validateIdCard,
  validateUrl,
  validateNumber,
  validateDate,
  validateRequired,
  validateLength,
  filterXSS,
  filterSQL,
  validateForm
}
