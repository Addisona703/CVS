<template>
  <view class="my-activities">
    <!-- 筛选标签页 -->
    <view class="tabs-section">
      <view
        v-for="status in statusOptions"
        :key="status.value"
        class="tab-item"
        :class="{ active: currentStatus === status.value }"
        @click="changeStatus(status.value)"
      >
        <text class="tab-text">{{ status.label }}</text>
        <view v-if="currentStatus === status.value" class="tab-indicator"></view>
      </view>
    </view>

    <!-- 活动列表 -->
    <scroll-view class="scroll-view" scroll-y @scrolltolower="loadMore">
      <view class="activity-list">
        <!-- 加载状态 -->
        <view v-if="loading && activities.length === 0" class="loading-state">
          <view class="loading-spinner"></view>
          <text>加载中...</text>
        </view>
        
        <!-- 空状态 -->
        <view v-else-if="!loading && activities.length === 0" class="empty-state">
          <ph-icon name="list-bullets" :size="120" color="#ccc" />
          <text class="empty-text">暂无活动</text>
        </view>
        
        <!-- 活动卡片 -->
        <view v-else class="card-list">
          <view
            v-for="activity in activities"
            :key="activity.id"
            class="activity-card"
            @click="viewDetail(activity.id)"
          >
            <!-- 顶部：标题和状态 -->
            <view class="card-header">
              <text class="activity-title">{{ activity.title }}</text>
              <view class="status-badge" :class="getStatusClass(activity.status)">
                {{ getStatusText(activity.status) }}
              </view>
            </view>

            <!-- 中部：日期和地点 -->
            <view class="card-info">
              <view class="info-item">
                <ph-icon name="calendar" :size="32" color="#999" />
                <text class="info-text">{{ formatDate(activity.startTime) }}</text>
              </view>
              <view class="info-item">
                <ph-icon name="map-pin" :size="32" color="#999" />
                <text class="info-text">{{ activity.location }}</text>
              </view>
            </view>

            <!-- 底部：关键指标 -->
            <view class="card-footer">
              <view class="metric-item highlight">
                <text class="metric-label">报名人数：</text>
                <text class="metric-value">{{ activity.currentParticipants || 0 }}/{{ activity.maxParticipants }}</text>
              </view>
              <view class="metric-item">
                <text class="metric-label">积分：</text>
                <text class="metric-value">{{ activity.points || 0 }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 加载更多 -->
        <view v-if="loadingMore" class="loading-more">
          <text>加载中...</text>
        </view>
        <view v-else-if="!hasMore && activities.length > 0" class="no-more">
          <text>没有更多了</text>
        </view>
      </view>
    </scroll-view>

    <!-- 创建按钮 -->
    <view class="fab" @click="createActivity">
      <text class="fab-icon">+</text>
    </view>

    <!-- Custom Tab Bar -->
    <custom-tabbar :current="1" role="teacher" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getActivityList } from '@/api/activity'
import { useAuthStore } from '@/stores/auth'
import CustomTabbar from '@/components/common/custom-tabbar/custom-tabbar.vue'
import PhIcon from '@/components/common/ph-icon/ph-icon.vue'

const statusOptions = [
  { label: '全部', value: '' },
  { label: '草稿', value: 'DRAFT' },
  { label: '待审核', value: 'PENDING_APPROVAL' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '进行中', value: 'ONGOING' },
  { label: '已完成', value: 'COMPLETED' }
]

const currentStatus = ref('')
const activities = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 10

onMounted(() => {
  loadActivities()
})

// 下拉刷新
onPullDownRefresh(async () => {
  await loadActivities(true)
  uni.stopPullDownRefresh()
})

