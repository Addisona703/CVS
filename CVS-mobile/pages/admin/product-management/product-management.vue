<template>
  <view class="product-management-container">
    <!-- 顶部操作栏 -->
    <view class="top-bar">
      <button class="add-btn" @click="handleAdd">+ 添加商品</button>
      <button class="filter-btn" @click="showFilterSheet = true">
        <text class="filter-icon">🔍</text>
        <text>筛选</text>
        <view v-if="hasActiveFilters" class="filter-badge"></view>
      </button>
    </view>

    <!-- 商品列表 -->
    <view class="product-list">
      <view
        class="product-item"
        v-for="product in productList"
        :key="product.id"
        @click="handleViewDetail(product)"
      >
        <image class="product-image" :src="product.imageUrl" mode="aspectFill" />
        <view class="product-info">
          <text class="product-name">{{ product.name }}</text>
          <view class="product-meta">
            <text class="product-points">{{ product.points }}积分</text>
            <text class="product-stock">库存: {{ product.stock }}</text>
          </view>
        </view>
        <view class="more-btn" @click.stop="handleShowActions(product)">
          <text class="more-icon">⋮</text>
        </view>
      </view>
      
      <empty-state v-if="!loading && productList.length === 0" message="暂无商品" />
      <loading-state v-if="loading" />
    </view>

    <!-- 操作菜单弹窗 -->
    <view v-if="showActionSheet" class="action-sheet-mask" @click="showActionSheet = false">
      <view class="action-sheet" @click.stop>
        <view class="action-sheet-header">
          <text class="action-sheet-title">{{ currentProduct?.name }}</text>
        </view>
        <view class="action-sheet-body">
          <view class="action-sheet-item" @click="handleEdit(currentProduct)">
            <text class="action-sheet-icon">✏️</text>
            <text class="action-sheet-text">编辑</text>
          </view>
          <view class="action-sheet-item" @click="handleToggleStatus(currentProduct)">
            <text class="action-sheet-icon">{{ currentProduct?.status === 1 ? '📦' : '📤' }}</text>
            <text class="action-sheet-text">{{ currentProduct?.status === 1 ? '下架' : '上架' }}</text>
          </view>
          <view class="action-sheet-item danger" @click="handleDelete(currentProduct)">
            <text class="action-sheet-icon">🗑️</text>
            <text class="action-sheet-text">删除</text>
          </view>
        </view>
        <view class="action-sheet-footer">
          <button class="action-sheet-cancel" @click="showActionSheet = false">取消</button>
        </view>
      </view>
    </view>

    <!-- 筛选底部弹窗 -->
    <view v-if="showFilterSheet" class="filter-sheet-mask" @click="showFilterSheet = false">
      <view class="filter-sheet" @click.stop>
        <view class="sheet-handle"></view>
        <view class="sheet-header">
          <text class="sheet-title">筛选</text>
          <text class="sheet-reset" @click="handleResetFilters">重置</text>
        </view>

        <view class="sheet-body">
          <!-- 分类筛选 -->
          <view class="filter-group">
            <text class="filter-group-title">商品分类</text>
            <view class="filter-options">
              <view
                class="filter-option"
                :class="{ active: selectedCategory === category.id }"
                v-for="category in categories"
                :key="category.id"
                @click="handleCategoryFilter(category.id)"
              >
                {{ category.name }}
              </view>
            </view>
          </view>

          <!-- 状态筛选 -->
          <view class="filter-group">
            <text class="filter-group-title">商品状态</text>
            <view class="filter-options">
              <view
                class="filter-option"
                :class="{ active: selectedStatus === status.value }"
                v-for="status in statusList"
                :key="status.value"
                @click="handleStatusFilter(status.value)"
              >
                {{ status.label }}
              </view>
            </view>
          </view>
        </view>

        <view class="sheet-footer">
          <button class="sheet-btn cancel" @click="showFilterSheet = false">取消</button>
          <button class="sheet-btn confirm" @click="handleConfirmFilter">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getProductList, deleteProduct, updateProduct } from '@/api/mall'
import { getCategoryList } from '@/api/mall'
import { usePagination } from '@/composables/usePagination'
import EmptyState from '@/components/common/empty-state/empty-state.vue'
import LoadingState from '@/components/common/loading-state/loading-state.vue'

