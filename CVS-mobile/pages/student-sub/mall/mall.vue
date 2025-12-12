<template>
  <view class="mall-page">
    <!-- 顶部导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @click="handleBack">
          <text class="back-icon">←</text>
        </view>
        <view class="navbar-title">积分商城</view>
        <view class="navbar-right"></view>
      </view>
    </view>

    <!-- 积分余额和筛选按钮 -->
    <view class="balance-bar">
      <view class="balance-info">
        <text class="balance-label">我的积分</text>
        <text class="balance-value">{{ balance }}</text>
      </view>
      <view class="header-actions">
        <view class="filter-btn" @click="showFilterSheet = true">
          <ph-icon name="funnel" :size="28" color="#fff" />
          <text>筛选</text>
        </view>
        <view class="my-redemptions" @click="navigateTo('/pages/student-sub/my-redemptions/my-redemptions')">
          我的兑换 →
        </view>
      </view>
    </view>

    <!-- 商品列表 -->
    <scroll-view class="scroll-view" scroll-y @scrolltolower="loadMore" :refresher-enabled="true"
      :refresher-triggered="refreshing" @refresherrefresh="refresh">
      <view class="product-grid">
        <product-card v-for="product in list" :key="product.id" :product="product"
          @click="handleProductClick(product)" />
      </view>

      <!-- 加载状态 -->
      <view v-if="loading" class="loading">加载中...</view>
      <view v-else-if="finished && list.length > 0" class="finished">没有更多了</view>
      <view v-else-if="isEmpty" class="empty">
        <ph-icon name="shopping-cart" :size="120" color="#ccc" />
        <text class="empty-text">暂无商品</text>
      </view>
    </scroll-view>

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
                :class="{ active: currentCategory === category.id }"
                v-for="category in categories"
                :key="category.id"
                @click="currentCategory = category.id"
              >
                {{ category.name }}
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
import { ref, computed, onMounted, watch } from 'vue'
import { usePagination } from '@/composables/usePagination'
import { getProductList, getCategoryList } from '@/api/mall'
import { getCurrentUserPointsStats } from '@/api/points'
import ProductCard from '@/components/business/product-card/product-card.vue'
import PhIcon from '@/components/common/ph-icon/ph-icon.vue'

const balance = ref(0)
const statusBarHeight = ref(0)
const categories = ref([{ id: '', name: '全部' }])
const currentCategory = ref('')
const showFilterSheet = ref(false)



const {
  list,
  loading,
  refreshing,
  finished,
  isEmpty,
  refresh,
  loadMore,
  updateParams
} = usePagination(
  async (requestParams) => {
    // 将 params 中的参数提取到顶层
    const apiParams = {
      pageNum: requestParams.pageNum,
      pageSize: requestParams.pageSize,
      ...requestParams.params  // 展开 params 对象
    }
    console.log('商城API请求参数:', apiParams)
    const result = await getProductList(apiParams)
    console.log('商城API返回结果:', result)
    return result
  },
  {
    params: {}
  }
)

// 监听list变化，打印商品数据
watch(list, (newList) => {
  if (newList && newList.length > 0) {
    console.log('商城商品列表更新，数量:', newList.length)
    console.log('第一个商品数据示例:', newList[0])
    console.log('第一个商品的所有字段:', Object.keys(newList[0]))
  }
}, { immediate: true })

const loadBalance = async () => {
  try {
    console.log('商城页面：开始加载积分余额')
    const res = await getCurrentUserPointsStats()
    console.log('商城页面：积分API响应:', res)
    console.log('商城页面：积分余额:', res.balance)
    balance.value = res.balance || res.totalPoints || 0
    console.log('商城页面：设置后的balance值:', balance.value)
  } catch (error) {
    console.error('商城页面：加载积分余额失败:', error)
    console.error('商城页面：错误详情:', error.message)
  }
}

const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    categories.value = [{ id: '', name: '全部' }, ...(res || [])]
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const handleCategoryChange = (categoryId) => {
  currentCategory.value = categoryId
  updateParams({
    categoryId: categoryId || undefined
  })
}

const handleConfirmFilter = () => {
  console.log('确认筛选，当前分类ID:', currentCategory.value)
  showFilterSheet.value = false
  // 确保 categoryId 是数字类型，空字符串转为 undefined
  const categoryId = currentCategory.value ? Number(currentCategory.value) : undefined
  console.log('转换后的分类ID:', categoryId, '类型:', typeof categoryId)
  updateParams({
    categoryId: categoryId
  })
}

const handleResetFilters = () => {
  currentCategory.value = ''
  showFilterSheet.value = false
  updateParams({
    categoryId: undefined
  })
}

const handleProductClick = (product) => {
  if (!product || !product.id) {
    console.error('商品数据无效:', product)
    uni.showToast({
      title: '商品信息错误',
      icon: 'none'
    })
    return
  }
  
  console.log('跳转到商品详情，ID:', product.id)
  uni.navigateTo({
    url: `/pages/student-sub/product-detail/product-detail?id=${product.id}`
  })
}

const navigateTo = (url) => {
  uni.navigateTo({ url })
}

const handleBack = () => {
  uni.navigateBack()
}

onMounted(() => {
  // 获取状态栏高度
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
  
  loadBalance()
  loadCategories()
})
</script>

<style scoped>
.mall-page {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

/* 自定义导航栏 */
.custom-navbar {
  background: #fff;
  flex-shrink: 0;
}

.navbar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 32rpx;
}

.navbar-left {
  width: 80rpx;
  display: flex;
  align-items: center;
}

.back-icon {
  font-size: 48rpx;
  color: #333;
  font-weight: 300;
}

.navbar-title {
  flex: 1;
  text-align: center;
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.navbar-right {
  width: 80rpx;
}

.balance-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  background: linear-gradient(135deg, #0052d9 0%, #0041a8 100%);
  color: #fff;
  flex-shrink: 0;
}

.balance-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.balance-label {
  font-size: 24rpx;
  opacity: 0.9;
}

.balance-value {
  font-size: 48rpx;
  font-weight: 600;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.filter-btn {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 32rpx;
  font-size: 26rpx;
  backdrop-filter: blur(10rpx);
}

.filter-btn :deep(.ph-icon) {
  flex-shrink: 0;
}

.my-redemptions {
  font-size: 26rpx;
  opacity: 0.9;
  white-space: nowrap;
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
  color: #0052D9;
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
  background-color: #0052D9;
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
  background-color: #0052D9;
  color: #fff;
}

.scroll-view {
  flex: 1;
  height: 0;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
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
