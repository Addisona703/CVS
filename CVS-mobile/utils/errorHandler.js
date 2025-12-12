/**
 * 错误处理工具
 * 提供统一的错误处理和错误码定义
 */

/**
 * 错误代码常量
 */
export const ErrorCode = {
  // 网络错误
  NETWORK_ERROR: 'NETWORK_ERROR',
  TIMEOUT: 'TIMEOUT',
  OFFLINE: 'OFFLINE',

  // 认证错误
  UNAUTHORIZED: 'UNAUTHORIZED',
  TOKEN_EXPIRED: 'TOKEN_EXPIRED',
  TOKEN_INVALID: 'TOKEN_INVALID',
  FORBIDDEN: 'FORBIDDEN',
  DEVICE_MISMATCH: 'DEVICE_MISMATCH',

  // 业务错误
  VALIDATION_ERROR: 'VALIDATION_ERROR',
  NOT_FOUND: 'NOT_FOUND',
  CONFLICT: 'CONFLICT',
  QUOTA_EXCEEDED: 'QUOTA_EXCEEDED',
  ALREADY_EXISTS: 'ALREADY_EXISTS',
  OPERATION_FAILED: 'OPERATION_FAILED',

  // 签到相关错误
  QR_CODE_EXPIRED: 'QR_CODE_EXPIRED',
  QR_CODE_INVALID: 'QR_CODE_INVALID',
  ALREADY_CHECKED_IN: 'ALREADY_CHECKED_IN',
  NOT_CHECKED_IN: 'NOT_CHECKED_IN',
  LOCATION_MISMATCH: 'LOCATION_MISMATCH',

  // 报名相关错误
  SIGNUP_CLOSED: 'SIGNUP_CLOSED',
  ACTIVITY_FULL: 'ACTIVITY_FULL',
  ALREADY_SIGNED_UP: 'ALREADY_SIGNED_UP',

  // 积分相关错误
  INSUFFICIENT_POINTS: 'INSUFFICIENT_POINTS',
  PRODUCT_OUT_OF_STOCK: 'PRODUCT_OUT_OF_STOCK',
  REDEMPTION_EXPIRED: 'REDEMPTION_EXPIRED',
  ALREADY_REDEEMED: 'ALREADY_REDEEMED',

  // 系统错误
  SERVER_ERROR: 'SERVER_ERROR',
  DATABASE_ERROR: 'DATABASE_ERROR',
  UNKNOWN_ERROR: 'UNKNOWN_ERROR'
}

/**
 * 错误消息映射
 */
export const ErrorMessage = {
  // 网络错误
  [ErrorCode.NETWORK_ERROR]: '网络连接失败，请检查网络设置',
  [ErrorCode.TIMEOUT]: '请求超时，请稍后重试',
  [ErrorCode.OFFLINE]: '当前处于离线状态，部分功能不可用',

  // 认证错误
  [ErrorCode.UNAUTHORIZED]: '未授权，请先登录',
  [ErrorCode.TOKEN_EXPIRED]: '登录已过期，请重新登录',
  [ErrorCode.TOKEN_INVALID]: '登录信息无效，请重新登录',
  [ErrorCode.FORBIDDEN]: '没有权限访问该资源',
  [ErrorCode.DEVICE_MISMATCH]: '检测到异常登录，请重新登录',

  // 业务错误
  [ErrorCode.VALIDATION_ERROR]: '输入数据不符合要求，请检查后重试',
  [ErrorCode.NOT_FOUND]: '请求的资源不存在',
  [ErrorCode.CONFLICT]: '操作冲突，请刷新后重试',
  [ErrorCode.QUOTA_EXCEEDED]: '已达到操作上限',
  [ErrorCode.ALREADY_EXISTS]: '该记录已存在',
  [ErrorCode.OPERATION_FAILED]: '操作失败，请稍后重试',

  // 签到相关错误
  [ErrorCode.QR_CODE_EXPIRED]: '二维码已过期，请联系教师刷新',
  [ErrorCode.QR_CODE_INVALID]: '二维码无效，请扫描正确的二维码',
  [ErrorCode.ALREADY_CHECKED_IN]: '您已签到，请勿重复签到',
  [ErrorCode.NOT_CHECKED_IN]: '请先签到再签退',
  [ErrorCode.LOCATION_MISMATCH]: '您不在活动地点附近，无法签到',

  // 报名相关错误
  [ErrorCode.SIGNUP_CLOSED]: '报名已截止',
  [ErrorCode.ACTIVITY_FULL]: '活动名额已满',
  [ErrorCode.ALREADY_SIGNED_UP]: '您已报名该活动',

  // 积分相关错误
  [ErrorCode.INSUFFICIENT_POINTS]: '积分不足，无法兑换',
  [ErrorCode.PRODUCT_OUT_OF_STOCK]: '商品库存不足',
  [ErrorCode.REDEMPTION_EXPIRED]: '兑换凭证已过期',
  [ErrorCode.ALREADY_REDEEMED]: '该凭证已核销',

  // 系统错误
  [ErrorCode.SERVER_ERROR]: '服务器错误，请稍后重试',
  [ErrorCode.DATABASE_ERROR]: '数据库错误，请联系管理员',
  [ErrorCode.UNKNOWN_ERROR]: '未知错误，请联系管理员'
}