const selectedCategory = ref(null)
const selectedStatus = ref('')
const showFilterSheet = ref(false)
const showActionSheet = ref(false)
const currentProduct = ref(null)

const categories = ref([
  { id: null, name: '全部' }
])

const statusList = ref([
  { label: '全部', value: '' },
  { label: '上架中', value: 1 },
  { label: '已下架', value: 0 }
])

const hasActiveFilters = computed(() => {
  return selectedCategory.value !== null || selectedStatus.value !== ''
})

const getImageUrl = (url) => {
  if (!url) return '/static/images/placeholder.png'
  
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

const {
  list: productList,
  loading,
  loadData,
  refresh
} = usePagination(async (requestParams) => {
  // 构建查询参数，注意status为0时也要传递
  const queryParams = {
    pageNum: requestParams.pageNum,
    pageSize: requestParams.pageSize
  }
  
  // 只有当categoryId不为null时才添加
  if (selectedCategory.value !== null && selectedCategory.value !== undefined) {
    queryParams.categoryId = selectedCategory.value
  }
  
  // status为0或1时都要传递，只有空字符串时不传
  if (selectedStatus.value !== '' && selectedStatus.value !== null && selectedStatus.value !== undefined) {
    queryParams.status = selectedStatus.value
  }
  
  console.log('商品管理查询参数:', queryParams)
  const result = await getProductList(queryParams)
  
  // 处理图片URL和字段映射
  if (result.records) {
    result.records = result.records.map(product => ({
      ...product,
      points: product.pointsRequired || product.points,
      imageUrl: getImageUrl(product.imageUrl)
    }))
  }
  
  return result
})

const loadCategories = async () => {
  try {
    const result = await getCategoryList()
    const categoryData = Array.isArray(result) ? result : (result.data || result.list || [])
    categories.value = [
      { id: null, name: '全部' },
      ...categoryData
    ]
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const handleCategoryFilter = (categoryId) => {
  selectedCategory.value = categoryId
}

const handleStatusFilter = (status) => {
  selectedStatus.value = status
}

const handleConfirmFilter = () => {
  showFilterSheet.value = false
  refresh()
}

const handleResetFilters = () => {
  selectedCategory.value = null
  selectedStatus.value = ''
  refresh()
  showFilterSheet.value = false
}

const handleAdd = () => {
  uni.navigateTo({
    url: '/pages/admin/product-edit/product-edit'
  })
}

const handleCategory = () => {
  uni.navigateTo({
    url: '/pages/admin/category-management/category-management'
  })
}

const handleViewDetail = (product) => {
  // 学工处点击商品进入详情页面
  uni.navigateTo({
    url: `/pages/admin/product-detail/product-detail?id=${product.id}`
  })
}

const handleEdit = (product) => {
  showActionSheet.value = false
  
  uni.navigateTo({
    url: `/pages/admin/product-edit/product-edit?id=${product.id}`
  })
}

const handleToggleStatus = async (product) => {
  showActionSheet.value = false
  
  // 后端返回的status是数字：0-下架，1-上架
  const currentStatus = product.status
  const newStatus = currentStatus === 1 ? 0 : 1
  const action = newStatus === 1 ? '上架' : '下架'
  
  uni.showModal({
    title: `确认${action}`,
    content: `确定要${action}商品"${product.name}"吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await updateProduct(product.id, { status: newStatus })
          uni.showToast({
            title: `${action}成功`,
            icon: 'success'
          })
          refresh()
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

const handleShowActions = (product) => {
  currentProduct.value = product
  showActionSheet.value = true
}

const handleDelete = async (product) => {
  showActionSheet.value = false
  
  uni.showModal({
    title: '确认删除',
    content: `确定要删除商品"${product.name}"吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteProduct(product.id)
          uni.showToast({
            title: '删除成功',
            icon: 'success'
          })
          refresh()
        } catch (error) {
          console.error('删除商品失败:', error)
          uni.showToast({
            title: '删除失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

// 下拉刷新
onPullDownRefresh(async () => {
  try {
    await refresh()
  } finally {
    uni.stopPullDownRefresh()
  }
})

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style scoped>
.product-management-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.top-bar {
  display: flex;
  gap: 20rpx;
  padding: 20rpx;
  background-color: #fff;
}

.add-btn,
.filter-btn {
  flex: 1;
  height: 64rpx;
  line-height: 64rpx;
  border-radius: 32rpx;
  font-size: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  position: relative;
}

.add-btn {
  background-color: #FF8C42;
  color: #fff;
}

.filter-btn {
  background-color: #f5f5f5;
  color: #333;
}

.filter-icon {
  font-size: 32rpx;
}

.filter-badge {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  width: 16rpx;
  height: 16rpx;
  background-color: #E34D59;
  border-radius: 50%;
  border: 2rpx solid #fff;
}

/* 筛选底部弹窗 */
.filter-sheet-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.filter-sheet {
  width: 100%;
  max-height: 80vh;
  background-color: #fff;
  border-radius: 32rpx 32rpx 0 0;
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.sheet-handle {
  width: 80rpx;
  height: 8rpx;
  background-color: #E5E7EB;
  border-radius: 4rpx;
  margin: 16rpx auto;
}

.sheet-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid #F3F4F6;
}

.sheet-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #111827;
}

.sheet-reset {
  font-size: 28rpx;
  color: #FF8C42;
}

.sheet-body {
  flex: 1;
  overflow-y: auto;
  padding: 32rpx;
}

.filter-group {
  margin-bottom: 40rpx;
}

.filter-group:last-child {
  margin-bottom: 0;
}

.filter-group-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #374151;
  margin-bottom: 20rpx;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.filter-option {
  padding: 16rpx 32rpx;
  background-color: #F3F4F6;
  border-radius: 32rpx;
  font-size: 28rpx;
  color: #6B7280;
  transition: all 0.3s ease;
}

.filter-option.active {
  background-color: #FF8C42;
  color: #fff;
}

.sheet-footer {
  display: flex;
  gap: 20rpx;
  padding: 24rpx 32rpx;
  border-top: 1rpx solid #F3F4F6;
  background-color: #fff;
}

.sheet-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 44rpx;
  font-size: 28rpx;
  font-weight: 500;
}

.sheet-btn.cancel {
  background-color: #F3F4F6;
  color: #6B7280;
}

.sheet-btn.confirm {
  background-color: #FF8C42;
  color: #fff;
}

.product-list {
  padding: 20rpx;
}

.product-item {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.product-item:active {
  transform: scale(0.98);
}

.product-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 16rpx;
  margin-right: 24rpx;
  flex-shrink: 0;
  background-color: #f5f5f5;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  min-width: 0;
}

.product-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #111827;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.product-points {
  font-size: 26rpx;
  color: #FA8C16;
  font-weight: 500;
}

.product-stock {
  font-size: 24rpx;
  color: #9CA3AF;
}

.more-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: #F3F4F6;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.more-btn:active {
  background-color: #E5E7EB;
}

.more-icon {
  font-size: 40rpx;
  color: #6B7280;
  font-weight: bold;
  line-height: 1;
}

/* 操作菜单弹窗 */
.action-sheet-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.action-sheet {
  width: 100%;
  background-color: #fff;
  border-radius: 32rpx 32rpx 0 0;
  animation: slideUp 0.3s ease-out;
}

.action-sheet-header {
  padding: 32rpx;
  border-bottom: 1rpx solid #F3F4F6;
}

.action-sheet-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #111827;
}

.action-sheet-body {
  padding: 16rpx 0;
}

.action-sheet-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 28rpx 32rpx;
  transition: all 0.3s ease;
}

.action-sheet-item:active {
  background-color: #F9FAFB;
}

.action-sheet-item.danger {
  color: #EF4444;
}

.action-sheet-icon {
  font-size: 36rpx;
}

.action-sheet-text {
  font-size: 30rpx;
  color: #374151;
}

.action-sheet-item.danger .action-sheet-text {
  color: #EF4444;
}

.action-sheet-footer {
  padding: 16rpx 32rpx 32rpx;
  border-top: 1rpx solid #F3F4F6;
}

.action-sheet-cancel {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background-color: #F3F4F6;
  color: #6B7280;
  border-radius: 44rpx;
  font-size: 28rpx;
  font-weight: 500;
}
</style>
