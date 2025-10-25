import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

export function setupRouterGuards(router) {
  // 全局前置守卫
  router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore()
    
    // 设置页面标题
    document.title = to.meta.title ? `${to.meta.title} - CVS系统` : 'CVS系统'
    
    // 检查是否需要认证
    if (to.meta.requiresAuth) {
      if (!authStore.isAuthenticated) {
        ElMessage.warning('请先登录')
        next({ name: 'Login', query: { redirect: to.fullPath } })
        return
      }
      // console.log(to.meta.roles)
      // 检查角色权限
      if (to.meta.roles && !to.meta.roles.includes(authStore.user.role)) {
        ElMessage.error('权限不足')
        next({ name: 'NotFound' })
        return
      }
    }
    
    // 商城管理路由权限检查
    if (to.path.includes('/mall/') && (to.path.includes('/admin/') || to.path.includes('/teacher/'))) {
      if (!authStore.isAuthenticated) {
        ElMessage.warning('请先登录')
        next({ name: 'Login', query: { redirect: to.fullPath } })
        return
      }
      
      // 商城管理功能只允许管理员和教师访问
      const userRole = authStore.user.role
      if (!['ADMIN', 'TEACHER'].includes(userRole)) {
        ElMessage.error('商城管理功能需要管理员或教师权限')
        next({ name: 'NotFound' })
        return
      }
    }
    
    // 已登录用户访问登录页或门户页，重定向到仪表板
    if ((to.name === 'Login' || to.name === 'Landing') && authStore.isAuthenticated) {
      const role = authStore.user.role.toLowerCase();
      // console.log(authStore)
      next({ name: `${role}Dashboard` })
      return
    }
    
    next()
  })
  
  // 全局后置钩子
  router.afterEach((to, from) => {
    // 页面加载完成后的处理
    // console.log(`路由跳转: ${from.path} -> ${to.path}`)
  })
}
