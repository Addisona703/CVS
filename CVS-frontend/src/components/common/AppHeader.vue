<template>
  <div class="app-header">
    <div class="header-left">
      <div class="logo">
        <span class="logo-icon" v-html="logoInfo.svg"></span>
        <span class="logo-text">{{ logoInfo.text }}</span>
      </div>
      
      <el-divider direction="vertical" style="height: 24px; margin: 0 20px;" />
      
      <el-breadcrumb separator="/">
        <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path" :to="item.path">
          {{ item.label }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="header-operations">
      <t-button variant="text" shape="square" @click="handleSearch">
        <template #icon><t-icon name="search" /></template>
      </t-button>

      <NotificationBell />

      <t-dropdown :min-column-width="160" trigger="click">
        <t-button variant="text" shape="square">
          <template #icon><t-icon name="user" /></template>
        </t-button>
        <t-dropdown-menu>
          <t-dropdown-item @click="goToProfile">
            <t-icon name="user" style="margin-right: 8px;" />
            个人资料
          </t-dropdown-item>
          <t-dropdown-item divider />
          <t-dropdown-item @click="handleLogout">
            <t-icon name="poweroff" style="margin-right: 8px;" />
            退出登录
          </t-dropdown-item>
        </t-dropdown-menu>
      </t-dropdown>

      <ProfileDialog v-model="profileVisible" />
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import ProfileDialog from '@/views/common/Profile.vue'
import NotificationBell from '@/components/NotificationBell.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const logoInfo = computed(() => {
  switch (authStore.userRole) {
    case 'ADMIN':
      return {
        text: 'CVS管理系统',
        svg: '<svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg"><path d="M315.392 507.904c-81.92-40.96-137.216-122.88-137.216-221.184 0-135.168 110.592-245.76 245.76-245.76s245.76 110.592 245.76 245.76c0 133.12-104.448 241.664-237.568 245.76h-2.048c-202.752 0-368.64 165.888-368.64 368.64 0 12.288-8.192 20.48-20.48 20.48s-20.48-8.192-20.48-20.48c0-186.368 124.928-344.064 294.912-393.216z m108.544-16.384c112.64 0 204.8-92.16 204.8-204.8s-92.16-204.8-204.8-204.8-204.8 92.16-204.8 204.8 92.16 204.8 204.8 204.8z m561.152 135.168l4.096 10.24-49.152 59.392c-8.192 10.24-8.192 26.624 0 36.864l49.152 57.344-4.096 10.24c-10.24 28.672-24.576 57.344-45.056 79.872l-8.192 8.192-75.776-12.288c-12.288-2.048-26.624 6.144-30.72 18.432l-26.624 73.728-10.24 2.048c-38.912 8.192-77.824 8.192-116.736 0l-12.288-2.048-24.576-71.68c-4.096-12.288-18.432-20.48-30.72-18.432l-77.824 12.288-8.192-8.192c-10.24-10.24-18.432-22.528-24.576-34.816-8.192-14.336-16.384-30.72-20.48-47.104l-4.096-10.24 51.2-57.344c8.192-10.24 8.192-26.624 0-34.816l-49.152-59.392 4.096-10.24c10.24-30.72 26.624-57.344 47.104-81.92l8.192-8.192 73.728 12.288c12.288 2.048 26.624-6.144 32.768-18.432l26.624-71.68 10.24-2.048c36.864-8.192 75.776-8.192 114.688 0l12.288 2.048 24.576 69.632c4.096 12.288 18.432 22.528 32.768 20.48l75.776-10.24 8.192 8.192c10.24 12.288 18.432 22.528 24.576 36.864 6.144 10.24 12.288 26.624 18.432 40.96z m-57.344-24.576c-4.096-8.192-8.192-14.336-14.336-22.528l-53.248 6.144c-32.768 4.096-65.536-16.384-75.776-47.104L768 491.52c-24.576-4.096-49.152-4.096-73.728 0l-18.432 49.152c-10.24 30.72-45.056 51.2-77.824 45.056l-51.2-8.192c-12.288 14.336-20.48 32.768-28.672 49.152l34.816 40.96c20.48 24.576 20.48 63.488 0 88.064l-34.816 40.96c4.096 10.24 8.192 18.432 14.336 28.672 4.096 8.192 8.192 14.336 14.336 20.48l55.296-8.192c32.768-4.096 65.536 16.384 75.776 47.104l16.384 49.152c24.576 4.096 51.2 4.096 75.776 0l18.432-51.2c10.24-30.72 45.056-51.2 75.776-45.056l51.2 8.192c10.24-14.336 20.48-30.72 26.624-49.152l-32.768-38.912c-20.48-24.576-22.528-63.488-2.048-88.064l34.816-40.96c-4.096-8.192-8.192-16.384-14.336-26.624z m-196.608 204.8c-51.2 0-92.16-40.96-92.16-92.16s40.96-92.16 92.16-92.16 92.16 40.96 92.16 92.16-43.008 92.16-92.16 92.16z m0-40.96c28.672 0 51.2-22.528 51.2-51.2s-22.528-51.2-51.2-51.2-51.2 22.528-51.2 51.2 22.528 51.2 51.2 51.2z" fill="#115e87"/></svg>'
      }
    case 'TEACHER':
      return {
        text: '教师工作台',
        svg: '<svg t="1760779642156" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="3784" width="200" height="200"><path d="M941.681778 221.724444h-233.216a25.230222 25.230222 0 0 1-25.400889-25.543111c0-14.279111 11.178667-25.514667 25.400889-25.514666h233.216c14.222222 0 25.400889 11.235556 25.400889 25.514666a25.230222 25.230222 0 0 1-25.400889 25.543111z m-21.333334 161.336889H740.977778a25.230222 25.230222 0 0 1-25.400889-25.543111c0-14.279111 11.178667-25.514667 25.400889-25.514666h179.370666c14.222222 0 25.400889 11.235556 25.400889 25.514666a25.230222 25.230222 0 0 1-25.400889 25.543111z m-150.414222 434.460445l124.501334-322.673778a25.6 25.6 0 0 1 33.024-14.791111c13.198222 5.091556 19.797333 19.911111 15.729777 33.678222L797.895111 890.026667a24.462222 24.462222 0 0 1-8.135111 10.723555c0 1.024 0.512 2.048 0.512 3.043556a59.904 59.904 0 0 1-14.734222 44.401778 56.120889 56.120889 0 0 1-42.183111 18.915555H114.033778a57.230222 57.230222 0 0 1-42.183111-18.915555c-11.178667-11.719111-16.241778-28.074667-14.734223-44.401778 14.734222-152.632889 117.873778-275.171556 254.549334-319.089778a215.04 215.04 0 0 1-100.096-182.243556c0-118.471111 94.520889-214.954667 211.370666-214.954666 116.337778 0 211.342222 96.483556 211.342223 214.954666a215.580444 215.580444 0 0 1-100.096 182.243556c109.738667 35.242667 197.660444 121.514667 235.747555 232.817778z" p-id="3785" fill="#0cc919"></path></svg>'
      }
    case 'STUDENT':
      return {
        text: '学生中心',
        svg: '<svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg"><path d="M672 576M640 576M879.994667 355.316267l-340.405333-125.1232c-21.2192-8.2112-16.766933-8.263467-37.816533-0.119467L159.316267 354.192c-21.048533 8.144-20.912 21.402667 0.308267 29.6128l81.374933 29.508267c-36.0352 37.330133-38.3712 76.1408-38.855467 121.143467-13.9264 5.736533-23.812267 20.168533-23.812267 37.0784 0 15.5264 8.337067 28.964267 20.481067 35.501867-5.771733 45.722667-22.289067 97.960533-70.8128 159.851733 24.004267 19.866667 36.3936 26.488533 54.977067 33.111467 67.8336-31.1392 59.5872-113.934933 54.3072-196.280533 9.317333-7.2192 15.3824-18.941867 15.3824-32.183467 0-14.197333-6.9696-26.645333-17.444267-33.672533 1.2064-44.096 10.190933-83.5456 41.0272-109.066667 0.2528-0.674133 0.9696-1.293867 2.301867-1.864533l234.010667-101.1072c8.6784-3.719467 18.5344 0.786133 22.013867 10.065067l0.308267 0.8192c3.479467 9.277867-0.734933 19.8144-9.4144 23.5328L327.706667 444.757333l176.968533 64.1728c21.2192 8.2112 16.766933 8.263467 37.816533 0.119467l337.812267-124.1216C901.352533 376.784 901.213867 363.525333 879.994667 355.316267zM504.138667 556.939733l-214.216533-77.6256 0 58.379733c11.198933 10.968533 17.378133 26.702933 17.378133 44.187733 0 15.720533-5.138133 30.021333-13.816533 40.72 2.839467 9.168 7.784533 18.129067 14.848 20.948267 124.666667 73.671467 296.9888 72.843733 435.592533-7.4496 10.279467-9.150933 18.2304-20.482133 18.2304-31.454933L762.154667 476.0576l-220.039467 81.000533C521.0688 565.2032 525.357867 565.149867 504.138667 556.939733z" fill="#1296db"/></svg>'
      }
    default:
      return {
        text: 'CVS系统',
        svg: '<svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg"><path d="M315.392 507.904c-81.92-40.96-137.216-122.88-137.216-221.184 0-135.168 110.592-245.76 245.76-245.76s245.76 110.592 245.76 245.76c0 133.12-104.448 241.664-237.568 245.76h-2.048c-202.752 0-368.64 165.888-368.64 368.64 0 12.288-8.192 20.48-20.48 20.48s-20.48-8.192-20.48-20.48c0-186.368 124.928-344.064 294.912-393.216z m108.544-16.384c112.64 0 204.8-92.16 204.8-204.8s-92.16-204.8-204.8-204.8-204.8 92.16-204.8 204.8 92.16 204.8 204.8 204.8z m561.152 135.168l4.096 10.24-49.152 59.392c-8.192 10.24-8.192 26.624 0 36.864l49.152 57.344-4.096 10.24c-10.24 28.672-24.576 57.344-45.056 79.872l-8.192 8.192-75.776-12.288c-12.288-2.048-26.624 6.144-30.72 18.432l-26.624 73.728-10.24 2.048c-38.912 8.192-77.824 8.192-116.736 0l-12.288-2.048-24.576-71.68c-4.096-12.288-18.432-20.48-30.72-18.432l-77.824 12.288-8.192-8.192c-10.24-10.24-18.432-22.528-24.576-34.816-8.192-14.336-16.384-30.72-20.48-47.104l-4.096-10.24 51.2-57.344c8.192-10.24 8.192-26.624 0-34.816l-49.152-59.392 4.096-10.24c10.24-30.72 26.624-57.344 47.104-81.92l8.192-8.192 73.728 12.288c12.288 2.048 26.624-6.144 32.768-18.432l26.624-71.68 10.24-2.048c36.864-8.192 75.776-8.192 114.688 0l12.288 2.048 24.576 69.632c4.096 12.288 18.432 22.528 32.768 20.48l75.776-10.24 8.192 8.192c10.24 12.288 18.432 22.528 24.576 36.864 6.144 10.24 12.288 26.624 18.432 40.96z m-57.344-24.576c-4.096-8.192-8.192-14.336-14.336-22.528l-53.248 6.144c-32.768 4.096-65.536-16.384-75.776-47.104L768 491.52c-24.576-4.096-49.152-4.096-73.728 0l-18.432 49.152c-10.24 30.72-45.056 51.2-77.824 45.056l-51.2-8.192c-12.288 14.336-20.48 32.768-28.672 49.152l34.816 40.96c20.48 24.576 20.48 63.488 0 88.064l-34.816 40.96c4.096 10.24 8.192 18.432 14.336 28.672 4.096 8.192 8.192 14.336 14.336 20.48l55.296-8.192c32.768-4.096 65.536 16.384 75.776 47.104l16.384 49.152c24.576 4.096 51.2 4.096 75.776 0l18.432-51.2c10.24-30.72 45.056-51.2 75.776-45.056l51.2 8.192c10.24-14.336 20.48-30.72 26.624-49.152l-32.768-38.912c-20.48-24.576-22.528-63.488-2.048-88.064l34.816-40.96c-4.096-8.192-8.192-16.384-14.336-26.624z m-196.608 204.8c-51.2 0-92.16-40.96-92.16-92.16s40.96-92.16 92.16-92.16 92.16 40.96 92.16 92.16-43.008 92.16-92.16 92.16z m0-40.96c28.672 0 51.2-22.528 51.2-51.2s-22.528-51.2-51.2-51.2-51.2 22.528-51.2 51.2 22.528 51.2 51.2 51.2z" fill="#115e87"/></svg>'
      }
  }
})

