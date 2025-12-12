<template>
  <div class="teacher-layout">
    <t-layout>
      <t-header class="header">
        <AppHeader />
      </t-header>

      <t-layout style="height: calc(100vh - 60px);">
        <t-aside width="240px" class="sidebar" style="flex-shrink: 0;">
          <t-menu :value="activeMenu" theme="light" class="sidebar-menu" @change="handleMenuChange">
            <t-menu-item value="/teacher/dashboard">
              <template #icon><t-icon name="dashboard" /></template>
              工作台
            </t-menu-item>

            <t-submenu value="activities">
              <template #icon><t-icon name="calendar" /></template>
              <template #title>活动管理</template>
              <t-menu-item value="/teacher/activities">我的活动</t-menu-item>
              <t-menu-item value="/teacher/activities/create">创建活动</t-menu-item>
            </t-submenu>

            <t-menu-item value="/teacher/signups">
              <template #icon><t-icon name="user" /></template>
              报名管理
            </t-menu-item>

            <t-menu-item value="/teacher/records">
              <template #icon><t-icon name="assignment" /></template>
              服务记录
            </t-menu-item>

            <t-menu-item value="/teacher/check">
              <template #icon><t-icon name="qrcode" /></template>
              签到二维码
            </t-menu-item>

            <t-menu-item value="/teacher/review">
              <template #icon><t-icon name="edit" /></template>
              签退审核
            </t-menu-item>

            <t-menu-item value="/teacher/points">
              <template #icon><t-icon name="star" /></template>
              积分管理
            </t-menu-item>

            <!-- <t-submenu value="mall">
              <template #icon><t-icon name="shop" /></template>
              <template #title>商城管理</template>
              <t-menu-item value="/teacher/mall/products">商品管理</t-menu-item>
              <t-menu-item value="/teacher/mall/categories">分类管理</t-menu-item>
              <t-menu-item value="/teacher/mall/verify">兑换核销</t-menu-item>
              <t-menu-item value="/teacher/mall/statistics">统计报表</t-menu-item>
            </t-submenu> -->
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
import AppHeader from '@/components/common/AppHeader.vue'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)

const handleMenuChange = (value) => {
  router.push(value)
}
</script>

<style lang="scss">
@use '@/styles/teacher-theme.scss';
</style>

<style lang="scss" scoped>
.teacher-layout {
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

      .t-icon {
        font-size: 18px;
        color: #909399;
      }

      &:hover {
        background-color: #e8f9f0;
        color: #97cf7b;

        .t-icon {
          color: #67c23a;
        }
      }

      &.t-is-active {
        background-color: #67c23a;
        color: #ffffff;
        font-weight: 600;

        .t-icon {
          color: #ffffff;
        }
      }
    }

    :deep(.t-submenu) {
      margin: 4px 0;

      .t-submenu__title {
        height: 48px;
        line-height: 48px;
        color: #606266;
        margin: 0;
        border-radius: 8px;
        padding-left: 16px !important;
        padding-right: 16px !important;

        .t-icon {
          font-size: 18px;
          color: #909399;
        }

        &:hover {
          background-color: #e8f9f0;
          color: #67c23a;

          .t-icon {
            color: #67c23a;
          }
        }
      }

      .t-submenu__content {
        background: transparent;
        padding: 0 8px;

        .t-menu__item {
          height: 40px;
          line-height: 40px;
          padding-left: 40px !important;
          padding-right: 16px !important;
          font-size: 14px;
          margin: 4px 0;
          border-radius: 8px;

          &:hover {
            background-color: #e8f9f0;
            color: #67c23a;
          }

          &.t-is-active {
            background-color: #67c23a;
            color: #ffffff;
            font-weight: 600;
          }
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
    box-shadow: 0 2px 8px rgba(103, 194, 58, 0.08);
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 4px 12px rgba(103, 194, 58, 0.12);
    }
  }
}

@media (max-width: 768px) {
  .main-content {
    padding: 16px;
  }
}
</style>
