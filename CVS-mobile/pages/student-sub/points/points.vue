<template>
  <view class="points-page">
    <!-- 积分余额卡片 -->
    <view class="balance-card">
      <view class="balance-label">我的积分</view>
      <view class="balance-value">{{ balance }}</view>
      <view class="balance-desc">可用于兑换商品</view>
    </view>

    <!-- 排行榜 -->
    <view class="ranking-section">
      <view class="section-header">
        <view class="section-title">积分排行榜</view>
        <view class="period-selector">
          <text
            v-for="period in periods"
            :key="period.value"
            class="period-item"
            :class="{ active: currentPeriod === period.value }"
            @click="handlePeriodChange(period.value)"
          >
            {{ period.label }}
          </text>
        </view>
      </view>
      
      <view v-if="loadingRanking" class="loading">加载中...</view>
      <view v-else-if="ranking.length === 0" class="empty">暂无排行数据</view>
      <view v-else class="ranking-list">
        <view v-for="(item, index) in ranking" :key="item.userId" class="ranking-item">
          <view class="rank" :class="getRankClass(index)">{{ index + 1 }}</view>
          <view class="user-info">
            <view class="username">{{ item.username }}</view>
            <view class="points">{{ item.points }} 积分</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 积分明细 -->
    <view class="records-section">
      <view class="section-title">积分明细</view>
      
      <scroll-view
        class="scroll-view"
        scroll-y
        @scrolltolower="loadMore"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="refresh"
      >
        <view class="records-list">
          <view v-for="record in list" :key="record.id" class="record-item">
            <view class="record-info">
              <view class="record-title">{{ record.description }}</view>
              <view class="record-time">{{ formatTime(record.createTime) }}</view>
            </view>
            <view class="record-amount" :class="record.type === 'EARN' ? 'earn' : 'spend'">
              {{ record.type === 'EARN' ? '+' : '-' }}{{ record.amount }}
            </view>
          </view>

          <!-- 加载状态 -->
          <view v-if="loading" class="loading">加载中...</view>
          <view v-else-if="finished && list.length > 0" class="finished">没有更多了</view>
          <view v-else-if="isEmpty" class="empty">暂无积分记录</view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { usePagination } from '@/composables/usePagination'
import { getCurrentUserPointsStats, getAllPointsRecords, getPointsRankingPage } from '@/api/points'
import { formatDateTime } from '@/utils/format'

const balance = ref(0)
const ranking = ref([])
const loadingRanking = ref(false)
const currentPeriod = ref('month')

const periods = [
  { label: '周', value: 'week' },
  { label: '月', value: 'month' },
  { label: '年', value: 'year' },
  { label: '总榜', value: 'all' }
]

const {
  list,
  loading,
  refreshing,
  finished,
  isEmpty,
  refresh,
  loadMore
} = usePagination(getAllPointsRecords, {
  params: {}
})

const loadBalance = async () => {
  try {
    const res = await getCurrentUserPointsStats()
    balance.value = res.balance || 0
  } catch (error) {
    console.error('加载积分余额失败:', error)
  }
}

const loadRanking = async () => {
  loadingRanking.value = true
  try {
    const res = await getPointsRankingPage(1, 10)
    ranking.value = res || []
  } catch (error) {
    console.error('加载排行榜失败:', error)
  } finally {
    loadingRanking.value = false
  }
}

const handlePeriodChange = (period) => {
  currentPeriod.value = period
  loadRanking()
}

const getRankClass = (index) => {
  if (index === 0) return 'rank-1'
  if (index === 1) return 'rank-2'
  if (index === 2) return 'rank-3'
  return ''
}

const formatTime = (time) => {
  return formatDateTime(time, 'MM-DD HH:mm')
}

onMounted(() => {
  loadBalance()
  loadRanking()
})
</script>

<style scoped>
.points-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.balance-card {
  background: linear-gradient(135deg, #0052d9 0%, #0041a8 100%);
  padding: 60rpx 32rpx;
  text-align: center;
  color: #fff;
}

.balance-label {
  font-size: 28rpx;
  opacity: 0.9;
  margin-bottom: 16rpx;
}

.balance-value {
  font-size: 96rpx;
  font-weight: 600;
  margin-bottom: 16rpx;
}

.balance-desc {
  font-size: 24rpx;
  opacity: 0.8;
}

.ranking-section,
.records-section {
  background: #fff;
  margin: 24rpx;
  border-radius: 12rpx;
  padding: 32rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.period-selector {
  display: flex;
  gap: 16rpx;
}

.period-item {
  padding: 8rpx 16rpx;
  font-size: 24rpx;
  color: #666;
  border-radius: 8rpx;
}

.period-item.active {
  background: #0052d9;
  color: #fff;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 16rpx;
  background: #f5f5f5;
  border-radius: 8rpx;
}

.rank {
  width: 48rpx;
  height: 48rpx;
  line-height: 48rpx;
  text-align: center;
  border-radius: 50%;
  background: #e0e0e0;
  color: #666;
  font-size: 24rpx;
  font-weight: 600;
  margin-right: 16rpx;
}

.rank-1 {
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
  color: #fff;
}

.rank-2 {
  background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
  color: #fff;
}

.rank-3 {
  background: linear-gradient(135deg, #cd7f32 0%, #e8a87c 100%);
  color: #fff;
}

.user-info {
  flex: 1;
}

.username {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 4rpx;
}

.points {
  font-size: 24rpx;
  color: #999;
}

.scroll-view {
  max-height: 800rpx;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  background: #f5f5f5;
  border-radius: 8rpx;
}

.record-info {
  flex: 1;
}

.record-title {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 8rpx;
}

.record-time {
  font-size: 24rpx;
  color: #999;
}

.record-amount {
  font-size: 32rpx;
  font-weight: 600;
}

.record-amount.earn {
  color: #52c41a;
}

.record-amount.spend {
  color: #ff4d4f;
}

.loading,
.finished,
.empty {
  text-align: center;
  padding: 40rpx 0;
  color: #999;
  font-size: 28rpx;
}
</style>
