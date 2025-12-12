<template>
  <view class="dashboard">
    <!-- Header -->
    <view class="header">
      <view class="greeting">
        <text class="greeting-text">你好, {{ userStore?.userInfo?.realName || '志愿者' }}</text>
      </view>
      <view class="header-right">
        <view class="avatar" @click="handleProfileClick">
          {{ userInitial }}
        </view>
        <view class="notification-btn" @click="navigateTo('/pages/common/notifications/notifications')">
          <ph-icon name="bell" :size="40" color="#666" />
          <view v-if="hasUnread" class="notification-badge"></view>
        </view>
      </view>
    </view>

    <!-- Search Bar -->
    <view class="search-bar" @click="handleSearchClick">
      <ph-icon name="magnifying-glass" :size="36" color="#999" />
      <text class="search-placeholder">搜索志愿活动...</text>
    </view>

    <!-- Service Categories -->
    <view class="category-section">
      <text class="section-label">服务分类</text>
      <view class="category-grid">
        <view v-for="category in categories" :key="category.id" class="category-item"
          @click="handleCategoryClick(category)">
          <view class="category-icon-wrapper">
            <ph-icon :name="category.icon" :size="52" color="#666" />
          </view>
          <text class="category-name">{{ category.name }}</text>
        </view>
      </view>
    </view>

    <!-- My Hours Widget -->
    <view class="hours-widget">
      <view class="widget-header">
        <ph-icon name="clock" :size="40" color="#0052d9" />
        <view class="widget-title">我的服务时长</view>
      </view>
      <view class="hours-content">
        <view class="hours-main">
          <text class="hours-value">{{ stats?.hours || 0 }}</text>
          <text class="hours-unit">小时</text>
        </view>
        <view class="hours-stats">
          <view class="hours-stat-item">
            <text class="stat-number">{{ stats?.activities || 0 }}</text>
            <text class="stat-text">参与活动</text>
          </view>
          <view class="hours-divider"></view>
          <view class="hours-stat-item">
            <text class="stat-number">{{ stats?.points || 0 }}</text>
            <text class="stat-text">获得积分</text>
          </view>
        </view>
      </view>
      <view class="hours-progress">
        <view class="progress-bar">
          <view class="progress-fill" :style="{ width: progressWidth }"></view>
        </view>
        <text class="progress-text">距离下一个成就还需 {{ nextMilestone }} 小时</text>
      </view>
    </view>

    <!-- Upcoming Events -->
    <view class="section upcoming-section">
      <view class="section-header">
        <view class="section-title-wrapper">
          <ph-icon name="calendar" :size="50" color="#0052d9" />
          <text class="section-title">即将开始</text>
        </view>
        <text class="section-more" @click="handleMySignupsClick">
          查看全部 →
        </text>
      </view>
      <view v-if="loading" class="loading">
        <view class="loading-spinner"></view>
        <text>加载中...</text>
      </view>
      <view v-else-if="!upcomingEvents || upcomingEvents.length === 0" class="empty-state">
        <text class="empty-icon">📭</text>
        <text class="empty-text">暂无即将开始的活动</text>
        <text class="empty-hint">未来30分钟内没有即将开始的活动</text>
      </view>
      <view v-else-if="upcomingEvents && upcomingEvents.length > 0" class="event-list">
        <view v-for="event in upcomingEvents" :key="event.id" class="event-card urgent" @click="handleActivityClick(event)">
          <view class="event-date urgent-date">
            <text class="event-day">{{ formatDay(event.startTime) }}</text>
            <text class="event-month">{{ formatMonth(event.startTime) }}</text>
          </view>
          <view class="event-info">
            <view class="event-title-row">
              <text class="event-title">{{ event.title }}</text>
              <view class="countdown-badge">
                <ph-icon name="bell" :size="24" color="#FF6B6B" />
                <text class="countdown-text">{{ event.timeUntilStart }}</text>
              </view>
            </view>
            <view class="event-meta">
              <view class="meta-item">
                <ph-icon name="clock" :size="28" color="#718096" />
                <text class="event-time">{{ formatTime(event.startTime) }}</text>
              </view>
              <view class="meta-item">
                <ph-icon name="map-pin" :size="28" color="#718096" />
                <text class="event-location">{{ event.location }}</text>
              </view>
            </view>
          </view>
          <view class="event-badge urgent-badge">
            <text>即将开始</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Bottom Safe Area -->
    <view class="bottom-safe-area"></view>

    <!-- Custom Tab Bar -->
    <custom-tabbar :current="0" role="student" />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'
