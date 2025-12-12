<template>
  <view class="teacher-dashboard">
    <!-- Header -->
    <view class="header">
      <view class="greeting">
        <text class="greeting-text">你好, {{ userStore?.userInfo?.realName || '教师' }}</text>
      </view>
      <view class="header-right">
        <view class="avatar" @click="navigateTo('/pages/teacher/profile/profile')">
          {{ userInitial }}
        </view>
        <view class="notification-btn" @click="navigateTo('/pages/common/notifications/notifications')">
          <ph-icon name="bell" :size="44" color="#666" />
          <view v-if="hasUnread" class="notification-badge"></view>
        </view>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="quick-actions-section">
      <view class="section-header">
        <text class="section-title">快捷入口</text>
      </view>
      <scroll-view class="quick-scroll" scroll-x show-scrollbar="false">
        <view class="quick-list">
          <view class="quick-card" @click="navigateTo('/pages/teacher/create-activity/create-activity')">
            <view class="quick-icon-wrapper" style="background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);">
              <text class="iconfont quick-icon">&#xe8e4;</text>
            </view>
            <text class="quick-title">创建活动</text>
            <text class="quick-subtitle">发布新活动</text>
          </view>
          <view class="quick-card" @click="navigateTo('/pages/teacher/check-qrcode/check-qrcode')">
            <view class="quick-icon-wrapper" style="background: linear-gradient(135deg, #4FACFE 0%, #00F2FE 100%);">
              <ph-icon name="qr-code" :size="60" color="#fff" />
            </view>
            <text class="quick-title">签到码</text>
            <text class="quick-subtitle">生成二维码</text>
          </view>
          <view class="quick-card" @click="navigateTo('/pages/teacher/signup-management/signup-management')">
            <view class="quick-icon-wrapper" style="background: linear-gradient(135deg, #FFD93D 0%, #FFAA00 100%);">
              <ph-icon name="clipboard-text" :size="60" color="#fff" />
            </view>
            <text class="quick-title">报名管理</text>
            <text class="quick-subtitle">审核报名</text>
          </view>
          <view class="quick-card" @click="navigateTo('/pages/teacher/review-signout/review-signout')">
            <view class="quick-icon-wrapper" style="background: linear-gradient(135deg, #7BC96F 0%, #5FB878 100%);">
              <ph-icon name="check-circle" :size="60" color="#fff" />
            </view>
            <text class="quick-title">签退审核</text>
            <text class="quick-subtitle">审核签退</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 最近活动 -->
    <view class="recent-activities">
      <view class="section-header">
        <text class="section-title">最近活动</text>
        <text class="section-more" @click="navigateTo('/pages/teacher/my-activities/my-activities')">
          查看全部 →
        </text>
      </view>
      <view v-if="loading" class="loading-state">
        <view class="loading-spinner"></view>
        <text>加载中...</text>
      </view>
      <view v-else-if="!loading && activities.length === 0" class="empty-state">
        <ph-icon name="list-bullets" :size="120" color="#ccc" />
        <text class="empty-text">暂无活动</text>
      </view>
      <view v-else class="activity-grid">
        <view
          v-for="activity in activities"
          :key="activity.id"
          class="activity-card"
          @click="viewActivityDetail(activity.id)"
        >
          <view class="activity-image-wrapper">
            <image v-if="activity.coverImage" :src="activity.coverImage" class="activity-image" mode="aspectFill" />
            <view v-else class="activity-image-placeholder">
              <ph-icon name="image" :size="80" color="#999" />
            </view>
            <view class="activity-status" :class="getStatusClass(activity.status)">
              {{ getStatusText(activity.status) }}
            </view>
          </view>
          <view class="activity-content">
            <text class="activity-title">{{ activity.title }}</text>
            <view class="activity-meta">
              <view class="meta-item">
                <ph-icon name="map-pin" :size="28" color="#718096" />
                <text>{{ activity.location }}</text>
              </view>
              <view class="meta-item">
                <ph-icon name="users" :size="28" color="#718096" />
                <text>{{ activity.currentSignups || 0 }}/{{ activity.maxParticipants }}</text>
              </view>
            </view>
            <view class="activity-footer">
              <view class="activity-time">
                <ph-icon name="clock" :size="28" color="#718096" />
                <text class="time-text">{{ formatDate(activity.startTime) }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- Bottom Safe Area -->
    <view class="bottom-safe-area"></view>

    <!-- Custom Tab Bar -->
    <custom-tabbar :current="0" role="teacher" />
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getActivityList } from '@/api/activity'
import { getSignupList } from '@/api/signup'
import CustomTabbar from '@/components/common/custom-tabbar/custom-tabbar.vue'
import PhIcon from '@/components/common/ph-icon/ph-icon.vue'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'
import * as notificationApi from '@/api/notification'

