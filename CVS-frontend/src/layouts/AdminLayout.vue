<template>
  <div class="admin-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="sidebarWidth" class="sidebar">
        <div class="logo">
          <el-icon size="24" color="#409eff">
            <School />
          </el-icon>
          <span v-if="!sidebarCollapsed" class="logo-text">CVS管理系统</span>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          :collapse="sidebarCollapsed"
          :unique-opened="true"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><Odometer /></el-icon>
            <template #title>仪表板</template>
          </el-menu-item>
          
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          
          <el-menu-item index="/admin/activities">
            <el-icon><Calendar /></el-icon>
            <template #title>活动管理</template>
          </el-menu-item>
          
          <el-sub-menu index="points">
            <template #title>
              <el-icon><Star /></el-icon>
              <span>积分管理</span>
            </template>
            <el-menu-item index="/admin/points/records">积分记录</el-menu-item>
            <el-menu-item index="/admin/points/ranking">积分排行</el-menu-item>
          </el-sub-menu>
          
          <el-menu-item index="/admin/certificates">
            <el-icon><Medal /></el-icon>
            <template #title>证明管理</template>
          </el-menu-item>
          
          <el-menu-item index="/admin/statistics">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>统计分析</template>
          </el-menu-item>

          <el-menu-item index="/admin/records">
            <el-icon><Document /></el-icon>
            <template #title>服务记录管理</template>
          </el-menu-item>

          <el-menu-item index="/admin/roles">
            <el-icon><User /></el-icon>
            <template #title>角色管理</template>
          </el-menu-item>

          <!-- <el-menu-item index="/system/status">
            <el-icon><Setting /></el-icon>
            <template #title>系统状态</template>
          </el-menu-item> -->
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
  User,
  Calendar,
  Star,
  Medal,
  DataAnalysis,
  Setting
} from '@element-plus/icons-vue'

const route = useRoute()
const appStore = useAppStore()

const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)
const sidebarWidth = computed(() => sidebarCollapsed.value ? '64px' : '200px')
const activeMenu = computed(() => route.path)
</script>

<style lang="scss" scoped>
.admin-layout {
  height: 100%;
  overflow: hidden;
  display: flex
}

.sidebar {
  background: #304156;
  transition: width 0.3s;
  overflow: hidden;
  
  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 0 20px;
    border-bottom: 1px solid #434a5a;
    
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
      color: #bfcbd9;
      border-bottom: 1px solid #434a5a;
      
      &:hover {
        background-color: #434a5a;
        color: #fff;
      }
      
      &.is-active {
        background-color: #409eff;
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
        color: #bfcbd9;
        border-bottom: 1px solid #434a5a;
        
        &:hover {
          background-color: #434a5a;
          color: #fff;
        }
      }
      
      .el-menu {
        background: #263445;
        
        .el-menu-item {
          color: #bfcbd9;
          border-bottom: none;
          padding-left: 50px !important;
          
          &:hover {
            background-color: #434a5a;
            color: #fff;
          }
          
          &.is-active {
            background-color: #409eff;
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