/**
 * 错误恢复建议
 */
export const ErrorRecovery = {
  [ErrorCode.NETWORK_ERROR]: {
    action: 'retry',
    message: '点击重试',
    canRetry: true
  },
  [ErrorCode.TIMEOUT]: {
    action: 'retry',
    message: '点击重试',
    canRetry: true
  },
  [ErrorCode.OFFLINE]: {
    action: 'wait',
    message: '等待网络恢复',
    canRetry: false
  },
  [ErrorCode.TOKEN_EXPIRED]: {
    action: 'login',
    message: '重新登录',
    canRetry: false
  },
  [ErrorCode.QR_CODE_EXPIRED]: {
    action: 'refresh',
    message: '刷新二维码',
    canRetry: true
  },
  [ErrorCode.ACTIVITY_FULL]: {
    action: 'back',
    message: '返回列表',
    canRetry: false
  },
  [ErrorCode.INSUFFICIENT_POINTS]: {
    action: 'back',
    message: '返回商城',
    canRetry: false
  }
}

/**
 * 全局错误处理器
 */
export class ErrorHandler {
  /**
   * 处理错误
   * @param {Error|Object} error 错误对象
   * @param {Object} options 选项
   */
  static handle(error, options = {}) {
    console.error('ErrorHandler.handle 被调用:', error)

    const { showToast = true, showModal = false, callback } = options

    // 解析错误
    const errorInfo = this.parseError(error)
    console.log('解析后的错误信息:', errorInfo)

    // 显示错误提示
    if (showModal) {
      this.showErrorModal(errorInfo, callback)
    } else if (showToast) {
      this.showErrorToast(errorInfo.message)
    }

    // 特殊错误处理
    console.log('调用 handleSpecialError, code:', errorInfo.code)
    this.handleSpecialError(errorInfo)

    // 执行回调
    if (callback && typeof callback === 'function') {
      callback(errorInfo)
    }

    return errorInfo
  }

  /**
   * 解析错误
   * @param {Error|Object} error 错误对象
   * @returns {Object} 错误信息
   */
  static parseError(error) {
    console.log('parseError - 原始错误对象:', error)
    console.log('parseError - error.response:', error.response)
    console.log('parseError - error.statusCode:', error.statusCode)
    console.log('parseError - error.code:', error.code)
    
    // 网络错误
    if (!error.response && error.errMsg) {
      if (error.errMsg.includes('timeout')) {
        return {
          code: ErrorCode.TIMEOUT,
          message: ErrorMessage[ErrorCode.TIMEOUT],
          recovery: ErrorRecovery[ErrorCode.TIMEOUT]
        }
      }
      if (error.errMsg.includes('fail')) {
        return {
          code: ErrorCode.NETWORK_ERROR,
          message: ErrorMessage[ErrorCode.NETWORK_ERROR],
          recovery: ErrorRecovery[ErrorCode.NETWORK_ERROR]
        }
      }
    }

    // HTTP错误 - 优先检查 error.statusCode（直接在 error 对象上）
    if (error.statusCode) {
      console.log('parseError - 使用 error.statusCode:', error.statusCode)
      const errorCode = this.mapHttpStatusToErrorCode(error.statusCode)
      const message = error.message || ErrorMessage[errorCode] || ErrorMessage[ErrorCode.UNKNOWN_ERROR]

      return {
        code: errorCode,
        message,
        recovery: ErrorRecovery[errorCode],
        data: error.data
      }
    }

    // HTTP错误 - 检查 error.response.status
    if (error.response) {
      const { status, statusCode, data } = error.response
      const httpStatus = status || statusCode
      
      console.log('parseError - 使用 error.response.status:', httpStatus)

      // 根据HTTP状态码映射错误
      const errorCode = this.mapHttpStatusToErrorCode(httpStatus)
      const message = data?.message || ErrorMessage[errorCode] || ErrorMessage[ErrorCode.UNKNOWN_ERROR]

      return {
        code: errorCode,
        message,
        recovery: ErrorRecovery[errorCode],
        data: data
      }
    }

    // 业务错误 - 先检查是否有 code 属性
    if (error.code) {
      console.log('parseError - 检测到业务错误, code:', error.code)
      console.log('parseError - ErrorMessage 中是否存在:', ErrorMessage[error.code])
      
      // 如果 code 在 ErrorMessage 中定义，使用它
      if (ErrorMessage[error.code]) {
        return {
          code: error.code,
          message: error.message || ErrorMessage[error.code],
          recovery: ErrorRecovery[error.code],
          data: error.data
        }
      }
      
      // 如果 code 在 ErrorCode 中定义（即使没有对应的 ErrorMessage），也使用它
      if (Object.values(ErrorCode).includes(error.code)) {
        console.log('parseError - code 在 ErrorCode 中找到')
        return {
          code: error.code,
          message: error.message || ErrorMessage[ErrorCode.UNKNOWN_ERROR],
          recovery: ErrorRecovery[error.code],
          data: error.data
        }
      }
    }

    // 未知错误
    return {
      code: ErrorCode.UNKNOWN_ERROR,
      message: error.message || ErrorMessage[ErrorCode.UNKNOWN_ERROR],
      recovery: null
    }
  }

