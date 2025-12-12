<template>
  <view class="my-records-page">
    <!-- 统计卡片 -->
    <view class="stats-card">
      <view class="stat-item">
        <view class="stat-icon">⏰</view>
        <view class="stat-value">{{ stats.totalHours }}</view>
        <view class="stat-label">总服务时长(h)</view>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <view class="stat-icon">🎯</view>
        <view class="stat-value">{{ stats.totalPoints }}</view>
        <view class="stat-label">总获得积分</view>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <view class="stat-icon">📊</view>
        <view class="stat-value">{{ stats.activityCount }}</view>
        <view class="stat-label">参与活动数</view>
      </view>
    </view>

    <!-- 记录列表 -->
    <scroll-view
      class="scroll-view"
      scroll-y
      enable-back-to-top
      scroll-with-animation
      :lower-threshold="100"
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="refresh"
    >
      <view class="list-container">
        <record-card
          v-for="record in list"
          :key="record.id"
          :record="record"
          @click="handleRecordClick(record)"
        />

        <!-- 加载状态 -->
        <view v-if="loading" class="loading">加载中...</view>
        <view v-else-if="finished && list.length > 0" class="finished">没有更多了</view>
        <view v-else-if="isEmpty" class="empty">
          <text class="empty-icon">📭</text>
          <text class="empty-text">暂无服务记录</text>
          <text class="empty-hint">参与活动后会自动生成记录</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { usePagination } from '@/composables/usePagination'
import { getMyServiceRecords, getMyServiceStats } from '@/api/serviceRecord'
import RecordCard from '@/components/business/record-card/record-card.vue'

const stats = ref({
  totalHours: 0,
  totalPoints: 0,
  activityCount: 0
})

const {
  list,
  loading,
  refreshing,
  finished,
  isEmpty,
  refresh,
  loadMore
} = usePagination(getMyServiceRecords, {
  params: {}
})

const loadStats = async () => {
  try {
    const res = await getMyServiceStats()
    console.log('服务统计数据:', res)
    stats.value = {
      totalHours: (res.totalServiceHours || 0).toFixed(1),
      totalPoints: res.totalPointsEarned || 0,
      activityCount: res.totalActivities || 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    uni.showToast({
      title: '加载统计失败',
      icon: 'none'
    })
  }
}

const handleRecordClick = (record) => {
  uni.navigateTo({
    url: `/pages/common/activity-detail/activity-detail?id=${record.activityId}`
  })
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.my-records-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
  overflow: hidden;
}

.stats-card {
  display: flex;
  background: linear-gradient(135deg, #0052d9 0%, #0041a8 100%);
  padding: 40rpx 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 82, 217, 0.2);
}

.stat-item {
  flex: 1;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.stat-icon {
  font-size: 40rpx;
  margin-bottom: 4rpx;
}

.stat-value {
  font-size: 48rpx;
  font-weight: 700;
  color: #fff;
  line-height: 1;
}

.stat-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.stat-divider {
  width: 1rpx;
  background: rgba(255, 255, 255, 0.2);
  margin: 0 16rpx;
}

.scroll-view {
  flex: 1;
  height: 0;
  -webkit-overflow-scrolling: touch;
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
  gap: 16rpx;
  padding: 120rpx 0;
}

.empty-icon {
  font-size: 96rpx;
  opacity: 0.6;
}

.empty-text {
  font-size: 28rpx;
  color: #666;
  font-weight: 500;
}

.empty-hint {
  font-size: 24rpx;
  color: #999;
}
</style>
