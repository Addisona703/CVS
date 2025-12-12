<template>
  <view class="product-detail-page">
    <view v-if="loading" class="loading">加载中...</view>
    <view v-else-if="product" class="content">
      <!-- 商品图片 -->
      <view class="product-image" @click="previewImage">
        <image 
          v-if="productImage" 
          :src="productImage" 
          mode="aspectFill"
          @error="handleImageError"
          @load="handleImageLoad"
        />
        <view v-else class="image-placeholder">
          <text class="placeholder-text">{{ imageError ? '图片加载失败' : '暂无图片' }}</text>
        </view>
      </view>

      <!-- 商品信息 -->
      <view class="product-info">
        <view class="product-name">{{ product.name }}</view>
        <view class="product-meta">
          <view class="product-points">
            <text class="points-value">{{ product.pointsRequired || product.points || 0 }}</text>
            <text class="points-label">积分</text>
          </view>
          <view class="product-stock">
            库存: {{ product.stock || 0 }}
          </view>
        </view>
      </view>

      <!-- 商品描述 -->
      <view class="product-desc">
        <view class="desc-title">商品详情</view>
        <view class="desc-content">{{ product.description || '暂无描述' }}</view>
      </view>

      <!-- 我的积分 -->
      <view class="my-points">
        <text class="label">我的积分：</text>
        <text class="value">{{ balance }}</text>
      </view>

      <!-- 兑换按钮 -->
      <view class="action-bar">
        <button
          class="redeem-btn"
          :disabled="!canRedeem"
          @click="handleRedeem"
        >
          {{ redeemBtnText }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getProductById, createRedemption } from '@/api/mall'
import { getCurrentUserPointsStats } from '@/api/points'

const product = ref(null)
const balance = ref(0)
const loading = ref(false)
const imageError = ref(false)

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

// 支持多种可能的图片字段名
const productImage = computed(() => {
  if (!product.value) return ''
  const img = product.value.imageUrl || product.value.image || product.value.productImageUrl || product.value.coverImage
  const fullUrl = getFullImageUrl(img)
  console.log('商品详情图片原始路径:', img)
  console.log('商品详情图片完整URL:', fullUrl)
  return fullUrl
})

const handleImageError = (e) => {
  console.error('商品详情图片加载失败:', productImage.value, e)
  imageError.value = true
}

const handleImageLoad = () => {
  console.log('商品详情图片加载成功:', productImage.value)
  imageError.value = false
}

// 图片预览
const previewImage = () => {
  if (!productImage.value || imageError.value) return
  
  uni.previewImage({
    urls: [productImage.value],
    current: productImage.value
  })
}

const redeeming = ref(false)

const productPoints = computed(() => {
  return product.value?.pointsRequired || product.value?.points || 0
})

const canRedeem = computed(() => {
  if (!product.value) return false
  if (product.value.stock <= 0) return false
  if (balance.value < productPoints.value) return false
  return true
})

const redeemBtnText = computed(() => {
  if (!product.value) return '加载中...'
  if (product.value.stock <= 0) return '已售罄'
  if (balance.value < productPoints.value) return '积分不足'
  if (redeeming.value) return '兑换中...'
  return '立即兑换'
})

const loadProduct = async () => {
  loading.value = true
  try {
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const options = currentPage.options || currentPage.$page?.options || {}
    const id = options.id
    
    console.log('商品详情页面参数:', options, 'ID:', id)
    
    if (!id) {
      throw new Error('商品ID不能为空')
    }

    const res = await getProductById(id)
    console.log('商品详情数据:', res)
    product.value = res
  } catch (error) {
    console.error('加载商品详情失败:', error)
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

const loadBalance = async () => {
  try {
    console.log('开始加载积分余额')
    const res = await getCurrentUserPointsStats()
    console.log('积分API响应:', res)
    console.log('积分余额:', res.balance)
    balance.value = res.balance || res.totalPoints || 0
    console.log('设置后的balance值:', balance.value)
  } catch (error) {
    console.error('加载积分余额失败:', error)
    console.error('错误详情:', error.message)
  }
}

const handleRedeem = () => {
  if (!canRedeem.value) return

  uni.showModal({
    title: '确认兑换',
    content: `确定要花费 ${productPoints.value} 积分兑换 ${product.value.name} 吗？`,
    success: async (res) => {
      if (res.confirm) {
        await doRedeem()
      }
    }
  })
}

const doRedeem = async () => {
  if (redeeming.value) return

  redeeming.value = true
  try {
    const res = await createRedemption({
      productId: product.value.id
    })

    uni.showToast({
      title: '兑换成功',
      icon: 'success'
    })

    // 跳转到兑换凭证页面
    setTimeout(() => {
      uni.redirectTo({
        url: `/pages/student-sub/voucher-detail/voucher-detail?id=${res.id}`
      })
    }, 1500)
  } catch (error) {
    uni.showToast({
      title: error.message || '兑换失败',
      icon: 'none'
    })
  } finally {
    redeeming.value = false
  }
}

onMounted(() => {
  loadProduct()
  loadBalance()
})
</script>

<style scoped>
.product-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 120rpx;
}

.loading {
  text-align: center;
  padding: 200rpx 0;
  color: #999;
  font-size: 28rpx;
}

.product-image {
  width: 100%;
  height: 600rpx;
  background: #fff;
}

.product-image image {
  width: 100%;
  height: 100%;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-text {
  color: #999;
  font-size: 32rpx;
}

.product-info {
  background: #fff;
  padding: 32rpx;
  margin-bottom: 16rpx;
}

.product-name {
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 24rpx;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-points {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.points-value {
  font-size: 48rpx;
  font-weight: 600;
  color: #ff4d4f;
}

.points-label {
  font-size: 28rpx;
  color: #999;
}

.product-stock {
  font-size: 28rpx;
  color: #999;
}

.product-desc {
  background: #fff;
  padding: 32rpx;
  margin-bottom: 16rpx;
}

.desc-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 16rpx;
}

.desc-content {
  font-size: 28rpx;
  color: #666;
  line-height: 1.8;
}

.my-points {
  background: #fff;
  padding: 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.my-points .label {
  font-size: 28rpx;
  color: #666;
}

.my-points .value {
  font-size: 32rpx;
  font-weight: 600;
  color: #0052d9;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx;
  background: #fff;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.08);
}

.redeem-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: #0052d9;
  color: #fff;
  border-radius: 44rpx;
  font-size: 32rpx;
  border: none;
}

.redeem-btn[disabled] {
  background: #d9d9d9;
  color: #999;
}
</style>