  /**
   * 映射HTTP状态码到错误代码
   * @param {Number} status HTTP状态码
   * @returns {String} 错误代码
   */
  static mapHttpStatusToErrorCode(status) {
    const statusMap = {
      400: ErrorCode.VALIDATION_ERROR,
      401: ErrorCode.UNAUTHORIZED,
      403: ErrorCode.FORBIDDEN,
      404: ErrorCode.NOT_FOUND,
      409: ErrorCode.CONFLICT,
      429: ErrorCode.QUOTA_EXCEEDED,
      500: ErrorCode.SERVER_ERROR,
      502: ErrorCode.SERVER_ERROR,
      503: ErrorCode.SERVER_ERROR,
      504: ErrorCode.TIMEOUT
    }

    return statusMap[status] || ErrorCode.UNKNOWN_ERROR
  }

  /**
   * 显示错误Toast
   * @param {String} message 错误消息
   */
  static showErrorToast(message) {
    uni.showToast({
      title: message,
      icon: 'none',
      duration: 2000
    })
  }

  /**
   * 显示错误Modal
   * @param {Object} errorInfo 错误信息
   * @param {Function} callback 回调函数
   */
  static showErrorModal(errorInfo, callback) {
    const { message, recovery } = errorInfo

    uni.showModal({
      title: '错误提示',
      content: message,
      showCancel: recovery?.canRetry || false,
      confirmText: recovery?.message || '确定',
      success: (res) => {
        if (res.confirm && callback) {
          callback(errorInfo)
        }
      }
    })
  }

  /**
   * 处理特殊错误
   * @param {Object} errorInfo 错误信息
   */
  static handleSpecialError(errorInfo) {
    const { code } = errorInfo
    console.log('handleSpecialError - 错误代码:', code)
    console.log('ErrorCode.UNAUTHORIZED:', ErrorCode.UNAUTHORIZED)

    // 未授权或token过期，跳转登录
    if (code === ErrorCode.UNAUTHORIZED || code === ErrorCode.TOKEN_EXPIRED || code === ErrorCode.TOKEN_INVALID) {
      console.log('检测到未授权错误，准备跳转登录')
      // 防止重复跳转
      if (!this._isRedirecting) {
        console.log('执行跳转登录')
        this._isRedirecting = true
        this.handleUnauthorized()
      } else {
        console.log('已经在跳转中，跳过')
      }
    }

    // 设备不匹配，清除token并跳转登录
    if (code === ErrorCode.DEVICE_MISMATCH) {
      // 防止重复跳转
      if (!this._isRedirecting) {
        this._isRedirecting = true
        this.handleDeviceMismatch()
      }
    }
  }

  /**
   * 处理未授权错误
   */
  static handleUnauthorized() {
    console.log('handleUnauthorized 被调用')
    
    // 清除token
    uni.removeStorageSync('accessToken')
    uni.removeStorageSync('refreshToken')
    uni.removeStorageSync('tokenTime')
    uni.removeStorageSync('userInfo')
    uni.removeStorageSync('token')
    
    console.log('Token 已清除，准备跳转登录页')

    // 立即跳转到登录页，不延迟
    uni.reLaunch({
      url: '/pages/index/index',
      success: () => {
        console.log('跳转登录页成功')
      },
      fail: (err) => {
        console.error('跳转登录页失败:', err)
      }
    })
  }

  /**
   * 处理设备不匹配错误
   */
  static handleDeviceMismatch() {
    // 清除所有认证信息
    uni.removeStorageSync('accessToken')
    uni.removeStorageSync('refreshToken')
    uni.removeStorageSync('tokenTime')
    uni.removeStorageSync('userInfo')
    uni.removeStorageSync('deviceId')
    uni.removeStorageSync('token')

    // 立即跳转到登录页，不延迟
    uni.reLaunch({
      url: '/pages/index/index'
    })
  }

  /**
   * 记录错误日志
   * @param {Object} errorInfo 错误信息
   */
  static logError(errorInfo) {
    // TODO: 实现错误日志上报
    console.error('Error Log:', {
      ...errorInfo,
      timestamp: new Date().toISOString(),
      userAgent: navigator.userAgent
    })
  }
}

/**
 * 创建业务错误
 * @param {String} code 错误代码
 * @param {String} message 错误消息
 * @param {any} data 附加数据
 * @returns {Error} 错误对象
 */
export function createError(code, message, data = null) {
  const error = new Error(message || ErrorMessage[code])
  error.code = code
  error.data = data
  return error
}

export default {
  ErrorCode,
  ErrorMessage,
  ErrorRecovery,
  ErrorHandler,
  createError
}
