/**
 * 权限控制组合式函数
 * 提供角色和权限检查功能
 */
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

/**
 * 使用权限控制
 * @returns {Object} 权限控制方法和状态
 */
export function usePermission() {
  const authStore = useAuthStore()

  // 当前用户角色
  const currentRole = computed(() => authStore.role)

  // 是否是学生
  const isStudent = computed(() => authStore.isStudent)

  // 是否是教师
  const isTeacher = computed(() => authStore.isTeacher)

  // 是否是学工处
  const isAdmin = computed(() => authStore.isAdmin)

  /**
   * 检查用户是否拥有指定角色
   * @param {string|Array<string>} roles - 角色或角色数组
   * @returns {boolean} 是否拥有角色
   */
  const hasRole = (roles) => {
    if (!authStore.userInfo) {
      return false
    }

    const userRole = authStore.role

    if (Array.isArray(roles)) {
      return roles.includes(userRole)
    }

    return userRole === roles
  }

  /**
   * 检查用户是否可以访问指定资源
   * @param {Object} options - 访问选项
   * @param {string|Array<string>} options.roles - 允许的角色
   * @param {Function} options.condition - 额外的条件函数（可选）
   * @returns {boolean} 是否可以访问
   */
  const canAccess = (options) => {
    const { roles, condition } = options

    // 检查角色权限
    if (roles && !hasRole(roles)) {
      return false
    }

    // 检查额外条件
    if (condition && typeof condition === 'function') {
      return condition(authStore.userInfo)
    }

    return true
  }

  /**
   * 检查是否可以编辑资源
   * @param {Object} resource - 资源对象
   * @param {number} resource.organizerId - 资源创建者ID（可选）
   * @param {number} resource.userId - 资源所属用户ID（可选）
   * @returns {boolean} 是否可以编辑
   */
  const canEdit = (resource) => {
    if (!authStore.userInfo) {
      return false
    }

    // 学工处可以编辑所有资源
    if (isAdmin.value) {
      return true
    }

    // 检查是否是资源的创建者或所有者
    const userId = authStore.userId
    return resource.organizerId === userId || resource.userId === userId
  }

  /**
   * 检查是否可以删除资源
   * @param {Object} resource - 资源对象
   * @param {number} resource.organizerId - 资源创建者ID（可选）
   * @param {number} resource.userId - 资源所属用户ID（可选）
   * @returns {boolean} 是否可以删除
   */
  const canDelete = (resource) => {
    if (!authStore.userInfo) {
      return false
    }

    // 学工处可以删除所有资源
    if (isAdmin.value) {
      return true
    }

    // 检查是否是资源的创建者或所有者
    const userId = authStore.userId
    return resource.organizerId === userId || resource.userId === userId
  }

  /**
   * 检查是否可以审核
   * @returns {boolean} 是否可以审核
   */
  const canApprove = () => {
    return isTeacher.value || isAdmin.value
  }

  /**
   * 检查是否可以管理用户
   * @returns {boolean} 是否可以管理用户
   */
  const canManageUsers = () => {
    return isAdmin.value
  }

  /**
   * 检查是否可以查看统计
   * @returns {boolean} 是否可以查看统计
   */
  const canViewStatistics = () => {
    return isTeacher.value || isAdmin.value
  }

  /**
   * 要求登录
   * 如果未登录，跳转到登录页
   */
  const requireLogin = () => {
    if (!authStore.isLoggedIn) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      uni.reLaunch({
        url: '/pages/index/index'
      })
      return false
    }
    return true
  }

  /**
   * 要求特定角色
   * 如果不满足角色要求，显示提示并返回false
   * @param {string|Array<string>} roles - 要求的角色
   * @param {string} message - 提示消息（可选）
   * @returns {boolean} 是否满足角色要求
   */
  const requireRole = (roles, message = '您没有权限访问') => {
    if (!requireLogin()) {
      return false
    }

    if (!hasRole(roles)) {
      uni.showToast({
        title: message,
        icon: 'none'
      })
      return false
    }

    return true
  }

  return {
    // 状态
    currentRole,
    isStudent,
    isTeacher,
    isAdmin,

    // 方法
    hasRole,
    canAccess,
    canEdit,
    canDelete,
    canApprove,
    canManageUsers,
    canViewStatistics,
    requireLogin,
    requireRole
  }
}