import { usePageGuard } from '@/composables/usePageGuard'
import * as statisticsApi from '@/api/statistics'
import * as notificationApi from '@/api/notification'
import * as signupApi from '@/api/signup'
import CustomTabbar from '@/components/common/custom-tabbar/custom-tabbar.vue'
import PhIcon from '@/components/common/ph-icon/ph-icon.vue'

// 页面权限守卫
usePageGuard()

const userStore = useAuthStore()
const notificationStore = useNotificationStore()

const hasUnread = computed(() => notificationStore.hasUnread)

const stats = ref({
  points: 0,
  activities: 0,
  hours: 0
})

const upcomingEvents = ref([])
const loading = ref(false)

// Service Categories - 根据项目功能设计（使用Phosphor图标）
const categories = [
  { id: 1, name: '活动报名', icon: 'file-text', type: 'signup' },
  { id: 2, name: '签到签退', icon: 'check-circle', type: 'checkin' },
  { id: 3, name: '服务记录', icon: 'list', type: 'records' },
  { id: 4, name: '积分商城', icon: 'gift', type: 'mall' },
  { id: 5, name: '服务证明', icon: 'medal', type: 'certificate' }
]



const progressWidth = computed(() => {
  const hours = stats.value.hours || 0
  const milestone = Math.ceil(hours / 10) * 10 || 10
  const progress = (hours / milestone) * 100
  return `${Math.min(progress, 100)}%`
})

const nextMilestone = computed(() => {
  const hours = stats.value.hours || 0
  const milestone = Math.ceil(hours / 10) * 10 || 10
  const remaining = milestone - hours
  return remaining.toFixed(1)
})

const userInitial = computed(() => {
  if (!userStore || !userStore.userInfo) return '学'
  const name = userStore.userInfo?.realName || userStore.userInfo?.name || userStore.userInfo?.username || '学'
  return name.charAt(0).toUpperCase()
})

