<template>
  <view class="product-edit-page">
    <scroll-view class="form-container" scroll-y>
      <!-- 商品名称 -->
      <view class="form-item">
        <view class="form-label required">商品名称</view>
        <input v-model="form.name" class="form-input" placeholder="请输入商品名称" maxlength="50" />
      </view>

      <!-- 商品描述 -->
      <view class="form-item">
        <view class="form-label">商品描述</view>
        <textarea v-model="form.description" class="form-textarea" placeholder="请输入商品描述" maxlength="500" />
      </view>

      <!-- 所需积分 -->
      <view class="form-item">
        <view class="form-label required">所需积分</view>
        <input v-model.number="form.pointsRequired" class="form-input" type="number" placeholder="请输入所需积分" />
      </view>

      <!-- 库存数量 -->
      <view class="form-item">
        <view class="form-label required">库存数量</view>
        <input v-model.number="form.stock" class="form-input" type="number" placeholder="请输入库存数量" />
      </view>

      <!-- 商品分类 -->
      <view class="form-item">
        <view class="form-label required">商品分类</view>
        <picker mode="selector" :range="categories" range-key="name" :value="selectedCategoryIndex" @change="onCategoryChange">
          <view class="picker-input">
            {{ selectedCategory ? selectedCategory.name : '请选择分类' }}
          </view>
        </picker>
      </view>

      <!-- 商品图片 -->
      <view class="form-item">
        <view class="form-label">商品图片</view>
        <view class="image-upload" @click="handleUploadImage">
          <image v-if="form.imageUrl" class="preview-image" :src="form.imageUrl" mode="aspectFill" />
          <view v-else class="upload-placeholder">
            <text class="upload-icon">📷</text>
            <text class="upload-text">点击上传图片</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="action-bar">
      <button class="action-btn" @click="handleCancel">取消</button>
      <button class="action-btn primary" @click="handleSave">保存</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getProductById, createProduct, updateProduct } from '@/api/mall'
import { getCategoryList } from '@/api/mall'

const form = ref({
  name: '',
  description: '',
  pointsRequired: 0,
  stock: 0,
  categoryId: null,
  imageUrl: '',
  status: 1
})

const categories = ref([])
const selectedCategoryIndex = ref(0)
const productId = ref(null)
const isEdit = ref(false)

const selectedCategory = computed(() => categories.value[selectedCategoryIndex.value])

const getImageUrl = (url) => {
  if (!url) return ''
  
  // 如果已经是完整URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  
  // 如果是相对路径，拼接基础URL（去掉/api）
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://192.168.155.104:8000/api'
  const baseUrl = apiBaseUrl.replace('/api', '')
  
  // 确保路径以 / 开头
  const path = url.startsWith('/') ? url : `/${url}`
  return `${baseUrl}${path}`
}

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || currentPage.$page.options
  
  if (options.id) {
    productId.value = parseInt(options.id)
    isEdit.value = true
    loadProductDetail()
  }
  
  loadCategories()
})

const loadCategories = async () => {
  try {
    const result = await getCategoryList()
    categories.value = Array.isArray(result) ? result : (result.data || result.list || [])
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const loadProductDetail = async () => {
  try {
    const product = await getProductById(productId.value)
    form.value = {
      name: product.name,
      description: product.description,
      pointsRequired: product.pointsRequired,
      stock: product.stock,
      categoryId: product.categoryId,
      imageUrl: getImageUrl(product.imageUrl),
      status: product.status
    }
    
    const index = categories.value.findIndex(c => c.id === product.categoryId)
    if (index !== -1) {
      selectedCategoryIndex.value = index
    }
  } catch (error) {
    console.error('加载商品详情失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

const onCategoryChange = (e) => {
  selectedCategoryIndex.value = e.detail.value
  form.value.categoryId = categories.value[e.detail.value].id
}

const handleUploadImage = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      form.value.imageUrl = tempFilePath
      uni.showToast({ title: '图片上传功能待实现', icon: 'none' })
    }
  })
}

const handleCancel = () => {
  uni.navigateBack()
}

const handleSave = async () => {
  if (!form.value.name) {
    uni.showToast({ title: '请输入商品名称', icon: 'none' })
    return
  }
  
  if (!form.value.pointsRequired || form.value.pointsRequired <= 0) {
    uni.showToast({ title: '请输入有效的积分', icon: 'none' })
    return
  }
  
  if (!form.value.stock || form.value.stock < 0) {
    uni.showToast({ title: '请输入有效的库存', icon: 'none' })
    return
  }
  
  if (!form.value.categoryId) {
    uni.showToast({ title: '请选择商品分类', icon: 'none' })
    return
  }
  
  try {
    if (isEdit.value) {
      await updateProduct(productId.value, form.value)
      uni.showToast({ title: '更新成功', icon: 'success' })
      setTimeout(() => {
        uni.redirectTo({
          url: `/pages/admin/product-detail/product-detail?id=${productId.value}`
        })
      }, 1500)
    } else {
      const result = await createProduct(form.value)
      uni.showToast({ title: '创建成功', icon: 'success' })
      setTimeout(() => {
        uni.redirectTo({
          url: `/pages/admin/product-detail/product-detail?id=${result.id}`
        })
      }, 1500)
    }
  } catch (error) {
    console.error('保存失败:', error)
    uni.showToast({ title: '保存失败', icon: 'none' })
  }
}
</script>

<style scoped>
.product-edit-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.form-container {
  flex: 1;
  padding: 24rpx;
}

.form-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
}

.form-label {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 16rpx;
  font-weight: 600;
}

.form-label.required::before {
  content: '*';
  color: #EF4444;
  margin-right: 4rpx;
}

.form-input {
  width: 100%;
  height: 64rpx;
  padding: 0 20rpx;
  background-color: #f5f5f5;
  border-radius: 8rpx;
  font-size: 28rpx;
}

.form-textarea {
  width: 100%;
  min-height: 200rpx;
  padding: 20rpx;
  background-color: #f5f5f5;
  border-radius: 8rpx;
  font-size: 28rpx;
  line-height: 1.6;
}

.picker-input {
  height: 64rpx;
  line-height: 64rpx;
  padding: 0 20rpx;
  background-color: #f5f5f5;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #333;
}

.image-upload {
  width: 200rpx;
  height: 200rpx;
  border-radius: 16rpx;
  overflow: hidden;
  background-color: #f5f5f5;
}

.preview-image {
  width: 100%;
  height: 100%;
}

.upload-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}

.upload-icon {
  font-size: 56rpx;
  opacity: 0.5;
}

.upload-text {
  font-size: 24rpx;
  color: #999;
}

.action-bar {
  display: flex;
  gap: 20rpx;
  padding: 24rpx 32rpx;
  background: #fff;
  border-top: 1rpx solid #f0f0f0;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.action-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 44rpx;
  font-size: 28rpx;
  font-weight: 500;
}

.action-btn {
  background-color: #F3F4F6;
  color: #6B7280;
}

.action-btn.primary {
  background-color: #FF8C42;
  color: #fff;
}
</style>
