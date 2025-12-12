<template>
  <div class="student-layout">
    <t-layout>
      <t-header class="header">
        <AppHeader />
      </t-header>

      <t-layout style="height: calc(100vh - 60px);">
        <t-aside width="240px" class="sidebar" style="flex-shrink: 0;">
          <t-menu :value="activeMenu" theme="light" class="sidebar-menu" @change="handleMenuChange">
            <t-menu-item value="/student/dashboard">
              <template #icon><el-icon>
                  <User />
                </el-icon></template>
              个人中心
            </t-menu-item>

            <t-menu-item value="/student/activities">
              <template #icon><el-icon>
                  <Calendar />
                </el-icon></template>
              活动列表
            </t-menu-item>

            <t-menu-item value="/student/signups">
              <template #icon><el-icon>
                  <Tickets />
                </el-icon></template>
              我的报名
            </t-menu-item>

            <t-menu-item value="/student/records">
              <template #icon><el-icon>
                  <Document />
                </el-icon></template>
              服务记录
            </t-menu-item>

            <t-menu-item value="/student/points">
              <template #icon><el-icon>
                  <Star />
                </el-icon></template>
              积分
            </t-menu-item>

            <t-menu-item value="/student/mall">
              <template #icon><el-icon>
                  <ShoppingCart />
                </el-icon></template>
              积分商城
            </t-menu-item>

            <t-menu-item value="/student/mall/my-redemptions">
              <template #icon><el-icon>
                  <List />
                </el-icon></template>
              我的兑换
            </t-menu-item>

            <t-menu-item value="/student/certificates">
              <template #icon><el-icon>
                  <Medal />
                </el-icon></template>
              证明
            </t-menu-item>
          </t-menu>
        </t-aside>

        <t-content class="main-content">
          <router-view />
        </t-content>
      </t-layout>
    </t-layout>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { User, Calendar, Location, Tickets, Document, Star, Medal, ShoppingCart, List } from '@element-plus/icons-vue'
import AppHeader from '@/components/common/AppHeader.vue'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)

const handleMenuChange = (value) => {
  router.push(value)
}
</script>

<style lang="scss" scoped>
.student-layout {
  height: 100vh;
  overflow: hidden;
}

.header {
  padding: 0;
  height: 60px;
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  z-index: 10;
}

.sidebar {
  background: #f5f7fa;
  border-right: 1px solid #e5e7eb;
  overflow-y: auto;
  width: 240px;
  min-width: 240px;
  max-width: 240px;
  flex-shrink: 0;

  .sidebar-menu {
    border: none;
    background: transparent;
    padding: 8px;

    :deep(.t-menu__item) {
      height: 48px;
      line-height: 48px;
      color: #606266;
      margin: 4px 0;
      border-radius: 8px;
      padding-left: 16px !important;
      padding-right: 16px !important;

      .t-icon,
      .el-icon {
        font-size: 18px;
        color: #909399;
      }

      &:hover {
        background-color: #e6f2ff;
        color: #1f6bff;

        .t-icon,
        .el-icon {
          color: #1f6bff;
        }
      }

      &.t-is-active {
        background-color: #1f6bff;
        color: #ffffff;
        font-weight: 600;

        .t-icon,
        .el-icon {
          color: #ffffff;
        }
      }
    }
  }
}

.main-content {
  background: #f0f2f5;
  overflow-y: auto;
  padding: 24px;
  flex: 1;
  min-width: 0;

  :deep(.el-card) {
    background: #ffffff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
    }
  }
}

@media (max-width: 768px) {
  .main-content {
    padding: 16px;
  }
}
</style>
