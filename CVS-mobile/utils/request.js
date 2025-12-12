/**
 * HTTP请求客户端
 * 基于uni.request封装，提供拦截器、错误处理、重试机制等功能
 */

import { API_BASE_URL, REQUEST_TIMEOUT, MAX_RETRY_COUNT, RETRY_DELAY } from '@/config'

/**
 * HTTP客户端类
 */
class HttpClient {
  constructor(config = {}) {
    this.baseURL = config.baseURL || API_BASE_URL
    this.timeout = config.timeout || REQUEST_TIMEOUT
    this.interceptors = {
      request: [],
      response: []
    }
  }

  /**
   * 添加请求拦截器
   * @param {Function} fulfilled 成功回调
   * @param {Function} rejected 失败回调
   */
  useRequestInterceptor(fulfilled, rejected) {
    this.interceptors.request.push({ fulfilled, rejected })
  }

  /**
   * 添加响应拦截器
   * @param {Function} fulfilled 成功回调
   * @param {Function} rejected 失败回调
   */
  useResponseInterceptor(fulfilled, rejected) {
    this.interceptors.response.push({ fulfilled, rejected })
  }

  /**
   * 执行请求拦截器
   * @param {Object} config 请求配置
   * @returns {Promise<Object>} 处理后的配置
   */
  async runRequestInterceptors(config) {
    let processedConfig = config

    for (const interceptor of this.interceptors.request) {
      try {
        if (interceptor.fulfilled) {
          processedConfig = await interceptor.fulfilled(processedConfig)
        }
      } catch (error) {
        if (interceptor.rejected) {
          return interceptor.rejected(error)
        }
        throw error
      }
    }

    return processedConfig
  }

  /**
   * 执行响应拦截器
   * @param {Object} response 响应数据
   * @returns {Promise<any>} 处理后的响应
   */
  async runResponseInterceptors(response) {
    let processedResponse = response

    for (const interceptor of this.interceptors.response) {
      try {
        if (interceptor.fulfilled) {
          processedResponse = await interceptor.fulfilled(processedResponse)
        }
      } catch (error) {
        if (interceptor.rejected) {
          return interceptor.rejected(error)
        }
        throw error
      }
    }

    return processedResponse
  }

