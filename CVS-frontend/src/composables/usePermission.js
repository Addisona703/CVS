import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { ROLE_PERMISSIONS } from '@/utils/constants'

export function usePermission() {
  const authStore = useAuthStore()
  
  const hasPermission = (permission) => {
    const userRole = authStore.user?.role
    if (!userRole) return false
    
    const permissions = ROLE_PERMISSIONS[userRole] || []
    return permissions.includes(permission)
  }
  
  const hasRole = (role) => {
    return authStore.user?.role === role
  }
  
  const hasAnyRole = (roles) => {
    return roles.includes(authStore.user?.role)
  }
  
  return {
    hasPermission,
    hasRole,
    hasAnyRole
  }
}
