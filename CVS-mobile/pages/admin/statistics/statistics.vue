<template>
  <view class="statistics-container">


    <!-- 商品兑换排行 -->
    <view class="chart-section" v-if="productRanking.length > 0">
      <view class="section-header">
        <text class="section-title">商品兑换排行</text>
      </view>
      <view class="product-ranking-list">
        <view
          class="product-ranking-item"
          v-for="(product, index) in productRanking"
          :key="product.productId"
        >
          <view class="ranking-badge" :class="`rank-${index + 1}`">
            {{ index + 1 }}
          </view>
          <view class="product-info">
            <text class="product-name">{{ product.productName }}</text>
            <text class="product-count">兑换 {{ product.redemptionCount }} 次</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 库存预警 -->
    <view class="chart-section" v-if="lowStockProducts.length > 0">
      <view class="section-header">
        <text class="section-title">库存预警</text>
      </view>
      <view class="low-stock-list">
        <view
          class="low-stock-item"
          v-for="product in lowStockProducts"
          :key="product.id"
        >
          <view class="product-info">
            <text class="product-name">{{ product.name }}</text>
            <text class="stock-warning">剩余 {{ product.stock }} 件</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 服务时长排行榜 -->
    <view class="ranking-section">
      <view class="section-header">
        <text class="section-title">积分排行榜</text>
      </view>
      <view class="ranking-list">
        <view
          class="ranking-item"
          v-for="(user, index) in rankingList"
          :key="user.id"
        >
          <view class="ranking-badge" :class="`rank-${index + 1}`">
            {{ index + 1 }}
          </view>
          <view class="user-info">
            <text class="user-name">{{ user.name }}</text>
            <text class="user-points">{{ user.points }}分</text>
          </view>
        </view>
        
        <empty-state v-if="rankingList.length === 0" message="暂无数据" />
      </view>
    </view>

    <!-- Custom Tab Bar -->
    <custom-tabbar :current="0" role="admin" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getAdminDashboardStats, getProductRanking, getLowStockProducts } from '@/api/statistics'
import { getPointsRankingPage } from '@/api/points'
import EmptyState from '@/components/common/empty-state/empty-state.vue'
import CustomTabbar from '@/components/common/custom-tabbar/custom-tabbar.vue'

const rankingList = ref([])
const productRanking = ref([])
const lowStockProducts = ref([])

const loadData = async () => {
  try {
    // 加载积分排行榜
    try {
      const rankingPage = await getPointsRankingPage({ pageNum: 1, pageSize: 10 })
      console.log('积分排行榜:', rankingPage)
      rankingList.value = (rankingPage.records || rankingPage.list || []).map(item => ({
        id: item.userId,
        name: item.userName || item.username || item.realName || '未知用户',
        points: item.totalPoints || item.points || 0
      }))
    } catch (error) {
      console.error('加载积分排行榜失败:', error)
    }

    // 加载商品兑换排行
    try {
      const ranking = await getProductRanking(10)
      console.log('商品兑换排行:', ranking)
      productRanking.value = ranking || []
    } catch (error) {
      console.error('加载商品兑换排行失败:', error)
    }

    // 加载库存预警
    try {
      const lowStock = await getLowStockProducts()
      console.log('库存预警:', lowStock)
      lowStockProducts.value = lowStock || []
    } catch (error) {
      console.error('加载库存预警失败:', error)
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

// 下拉刷新
onPullDownRefresh(async () => {
  try {
    await loadData()
  } finally {
    uni.stopPullDownRefresh()
  }
})

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.statistics-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;
  padding-bottom: 120rpx;
}

.chart-section,
.ranking-section {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.section-header {
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background-color: #f5f5f5;
  border-radius: 12rpx;
}

.ranking-badge {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: bold;
  color: #fff;
  margin-right: 20rpx;
  background-color: #999;
}

.ranking-badge.rank-1 {
  background-color: #FFD700;
}

.ranking-badge.rank-2 {
  background-color: #C0C0C0;
}

.ranking-badge.rank-3 {
  background-color: #CD7F32;
}

.user-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-name {
  font-size: 28rpx;
  color: #333;
}

.user-points {
  font-size: 26rpx;
  color: #e6a23c;
  font-weight: bold;
}

.product-ranking-list,
.low-stock-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.product-ranking-item,
.low-stock-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background-color: #f5f5f5;
  border-radius: 12rpx;
}

.product-name {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 4rpx;
}

.product-count {
  font-size: 24rpx;
  color: #666;
}

.stock-warning {
  font-size: 24rpx;
  color: #ff4d4f;
  font-weight: bold;
}
</style>
