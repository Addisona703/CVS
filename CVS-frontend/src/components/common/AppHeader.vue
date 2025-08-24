<template>
  <div class="app-header">
    <div class="header-left">
      <el-button
        type="text"
        size="large"
        @click="toggleSidebar"
      >
        <el-icon>
          <Fold v-if="!sidebarCollapsed" />
          <Expand v-else />
        </el-icon>
      </el-button>
      
      <div class="breadcrumb">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item
            v-for="item in breadcrumbItems"
            :key="item.path"
            :to="item.path"
          >
            {{ item.title }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>
    
    <div class="header-right">
      <el-dropdown trigger="hover">
        <div class="user-info">
          <el-avatar :size="32" :src="userAvatar">
            <el-icon><User /></el-icon>
          </el-avatar>
          <span class="username">{{ userName }}</span>
          <el-icon class="arrow-down"><ArrowDown /></el-icon>
        </div>
        
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="goToProfile">
              <el-icon><User /></el-icon>
              个人资料
            </el-dropdown-item>
            <el-dropdown-item @click="toggleTheme">
              <el-icon><Moon /></el-icon>
              切换主题
            </el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { ElMessageBox } from 'element-plus'
import {
  Fold,
  Expand,
  User,
  ArrowDown,
  Moon,
  SwitchButton
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const appStore = useAppStore()

const userName = computed(() => authStore.userName)
const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)
const userAvatar = computed(() => {
  switch (authStore.userRole) {
    case 'ADMIN':
      return new URL('@/assets/images/admin.png', import.meta.url).href
    case 'TEACHER':
      return new URL('@/assets/images/teacher.png', import.meta.url).href
    case 'STUDENT':
      return new URL('@/assets/images/student.png', import.meta.url).href
    default:
      return new URL('@/assets/images/duke.jpg', import.meta.url).href
  }
})

const breadcrumbItems = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  return matched.map(item => ({
    path: item.path,
    title: item.meta.title
  }))
})

const toggleSidebar = () => {
  appStore.toggleSidebar()
}

const goToProfile = () => {
  router.push('/profile')
}

const toggleTheme = () => {
  const currentTheme = appStore.theme
  const newTheme = currentTheme === 'light' ? 'dark' : 'light'
  appStore.setTheme(newTheme)
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await authStore.logout()
    router.push('/auth/login')
  } catch (error) {
    // 用户取消
  }
}
</script>

<style lang="scss" scoped>
.app-header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.breadcrumb {
  :deep(.el-breadcrumb__item) {
    .el-breadcrumb__inner {
      color: #606266;
      font-weight: normal;
      
      &:hover {
        color: #409eff;
      }
    }
    
    &:last-child .el-breadcrumb__inner {
      color: #303133;
      font-weight: 500;
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
  
  &:hover {
    background-color: #f5f7fa;
  }
  
  .username {
    font-size: 14px;
    color: #303133;
    font-weight: 500;
  }
  
  .arrow-down {
    font-size: 12px;
    color: #909399;
  }
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
