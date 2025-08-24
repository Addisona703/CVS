<template>
  <div class="student-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="sidebarWidth" class="sidebar">
        <div class="logo">
          <el-icon size="24" color="#e6a23c">
            <School />
          </el-icon>
          <span v-if="!sidebarCollapsed" class="logo-text">学生中心</span>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          :collapse="sidebarCollapsed"
          :unique-opened="true"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/student/dashboard">
            <el-icon><Odometer /></el-icon>
            <template #title>个人中心</template>
          </el-menu-item>
          
          <el-menu-item index="/student/activities">
            <el-icon><Calendar /></el-icon>
            <template #title>活动报名</template>
          </el-menu-item>
          
          <el-menu-item index="/student/signups">
            <el-icon><UserFilled /></el-icon>
            <template #title>我的报名</template>
          </el-menu-item>
          
          <el-menu-item index="/student/records">
            <el-icon><Document /></el-icon>
            <template #title>服务记录</template>
          </el-menu-item>
          
          <el-menu-item index="/student/points">
            <el-icon><Star /></el-icon>
            <template #title>积分中心</template>
          </el-menu-item>
          
          <el-menu-item index="/student/certificates">
            <el-icon><Medal /></el-icon>
            <template #title>志愿证明</template>
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
  Star,
  Medal
} from '@element-plus/icons-vue'

const route = useRoute()
const appStore = useAppStore()

const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)
const sidebarWidth = computed(() => sidebarCollapsed.value ? '64px' : '200px')
const activeMenu = computed(() => route.path)
</script>

<style lang="scss" scoped>
.student-layout {
  height: 100%;
  overflow: hidden;
  display: flex
}

.sidebar {
  background: #5a4a2d;
  transition: width 0.3s;
  overflow: hidden;
  
  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 0 20px;
    border-bottom: 1px solid #6a5a3d;
    
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
      color: #d4c4b8;
      border-bottom: 1px solid #6a5a3d;
      
      &:hover {
        background-color: #6a5a3d;
        color: #fff;
      }
      
      &.is-active {
        background-color: #e6a23c;
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
        color: #d4c4b8;
        border-bottom: 1px solid #6a5a3d;
        
        &:hover {
          background-color: #6a5a3d;
          color: #fff;
        }
      }
      
      .el-menu {
        background: #4a3a1f;
        
        .el-menu-item {
          color: #d4c4b8;
          border-bottom: none;
          padding-left: 50px !important;
          
          &:hover {
            background-color: #6a5a3d;
            color: #fff;
          }
          
          &.is-active {
            background-color: #e6a23c;
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

@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    top: 0;
    left: 0;
    bottom: 0;
    z-index: 1000;
  }
  
  .main-content {
    padding: 16px;
  }
}
</style>
