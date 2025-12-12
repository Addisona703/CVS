/**
 * 引导页工具函数
 */

const ONBOARDING_KEY = 'has_seen_onboarding'

/**
 * 检查是否已经看过引导页
 */
export function hasSeenOnboarding() {
  try {
    const value = uni.getStorageSync(ONBOARDING_KEY)
    return value === 'true'
  } catch (e) {
    console.error('检查引导页状态失败:', e)
    return false
  }
}

/**
 * 标记已经看过引导页
 */
export function markOnboardingAsSeen() {
  try {
    uni.setStorageSync(ONBOARDING_KEY, 'true')
    return true
  } catch (e) {
    console.error('保存引导页状态失败:', e)
    return false
  }
}

/**
 * 重置引导页状态（用于测试）
 */
export function resetOnboarding() {
  try {
    uni.removeStorageSync(ONBOARDING_KEY)
    return true
  } catch (e) {
    console.error('重置引导页状态失败:', e)
    return false
  }
}

/**
 * 检查是否需要显示引导页
 * 如果是首次启动，跳转到引导页
 */
export function checkAndShowOnboarding() {
  if (!hasSeenOnboarding()) {
    uni.reLaunch({
      url: '/pages/common/onboarding/onboarding'
    })
    return true
  }
  return false
}
