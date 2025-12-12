<template>
  <view class="redemption-list-page">
    <!-- 标题栏 -->
    <view class="header">
      <text class="title">{{ pageTitle }}</text>
    </view>

    <!-- 列表 -->
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
        <view
          v-for="item in list"
          :key="item.id"
          class="redemption-item"
        >
          <view class="item-header">
            <view class="user-info">
              <text class="user-name">{{ item.userName }}</text>
              <text class="user-id">{{ item.userUsername }}</text>
            </view>
            <view :class="['status-badge', `status-${item.status}`]">
              {{ item.statusText }}
            </view>
          </view>

          <view class="item-body">
            <view class="product-info">
              <text class="product-name">{{ item.productName }}</text>
              <text class="points">{{ item.pointsSpent }} 积分</text>
            </view>
            
            <view class="info-row">
              <text class="label">凭证编号：</text>
              <text class="value">{{ item.voucherCode }}</text>
            </view>
            
            <view class="info-row">
              <text class="label">兑换时间：</text>
              <text class="value">{{ formatDateTime(item.createdAt) }}</text>
            </view>
            
            <view v-if="item.verifiedAt" class="info-row">
              <text class="label">核销时间：</text>
              <text class="value">{{ formatDateTime(item.verifiedAt) }}</text>
            </view>
            
            <view v-if="item.verifiedByName" class="info-row">
              <text class="label">核销人员：</text>
              <text class="value">{{ item.verifiedByName }}</text>
            </view>
          </view>

          <view v-if="item.status === 'PENDING'" class="item-footer">
            <button class="verify-btn" @click="handleVerify(item)">立即核销</button>
          </view>
        </view>

        <!-- 加载状态 -->
        <view v-if="loading" class="loading">加载中...</view>
        <view v-else-if="finished && list.length > 0" class="finished">没有更多了</view>
        <view v-else-if="isEmpty" class="empty">
          <text class="empty-icon">📦</text>
          <text class="empty-text">暂无记录</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { usePagination } from '@/composables/usePagination'
import { getRedemptionsByStatus, verifyRedemption } from '@/api/mall'
import { formatDateTime } from '@/utils/format'

const statusType = ref('')
const pageTitle = computed(() => {
  const titleMap = {
    'TODAY': '今日核销',
    'VERIFIED': '累计核销',
    'PENDING': '待核销'
  }
  return titleMap[statusType.value] || '兑换记录'
})

// 先不立即加载，等获取到 statusType 后再加载
const {
  list,
  loading,
  refreshing,
  finished,
  isEmpty,
  refresh,
  loadMore,
  loadData
} = usePagination((params) => getRedemptionsByStatus(params, statusType.value), {
  params: {},
  immediate: false  // 关闭立即加载
})

const handleVerify = (item) => {
  uni.showModal({
    title: '确认核销',
    content: `确定要核销"${item.productName}"吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await verifyRedemption({ voucherCode: item.voucherCode })
          uni.showToast({
            title: '核销成功',
            icon: 'success'
          })
          // 刷新列表
          refresh()
        } catch (error) {
          console.error('核销失败:', error)
          uni.showToast({
            title: error.message || '核销失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

// 使用 onLoad 生命周期获取页面参数
onLoad((options) => {
  if (options.statusType) {
    statusType.value = options.statusType
  }
  // 获取到参数后再加载数据
  loadData(true)
})
</script>

<style scoped>
.redemption-list-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
  overflow: hidden;
}

.header {
  background: #fff;
  padding: 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.title {
  font-size: 36rpx;
  font-weight: 700;
  color: #333;
}

.scroll-view {
  flex: 1;
  height: 0;
  -webkit-overflow-scrolling: touch;
}

.list-container {
  padding: 24rpx;
}

.redemption-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
  transform: translateZ(0);
  will-change: transform;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.user-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.user-id {
  font-size: 24rpx;
  color: #999;
}

.status-badge {
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
  white-space: nowrap;
}

.status-badge.status-PENDING {
  background: #fff7e6;
  color: #fa8c16;
}

.status-badge.status-VERIFIED {
  background: #f6ffed;
  color: #FFA726;
}

.status-badge.status-CANCELLED {
  background: #fff1f0;
  color: #ff4d4f;
}

.item-body {
  margin-bottom: 20rpx;
}

.product-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.product-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.points {
  font-size: 28rpx;
  font-weight: 700;
  color: #ed7b2f;
}

.info-row {
  display: flex;
  margin-bottom: 12rpx;
  font-size: 26rpx;
}

.label {
  color: #999;
  margin-right: 8rpx;
}

.value {
  flex: 1;
  color: #666;
  word-break: break-all;
}

.item-footer {
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
}

.verify-btn {
  width: 100%;
  height: 72rpx;
  line-height: 72rpx;
  background: #FF8C42;
  color: #fff;
  border-radius: 8rpx;
  font-size: 28rpx;
  border: none;
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
</style>
