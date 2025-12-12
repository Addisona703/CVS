<template>
  <view class="home-page">
    <!-- Header -->
    <view class="header">
      <view class="location-selector" @click="selectLocation">
        <text class="location-icon">📍</text>
        <text class="location-text">{{ currentLocation }}</text>
        <text class="dropdown-icon">▼</text>
      </view>
      <view class="notification-btn" @click="navigateTo('/pages/common/notifications/notifications')">
        <text class="notification-icon">🔔</text>
        <view v-if="hasUnread" class="notification-badge"></view>
      </view>
    </view>

    <!-- Search Bar -->
    <view class="search-bar" @click="navigateTo('/pages/student/search/search')">
      <text class="search-icon">🔍</text>
      <text class="search-placeholder">搜索志愿活动、组织...</text>
    </view>

    <!-- Category Grid (2x4) -->
    <view class="category-section">
      <view class="category-grid">
        <view v-for="category in categories" :key="category.id" class="category-item"
          @click="handleCategoryClick(category)">
          <view class="category-icon-wrapper">
            <text class="category-icon">{{ category.icon }}</text>
          </view>
          <text class="category-name">{{ category.name }}</text>
        </view>
      </view>
    </view>

    <!-- Featured Banner Carousel -->
    <view class="banner-section">
      <swiper class="banner-swiper" :indicator-dots="true" :autoplay="true" :interval="5000" :circular="true"
        indicator-color="rgba(255, 255, 255, 0.5)" indicator-active-color="#FFFFFF">
        <swiper-item v-for="banner in banners" :key="banner.id">
          <view class="banner-item" @click="handleBannerClick(banner)">
            <image :src="banner.image" class="banner-image" mode="aspectFill" />
            <view class="banner-overlay">
              <text class="banner-tag">{{ banner.tag }}</text>
              <text class="banner-title">{{ banner.title }}</text>
              <text class="banner-subtitle">{{ banner.subtitle }}</text>
            </view>
          </view>
        </swiper-item>
      </swiper>
    </view>

    <!-- Horizontal Scrolling Cards -->
    <view class="quick-access-section">
      <view class="section-header">
        <text class="section-title">快速入口</text>
      </view>
      <scroll-view class="quick-scroll" scroll-x show-scrollbar="false">
        <view class="quick-list">
          <view v-for="item in quickAccessItems" :key="item.id" class="quick-card"
            :style="{ background: item.gradient }" @click="handleQuickAccessClick(item)">
            <text class="quick-icon">{{ item.icon }}</text>
            <text class="quick-title">{{ item.title }}</text>
            <text class="quick-subtitle">{{ item.subtitle }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- Nearby Opportunities -->
    <view class="opportunities-section">
      <view class="section-header">
        <text class="section-title">附近的机会</text>
        <text class="section-more" @click="navigateTo('/pages/student/activities/activities')">
          查看全部 →
        </text>
      </view>
      <view v-if="loading" class="loading-state">
        <view class="loading-spinner"></view>
        <text>加载中...</text>
      </view>
      <view v-else-if="!nearbyActivities || nearbyActivities.length === 0" class="empty-state">
        <text class="empty-icon">🔍</text>
        <text class="empty-text">暂无附近的活动</text>
      </view>
      <view v-else-if="nearbyActivities && nearbyActivities.length > 0" class="activity-list">
        <view v-for="activity in nearbyActivities" :key="activity.id" class="activity-card"
          @click="handleActivityClick(activity)">
          <view class="activity-image-wrapper">
            <image v-if="activity.coverImage" :src="activity.coverImage" class="activity-image" mode="aspectFill" />
            <view v-else class="activity-image-placeholder">
              <text class="placeholder-icon">📸</text>
            </view>
            <view class="activity-badge">{{ activity.category || '志愿活动' }}</view>
          </view>
          <view class="activity-content">
            <text class="activity-title">{{ activity.title }}</text>
            <view class="activity-meta">
              <text class="meta-item">📍 {{ activity.location }}</text>
              <text class="meta-item">👥 {{ activity.currentSignups || 0 }}/{{ activity.maxParticipants }}</text>
            </view>
            <view class="activity-footer">
              <view class="activity-points">
                <text class="points-icon">💎</text>
                <text class="points-text">+{{ activity.points || 10 }}积分</text>
              </view>
              <view class="activity-distance">
                <text>{{ calculateDistance(activity) }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- Bottom Safe Area -->
    <view class="bottom-safe-area"></view>

    <!-- Custom Tab Bar -->
    <custom-tabbar :current="0" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as activityApi from '@/api/activity'
import CustomTabbar from '@/components/common/custom-tabbar/custom-tabbar.vue'

const currentLocation = ref('我的位置')
const hasUnread = ref(true)
const loading = ref(false)
const nearbyActivities = ref([])

// Categories (2x4 grid)
const categories = [
  { id: 1, name: '社区服务', icon: '🏘️', type: 'community' },
  { id: 2, name: '环境保护', icon: '🌱', type: 'environmental' },
  { id: 3, name: '教育支持', icon: '📚', type: 'education' },
  { id: 4, name: '动物关爱', icon: '🐾', type: 'animal' },
  { id: 5, name: '医疗健康', icon: '❤️', type: 'health' },
  { id: 6, name: '文化艺术', icon: '🎨', type: 'culture' },
  { id: 7, name: '体育运动', icon: '⚽', type: 'sports' },
  { id: 8, name: '科技创新', icon: '💻', type: 'tech' }
]

console.log('首页初始化 - 分类数量:', categories.length)

// Featured Banners
const banners = ref([
  {
    id: 1,
    image: '/static/banners/featured-1.jpg',
    tag: '特色活动',
    title: '社区环保日',
    subtitle: '一起守护我们的家园'
  },
  {
    id: 2,
    image: '/static/banners/featured-2.jpg',
    tag: '热门推荐',
    title: '关爱老人行动',
    subtitle: '温暖传递，爱心接力'
  },
  {
    id: 3,
    image: '/static/banners/featured-3.jpg',
    tag: '新活动',
    title: '儿童阅读计划',
    subtitle: '用知识点亮未来'
  }
])

// Quick Access Items
const quickAccessItems = [
  {
    id: 1,
    icon: '🔥',
    title: '紧急需求',
    subtitle: '3个活动',
    gradient: 'linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%)',
    type: 'urgent'
  },
  {
    id: 2,
    icon: '🆕',
    title: '新发布',
    subtitle: '12个活动',
    gradient: 'linear-gradient(135deg, #4FACFE 0%, #00F2FE 100%)',
    type: 'new'
  },
  {
    id: 3,
    icon: '⭐',
    title: '热门活动',
    subtitle: '8个活动',
    gradient: 'linear-gradient(135deg, #FFD93D 0%, #FFAA00 100%)',
    type: 'popular'
  },
  {
    id: 4,
    icon: '🏢',
    title: '新组织',
    subtitle: '5个组织',
    gradient: 'linear-gradient(135deg, #7BC96F 0%, #5FB878 100%)',
    type: 'organizations'
  }
]

const selectLocation = () => {
  uni.showToast({
    title: '位置选择功能开发中',
    icon: 'none'
  })
}

const handleCategoryClick = (category) => {
  uni.navigateTo({
    url: `/pages/student/activities/activities?category=${category.type}`
  })
}

const handleBannerClick = (banner) => {
  console.log('Banner clicked:', banner)
  // Navigate to banner detail or activity
}

const handleQuickAccessClick = (item) => {
  uni.navigateTo({
    url: `/pages/student/activities/activities?filter=${item.type}`
  })
}

const handleActivityClick = (activity) => {
  uni.navigateTo({
    url: `/pages/common/activity-detail/activity-detail?id=${activity.id}`
  })
}

const calculateDistance = (activity) => {
  // Mock distance calculation
  const distances = ['500m', '1.2km', '2.5km', '3.8km']
  return distances[Math.floor(Math.random() * distances.length)]
}

const loadNearbyActivities = async () => {
  loading.value = true
  console.log('开始加载附近活动...')
  try {
    const res = await activityApi.getActivityList({
      pageNum: 1,
      pageSize: 6,
      params: {
        status: 'PUBLISHED'
      }
    })
    console.log('API响应:', res)
    const activities = res.records || res.list || res || []
    nearbyActivities.value = activities
    console.log('附近活动数量:', nearbyActivities.value.length)

    // 如果API没有返回数据，使用模拟数据
    if (nearbyActivities.value.length === 0) {
      console.log('使用模拟数据')
      nearbyActivities.value = [
        {
          id: 'mock-1',
          title: '社区清洁活动',
          location: '社区公园',
          currentSignups: 15,
          maxParticipants: 20,
          points: 10,
          category: '环境保护'
        },
        {
          id: 'mock-2',
          title: '关爱老人活动',
          location: '养老院',
          currentSignups: 8,
          maxParticipants: 15,
          points: 15,
          category: '社区服务'
        }
      ]
    }
  } catch (error) {
    console.error('加载附近活动失败:', error)
    // API失败时使用模拟数据
    nearbyActivities.value = [
      {
        id: 'mock-1',
        title: '社区清洁活动',
        location: '社区公园',
        currentSignups: 15,
        maxParticipants: 20,
        points: 10,
        category: '环境保护'
      },
      {
        id: 'mock-2',
        title: '关爱老人活动',
        location: '养老院',
        currentSignups: 8,
        maxParticipants: 15,
        points: 15,
        category: '社区服务'
      }
    ]
  } finally {
    loading.value = false
    console.log('加载完成，loading状态:', loading.value)
    console.log('最终活动数据:', nearbyActivities.value)
  }
}

const navigateTo = (url) => {
  uni.navigateTo({ url })
}

onMounted(() => {
  console.log('页面已挂载，开始加载数据')
  console.log('分类数据:', categories)
  console.log('横幅数据:', banners.value)
  console.log('快速入口数据:', quickAccessItems)
  loadNearbyActivities()
})

// Pull down refresh (仅在支持的平台上注册)
if (typeof uni.onPullDownRefresh === 'function') {
  uni.onPullDownRefresh(() => {
    loadNearbyActivities().finally(() => {
      uni.stopPullDownRefresh()
    })
  })
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #F8F9FA;
  padding-bottom: 120rpx;
}

/* Header */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  background: #FFFFFF;
}

.location-selector {
  display: flex;
  align-items: center;
  padding: 12rpx 20rpx;
  background: #F8F9FA;
  border-radius: 24rpx;
}

.location-icon {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.location-text {
  font-size: 28rpx;
  color: #2D3748;
  font-weight: 500;
  margin-right: 8rpx;
}

.dropdown-icon {
  font-size: 20rpx;
  color: #718096;
}

.notification-btn {
  position: relative;
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #F8F9FA;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-icon {
  font-size: 36rpx;
}

.notification-badge {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  width: 16rpx;
  height: 16rpx;
  background: #FF6B6B;
  border-radius: 50%;
  border: 3rpx solid #FFFFFF;
}

/* Search Bar */
.search-bar {
  margin: 24rpx 32rpx;
  padding: 24rpx 28rpx;
  background: #FFFFFF;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.search-icon {
  font-size: 32rpx;
  margin-right: 16rpx;
}

.search-placeholder {
  font-size: 28rpx;
  color: #A0AEC0;
}

/* Category Grid */
.category-section {
  padding: 0 32rpx 32rpx;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24rpx;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: all 0.3s ease;
}

.category-item:active {
  transform: scale(0.95);
}

.category-icon-wrapper {
  width: 120rpx;
  height: 120rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.category-icon {
  font-size: 56rpx;
}

.category-name {
  font-size: 24rpx;
  color: #2D3748;
  font-weight: 500;
  text-align: center;
}

/* Banner Section */
.banner-section {
  padding: 0 32rpx 32rpx;
}

.banner-swiper {
  width: 100%;
  height: 360rpx;
  border-radius: 24rpx;
  overflow: hidden;
}

.banner-item {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 24rpx;
  overflow: hidden;
}

.banner-image {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.banner-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 32rpx;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7) 0%, transparent 100%);
}

.banner-tag {
  display: inline-block;
  padding: 8rpx 16rpx;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10rpx);
  border-radius: 8rpx;
  font-size: 22rpx;
  color: #FFFFFF;
  font-weight: 600;
  margin-bottom: 12rpx;
}