const loadStats = async () => {
  try {
    // 调用学生仪表板统计 API
    const res = await statisticsApi.getStudentDashboardStats()
    console.log('统计数据响应:', res)

    stats.value = {
      points: res?.totalPoints || 0,
      activities: res?.mySignupsCount || 0,
      hours: res?.totalServiceHours || 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 失败时使用默认值
    stats.value = {
      points: 0,
      activities: 0,
      hours: 0
    }
  }
}

const loadUpcomingEvents = async () => {
  try {
    // 获取用户已报名的活动
    const res = await signupApi.getMySignups({
      pageNum: 1,
      pageSize: 20, // 获取更多数据以便过滤
      params: {
        status: 'APPROVED' // 只显示已通过审核的报名
      }
    })
    
    // 从报名记录中提取活动信息
    const signups = res.records || res.list || res || []
    const now = new Date()
    const thresholdMillis = 30 * 60 * 1000 // 30分钟
    
    console.log('当前时间:', now.toLocaleString())
    console.log('所有已报名活动数量:', signups.length)
    
    // 先看看所有活动的数据结构
    signups.forEach((signup, index) => {
      const startStr = signup.activityStartTime || signup.startTime
      console.log(`报名记录${index + 1}:`, signup)
      if (startStr) {
        const startTime = new Date(startStr)
        console.log(`  - 活动: ${signup.activityTitle || signup.title}`)
        console.log(`  - 开始时间: ${startStr}`)
        console.log(`  - 解析后: ${startTime.toLocaleString()}`)
      } else {
        console.log(`  - 没有开始时间字段 (activityStartTime / startTime)`)
      }
    })
    
    upcomingEvents.value = signups
      .filter(signup => {
        const startStr = signup.activityStartTime || signup.startTime
        if (!startStr) return false
        
        const startTime = new Date(startStr)
        const diff = startTime - now
        // 过滤出：开始时间在当前时间之后，且在未来30分钟内的活动
        const isUpcoming = diff > 0 && diff <= thresholdMillis
        
        if (isUpcoming) {
          console.log('✓ 符合条件的活动:', signup.activityTitle, startTime.toLocaleString())
        }
        
        return isUpcoming
      })
      .map(signup => ({
        id: signup.activityId,
        title: signup.activityTitle || signup.title,
        startTime: signup.activityStartTime || signup.startTime,
        endTime: signup.activityEndTime || signup.endTime,
        location: signup.activityLocation || signup.location,
        signupId: signup.id,
        signupStatus: signup.status,
        timeUntilStart: getTimeUntilStart(signup.activityStartTime || signup.startTime) // 添加倒计时
      }))
      .sort((a, b) => new Date(a.startTime) - new Date(b.startTime)) // 按开始时间排序
      .slice(0, 3) // 只取前3个
      
    console.log('即将开始的活动数量:', upcomingEvents.value.length)
    if (upcomingEvents.value.length > 0) {
      console.log('最近的活动:', upcomingEvents.value[0].title, '开始时间:', upcomingEvents.value[0].startTime)
    } else {
      console.log('没有找到即将开始的活动')
    }
  } catch (error) {
    console.error('加载即将开始的活动失败:', error)
    upcomingEvents.value = []
  }
}

// 计算距离开始还有多久
const getTimeUntilStart = (startTime) => {
  const now = new Date()
  const start = new Date(startTime)
  const diff = start - now
  
  if (diff <= 0) return '即将开始'
  
  const minutes = Math.floor(diff / (1000 * 60))
  if (minutes < 1) return '即将开始'
  if (minutes < 60) return `${minutes}分钟后开始`
  
  const hours = Math.floor(minutes / 60)
  const remainingMinutes = minutes % 60
  if (remainingMinutes === 0) return `${hours}小时后开始`
  return `${hours}小时${remainingMinutes}分钟后开始`
}

const formatDay = (dateStr) => {
  if (!dateStr) return '--'
  const date = new Date(dateStr)
  return date.getDate()
}

const formatMonth = (dateStr) => {
  if (!dateStr) return '--'
  const date = new Date(dateStr)
  const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
  return months[date.getMonth()]
}

const formatTime = (dateStr) => {
  if (!dateStr) return '--:--'
  const date = new Date(dateStr)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '--'
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}月${day}日 ${hours}:${minutes}`
}

const handleActivityClick = (activity) => {
  console.log('点击活动:', activity)
  if (!activity || !activity.id) {
    console.error('活动数据无效:', activity)
    uni.showToast({
      title: '活动数据无效',
      icon: 'none'
    })
    return
  }
  
  uni.navigateTo({
    url: `/pages/common/activity-detail/activity-detail?id=${activity.id}`,
    fail: (err) => {
      console.error('跳转失败:', err)
      uni.showToast({
        title: '跳转失败',
        icon: 'none'
      })
    }
  })
}

const selectLocation = () => {
  uni.showToast({
    title: '位置选择功能开发中',
    icon: 'none'
  })
}

const handleCategoryClick = (category) => {
  const routes = {
    signup: '/pages/student/activities/activities',
    checkin: '/pages/student-sub/check-in/check-in',
    records: '/pages/student-sub/my-records/my-records',
    mall: '/pages/student-sub/mall/mall',
    certificate: '/pages/student-sub/certificates/certificates'
  }
  const url = routes[category.type] || '/pages/student/activities/activities'
  
  // 如果是 tabBar 页面，使用 switchTab
  if (category.type === 'signup') {
    uni.switchTab({ url })
  } else {
    uni.navigateTo({ url })
  }
}

const handleSearchClick = () => {
  // 跳转到活动列表 tabbar 页面（已经是 switchTab，无需修改）
  uni.switchTab({
    url: '/pages/student/activities/activities'
  })
}

const navigateTo = (url) => {
  uni.navigateTo({ url })
}

const handleProfileClick = () => {
  uni.switchTab({
    url: '/pages/student/profile/profile'
  })
}

const handleMySignupsClick = () => {
  uni.switchTab({
    url: '/pages/student/my-signups/my-signups'
  })
}

// 加载未读通知数量
const loadUnreadCount = async () => {
  try {
    const data = await notificationApi.getUnreadCount()
    notificationStore.setUnreadCount(data.count || data)
  } catch (error) {
    console.error('加载未读通知数量失败:', error)
  }
}

onMounted(() => {
  // 隐藏官方 tabBar，使用自定义 tabBar
  uni.hideTabBar({
    animation: false
  })
  
  loadStats()
  loadUpcomingEvents()
  loadUnreadCount()
})

// 下拉刷新
onPullDownRefresh(() => {
  Promise.all([
    loadStats(),
    loadUpcomingEvents(),
    loadUnreadCount()
  ]).finally(() => {
    uni.stopPullDownRefresh()
  })
})
</script>

<style scoped>
/* 极简纯白背景 */
.dashboard {
  min-height: 100vh;
  background: #FFFFFF;
  padding-bottom: 220rpx; /* 增加底部内边距，避免被 tabbar 遮挡 */
}

/* Header */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 32rpx 24rpx;
  padding-top: calc(var(--status-bar-height) + 32rpx);
  background: #FFFFFF;
  position: sticky;
  top: 0;
  z-index: 100;
}

.greeting {
  flex: 1;
}

.greeting-text {
  font-size: 40rpx;
  font-weight: 600;
  color: #333;
  line-height: 1.3;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #0052d9 0%, #0041a8 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  color: #fff;
  font-weight: 600;
}

.notification-btn {
  position: relative;
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #F8F8F8;
  display: flex;
  align-items: center;
  justify-content: center;
}



.notification-badge {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  width: 16rpx;
  height: 16rpx;
  background: #FF6B6B;
  border-radius: 50%;
}

/* 醒目的圆角搜索框 */
.search-bar {
  margin: 0 32rpx 32rpx;
  padding: 28rpx 32rpx;
  background: #F8F8F8;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  border: 2rpx solid #F8F8F8;
  transition: all 0.3s ease;
}

.search-bar:active {
  border-color: #0052d9;
  background: #FFF;
}



.search-placeholder {
  font-size: 28rpx;
  color: #999;
}

/* 服务分类 */
.category-section {
  padding: 0 32rpx 32rpx;
}

.section-label {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 24rpx;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20rpx;
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

/* 简约的单色线性图标 */
.category-icon-wrapper {
  width: 110rpx;
  height: 110rpx;
  background: #F8F8F8;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
  transition: all 0.3s ease;
}

.category-item:active .category-icon-wrapper {
  background: linear-gradient(135deg, #0052d9 0%, #0041a8 100%);
}



.category-name {
  font-size: 22rpx;
  color: #666;
  font-weight: 500;
  text-align: center;
}

/* 简洁的服务时长卡片 */
.hours-widget {
  margin: 0 32rpx 32rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx 32rpx;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.06);
  border: 1rpx solid #F0F0F0;
}

.widget-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 28rpx;
}



.widget-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #2D3748;
}

.hours-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28rpx;
}

.hours-main {
  display: flex;
  align-items: baseline;
}

.hours-value {
  font-size: 88rpx;
  font-weight: 700;
  background: linear-gradient(135deg, #0052d9 0%, #0041a8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
}

.hours-unit {
  font-size: 28rpx;
  color: #718096;
  margin-left: 8rpx;
  font-weight: 500;
}

.hours-stats {
  display: flex;
  gap: 24rpx;
}

.hours-stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-number {
  font-size: 36rpx;
  font-weight: 700;
  color: #2D3748;
  margin-bottom: 4rpx;
}

.stat-text {
  font-size: 22rpx;
  color: #A0AEC0;
}

.hours-divider {
  width: 2rpx;
  height: 60rpx;
  background: #E2E8F0;
}

.hours-progress {
  margin-top: 8rpx;
}

.progress-bar {
  height: 12rpx;
  background: #E8F5E6;
  border-radius: 6rpx;
  overflow: hidden;
  margin-bottom: 12rpx;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #0052d9 0%, #0041a8 100%);
  border-radius: 6rpx;
  transition: width 0.6s ease;
}

.progress-text {
  font-size: 24rpx;
  color: #718096;
}

/* Section Styles */
.section {
  margin: 0 24rpx 32rpx;
  background: #fff;
  border-radius: 28rpx;
  padding: 32rpx 28rpx;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.04);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28rpx;
}

.section-title-wrapper {
  display: flex;
  align-items: center;
  gap: 12rpx;
}



.section-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #2D3748;
}

.section-more {
  font-size: 26rpx;
  color: #0052d9;
  font-weight: 500;
}

/* Loading & Empty States */
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
  color: #A0AEC0;
  font-size: 28rpx;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid #E2E8F0;
  border-top-color: #0052d9;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 16rpx;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0;
}

.empty-icon {
  font-size: 96rpx;
  margin-bottom: 16rpx;
  opacity: 0.6;
}

.empty-text {
  font-size: 28rpx;
  color: #718096;
  margin-bottom: 8rpx;
  font-weight: 500;
}

.empty-hint {
  font-size: 24rpx;
  color: #A0AEC0;
}

/* Upcoming Events */
.event-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.event-card {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background: linear-gradient(135deg, #FFF5EE 0%, #F7FAFC 100%);
  border-radius: 20rpx;
  border: 2rpx solid #E2E8F0;
  transition: all 0.3s ease;
}

.event-card:active {
  transform: translateX(8rpx);
  border-color: #0052d9;
  box-shadow: 0 4rpx 16rpx rgba(0, 82, 217, 0.15);
}

.event-date {
  width: 96rpx;
  height: 96rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #0052d9 0%, #0041a8 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.event-day {
  font-size: 44rpx;
  font-weight: 700;
  color: #fff;
  line-height: 1;
}

.event-month {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.9);
  margin-top: 4rpx;
}

.event-info {
  flex: 1;
  min-width: 0;
}

.event-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #2D3748;
  margin-bottom: 12rpx;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.event-meta {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.event-time,
.event-location {
  font-size: 24rpx;
  color: #718096;
}

.event-badge {
  padding: 8rpx 16rpx;
  background: #E8F5E6;
  color: #7BC96F;
  border-radius: 8rpx;
  font-size: 22rpx;
  font-weight: 600;
  margin-left: 12rpx;
  flex-shrink: 0;
}

/* 即将开始的活动样式 */
.event-card.urgent {
  background: linear-gradient(135deg, #FFF5F5 0%, #FFF0F0 100%);
  border: 2rpx solid #FFE5E5;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 4rpx 16rpx rgba(255, 107, 107, 0.1);
  }
  50% {
    box-shadow: 0 4rpx 20rpx rgba(255, 107, 107, 0.2);
  }
}

.event-card.urgent:active {
  border-color: #FF6B6B;
  box-shadow: 0 4rpx 16rpx rgba(255, 107, 107, 0.25);
}

.event-date.urgent-date {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF5252 100%);
}

.event-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
  gap: 12rpx;
}

.event-title-row .event-title {
  margin-bottom: 0;
  flex: 1;
}

.countdown-badge {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx 12rpx;
  background: rgba(255, 107, 107, 0.1);
  border-radius: 12rpx;
  flex-shrink: 0;
}

.countdown-text {
  font-size: 22rpx;
  color: #FF6B6B;
  font-weight: 600;
  white-space: nowrap;
}

.event-badge.urgent-badge {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF5252 100%);
  color: #fff;
}

/* Bottom Safe Area */
.bottom-safe-area {
  height: 160rpx;
  flex-shrink: 0;
}
</style>
