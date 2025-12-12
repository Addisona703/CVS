/**
 * 页面权限守卫 Composable
 * 在页面加载时自动检查权限
 */
import { onMounted } from 'vue'
import { checkPagePermission } from '@/utils/route-guard'

export const usePageGuard = () => {
  onMounted(() => {
    checkPagePermission()
  })
}
