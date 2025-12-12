<template>
  <view class="activities-page">
    <!-- 搜索框 - 固定在顶部 -->
    <view class="search-section">
      <view class="search-box">
        <ph-icon name="magnifying-glass" :size="36" color="#999" />
        <input
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索活动标题..."
          @confirm="handleSearch"
        />
      </view>
    </view>

    <!-- 筛选标签页 - 固定在顶部 -->
    <view class="tabs-section">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: currentTab === tab.value }"
        @click="handleTabChange(tab.value)"
      >
        <text class="tab-text">{{ tab.label }}</text>
        <view v-if="currentTab === tab.value" class="tab-indicator"></view>
      </view>
    </view>

    <!-- 活动列表 - 只有这部分可以滚动和下拉刷新 -->
    <scroll-view
      class="scroll-view"
      scroll-y
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="refresh"
    >
      <view class="list-container">
        <activity-card
          v-for="activity in list"
          :key="activity.id"
          :activity="activity"
          @click="handleActivityClick(activity)"
        />

        <!-- 加载更多状态 -->
        <view v-if="loading && list.length > 0" class="loading-more">
          <text>加载中...</text>
        </view>
        <view v-else-if="finished && list.length > 0" class="finished">没有更多了</view>
        
        <!-- 空状态 -->
        <view v-if="isEmpty && !loading" class="empty-state">
          <text class="empty-icon">📋</text>
          <text class="empty-text">暂无活动</text>
        </view>
      </view>
    </scroll-view>

    <!-- Custom Tab Bar -->
    <custom-tabbar :current="1" role="student" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { usePagination } from '@/composables/usePagination'
import { getActivityList } from '@/api/activity'
import ActivityCard from '@/components/business/activity-card/activity-card.vue'
import CustomTabbar from '@/components/common/custom-tabbar/custom-tabbar.vue'
import PhIcon from '@/components/common/ph-icon/ph-icon.vue'

// 隐藏官方 tabBar
onMounted(() => {
  uni.hideTabBar({
    animation: false
  })
})

const searchKeyword = ref('')
const currentTab = ref('')

const tabs = [
  { label: '全部', value: '' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '进行中', value: 'ONGOING' },
  { label: '已完成', value: 'COMPLETED' }
]

const {
  list,
  loading,
  refreshing,
  finished,
  isEmpty,
  refresh,
  loadMore,
  updateParams
} = usePagination(getActivityList, {
  params: {
    status: 'PUBLISHED'
  }
})

const handleSearch = () => {
  updateParams({
    title: searchKeyword.value,
    status: currentTab.value || 'PUBLISHED'
  })
}

const handleTabChange = (value) => {
  currentTab.value = value
  updateParams({
    title: searchKeyword.value,
    status: value || 'PUBLISHED'
  })
}

const handleActivityClick = (activity) => {
  uni.navigateTo({
    url: `/pages/common/activity-detail/activity-detail?id=${activity.id}`
  })
}
</script>

<style scoped>
.activities-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #F5F5F5;
  padding-bottom: 120rpx;
}

/* 搜索框区域 */
.search-section {
  padding: 24rpx 32rpx;
  background: #FFFFFF;
}

.search-box {
  display: flex;
  align-items: center;
  height: 80rpx;
  padding: 0 24rpx;
  background: #F5F5F5;
  border-radius: 16rpx;
  gap: 12rpx;
}



.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  background: transparent;
}

.search-input::placeholder {
  color: #999;
}

/* 筛选标签页 */
.tabs-section {
  display: flex;
  background: #FFFFFF;
  padding: 0 32rpx;
  border-bottom: 1rpx solid #F0F0F0;
}

.tab-item {
  position: relative;
  flex: 1;
  height: 88rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.tab-text {
  font-size: 28rpx;
  color: #666;
  transition: all 0.3s;
}

.tab-item.active .tab-text {
  color: #0052d9;
  font-weight: 600;
}

/* 高亮下划线 */
.tab-indicator {
  position: absolute;
  bottom: 0;
  width: 48rpx;
  height: 6rpx;
  background: #0052d9;
  border-radius: 3rpx;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    width: 0;
  }
  to {
    width: 48rpx;
  }
}

/* 列表区域 */
.scroll-view {
  flex: 1;
  background: #F5F5F5;
}

.list-container {
  padding: 24rpx;
}

/* 加载更多状态 */
.loading-more {
  text-align: center;
  padding: 40rpx 0;
  color: #999;
  font-size: 26rpx;
}

.finished {
  text-align: center;
  padding: 40rpx 0;
  color: #999;
  font-size: 26rpx;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx 0;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 24rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}
</style>