// 路由标题映射
const routeTitleMap = {
  // Admin
  '/admin/dashboard': '仪表板',
  '/admin/users': '用户管理',
  '/admin/activities': '活动管理',
  '/admin/activities/create': '创建活动',
  '/admin/statistics': '统计分析',
  '/admin/records': '服务记录管理',
  '/admin/points': '积分管理',
  '/admin/mall/products': '商品管理',
  '/admin/mall/categories': '分类管理',
  '/admin/mall/verify': '兑换核销',
  '/admin/mall/statistics': '统计报表',
  '/admin/activity-approval': '活动审核',
  '/admin/signups': '报名审核',
  '/admin/certificate-review': '证书审核',
  
  // Teacher
  '/teacher/dashboard': '仪表板',
  '/teacher/activities': '我的活动',
  '/teacher/activities/create': '创建活动',
  '/teacher/signups': '报名管理',
  '/teacher/records': '服务记录',
  '/teacher/check': '签到二维码',
  '/teacher/review': '签退审核',
  '/teacher/certificates': '证明审核',
  '/teacher/points': '积分管理',
  
  // Student
  '/student/dashboard': '个人中心',
  '/student/activities': '活动列表',
  '/student/signups': '我的报名',
  '/student/records': '我的记录',
  '/student/points': '积分中心',
  '/student/certificates': '证明管理',
  '/student/mall': '积分商城',
  '/student/mall/my-redemptions': '我的兑换',
  '/student/mall/product': '商品详情',
  '/student/mall/voucher': '兑换凭证',
  
  // Common
  '/profile': '个人资料',
  '/notifications': '通知中心',
  '/activities': '活动详情'
}

