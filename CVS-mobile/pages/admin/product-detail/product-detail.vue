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
        <view class="product-status">
          <text class="status-label">状态：</text>
          <text :class="['status-value', product.status === 1 ? 'online' : 'offline']">
            {{ product.status === 1 ? '上架中' : '已下架' }}
          </text>
        </view>
      </view>

      <!-- 商品描述 -->
      <view class="product-desc">
        <view class="desc-title">商品详情</view>
        <view class="desc-content">{{ product.description || '暂无描述' }}</view>
      </view>

      <!-- 商品分类 -->
      <view class="product-category">
        <text class="label">商品分类：</text>
        <text class="value">{{ categoryName }}</text>
      </view>

      <!-- 底部操作栏 -->
      <view class="action-bar">
        <button class="action-btn secondary" @click="handleToggleStatus">
          {{ product.status === 1 ? '下架商品' : '上架商品' }}
        </button>
        <button class="action-btn primary" @click="handleEdit">
          编辑商品
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getProductById, updateProduct } from '@/api/mall'
import { getCategoryList } from '@/api/mall'

const product = ref(null)
const categories = ref([])
const loading = ref(false)
const imageError = ref(false)

const getFullImageUrl = (url) => {
  if (!url) return ''
  
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://192.168.155.104:8000/api'
  const baseUrl = apiBaseUrl.replace('/api', '')
  const path = url.startsWith('/') ? url : `/${url}`
  return `${baseUrl}${path}`
}

const productImage = computed(() => {
  if (!product.value) return ''
  const img = product.value.imageUrl || product.value.image || product.value.productImageUrl || product.value.coverImage
  return getFullImageUrl(img)
})

const categoryName = computed(() => {
  if (!product.value || !product.value.categoryId) return '未分类'
  const category = categories.value.find(c => c.id === product.value.categoryId)
  return category ? category.name : '未分类'
})

const handleImageError = (e) => {
  console.error('图片加载失败:', productImage.value, e)
  imageError.value = true
}

const handleImageLoad = () => {
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

const loadProduct = async () => {
  loading.value = true
  try {
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const options = currentPage.options || currentPage.$page?.options || {}
    const id = options.id
    
    if (!id) {
      throw new Error('商品ID不能为空')
    }

    const res = await getProductById(id)
    product.value = res
  } catch (error) {
    console.error('加载商品详情失败:', error)
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
    setTimeout(() => {
      uni.navigateBack()
    }, 2000)
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const result = await getCategoryList()
    categories.value = Array.isArray(result) ? result : (result.data || result.list || [])
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const handleEdit = () => {
  uni.navigateTo({
    url: `/pages/admin/product-edit/product-edit?id=${product.value.id}`
  })
}

const handleToggleStatus = () => {
  const currentStatus = product.value.status
  const newStatus = currentStatus === 1 ? 0 : 1
  const action = newStatus === 1 ? '上架' : '下架'
  
  uni.showModal({
    title: `确认${action}`,
    content: `确定要${action}商品"${product.value.name}"吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await updateProduct(product.value.id, { status: newStatus })
          uni.showToast({
            title: `${action}成功`,
            icon: 'success'
          })
          product.value.status = newStatus
        } catch (error) {
          console.error(`${action}失败:`, error)
          uni.showToast({
            title: `${action}失败`,
            icon: 'none'
          })
        }
      }
    }
  })
}

onMounted(() => {
  loadProduct()
  loadCategories()
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
  margin-bottom: 16rpx;
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

.product-status {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.status-label {
  font-size: 28rpx;
  color: #666;
}

.status-value {
  font-size: 28rpx;
  font-weight: 500;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
}

.status-value.online {
  color: #FFA726;
  background: #f6ffed;
}

.status-value.offline {
  color: #999;
  background: #f5f5f5;
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

.product-category {
  background: #fff;
  padding: 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-category .label {
  font-size: 28rpx;
  color: #666;
}

.product-category .value {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx;
  background: #fff;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.08);
  display: flex;
  gap: 20rpx;
}

.action-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 44rpx;
  font-size: 28rpx;
  font-weight: 500;
  border: none;
}

.action-btn.secondary {
  background: #F3F4F6;
  color: #6B7280;
}

.action-btn.primary {
  background: #FF8C42;
  color: #fff;
}
</style>