  /**
   * 发送请求
   * @param {Object} config 请求配置
   * @returns {Promise<any>} 响应数据
   */
  async request(config) {
    // 执行请求拦截器
    const processedConfig = await this.runRequestInterceptors(config)

    // 合并配置
    let requestUrl = processedConfig.url
    if (processedConfig.params && typeof processedConfig.params === 'object') {
      const queryString = Object.keys(processedConfig.params)
        .map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(processedConfig.params[key])}`)
        .join('&')
      if (queryString) {
        requestUrl += (requestUrl.includes('?') ? '&' : '?') + queryString
      }
    }

    const finalConfig = {
      url: requestUrl.startsWith('http')
        ? requestUrl
        : this.baseURL + requestUrl,
      method: processedConfig.method || 'GET',
      data: processedConfig.data,
      header: processedConfig.header || {},
      timeout: processedConfig.timeout || this.timeout
    }

    return new Promise((resolve, reject) => {
      uni.request({
        ...finalConfig,
        success: async (res) => {
          try {
            // 将请求配置附加到响应，便于后续拦截器获取自定义选项
            res.config = processedConfig
            // 执行响应拦截器
            const processedResponse = await this.runResponseInterceptors(res)
            resolve(processedResponse)
          } catch (error) {
            reject(error)
          }
        },
        fail: (error) => {
          // 同样在错误对象上挂载请求配置
          error.config = processedConfig
          reject(error)
        }
      })
    })
  }

  /**
   * 带重试的请求
   * @param {Object} config 请求配置
   * @param {Number} retryCount 当前重试次数
   * @returns {Promise<any>} 响应数据
   */
  async requestWithRetry(config, retryCount = 0) {
    try {
      return await this.request(config)
    } catch (error) {
      // 判断是否需要重试
      const shouldRetry =
        retryCount < MAX_RETRY_COUNT &&
        (error.errMsg?.includes('timeout') || error.errMsg?.includes('fail'))

      if (shouldRetry) {
        // 延迟后重试
        await new Promise((resolve) => setTimeout(resolve, RETRY_DELAY * (retryCount + 1)))
        console.log(`请求重试 ${retryCount + 1}/${MAX_RETRY_COUNT}`)
        return this.requestWithRetry(config, retryCount + 1)
      }

      throw error
    }
  }

  /**
   * GET请求
   * @param {String} url 请求地址
   * @param {Object} params 请求参数
   * @param {Object} config 额外配置
   * @returns {Promise<any>} 响应数据
   */
  get(url, params = {}, config = {}) {
    // 将参数拼接到URL
    const queryString = Object.keys(params)
      .map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
      .join('&')

    const finalUrl = queryString ? `${url}?${queryString}` : url

    return this.requestWithRetry({
      url: finalUrl,
      method: 'GET',
      ...config
    })
  }

  /**
   * POST请求
   * @param {String} url 请求地址
   * @param {Object} data 请求数据
   * @param {Object} config 额外配置
   * @returns {Promise<any>} 响应数据
   */
  post(url, data = {}, config = {}) {
    return this.requestWithRetry({
      url,
      method: 'POST',
      data,
      ...config
    })
  }

  /**
   * PUT请求
   * @param {String} url 请求地址
   * @param {Object} data 请求数据
   * @param {Object} config 额外配置
   * @returns {Promise<any>} 响应数据
   */
  put(url, data = {}, config = {}) {
    return this.requestWithRetry({
      url,
      method: 'PUT',
      data,
      ...config
    })
  }

  /**
   * PATCH请求
   * @param {String} url 请求地址
   * @param {Object} data 请求数据
   * @param {Object} config 额外配置
   * @returns {Promise<any>} 响应数据
   */
  patch(url, data = {}, config = {}) {
    return this.requestWithRetry({
      url,
      method: 'PATCH',
      data,
      ...config
    })
  }

  /**
   * DELETE请求
   * @param {String} url 请求地址
   * @param {Object} config 额外配置
   * @returns {Promise<any>} 响应数据
   */
  delete(url, config = {}) {
    return this.requestWithRetry({
      url,
      method: 'DELETE',
      ...config
    })
  }
}

// 创建默认实例
const http = new HttpClient()

export default http
export { HttpClient }


// 配置请求和响应拦截器
import { tokenManager } from './auth'
import { ErrorHandler, ErrorCode, createError } from './errorHandler'
import { logger } from './logger'

// 请求拦截器：添加token和设备ID
http.useRequestInterceptor(
  async (config) => {
    // 添加token到请求头
    const token = tokenManager.getAccessToken()
    const deviceId = tokenManager.getDeviceId()

    console.log('请求拦截器 - Token:', token ? '存在' : '不存在')
    console.log('请求拦截器 - URL:', config.url)

    if (token) {
      config.header = {
        ...config.header,
        Authorization: `Bearer ${token}`,
        'X-Device-Id': deviceId,
        'Content-Type': 'application/json'
      }
      console.log('请求拦截器 - 已添加 Authorization 头')
    } else {
      console.warn('请求拦截器 - Token 不存在，跳过认证')
    }

    // 检查token是否过期（仅在有token时检查）
    if (token && tokenManager.isAccessTokenExpired() && !tokenManager.isRefreshTokenExpired()) {
      try {
        // TODO: 实现token刷新逻辑
        // await refreshToken()
        logger.info('Token已过期，需要刷新')
      } catch (error) {
        logger.error('Token刷新失败', error)
        throw createError(ErrorCode.TOKEN_EXPIRED, '登录已过期，请重新登录')
      }
    }

    logger.debug('发送请求', {
      url: config.url,
      method: config.method,
      data: config.data,
      hasToken: !!token
    })

    return config
  },
  (error) => {
    logger.error('请求拦截器错误', error)
    return Promise.reject(error)
  }
)

// 响应拦截器：处理响应和错误
http.useResponseInterceptor(
  (response) => {
    logger.debug('收到响应', {
      url: response.config?.url,
      status: response.statusCode,
      data: response.data
    })

    const { statusCode, data } = response

    // HTTP状态码检查
    if (statusCode !== 200) {
      const error = createError(
        ErrorHandler.mapHttpStatusToErrorCode(statusCode),
        data?.message || '请求失败'
      )
      error.response = response
      error.statusCode = statusCode
      throw error
    }

    // 业务状态码检查
    if (data.code !== undefined && data.code !== 200 && data.code !== 0) {
      // 特殊处理 401 未授权错误
      if (data.code === 401) {
        console.log('检测到业务状态码 401，触发未授权处理')
        const error = createError(ErrorCode.UNAUTHORIZED, data.message || '未授权，请先登录', data.data)
        error.response = response
        error.statusCode = 401
        throw error
      }
      
      const error = createError(data.code, data.message || '操作失败', data.data)
      error.response = response
      throw error
    }

    // 返回数据 - 优先返回 data.data，如果不存在则返回整个 data
    // 这样可以兼容不同的后端响应格式
    if (data.data !== undefined) {
      return data.data
    }
    
    // 如果有 code 字段，说明是标准响应格式，但没有 data 字段
    if (data.code !== undefined) {
      return data
    }
    
    // 否则直接返回整个响应数据
    return data
  },
  (error) => {
    console.log('响应拦截器捕获错误:', error)
    console.log('错误状态码:', error.statusCode)
    console.log('错误代码:', error.code)
    
    logger.error('响应拦截器错误', error)

    // 根据请求配置决定是否显示 toast，默认显示
    const suppressToast = error?.response?.config?.suppressErrorToast || error?.config?.suppressErrorToast
    // 对于 404 错误（API 未实现）和 401 错误（未授权，会自动跳转登录），不显示 toast
    const shouldShowToast = !suppressToast && error.statusCode !== 404 && error.statusCode !== 401

    // 处理错误
    console.log('调用 ErrorHandler.handle, showToast:', shouldShowToast)
    ErrorHandler.handle(error, { showToast: shouldShowToast })

    return Promise.reject(error)
  }
)