const breadcrumbs = computed(() => {
  const path = route.path
  const role = authStore.userRole
  const crumbs = []
  
  // 添加首页
  if (role === 'ADMIN') {
    crumbs.push({ label: '学工处', path: '/admin/dashboard' })
  } else if (role === 'TEACHER') {
    crumbs.push({ label: '教师工作台', path: '/teacher/dashboard' })
  } else if (role === 'STUDENT') {
    crumbs.push({ label: '学生中心', path: '/student/dashboard' })
  }
  
  // 解析当前路径
  const pathSegments = path.split('/').filter(Boolean)
  let currentPath = ''
  
  for (let i = 0; i < pathSegments.length; i++) {
    currentPath += '/' + pathSegments[i]
    
    // 跳过首页路径
    if (currentPath === '/admin/dashboard' || 
        currentPath === '/teacher/dashboard' || 
        currentPath === '/student/dashboard') {
      continue
    }
    
    // 获取标题
    let title = routeTitleMap[currentPath]
    
    // 处理动态路由
    if (!title && pathSegments[i].match(/^\d+$/)) {
      // 如果是数字ID，使用上一级的标题
      const parentPath = currentPath.substring(0, currentPath.lastIndexOf('/'))
      if (parentPath.includes('product')) {
        title = '商品详情'
      } else if (parentPath.includes('voucher')) {
        title = '兑换凭证'
      } else if (parentPath.includes('activities')) {
        title = '活动详情'
      }
    }
    
    if (title) {
      crumbs.push({ label: title, path: currentPath })
    }
  }
  
  return crumbs
})

