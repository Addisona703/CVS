<template>
  <view class="notifications-page">
    <view class="page-header">
      <text class="unread-count" v-if="unreadCount > 0">
        {{ unreadCount }}条未读
      </text>
      <button class="mark-all-btn" :style="{ background: themeColor }" @click="handleMarkAllRead">
        全部已读
      </button>
    </view>
    
    <scroll-view 
      class="notification-list"
      scroll-y
      @scrolltolower="loadMore"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="refresh"
    >
      <view 
        v-for="item in notifications" 
        :key="item.id"
        :class="['notification-item', { unread: !item.isRead }]"
        @click="handleNotificationClick(item)"
      >
        <view class="notification-header">
          <text class="notification-title">{{ item.title }}</text>
          <view class="unread-dot" v-if="!item.isRead"></view>
        </view>
        
        <text class="notification-content">{{ item.content }}</text>
        
        <view class="notification-footer">
          <text class="notification-time">{{ formatTime(item.createTime) }}</text>
          <text class="delete-btn" @click.stop="handleDelete(item.id)">删除</text>
        </view>
      </view>
      
      <view v-if="loading" class="loading-more">
        <text>加载中...</text>
      </view>
      
      <view v-if="finished && notifications.length > 0" class="no-more">
        <text>没有更多了</text>
      </view>
      
      <view v-if="!loading && notifications.length === 0" class="empty-state">
        <text class="empty-text">暂无通知</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import * as notificationApi from '@/api/notification'
import { useNotificationStore } from '@/stores/notification'
import { useAuthStore } from '@/stores/auth'
import { formatRelativeTime } from '@/utils/format'

const notificationStore = useNotificationStore()
const authStore = useAuthStore()

// 根据角色获取主题色
const themeColor = computed(() => {
  if (authStore.isAdmin) {
    return '#FF8C42' // 管理员：橙色
  } else if (authStore.isTeacher) {
    return '#52c41a' // 教师：绿色
  } else {
    return '#0052d9' // 学生：蓝色
  }
})

const notifications = ref([])
const unreadCount = ref(0)
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)

const pagination = ref({
  current: 1,
  size: 20,
  total: 0
})

onMounted(() => {
  loadNotifications()
  loadUnreadCount()
})

// 加载通知列表
const loadNotifications = async (isRefresh = false) => {
  if (loading.value) return
  
  if (isRefresh) {
    pagination.value.current = 1
    finished.value = false
  }
  
  loading.value = true
  
  try {
    const data = await notificationApi.getNotificationList({
      pageNum: pagination.value.current,
      pageSize: pagination.value.size
    })
    
    if (isRefresh) {
      notifications.value = data.records
    } else {
      notifications.value.push(...data.records)
    }
    
    pagination.value.total = data.total
    
    if (notifications.value.length >= data.total) {
      finished.value = true
    }
  } catch (error) {
    console.error('加载通知列表失败:', error)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 加载未读数量
const loadUnreadCount = async () => {
  try {
    const data = await notificationApi.getUnreadCount()
    unreadCount.value = data.count
    notificationStore.setUnreadCount(data.count)
  } catch (error) {
    console.error('加载未读数量失败:', error)
  }
}

// 下拉刷新
const refresh = () => {
  refreshing.value = true
  loadNotifications(true)
  loadUnreadCount()
}

// 加载更多
const loadMore = () => {
  if (finished.value || loading.value) return
  
  pagination.value.current++
  loadNotifications()
}

// 点击通知
const handleNotificationClick = async (item) => {
  // 标记为已读
  if (!item.isRead) {
    try {
      await notificationApi.markAsRead(item.id)
      item.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
      notificationStore.setUnreadCount(unreadCount.value)
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
  
  // 根据通知类型跳转到相关页面
  if (item.relatedType && item.relatedId) {
    const routeMap = {
      'ACTIVITY': `/pages/common/activity-detail/activity-detail?id=${item.relatedId}`,
      'SIGNUP': `/pages/student/my-signups/my-signups`,
      'CERTIFICATE': `/pages/student-sub/certificates/certificates`,
      'REDEMPTION': `/pages/student-sub/my-redemptions/my-redemptions`
    }
    
    const url = routeMap[item.relatedType]
    if (url) {
      uni.navigateTo({ url })
    }
  }
}

// 全部已读
const handleMarkAllRead = async () => {
  if (unreadCount.value === 0) {
    uni.showToast({ title: '没有未读通知', icon: 'none' })
    return
  }
  
  try {
    await notificationApi.markAllAsRead()
    
    notifications.value.forEach(item => {
      item.isRead = true
    })
    
    unreadCount.value = 0
    notificationStore.setUnreadCount(0)
    
    uni.showToast({ title: '已全部标记为已读', icon: 'success' })
  } catch (error) {
    console.error('标记全部已读失败:', error)
  }
}

// 删除通知
const handleDelete = (id) => {
  uni.showModal({
    title: '提示',
    content: '确定要删除这条通知吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await notificationApi.deleteNotification(id)
          
          const index = notifications.value.findIndex(item => item.id === id)
          if (index > -1) {
            const item = notifications.value[index]
            if (!item.isRead) {
              unreadCount.value = Math.max(0, unreadCount.value - 1)
              notificationStore.setUnreadCount(unreadCount.value)
            }
            notifications.value.splice(index, 1)
          }
          
          uni.showToast({ title: '删除成功', icon: 'success' })
        } catch (error) {
          console.error('删除通知失败:', error)
        }
      }
    }
  })
}

// 格式化时间
const formatTime = (time) => {
  return formatRelativeTime(time)
}
</script>

<style scoped>
.notifications-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
}

.unread-count {
  font-size: 28rpx;
  color: #e34d59;
}

.mark-all-btn {
  padding: 8rpx 24rpx;
  color: #fff;
  border-radius: 8rpx;
  font-size: 24rpx;
  border: none;
  line-height: 1.5;
}

.notification-list {
  flex: 1;
  padding: 16rpx 0;
}

.notification-item {
  background: #fff;
  margin: 0 32rpx 16rpx;
  padding: 32rpx;
  border-radius: 12rpx;
}

.notification-item.unread {
  background: #f0f7ff;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.notification-title {
  flex: 1;
  font-size: 30rpx;
  font-weight: 700;
  color: #333;
}

.unread-dot {
  width: 16rpx;
  height: 16rpx;
  background: #e34d59;
  border-radius: 50%;
  margin-left: 16rpx;
}

.notification-content {
  display: block;
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
  margin-bottom: 16rpx;
}

.notification-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notification-time {
  font-size: 24rpx;
  color: #999;
}

.delete-btn {
  font-size: 24rpx;
  color: #e34d59;
}

.loading-more,
.no-more {
  padding: 32rpx;
  text-align: center;
  font-size: 24rpx;
  color: #999;
}

.empty-state {
  padding: 200rpx 32rpx;
  text-align: center;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}
</style>