.banner-title {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #FFFFFF;
  margin-bottom: 8rpx;
  line-height: 1.3;
}

.banner-subtitle {
  display: block;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
}

/* Quick Access Section */
.quick-access-section {
  padding: 0 0 32rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32rpx 20rpx;
}

.section-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #2D3748;
}

.section-more {
  font-size: 26rpx;
  color: #4FACFE;
  font-weight: 500;
}

.quick-scroll {
  white-space: nowrap;
}

.quick-list {
  display: inline-flex;
  gap: 20rpx;
  padding: 0 32rpx;
}

.quick-card {
  width: 240rpx;
  height: 160rpx;
  border-radius: 20rpx;
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.quick-card:active {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
}

.quick-icon {
  font-size: 48rpx;
}

.quick-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #FFFFFF;
  margin-bottom: 4rpx;
}

.quick-subtitle {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.9);
}

/* Opportunities Section */
.opportunities-section {
  padding: 0 32rpx 32rpx;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid #E2E8F0;
  border-top-color: #4FACFE;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 16rpx;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.empty-icon {
  font-size: 96rpx;
  margin-bottom: 16rpx;
  opacity: 0.6;
}

.empty-text {
  font-size: 28rpx;
  color: #718096;
}

.activity-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.activity-card {
  background: #FFFFFF;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.activity-card:active {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
}