const goToProfile = () => {
  profileVisible.value = true
}

const handleSearch = () => {
  MessagePlugin.info('搜索功能开发中...')
}

const handleLogout = () => {
  const confirmDialog = DialogPlugin.confirm({
    header: '提示',
    body: '确定要退出登录吗？',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: () => {
      confirmDialog.destroy()
      // 在对话框关闭后执行登出和跳转
      setTimeout(async () => {
        try {
          await authStore.logout()
          router.push('/auth/login')
        } catch (error) {
          console.error('退出登录失败:', error)
        }
      }, 100)
    },
    onCancel: () => {
      confirmDialog.destroy()
    }
  })
}

const profileVisible = ref(false)
</script>

<style lang="scss" scoped>
.app-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .header-left {
    display: flex;
    align-items: center;
    flex: 1;
    min-width: 0;
  }

  .header-operations {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
  }
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;

  .logo-icon {
    display: inline-flex;
    align-items: center;
    width: 28px;
    height: 28px;

    :deep(svg) {
      width: 100%;
      height: 100%;
    }
  }

  .logo-text {
    font-size: 18px;
    font-weight: 600;
    white-space: nowrap;
    color: var(--td-text-color-primary);
  }
}

:deep(.el-breadcrumb) {
  font-size: 14px;
  
  .el-breadcrumb__item {
    .el-breadcrumb__inner {
      color: #606266;
      font-weight: 400;
      
      &:hover {
        color: var(--primary-color, #409eff);
      }
    }
    
    &:last-child {
      .el-breadcrumb__inner {
        color: var(--primary-color, #303133);
        font-weight: 500;
      }
    }
  }
  
  .el-breadcrumb__separator {
    color: #c0c4cc;
    margin: 0 8px;
  }
}

@media (max-width: 768px) {
  .app-header {
    padding: 0 16px;
  }

  .logo {
    .logo-text {
      display: none;
    }
  }
  
  :deep(.el-breadcrumb) {
    font-size: 13px;
    
    .el-breadcrumb__separator {
      margin: 0 6px;
    }
  }
  
  :deep(.el-divider) {
    margin: 0 12px !important;
  }
}
</style>
