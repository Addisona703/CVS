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
      console.log(to.meta.roles)
      // 检查角色权限
      if (to.meta.roles && !to.meta.roles.includes(authStore.user.role)) {
        ElMessage.error('权限不足')
        next({ name: 'NotFound' })
        return
      }
    }
    
    // 已登录用户访问登录页，重定向到仪表板
    if (to.name === 'Login' && authStore.isAuthenticated) {
      const role = authStore.user.role.toLowerCase();
      console.log(authStore)
      next({ name: `${role}Dashboard` })
      return
    }
    
    // 处理根路径重定向
    if (to.path === '/' && authStore.isAuthenticated) {
      const role = authStore.user.role
      next(`/${role}/dashboard`)
      return
    }
    
    next()
  })
  
  // 全局后置钩子
  router.afterEach((to, from) => {
    // 页面加载完成后的处理
    console.log(`路由跳转: ${from.path} -> ${to.path}`)
  })
}