.activity-image-wrapper {
  position: relative;
  width: 100%;
  height: 240rpx;
}

.activity-image,
.activity-image-placeholder {
  width: 100%;
  height: 100%;
}

.activity-image-placeholder {
  background: linear-gradient(135deg, #E8F5E6 0%, #FFE5D9 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-icon {
  font-size: 64rpx;
  opacity: 0.5;
}

.activity-badge {
  position: absolute;
  top: 16rpx;
  left: 16rpx;
  padding: 8rpx 16rpx;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10rpx);
  border-radius: 8rpx;
  font-size: 20rpx;
  color: #4FACFE;
  font-weight: 600;
}

.activity-content {
  padding: 20rpx;
}

.activity-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #2D3748;
  margin-bottom: 12rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
  min-height: 78rpx;
}

.activity-meta {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-bottom: 16rpx;
}

.meta-item {
  font-size: 22rpx;
  color: #718096;
}

.activity-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16rpx;
  border-top: 2rpx solid #E2E8F0;
}

.activity-points {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.points-icon {
  font-size: 24rpx;
}

.points-text {
  font-size: 22rpx;
  font-weight: 600;
  color: #4FACFE;
}

.activity-distance {
  font-size: 22rpx;
  color: #A0AEC0;
}

/* Bottom Safe Area */
.bottom-safe-area {
  height: 120rpx;
}
</style>