const userStore = useAuthStore()
const notificationStore = useNotificationStore()

const activities = ref([])
const loading = ref(false)

const hasUnread = computed(() => notificationStore.hasUnread)

const userInitial = computed(() => {
  if (!userStore || !userStore.userInfo) return '教'
  const name = userStore.userInfo?.realName || userStore.userInfo?.name || userStore.userInfo?.username || '教'
  return name.charAt(0).toUpperCase()
})

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
  loadDashboardData()
  loadUnreadCount()
})

// 下拉刷新
onPullDownRefresh(() => {
  Promise.all([
    loadDashboardData(),
    loadUnreadCount()
  ]).finally(() => {
    uni.stopPullDownRefresh()
  })
})

const loadDashboardData = async () => {
  loading.value = true
  try {
    await loadRecentActivities()
  } catch (error) {
    console.error('加载仪表板数据失败:', error)
    // 使用模拟数据
    activities.value = [
      {
        id: 'mock-1',
        title: '社区清洁活动',
        location: '社区公园',
        currentSignups: 15,
        maxParticipants: 20,
        status: 'PUBLISHED',
        startTime: new Date().toISOString()
      },
      {
        id: 'mock-2',
        title: '关爱老人活动',
        location: '养老院',
        currentSignups: 8,
        maxParticipants: 15,
        status: 'ONGOING',
        startTime: new Date().toISOString()
      }
    ]
  } finally {
    loading.value = false
  }
}

const loadRecentActivities = async () => {
  try {
    // 获取当前用户信息
    const authStore = useAuthStore()
    const userId = authStore.userId

    const res = await getActivityList({
      pageNum: 1,
      pageSize: 6,
      params: {
        organizerId: userId
      }
    })
    activities.value = res.records || []
  } catch (error) {
    console.error('加载最近活动失败:', error)
  }
}

const getStatusClass = (status) => {
  const statusMap = {
    DRAFT: 'status-draft',
    PUBLISHED: 'status-published',
    ONGOING: 'status-ongoing',
    COMPLETED: 'status-completed',
    CANCELLED: 'status-cancelled'
  }
  return statusMap[status] || 'status-draft'
}

const getStatusText = (status) => {
  const statusMap = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    ONGOING: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return statusMap[status] || '未知'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours()
  const minute = date.getMinutes()
  return `${month}月${day}日 ${hour}:${minute.toString().padStart(2, '0')}`
}

const navigateTo = (url) => {
  uni.navigateTo({ url })
}

const viewActivityDetail = (id) => {
  uni.navigateTo({
    url: `/pages/common/activity-detail/activity-detail?id=${id}`
  })
}
</script>

<style scoped>
/* 极简纯白背景 */
.teacher-dashboard {
  min-height: 100vh;
  background: #FFFFFF;
  padding-bottom: 120rpx;
}

/* Header */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 32rpx 24rpx;
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
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
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

/* Quick Actions Section - 简化设计 */
.quick-actions-section {
  padding: 0 32rpx 32rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 0 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.section-more {
  font-size: 26rpx;
  color: #52c41a;
  font-weight: 500;
}

.quick-scroll {
  white-space: nowrap;
}

.quick-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;
}

.quick-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 12rpx;
  transition: all 0.3s ease;
}

.quick-card:active {
  transform: scale(0.95);
}

.quick-icon-wrapper {
  width: 96rpx;
  height: 96rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
}

.quick-icon {
  font-size: 56rpx;
  color: #fff;
}



.quick-title {
  font-size: 24rpx;
  font-weight: 500;
  color: #333;
  text-align: center;
}

.quick-subtitle {
  display: none;
}

/* Recent Activities - 简洁设计 */
.recent-activities {
  padding: 0 32rpx 32rpx;
  background: #FFFFFF;
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
  border-top-color: #52c41a;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 16rpx;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}



.empty-text {
  font-size: 28rpx;
  color: #718096;
}

.activity-grid {
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



.activity-status {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  padding: 8rpx 16rpx;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10rpx);
  border-radius: 8rpx;
  font-size: 20rpx;
  font-weight: 600;
}

.status-draft {
  color: #A0AEC0;
}

.status-published {
  color: #4FACFE;
}

.status-ongoing {
  color: #48BB78;
}

.status-completed {
  color: #718096;
}

.status-cancelled {
  color: #F56565;
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
  display: flex;
  align-items: center;
  gap: 6rpx;
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

.activity-time {
  display: flex;
  align-items: center;
  gap: 6rpx;
}



.time-text {
  font-size: 22rpx;
  color: #718096;
}

/* Bottom Safe Area */
.bottom-safe-area {
  height: 120rpx;
}
</style>
