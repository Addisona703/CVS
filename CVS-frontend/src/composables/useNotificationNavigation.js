import { useRouter, useRoute } from 'vue-router'
import { nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * 通知导航处理组合式函数
 * 处理通知点击跳转逻辑、目标页面内容定位和自动标记已读
 */
export function useNotificationNavigation() {
  const router = useRouter()
  const route = useRoute()

  const notificationTypeRoutes = {
    REGISTRATION_PENDING: '/teacher/signups?status=PENDING'
  }

  /**
   * 根据通知类型解析跳转链接
   * @param {Object} notification 通知对象
   * @returns {string|null} 目标链接
   */
  const resolveNotificationLink = (notification) => {
    if (!notification) return null

    const mappedRoute = notificationTypeRoutes[notification.type]
    if (mappedRoute) return mappedRoute

    return notification.linkUrl || null
  }

  /**
   * 处理通知跳转
   * @param {Object} notification 通知对象
   * @param {Function} markAsReadCallback 标记已读的回调函数
   */
  const handleNotificationClick = async (notification, markAsReadCallback) => {
    try {
      // 1. 标记通知为已读
      if (!notification.isRead && markAsReadCallback) {
        await markAsReadCallback(notification.id)
        notification.isRead = true
      }

      // 2. 处理跳转链接
      const targetLink = resolveNotificationLink(notification)
      if (targetLink) {
        await navigateToNotificationTarget(targetLink)
      }
    } catch (error) {
      console.error('处理通知跳转失败:', error)
      ElMessage.error('跳转失败，请稍后重试')
    }
  }

  /**
   * 导航到通知目标页面
   * @param {string} linkUrl 目标链接
   */
  const navigateToNotificationTarget = async (linkUrl) => {
    if (!linkUrl) return

    // 处理外部链接
    if (linkUrl.startsWith('http')) {
      window.open(linkUrl, '_blank')
      return
    }

    // 处理内部路由跳转
    try {
      await router.push(linkUrl)
      
      // 等待路由跳转完成后处理页面定位
      await nextTick()
      await handlePageHighlight(linkUrl)
    } catch (error) {
      console.error('路由跳转失败:', error)
      ElMessage.error('页面跳转失败')
    }
  }

  /**
   * 处理页面高亮和定位
   * @param {string} linkUrl 链接URL
   */
  const handlePageHighlight = async (linkUrl) => {
    const url = new URL(linkUrl, window.location.origin)
    const params = new URLSearchParams(url.search)
    
    const tab = params.get('tab')
    const highlight = params.get('highlight')
    const status = params.get('status')

    // 处理标签页切换
    if (tab) {
      await handleTabSwitch(tab)
    }

    // 处理内容高亮
    if (highlight) {
      await handleContentHighlight(highlight)
    }

    // 处理状态筛选
    if (status) {
      await handleStatusFilter(status)
    }
  }

  /**
   * 处理标签页切换
   * @param {string} tabName 标签页名称
   */
  const handleTabSwitch = async (tabName) => {
    await nextTick()
    
    // 查找并点击对应的标签页
    const tabElement = document.querySelector(`[data-tab="${tabName}"]`) || 
                      document.querySelector(`.el-tabs__item[name="${tabName}"]`)
    
    if (tabElement) {
      tabElement.click()
      await nextTick()
    }
  }

  /**
   * 处理内容高亮
   * @param {string} highlightTarget 高亮目标
   */
  const handleContentHighlight = async (highlightTarget) => {
    await nextTick()
    
    const highlightSelectors = {
      'signin': '[data-highlight="signin"], .signin-section, .checkin-section',
      'signout': '[data-highlight="signout"], .signout-section, .checkout-section',
      'approved': '[data-highlight="approved"], .approval-success, .status-approved',
      'rejected': '[data-highlight="rejected"], .approval-rejected, .status-rejected',
      'checkout-approved': '[data-highlight="checkout-approved"], .checkout-success',
      'checkout-rejected': '[data-highlight="checkout-rejected"], .checkout-rejected'
    }

    const selector = highlightSelectors[highlightTarget]
    if (selector) {
      const element = document.querySelector(selector)
      if (element) {
        // 滚动到目标元素
        element.scrollIntoView({ 
          behavior: 'smooth', 
          block: 'center' 
        })
        
        // 添加高亮效果
        element.classList.add('notification-highlight')
        
        // 3秒后移除高亮效果
        setTimeout(() => {
          element.classList.remove('notification-highlight')
        }, 3000)
      }
    }
  }

  /**
   * 处理状态筛选
   * @param {string} statusValue 状态值
   */
  const handleStatusFilter = async (statusValue) => {
    await nextTick()
    
    // 查找状态筛选器并设置值
    const statusSelect = document.querySelector('.status-filter select') ||
                        document.querySelector('[data-filter="status"]')
    
    if (statusSelect) {
      // 触发状态筛选变更
      const event = new Event('change', { bubbles: true })
      statusSelect.value = statusValue
      statusSelect.dispatchEvent(event)
    }
  }

  /**
   * 检查当前页面是否来自通知跳转
   */
  const checkNotificationNavigation = () => {
    const params = new URLSearchParams(route.query)
    const fromNotification = params.has('tab') || params.has('highlight') || params.has('status')
    
    if (fromNotification) {
      // 延迟处理，确保页面完全加载
      setTimeout(() => {
        handlePageHighlight(route.fullPath)
      }, 500)
    }
  }

  // 在组件挂载时检查是否来自通知跳转
  onMounted(() => {
    checkNotificationNavigation()
  })

  return {
    handleNotificationClick,
    navigateToNotificationTarget,
    handlePageHighlight,
    checkNotificationNavigation
  }
}
