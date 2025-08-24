<template>
  <div class="teacher-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="sidebarWidth" class="sidebar">
        <div class="logo">
          <el-icon size="24" color="#67c23a">
            <School />
          </el-icon>
          <span v-if="!sidebarCollapsed" class="logo-text">教师工作台</span>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          :collapse="sidebarCollapsed"
          :unique-opened="true"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/teacher/dashboard">
            <el-icon><Odometer /></el-icon>
            <template #title>工作台</template>
          </el-menu-item>
          
          <el-sub-menu index="activities">
            <template #title>
              <el-icon><Calendar /></el-icon>
              <span>活动管理</span>
            </template>
            <el-menu-item index="/teacher/activities">我的活动</el-menu-item>
            <el-menu-item index="/teacher/activities/create">创建活动</el-menu-item>
          </el-sub-menu>
          
          <el-menu-item index="/teacher/signups">
            <el-icon><UserFilled /></el-icon>
            <template #title>报名管理</template>
          </el-menu-item>
          
          <el-menu-item index="/teacher/records">
            <el-icon><Document /></el-icon>
            <template #title>服务记录</template>
          </el-menu-item>
          
          <el-menu-item index="/teacher/certificates">
            <el-icon><Medal /></el-icon>
            <template #title>证明审核</template>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 主内容区 -->
      <el-container>
        <el-header class="header">
          <AppHeader />
        </el-header>
        
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'
import AppHeader from '@/components/common/AppHeader.vue'
import {
  School,
  Odometer,
  Calendar,
  UserFilled,
  Document,
  Medal
} from '@element-plus/icons-vue'

const route = useRoute()
const appStore = useAppStore()

const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)
const sidebarWidth = computed(() => sidebarCollapsed.value ? '64px' : '200px')
const activeMenu = computed(() => route.path)
</script>

<style lang="scss" scoped>
.teacher-layout {
  height: 100%;
  overflow: hidden;
  display: flex
}

.sidebar {
  background: #2d5a27;
  transition: width 0.3s;
  overflow: hidden;
  
  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 0 20px;
    border-bottom: 1px solid #3d6a37;
    
    .logo-text {
      color: #fff;
      font-size: 16px;
      font-weight: 600;
      white-space: nowrap;
    }
  }
  
  .sidebar-menu {
    border: none;
    background: transparent;
    
    :deep(.el-menu-item) {
      color: #b8d4b5;
      border-bottom: 1px solid #3d6a37;
      
      &:hover {
        background-color: #3d6a37;
        color: #fff;
      }
      
      &.is-active {
        background-color: #67c23a;
        color: #fff;
        
        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 0;
          bottom: 0;
          width: 3px;
          background: #fff;
        }
      }
    }
    
    :deep(.el-sub-menu) {
      .el-sub-menu__title {
        color: #b8d4b5;
        border-bottom: 1px solid #3d6a37;
        
        &:hover {
          background-color: #3d6a37;
          color: #fff;
        }
      }
      
      .el-menu {
        background: #1f4a1a;
        
        .el-menu-item {
          color: #b8d4b5;
          border-bottom: none;
          padding-left: 50px !important;
          
          &:hover {
            background-color: #3d6a37;
            color: #fff;
          }
          
          &.is-active {
            background-color: #67c23a;
            color: #fff;
          }
        }
      }
    }
  }
}

.header {
  padding: 0;
  height: 60px;
}

.main-content {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
