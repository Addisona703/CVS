<template>
  <view class="my-redemptions-page">
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

    <!-- 兑换列表 -->
    <scroll-view
      class="scroll-view"
      scroll-y
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="refresh"
    >
      <view class="list-container">
        <view v-for="redemption in list" :key="redemption.id" class="redemption-item" @click="handleItemClick(redemption)">
          <view class="item-header">
            <view class="product-info">
              <image
                class="product-image"
                :src="getProductImage(redemption)"
                mode="aspectFill"
                @error="handleImageError"
              />
              <view class="product-details">
                <view class="product-name">{{ redemption.productName || '商品' }}</view>
                <view class="product-points">{{ redemption.pointsSpent || 0 }} 积分</view>
              </view>
            </view>
            <view class="status-badge" :class="getStatusClass(redemption.status)">
              {{ redemption.statusText || getStatusText(redemption.status) }}
            </view>
          </view>

          <view class="item-footer">
            <view class="redemption-time">
              兑换时间: {{ formatTime(redemption.createdAt) }}
            </view>
            <view v-if="redemption.status === 0 || redemption.status === '0'" class="view-voucher">
              查看凭证 →
            </view>
          </view>
        </view>

        <!-- 加载状态 -->
        <view v-if="loading" class="loading">加载中...</view>
        <view v-else-if="finished && list.length > 0" class="finished">没有更多了</view>
        <view v-else-if="isEmpty" class="empty">
          <ph-icon name="gift" :size="120" color="#ccc" />
          <text class="empty-text">暂无兑换记录</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getMyRedemptions } from '@/api/mall'
import { formatDateTime } from '@/utils/format'
import PhIcon from '@/components/common/ph-icon/ph-icon.vue'

const currentTab = ref('')

const tabs = [
  { label: '全部', value: '' },
  { label: '待核销', value: 0 },
  { label: '已核销', value: 1 },
  { label: '已过期', value: 2 }
]

// 使用自定义分页逻辑，因为后端不接受嵌套的 params 对象
const list = ref([])
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const page = ref(1)
const pageSize = 20

const isEmpty = computed(() => {
  return !loading.value && !refreshing.value && list.value.length === 0
})

const loadData = async (isRefresh = false) => {
  if (loading.value || (finished.value && !isRefresh)) return

  if (isRefresh) {
    refreshing.value = true
    page.value = 1
    finished.value = false
  } else {
    loading.value = true
  }

  try {
    const params = {
      pageNum: page.value,
      pageSize: pageSize,
      params: {}
    }
    
    // 只在有状态筛选时添加 status 参数（注意：0 也是有效值）
    if (currentTab.value !== '' && currentTab.value !== null && currentTab.value !== undefined) {
      params.params.status = currentTab.value
    }

    const response = await getMyRedemptions(params)
    console.log('兑换记录响应:', response)
    const records = response.records || response.list || []
    console.log('兑换记录数据:', records)

    if (isRefresh) {
      list.value = records
    } else {
      list.value.push(...records)
    }

    const total = response.total || 0
    if (list.value.length >= total) {
      finished.value = true
    } else {
      page.value++
    }
  } catch (error) {
    console.error('加载兑换记录失败:', error)
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const refresh = () => loadData(true)
const loadMore = () => {
  if (!loading.value && !finished.value) {
    loadData(false)
  }
}

const handleTabChange = (value) => {
  currentTab.value = value
  list.value = []
  page.value = 1
  finished.value = false
  loadData(true)
}

// 修复状态值的比较
const isStatusMatch = (redemptionStatus, filterStatus) => {
  if (filterStatus === '' || filterStatus === null || filterStatus === undefined) return true
  return redemptionStatus === filterStatus || redemptionStatus === String(filterStatus)
}

const getStatusClass = (status) => {
  // 后端返回的是数字状态：0-待核销, 1-已核销, 2-已过期, 3-已取消
  const statusNum = typeof status === 'string' ? parseInt(status) : status
  const classMap = {
    0: 'status-pending',
    1: 'status-verified',
    2: 'status-expired',
    3: 'status-expired'
  }
  return classMap[statusNum] || ''
}

const getStatusText = (status) => {
  // 后端返回的是数字状态：0-待核销, 1-已核销, 2-已过期, 3-已取消
  const statusNum = typeof status === 'string' ? parseInt(status) : status
  const textMap = {
    0: '待核销',
    1: '已核销',
    2: '已过期',
    3: '已取消'
  }
  return textMap[statusNum] || '未知'
}

const formatTime = (time) => {
  if (!time) return '--'
  // 后端返回的格式是 "2025-10-24 17:54"，已经是格式化好的
  return time
}

// 拼接完整图片URL
const getFullImageUrl = (url) => {
  if (!url) return ''
  
  // 如果已经是完整URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  
  // 如果是相对路径，拼接基础URL
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://192.168.155.104:8000/api'
  const baseUrl = apiBaseUrl.replace('/api', '')
  
  // 确保路径以 / 开头
  const path = url.startsWith('/') ? url : `/${url}`
  return `${baseUrl}${path}`
}

const getProductImage = (redemption) => {
  const img = redemption.productImageUrl || redemption.imageUrl || redemption.image || redemption.productImage
  const fullUrl = getFullImageUrl(img)
  console.log('兑换记录图片原始路径:', img)
  console.log('兑换记录图片完整URL:', fullUrl)
  console.log('商品:', redemption.productName)
  return fullUrl || '/static/placeholder.png'
}

const handleImageError = (e) => {
  console.error('兑换记录图片加载失败:', e)
  // 图片加载失败时，image标签会自动显示alt或者空白
}

const handleItemClick = (redemption) => {
  if (!redemption || !redemption.id) {
    console.error('兑换记录数据无效:', redemption)
    uni.showToast({
      title: '兑换记录信息错误',
      icon: 'none'
    })
    return
  }
  
  console.log('跳转到兑换凭证，ID:', redemption.id)
  uni.navigateTo({
    url: `/pages/student-sub/voucher-detail/voucher-detail?id=${redemption.id}`
  })
}

onMounted(() => {
  loadData(true)
})
</script>

<style scoped>
.my-redemptions-page {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.tabs {
  display: flex;
  background: #fff;
  padding: 0 24rpx;
  flex-shrink: 0;
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
  height: 0;
}

.list-container {
  padding: 24rpx;
}

.redemption-item {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.product-info {
  display: flex;
  flex: 1;
  gap: 16rpx;
}

.product-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  background: #f5f5f5;
}

.product-details {
  flex: 1;
}

.product-name {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 8rpx;
}

.product-points {
  font-size: 24rpx;
  color: #ff4d4f;
}

.status-badge {
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
  white-space: nowrap;
  margin-left: 16rpx;
}

.status-pending {
  background: #fff7e6;
  color: #fa8c16;
}

.status-verified {
  background: #f6ffed;
  color: #52c41a;
}

.status-expired {
  background: #f5f5f5;
  color: #999;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
}

.redemption-time {
  font-size: 24rpx;
  color: #999;
}

.view-voucher {
  font-size: 24rpx;
  color: #0052d9;
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

.empty :deep(.ph-icon) {
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}
</style>
