/**
 * 二维码处理组合式函数
 * 提供二维码生成、扫描、验证功能
 */
import { ref } from 'vue'
import * as checkApi from '@/api/check'

/**
 * 使用二维码功能
 * @returns {Object} 二维码相关方法和状态
 */
export function useQrCode() {
  // 二维码内容
  const qrCodeContent = ref('')

  // 二维码图片URL
  const qrCodeUrl = ref('')

  // 扫描结果
  const scanResult = ref(null)

  // 加载状态
  const loading = ref(false)

  /**
   * 生成签到二维码
   * @param {number} activityId - 活动ID
   * @returns {Promise<Object>} 二维码信息
   */
  const generateCheckinQrCode = async (activityId) => {
    loading.value = true

    try {
      const response = await checkApi.generateCheckinToken(activityId)

      qrCodeContent.value = response.token || ''
      qrCodeUrl.value = response.qrCodeUrl || ''

      return response

    } catch (error) {
      console.error('生成签到二维码失败:', error)
      uni.showToast({
        title: error.message || '生成签到二维码失败',
        icon: 'none'
      })
      throw error

    } finally {
      loading.value = false
    }
  }

  /**
   * 生成签退二维码
   * @param {number} activityId - 活动ID
   * @returns {Promise<Object>} 二维码信息
   */
  const generateCheckoutQrCode = async (activityId) => {
    loading.value = true

    try {
      const response = await checkApi.generateCheckoutToken(activityId)

      qrCodeContent.value = response.token || ''
      qrCodeUrl.value = response.qrCodeUrl || ''

      return response

    } catch (error) {
      console.error('生成签退二维码失败:', error)
      uni.showToast({
        title: error.message || '生成签退二维码失败',
        icon: 'none'
      })
      throw error

    } finally {
      loading.value = false
    }
  }

  /**
   * 扫描二维码
   * @param {Object} options - 扫描选项
   * @param {boolean} options.onlyFromCamera - 是否只允许相机扫描（默认true）
   * @param {Array<string>} options.scanType - 扫描类型（默认['qrCode']）
   * @returns {Promise<string>} 扫描结果
   */
  const scanQrCode = async (options = {}) => {
    const {
      onlyFromCamera = true,
      scanType = ['qrCode']
    } = options

    return new Promise((resolve, reject) => {
      uni.scanCode({
        onlyFromCamera,
        scanType,
        success: (res) => {
          scanResult.value = res.result
          resolve(res.result)
        },
        fail: (err) => {
          console.error('扫码失败:', err)

          // 用户取消扫码
          if (err.errMsg && err.errMsg.includes('cancel')) {
            reject(new Error('已取消扫码'))
          } else {
            uni.showToast({
              title: '扫码失败',
              icon: 'none'
            })
            reject(err)
          }
        }
      })
    })
  }



  /**
   * 扫码并签到
   * @param {number} activityId - 活动ID（可选，用于验证）
   * @returns {Promise<Object>} 签到结果
   */
  const scanAndCheckIn = async (activityId) => {
    try {
      // 扫描二维码
      uni.showLoading({ title: '扫码中...' })
      const qrCode = await scanQrCode()
      uni.hideLoading()

      // 验证并签到
      uni.showLoading({ title: '签到中...' })
      const response = await checkApi.studentCheckin({
        qrCode,
        activityId
      })
      uni.hideLoading()

      // 显示成功提示
      uni.showToast({
        title: '签到成功',
        icon: 'success'
      })

      return response

    } catch (error) {
      uni.hideLoading()

      // 处理特定错误
      if (error.message === '已取消扫码') {
        return null
      }

      // 显示错误提示
      const errorMessages = {
        'QR_CODE_EXPIRED': '二维码已过期，请联系教师刷新',
        'QR_CODE_INVALID': '二维码无效，请扫描正确的二维码',
        'ALREADY_CHECKED_IN': '您已签到，请勿重复签到',
        'NOT_SIGNED_UP': '您未报名该活动，无法签到',
        'LOCATION_MISMATCH': '您不在活动地点附近，无法签到'
      }

      const message = errorMessages[error.code] || error.message || '签到失败'

      uni.showModal({
        title: '签到失败',
        content: message,
        showCancel: false
      })

      throw error
    }
  }

  /**
   * 扫码并签退
   * @param {number} activityId - 活动ID（可选，用于验证）
   * @returns {Promise<Object>} 签退结果
   */
  const scanAndCheckOut = async (activityId) => {
    try {
      // 扫描二维码
      uni.showLoading({ title: '扫码中...' })
      const qrCode = await scanQrCode()
      uni.hideLoading()

      // 验证并签退
      uni.showLoading({ title: '签退中...' })
      const response = await checkApi.studentCheckout({
        qrCode,
        activityId
      })
      uni.hideLoading()

      // 显示成功提示
      uni.showToast({
        title: '签退成功',
        icon: 'success'
      })

      return response

    } catch (error) {
      uni.hideLoading()

      // 处理特定错误
      if (error.message === '已取消扫码') {
        return null
      }

      // 显示错误提示
      const errorMessages = {
        'QR_CODE_EXPIRED': '二维码已过期，请联系教师刷新',
        'QR_CODE_INVALID': '二维码无效，请扫描正确的二维码',
        'NOT_CHECKED_IN': '您尚未签到，无法签退',
        'ALREADY_CHECKED_OUT': '您已签退，请勿重复签退'
      }

      const message = errorMessages[error.code] || error.message || '签退失败'

      uni.showModal({
        title: '签退失败',
        content: message,
        showCancel: false
      })

      throw error
    }
  }

  /**
   * 扫码核销兑换
   * @returns {Promise<Object>} 核销结果
   */
  const scanAndVerifyRedemption = async () => {
    try {
      // 扫描二维码
      uni.showLoading({ title: '扫码中...' })
      const qrCode = await scanQrCode()
      uni.hideLoading()

      // 验证并核销
      uni.showLoading({ title: '核销中...' })
      const { mallApi } = await import('@/api/mall')
      const response = await mallApi.verifyRedemption({ qrCode })
      uni.hideLoading()

      // 显示成功提示
      uni.showToast({
        title: '核销成功',
        icon: 'success'
      })

      return response

    } catch (error) {
      uni.hideLoading()

      // 处理特定错误
      if (error.message === '已取消扫码') {
        return null
      }

      // 显示错误提示
      const errorMessages = {
        'REDEMPTION_NOT_FOUND': '兑换记录不存在',
        'REDEMPTION_EXPIRED': '兑换凭证已过期',
        'ALREADY_REDEEMED': '该凭证已核销',
        'QR_CODE_INVALID': '二维码无效'
      }

      const message = errorMessages[error.code] || error.message || '核销失败'

      uni.showModal({
        title: '核销失败',
        content: message,
        showCancel: false
      })

      throw error
    }
  }

  /**
   * 验证二维码格式
   * @param {string} qrCode - 二维码内容
   * @returns {Object} 验证结果 { valid: boolean, message: string, data: any }
   */
  const validateQrCode = (qrCode) => {
    if (!qrCode) {
      return {
        valid: false,
        message: '二维码内容为空'
      }
    }

    try {
      // 尝试解析JSON格式的二维码
      const data = JSON.parse(qrCode)
      
      // 验证必要字段
      if (data.redemptionId) {
        // 兑换核销二维码
        return {
          valid: true,
          message: '验证成功',
          data,
          type: 'redemption'
        }
      } else if (data.activityId) {
        // 签到签退二维码
        return {
          valid: true,
          message: '验证成功',
          data,
          type: 'checkin'
        }
      } else {
        return {
          valid: false,
          message: '二维码格式不正确'
        }
      }
    } catch (error) {
      // 如果不是JSON格式，可能是简单的token字符串
      
      // 检查是否是32位十六进制字符串（签到签退token）
      if (/^[a-f0-9]{32}$/i.test(qrCode)) {
        return {
          valid: true,
          message: '验证成功',
          data: { token: qrCode },
          type: 'checkin_token'
        }
      }
      
      // 检查是否是兑换凭证码（MALL开头的字符串）
      if (/^MALL\d+$/i.test(qrCode)) {
        return {
          valid: true,
          message: '验证成功',
          data: { token: qrCode },
          type: 'redemption_token'
        }
      }
      
      // 其他格式的字符串也认为是有效的token
      if (typeof qrCode === 'string' && qrCode.length > 0) {
        return {
          valid: true,
          message: '验证成功',
          data: { token: qrCode },
          type: 'token'
        }
      }
      
      return {
        valid: false,
        message: '二维码格式错误，请扫描正确的二维码'
      }
    }
  }

  /**
   * 清除二维码数据
   */
  const clearQrCode = () => {
    qrCodeContent.value = ''
    qrCodeUrl.value = ''
    scanResult.value = null
  }

  return {
    // 状态
    qrCodeContent,
    qrCodeUrl,
    scanResult,
    loading,

    // 方法
    generateCheckinQrCode,
    generateCheckoutQrCode,
    scanQrCode,
    validateQrCode,
    scanAndCheckIn,
    scanAndCheckOut,
    scanAndVerifyRedemption,
    clearQrCode
  }
}
