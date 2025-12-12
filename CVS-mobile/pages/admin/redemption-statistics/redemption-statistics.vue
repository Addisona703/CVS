<template>
  <view class="redemption-statistics-container">
    <!-- 统计概览 -->
    <view class="stats-section">
      <view class="stat-card" v-for="stat in stats" :key="stat.key">
        <text class="stat-value">{{ stat.value }}</text>
        <text class="stat-label">{{ stat.label }}</text>
      </view>
    </view>

    <!-- 兑换趋势图表 -->
    <view class="chart-section">
      <view class="section-header">
        <text class="section-title">兑换趋势</text>
      </view>
      <chart-wrapper
        v-if="trendChartData"
        :option="trendChartData"
        height="300px"
      />
    </view>

    <!-- 热门商品 -->
    <view class="popular-section">
      <view class="section-header">
        <text class="section-title">热门商品</text>
      </view>
      <chart-wrapper
        v-if="popularChartData"
        :option="popularChartData"
        height="300px"
      />
    </view>

    <!-- 库存预警 -->
    <view class="warning-section">
      <view class="section-header">
        <text class="section-title">库存预警</text>
      </view>
      <view class="warning-list">
        <view
          class="warning-item"
          v-for="product in warningList"
          :key="product.id"
        >
          <view class="product-info">
            <text class="product-name">{{ product.name }}</text>
            <text class="product-stock">剩余库存: {{ product.stock }}</text>
          </view>
          <view class="warning-badge">⚠️</view>
        </view>
        
        <empty-state v-if="warningList.length === 0" message="暂无库存预警" />
      </view>
    </view>

    <!-- 导出报表按钮 -->
    <view class="export-section">
      <button class="export-btn" @click="handleExport">导出报表</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getRedemptionStatistics } from '@/api/statistics'
import ChartWrapper from '@/components/business/chart-wrapper/chart-wrapper.vue'
import EmptyState from '@/components/common/empty-state/empty-state.vue'

const stats = ref([
  { key: 'total', label: '总兑换次数', value: 0 },
  { key: 'points', label: '总消耗积分', value: 0 },
  { key: 'today', label: '今日兑换', value: 0 },
  { key: 'pending', label: '待核销', value: 0 }
])

const trendChartData = ref(null)
const popularChartData = ref(null)
const warningList = ref([])

const loadData = async () => {
  try {
    const data = await getRedemptionStatistics()
    
    // 更新统计数据
    stats.value[0].value = data.totalRedemptions || 0
    stats.value[1].value = data.totalPoints || 0
    stats.value[2].value = data.todayRedemptions || 0
    stats.value[3].value = data.pendingRedemptions || 0

    // 生成兑换趋势图表
    trendChartData.value = {
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: data.trendData?.dates || []
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '兑换次数',
          type: 'line',
          data: data.trendData?.counts || [],
          smooth: true,
          areaStyle: {
            color: 'rgba(0, 82, 217, 0.1)'
          }
        }
      ]
    }

    // 生成热门商品图表
    popularChartData.value = {
      tooltip: {
        trigger: 'item'
      },
      series: [
        {
          name: '兑换次数',
          type: 'pie',
          radius: '50%',
          data: data.popularProducts?.map(p => ({
            name: p.name,
            value: p.count
          })) || []
        }
      ]
    }

    // 库存预警列表
    warningList.value = data.lowStockProducts || []
  } catch (error) {
    console.error('加载数据失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

const handleExport = () => {
  uni.showToast({
    title: '导出功能开发中',
    icon: 'none'
  })
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
.redemption-statistics-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.stat-card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.stat-value {
  font-size: 48rpx;
  font-weight: bold;
  color: #FF8C42;
  margin-bottom: 12rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
}

.chart-section,
.popular-section,
.warning-section {
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

.warning-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.warning-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx;
  background-color: #FFF3E0;
  border-radius: 12rpx;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.product-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.product-stock {
  font-size: 24rpx;
  color: #E37318;
}

.warning-badge {
  font-size: 32rpx;
}

.export-section {
  padding: 20rpx 0;
}

.export-btn {
  width: 100%;
  height: 80rpx;
  line-height: 80rpx;
  background-color: #FF8C42;
  color: #fff;
  border-radius: 16rpx;
  font-size: 28rpx;
}
</style>
