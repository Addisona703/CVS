/**
 * 路由守卫工具
 * 用于控制不同角色的页面访问权限
 */
import { useAuthStore } from '@/stores/auth'

// 角色路由映射
const ROLE_ROUTES = {
  STUDENT: {
    home: '/pages/student/home/home',
    allowedPaths: [
      '/pages/student/',
      '/pages/student-sub/',
      '/pages/common/',
      '/pages/index/index'
    ]
  },
  TEACHER: {
    home: '/pages/teacher/dashboard/dashboard',
    allowedPaths: [
      '/pages/teacher/',
      '/pages/common/',
      '/pages/index/index'
    ]
  },
  ADMIN: {
    home: '/pages/admin/dashboard/dashboard',
    allowedPaths: [
      '/pages/admin/',
      '/pages/common/',
      '/pages/index/index'
    ]
  }
}

/**
 * 检查用户是否有权限访问指定路径
 */
export const checkRoutePermission = (path, role) => {
  if (!role || !ROLE_ROUTES[role]) {
    return false
  }

  const allowedPaths = ROLE_ROUTES[role].allowedPaths
  return allowedPaths.some(allowedPath => path.startsWith(allowedPath))
}

/**
 * 导航到角色对应的首页
 */
export const navigateToRoleHome = (role) => {
  const homeRoute = ROLE_ROUTES[role]?.home || ROLE_ROUTES.STUDENT.home
  
  uni.reLaunch({
    url: homeRoute
  })
}

/**
 * 路由拦截器
 * 在页面跳转前检查权限
 */
export const routeInterceptor = () => {
  // 拦截 navigateTo
  const originalNavigateTo = uni.navigateTo
  uni.navigateTo = function(options) {
    const authStore = useAuthStore()
    
    if (options.url && authStore.isLoggedIn) {
      const hasPermission = checkRoutePermission(options.url, authStore.role)
      
      if (!hasPermission) {
        uni.showToast({
          title: '无权访问该页面',
          icon: 'none'
        })
        return
      }
    }
    
    return originalNavigateTo.call(this, options)
  }

  // 拦截 redirectTo
  const originalRedirectTo = uni.redirectTo
  uni.redirectTo = function(options) {
    const authStore = useAuthStore()
    
    if (options.url && authStore.isLoggedIn) {
      const hasPermission = checkRoutePermission(options.url, authStore.role)
      
      if (!hasPermission) {
        uni.showToast({
          title: '无权访问该页面',
          icon: 'none'
        })
        navigateToRoleHome(authStore.role)
        return
      }
    }
    
    return originalRedirectTo.call(this, options)
  }
}

/**
 * 页面加载时检查权限
 */
export const checkPagePermission = () => {
  const authStore = useAuthStore()
  
  // 如果未登录，跳转到登录页
  if (!authStore.isLoggedIn) {
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const currentRoute = '/' + currentPage.route
    
    // 如果不是登录页或注册页，跳转到登录页
    if (currentRoute !== '/pages/index/index' && 
        currentRoute !== '/pages/auth/register/register') {
      uni.reLaunch({
        url: '/pages/index/index'
      })
    }
    return false
  }
  
  // 检查当前页面权限
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const currentRoute = '/' + currentPage.route
  
  const hasPermission = checkRoutePermission(currentRoute, authStore.role)
  
  if (!hasPermission) {
    uni.showToast({
      title: '无权访问该页面',
      icon: 'none'
    })
    
    setTimeout(() => {
      navigateToRoleHome(authStore.role)
    }, 1500)
    
    return false
  }
  
  return true
}
