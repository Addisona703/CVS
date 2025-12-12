<template>
  <view class="voucher-detail-page">
    <view v-if="loading" class="loading">加载中...</view>
    <view v-else-if="redemption" class="content">
      <!-- 状态提示 -->
      <view class="status-banner" :class="getStatusClass(redemption.status)">
        <ph-icon 
          :name="getStatusIcon(redemption.status)" 
          :size="120" 
          :color="getStatusColor(redemption.status)"
          :weight="redemption.status === 1 ? 'fill' : 'regular'"
        />
        <view class="status-text">{{ getStatusText(redemption.status) }}</view>
      </view>

      <!-- 二维码 -->
      <view v-if="redemption.status === 0 || redemption.status === '0'" class="qrcode-section">
        <view class="qrcode-title">兑换凭证</view>
        <qr-display :data="qrCodeData" :size="200" />
        <view class="qrcode-tip">请向工作人员出示此二维码进行核销</view>
      </view>

      <!-- 商品信息 -->
      <view class="product-section">
        <view class="section-title">商品信息</view>
        <view class="product-info">
          <image class="product-image" :src="productImage"
            mode="aspectFill" @error="handleImageError" />
          <view class="product-details">
            <view class="product-name">{{ redemption.productName }}</view>
            <view class="product-points">{{ redemption.pointsSpent }} 积分</view>
          </view>
        </view>
      </view>

      <!-- 兑换信息 -->
      <view class="info-section">
        <view class="section-title">兑换信息</view>
        <view class="info-list">
          <view class="info-item">
            <text class="label">兑换单号</text>
            <text class="value">{{ redemption.id }}</text>
          </view>
          <view class="info-item">
            <text class="label">凭证编号</text>
            <text class="value">{{ redemption.voucherCode }}</text>
          </view>
          <view class="info-item">
            <text class="label">兑换时间</text>
            <text class="value">{{ formatTime(redemption.createdAt) }}</text>
          </view>
          <view v-if="redemption.verifiedAt" class="info-item">
            <text class="label">核销时间</text>
            <text class="value">{{ formatTime(redemption.verifiedAt) }}</text>
          </view>
          <view v-if="redemption.verifiedByName" class="info-item">
            <text class="label">核销人员</text>
            <text class="value">{{ redemption.verifiedByName }}</text>
          </view>
        </view>
      </view>

      <!-- 温馨提示 -->
      <view v-if="redemption.status === 0 || redemption.status === '0'" class="tips-section">
        <view class="section-title">温馨提示</view>
        <view class="tips-list">
          <view class="tip-item">• 请在有效期内使用兑换凭证</view>
          <view class="tip-item">• 每个凭证仅可使用一次</view>
          <view class="tip-item">• 核销后积分不可退回</view>
          <view class="tip-item">• 如有问题请联系学工处</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getRedemptionById } from '@/api/mall'
import { formatDateTime } from '@/utils/format'
import QrDisplay from '@/components/business/qr-display/qr-display.vue'
import PhIcon from '@/components/common/ph-icon/ph-icon.vue'

const redemption = ref(null)
const loading = ref(false)

const qrCodeData = computed(() => {
  if (!redemption.value) return ''
  // 使用voucherCode作为二维码内容（token）
  const token = redemption.value.voucherCode || redemption.value.id.toString()
  console.log('生成兑换二维码token:', token)
  return token
})

// 拼接完整图片URL
const getFullImageUrl = (url) => {
  if (!url) return '/static/placeholder.png'
  
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

// 商品图片URL
const productImage = computed(() => {
  if (!redemption.value) return '/static/placeholder.png'
  const img = redemption.value.productImageUrl
  const fullUrl = getFullImageUrl(img)
  console.log('兑换凭证商品图片原始路径:', img)
  console.log('兑换凭证商品图片完整URL:', fullUrl)
  return fullUrl
})

const handleImageError = (e) => {
  console.error('商品图片加载失败:', productImage.value, e)
}

const loadRedemption = async () => {
  loading.value = true
  try {
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const options = currentPage.options || currentPage.$page?.options || {}
    const id = options.id
    
    console.log('兑换凭证页面参数:', options, 'ID:', id)
    
    if (!id) {
      throw new Error('兑换记录ID不能为空')
    }

    const res = await getRedemptionById(id)
    console.log('兑换详情数据:', res)
    redemption.value = res
  } catch (error) {
    console.error('加载兑换详情失败:', error)
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none',
      duration: 2000
    })
    // 延迟返回上一页
    setTimeout(() => {
      uni.navigateBack()
    }, 2000)
  } finally {
    loading.value = false
  }
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

const getStatusIcon = (status) => {
  const statusNum = typeof status === 'string' ? parseInt(status) : status
  const iconMap = {
    0: 'clock',
    1: 'check-circle',
    2: 'x-circle',
    3: 'x-circle'
  }
  return iconMap[statusNum] || 'question'
}

const getStatusColor = (status) => {
  const statusNum = typeof status === 'string' ? parseInt(status) : status
  const colorMap = {
    0: '#fa8c16',
    1: '#52c41a',
    2: '#999',
    3: '#999'
  }
  return colorMap[statusNum] || '#999'
}

const getStatusText = (status) => {
  const statusNum = typeof status === 'string' ? parseInt(status) : status
  const textMap = {
    0: '待核销',
    1: '已核销',
    2: '已过期',
    3: '已取消'
  }
  return textMap[statusNum] || '未知状态'
}

const formatTime = (time) => {
  return formatDateTime(time, 'YYYY-MM-DD HH:mm')
}

onMounted(() => {
  loadRedemption()
})
</script>

<style scoped>
.voucher-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 24rpx;
}

.loading {
  text-align: center;
  padding: 200rpx 0;
  color: #999;
  font-size: 28rpx;
}

.status-banner {
  background: #fff;
  border-radius: 12rpx;
  padding: 48rpx 32rpx;
  text-align: center;
  margin-bottom: 24rpx;
}

.status-banner.status-pending {
  background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
}

.status-banner.status-verified {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
}

.status-banner.status-expired {
  background: linear-gradient(135deg, #f5f5f5 0%, #e0e0e0 100%);
}

.status-banner :deep(.ph-icon) {
  margin-bottom: 16rpx;
}

.status-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.qrcode-section {
  background: #fff;
  border-radius: 12rpx;
  padding: 48rpx 32rpx;
  text-align: center;
  margin-bottom: 24rpx;
}

.qrcode-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 32rpx;
}

.qrcode-tip {
  font-size: 24rpx;
  color: #999;
  margin-top: 32rpx;
}

.product-section,
.info-section,
.tips-section {
  background: #fff;
  border-radius: 12rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 24rpx;
}

.product-info {
  display: flex;
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

.info-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.info-item {
  display: flex;
  justify-content: space-between;
  font-size: 28rpx;
}

.info-item .label {
  color: #999;
}

.info-item .value {
  color: #333;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.tip-item {
  font-size: 26rpx;
  color: #666;
  line-height: 1.8;
}
</style>
