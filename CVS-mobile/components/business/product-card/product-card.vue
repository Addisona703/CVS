<template>
  <view class="product-card" @click="handleClick">
    <image 
      v-if="productImage" 
      class="product-image" 
      :src="productImage" 
      mode="aspectFill"
      @error="handleImageError"
      @load="handleImageLoad"
    />
    <view v-else class="product-image placeholder">
      <text class="placeholder-text">{{ imageError ? '图片加载失败' : '暂无图片' }}</text>
    </view>
    
    <view class="product-info">
      <view class="product-name">{{ product.name }}</view>
      
      <view class="product-footer">
        <view class="points">
          <text class="points-value">{{ product.pointsRequired || product.points || 0 }}</text>
          <text class="points-label">积分</text>
        </view>
        
        <view class="stock" :class="{ 'low-stock': isLowStock }">
          库存: {{ product.stock }}
        </view>
      </view>
    </view>
    
    <view v-if="product.stock === 0" class="sold-out-mask">
      <text class="sold-out-text">已售罄</text>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click'])

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
  const img = props.product.imageUrl || props.product.image || props.product.productImageUrl || props.product.coverImage
  const fullUrl = getFullImageUrl(img)
  console.log('商品图片原始路径:', img)
  console.log('商品图片完整URL:', fullUrl)
  return fullUrl
})

const isLowStock = computed(() => {
  return props.product.stock > 0 && props.product.stock <= 10
})

const handleImageError = (e) => {
  console.error('图片加载失败:', productImage.value, e)
  imageError.value = true
}

const handleImageLoad = () => {
  console.log('图片加载成功:', productImage.value)
  imageError.value = false
}

const handleClick = () => {
  if (props.product.stock > 0) {
    emit('click', props.product)
  }
}
</script>

<style scoped>
.product-card {
  position: relative;
  background: #fff;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
}

.product-image {
  width: 100%;
  height: 320rpx;
  display: block;
}

.product-image.placeholder {
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-text {
  color: #999;
  font-size: 28rpx;
}

.product-info {
  padding: 20rpx;
}

.product-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  margin-bottom: 16rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.points {
  display: flex;
  align-items: baseline;
}

.points-value {
  font-size: 36rpx;
  font-weight: 600;
  color: #fa8c16;
}

.points-label {
  font-size: 24rpx;
  color: #fa8c16;
  margin-left: 4rpx;
}

.stock {
  font-size: 24rpx;
  color: #999;
}

.stock.low-stock {
  color: #ff4d4f;
}

.sold-out-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.sold-out-text {
  font-size: 40rpx;
  font-weight: 600;
  color: #fff;
}
</style>
