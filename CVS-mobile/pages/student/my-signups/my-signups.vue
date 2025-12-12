<template>
  <view class="my-signups-page">
    <!-- 状态筛选 -->
    <view class="tabs">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: currentTab === tab.value }"
        @click="handleTabChange(tab.value)"
      >
        {{ tab.label }}
      </view>
    </view>

    <!-- 报名列表 -->
    <scroll-view
      class="scroll-view"
      scroll-y
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="refresh"
    >
      <view class="list-container">
        <signup-card
          v-for="signup in list"
          :key="signup.id"
          :signup="signup"
          @click="handleSignupClick(signup)"
          @cancel="handleCancelSignup(signup)"
        />

        <!-- 加载状态 -->
        <view v-if="loading" class="loading">加载中...</view>
        <view v-else-if="finished && list.length > 0" class="finished">没有更多了</view>
        <view v-else-if="isEmpty" class="empty">
          <text class="empty-icon">📭</text>
          <text class="empty-text">暂无报名记录</text>
        </view>
      </view>
    </scroll-view>

    <!-- Custom Tab Bar -->
    <custom-tabbar :current="2" role="student" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { usePagination } from '@/composables/usePagination'
import { getMySignups, cancelSignup } from '@/api/signup'
import SignupCard from '@/components/business/signup-card/signup-card.vue'
import CustomTabbar from '@/components/common/custom-tabbar/custom-tabbar.vue'

// 隐藏官方 tabBar
onMounted(() => {
  uni.hideTabBar({
    animation: false
  })
})

const currentTab = ref('')

const tabs = [
  { label: '全部', value: '' },
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' }
]

const {
  list,
  loading,
  refreshing,
  finished,
  isEmpty,
  refresh,
  loadMore,
  updateParams,
  removeItem
} = usePagination(getMySignups, {
  params: {}
})

const handleTabChange = (value) => {
  currentTab.value = value
  updateParams({ status: value })
}

const handleSignupClick = (signup) => {
  uni.navigateTo({
    url: `/pages/common/activity-detail/activity-detail?id=${signup.activityId}`
  })
}

const handleCancelSignup = async (signup) => {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消这个报名吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await cancelSignup(signup.id)
          uni.showToast({
            title: '取消成功',
            icon: 'success'
          })
          removeItem(signup.id)
        } catch (error) {
          uni.showToast({
            title: error.message || '取消失败',
            icon: 'none'
          })
        }
      }
    }
  })
}
</script>

<style scoped>
.my-signups-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
  padding-bottom: 120rpx;
}

.tabs {
  display: flex;
  background: #fff;
  padding: 0 24rpx;
}

.tab-item {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  font-size: 28rpx;
  color: #666;
  position: relative;
}

.tab-item.active {
  color: #0052d9;
  font-weight: 600;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 48rpx;
  height: 4rpx;
  background: #0052d9;
  border-radius: 2rpx;
}

.scroll-view {
  flex: 1;
}

.list-container {
  padding: 24rpx;
}

.loading,
.finished,
.empty {
  text-align: center;
  padding: 60rpx 0;
  color: #999;
  font-size: 28rpx;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;
}

.empty-icon {
  font-size: 96rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}
</style>
