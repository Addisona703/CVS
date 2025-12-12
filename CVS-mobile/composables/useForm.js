/**
 * 表单处理组合式函数
 * 提供表单验证、防重复提交、表单重置等功能
 */
import { ref, reactive, computed } from 'vue'
import { validateEmail, validatePhone, validatePassword } from '@/utils/validate'

/**
 * 使用表单功能
 * @param {Object} initialValues - 表单初始值
 * @param {Object} rules - 验证规则
 * @returns {Object} 表单状态和方法
 */
export function useForm(initialValues = {}, rules = {}) {
  // 表单数据
  const formData = reactive({ ...initialValues })

  // 表单错误
  const errors = reactive({})

  // 提交状态
  const submitting = ref(false)

  // 是否已验证
  const validated = ref(false)

  // 是否有错误
  const hasErrors = computed(() => {
    return Object.keys(errors).some(key => errors[key])
  })

  // 是否可以提交
  const canSubmit = computed(() => {
    return !submitting.value && !hasErrors.value
  })

  /**
   * 验证单个字段
   * @param {string} field - 字段名
   * @returns {boolean} 是否验证通过
   */
  const validateField = (field) => {
    const rule = rules[field]
    if (!rule) {
      return true
    }

    const value = formData[field]

    // 必填验证
    if (rule.required && !value) {
      errors[field] = rule.message || `${field}不能为空`
      return false
    }

    // 如果值为空且非必填，跳过其他验证
    if (!value && !rule.required) {
      errors[field] = ''
      return true
    }

    // 类型验证
    if (rule.type) {
      switch (rule.type) {
        case 'email':
          if (!validateEmail(value)) {
            errors[field] = rule.message || '邮箱格式不正确'
            return false
          }
          break

        case 'phone':
          if (!validatePhone(value)) {
            errors[field] = rule.message || '手机号格式不正确'
            return false
          }
          break

        case 'password':
          const passwordValidation = validatePassword(value)
          if (!passwordValidation.valid) {
            errors[field] = rule.message || passwordValidation.message
            return false
          }
          break

        case 'number':
          if (isNaN(value)) {
            errors[field] = rule.message || '必须是数字'
            return false
          }
          break

        case 'url':
          const urlPattern = /^https?:\/\/.+/
          if (!urlPattern.test(value)) {
            errors[field] = rule.message || 'URL格式不正确'
            return false
          }
          break
      }
    }

    // 最小长度验证
    if (rule.minLength && value.length < rule.minLength) {
      errors[field] = rule.message || `长度不能少于${rule.minLength}个字符`
      return false
    }

    // 最大长度验证
    if (rule.maxLength && value.length > rule.maxLength) {
      errors[field] = rule.message || `长度不能超过${rule.maxLength}个字符`
      return false
    }

    // 最小值验证
    if (rule.min !== undefined && Number(value) < rule.min) {
      errors[field] = rule.message || `不能小于${rule.min}`
      return false
    }

    // 最大值验证
    if (rule.max !== undefined && Number(value) > rule.max) {
      errors[field] = rule.message || `不能大于${rule.max}`
      return false
    }

    // 正则验证
    if (rule.pattern && !rule.pattern.test(value)) {
      errors[field] = rule.message || '格式不正确'
      return false
    }

    // 自定义验证函数
    if (rule.validator && typeof rule.validator === 'function') {
      const result = rule.validator(value, formData)
      if (result !== true) {
        errors[field] = result || rule.message || '验证失败'
        return false
      }
    }

    // 验证通过，清除错误
    errors[field] = ''
    return true
  }

  /**
   * 验证所有字段
   * @returns {boolean} 是否全部验证通过
   */
  const validate = () => {
    validated.value = true
    let isValid = true

    // 验证所有有规则的字段
    Object.keys(rules).forEach(field => {
      if (!validateField(field)) {
        isValid = false
      }
    })

    return isValid
  }

  /**
   * 清除验证错误
   * @param {string} field - 字段名（可选，不传则清除所有）
   */
  const clearValidation = (field) => {
    if (field) {
      errors[field] = ''
    } else {
      Object.keys(errors).forEach(key => {
        errors[key] = ''
      })
      validated.value = false
    }
  }

  /**
   * 重置表单
   */
  const resetForm = () => {
    // 重置表单数据
    Object.keys(formData).forEach(key => {
      formData[key] = initialValues[key]
    })

    // 清除验证错误
    clearValidation()

    // 重置提交状态
    submitting.value = false
  }

  /**
   * 设置表单值
   * @param {Object} values - 要设置的值
   */
  const setValues = (values) => {
    Object.keys(values).forEach(key => {
      if (key in formData) {
        formData[key] = values[key]
      }
    })
  }

  /**
   * 设置单个字段值
   * @param {string} field - 字段名
   * @param {any} value - 字段值
   */
  const setValue = (field, value) => {
    formData[field] = value

    // 如果已经验证过，实时验证该字段
    if (validated.value) {
      validateField(field)
    }
  }

  /**
   * 获取表单值
   * @returns {Object} 表单数据
   */
  const getValues = () => {
    return { ...formData }
  }

  /**
   * 提交表单
   * @param {Function} onSubmit - 提交处理函数
   * @returns {Promise<any>} 提交结果
   */
  const handleSubmit = async (onSubmit) => {
    // 验证表单
    if (!validate()) {
      // 显示第一个错误
      const firstError = Object.values(errors).find(error => error)
      if (firstError) {
        uni.showToast({
          title: firstError,
          icon: 'none'
        })
      }
      return
    }

    // 防重复提交
    if (submitting.value) {
      uni.showToast({
        title: '请勿重复提交',
        icon: 'none'
      })
      return
    }

    submitting.value = true

    try {
      const result = await onSubmit(getValues())
      return result

    } catch (error) {
      console.error('表单提交失败:', error)
      throw error

    } finally {
      submitting.value = false
    }
  }

  /**
   * 设置字段错误
   * @param {string} field - 字段名
   * @param {string} message - 错误消息
   */
  const setError = (field, message) => {
    errors[field] = message
  }

  /**
   * 设置多个字段错误
   * @param {Object} fieldErrors - 字段错误对象
   */
  const setErrors = (fieldErrors) => {
    Object.keys(fieldErrors).forEach(field => {
      errors[field] = fieldErrors[field]
    })
  }

  return {
    // 状态
    formData,
    errors,
    submitting,
    validated,
    hasErrors,
    canSubmit,

    // 方法
    validateField,
    validate,
    clearValidation,
    resetForm,
    setValues,
    setValue,
    getValues,
    handleSubmit,
    setError,
    setErrors
  }
}

/**
 * 创建表单规则
 * @param {Object} config - 规则配置
 * @returns {Object} 表单规则
 */
export function createFormRules(config) {
  const rules = {}

  Object.keys(config).forEach(field => {
    const fieldConfig = config[field]

    if (typeof fieldConfig === 'string') {
      // 简单的必填规则
      rules[field] = {
        required: true,
        message: fieldConfig
      }
    } else if (Array.isArray(fieldConfig)) {
      // 多个规则
      rules[field] = fieldConfig
    } else {
      // 单个规则对象
      rules[field] = fieldConfig
    }
  })

  return rules
}