const loadActivities = async (isRefresh = false) => {
  if (isRefresh) {
    page.value = 1
    activities.value = []
    hasMore.value = true
  }

  if (loading.value || loadingMore.value) return

  if (page.value === 1) {
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    // 获取当前用户信息
    const authStore = useAuthStore()
    const userId = authStore.userId

    const res = await getActivityList({
      pageNum: page.value,
      pageSize,
      params: {
        status: currentStatus.value || undefined,
        organizerId: userId
      }
    })

    const newActivities = res.records || []
    if (page.value === 1) {
      activities.value = newActivities
    } else {
      activities.value.push(...newActivities)
    }

    hasMore.value = activities.value.length < res.total
  } catch (error) {
    console.error('加载活动列表失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const changeStatus = (status) => {
  if (currentStatus.value === status) return
  currentStatus.value = status
  loadActivities(true)
}

const loadMore = () => {
  if (!hasMore.value || loadingMore.value) return
  page.value++
  loadActivities()
}

const viewDetail = (id) => {
  uni.navigateTo({
    url: `/pages/common/activity-detail/activity-detail?id=${id}`
  })
}

const createActivity = () => {
  uni.navigateTo({
    url: '/pages/teacher/create-activity/create-activity'
  })
}



const getStatusClass = (status) => {
  const statusMap = {
    DRAFT: 'status-draft',
    PENDING_APPROVAL: 'status-pending',
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
    PENDING_APPROVAL: '待审核',
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
</script>

<style lang="scss" scoped>
/* 浅灰色背景 */
.my-activities {
  min-height: 100vh;
  background: #F5F5F5;
  display: flex;
  flex-direction: column;
  padding-bottom: 120rpx;
}

/* 筛选标签页 - 固定在顶部 */
.tabs-section {
  position: sticky;
  top: 0;
  z-index: 99;
  display: flex;
  background: #FFFFFF;
  padding: 0 32rpx;
  border-bottom: 1rpx solid #F0F0F0;
}

.tab-item {
  position: relative;
  flex: 1;
  height: 88rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.tab-text {
  font-size: 28rpx;
  color: #666;
  transition: all 0.3s;
}

.tab-item.active .tab-text {
  color: #52c41a;
  font-weight: 600;
}

/* 主题色下划线 */
.tab-indicator {
  position: absolute;
  bottom: 0;
  width: 48rpx;
  height: 6rpx;
  background: #52c41a;
  border-radius: 3rpx;
}

/* 滚动区域 */
.scroll-view {
  flex: 1;
  background: #F5F5F5;
}

.activity-list {
  padding: 24rpx;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx 0;
  color: #999;
  font-size: 28rpx;
  gap: 16rpx;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid #E5E5E5;
  border-top-color: #52c41a;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx 0;
}



.empty-text {
  font-size: 28rpx;
  color: #999;
}

/* 活动卡片列表 */
.card-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

/* 白色圆角矩形卡片 */
.activity-card {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
}

.activity-card:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
}

/* 顶部：标题和状态 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16rpx;
  gap: 16rpx;
}

.activity-title {
  flex: 1;
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
}

/* 带颜色的状态标签 */
.status-badge {
  flex-shrink: 0;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  font-weight: 500;
}

.status-draft {
  background: #F0F0F0;
  color: #999;
}

.status-published {
  background: #E6F7FF;
  color: #1890FF;
}

.status-ongoing {
  background: #F6FFED;
  color: #52C41A;
}

.status-completed {
  background: #F0F0F0;
  color: #8C8C8C;
}

.status-cancelled {
  background: #FFF1F0;
  color: #FF4D4F;
}

/* 中部：日期和地点 - 简洁的单色线性图标 */
.card-info {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}



.info-text {
  font-size: 26rpx;
  color: #666;
}

/* 底部：关键指标 */
.card-footer {
  display: flex;
  align-items: center;
  gap: 32rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #F0F0F0;
}

.metric-item {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
}

.metric-label {
  font-size: 24rpx;
  color: #999;
}

.metric-value {
  font-size: 26rpx;
  color: #666;
  font-weight: 500;
}

/* 报名人数加粗并使用主题色 */
.metric-item.highlight .metric-label {
  color: #52c41a;
  font-weight: 600;
}

.metric-item.highlight .metric-value {
  color: #52c41a;
  font-weight: 700;
  font-size: 28rpx;
}

/* 加载更多 */
.loading-more,
.no-more {
  text-align: center;
  padding: 40rpx 0;
  font-size: 26rpx;
  color: #999;
}

/* 创建按钮 */
.fab {
  position: fixed;
  right: 32rpx;
  bottom: 160rpx;
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  box-shadow: 0 8rpx 24rpx rgba(82, 196, 26, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.fab-icon {
  font-size: 64rpx;
  color: white;
  font-weight: 300;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
