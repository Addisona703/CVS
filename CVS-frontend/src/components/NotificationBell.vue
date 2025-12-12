<template>
  <el-popover placement="bottom" :width="380" trigger="click" @show="handleShow">
    <template #reference>
      <t-button variant="text" shape="square">
        <template #icon>
          <t-badge :count="unreadCount" :offset="[-2, 2]">
            <t-icon name="mail" />
          </t-badge>
        </template>
      </t-button>
    </template>

    <div class="notification-popover">
      <div class="notification-header">
        <span class="title">消息通知</span>
        <el-button v-if="unreadCount > 0" type="primary" text size="small" @click="markAllAsRead">
          全部已读
        </el-button>
      </div>

      <el-scrollbar max-height="400px">
        <div v-if="notifications.length === 0" class="empty-state">
          <el-empty description="暂无消息" :image-size="80" />
        </div>

        <div v-else class="notification-list">
          <div v-for="item in notifications" :key="item.id" class="notification-item" :class="{ unread: !item.isRead }"
            @click="handleClick(item)">
            <div class="notification-icon">
              <el-icon :color="getIconColor(item.type)">
                <component :is="getIcon(item.type)" />
              </el-icon>
            </div>
            <div class="notification-content">
              <div class="notification-title">{{ item.title }}</div>
              <div class="notification-text">{{ item.content }}</div>
              <div class="notification-time">{{ formatTime(item.createdTime) }}</div>
            </div>
            <div v-if="!item.isRead" class="unread-dot"></div>
          </div>
        </div>
      </el-scrollbar>

      <div class="notification-footer">
        <el-button type="primary" text @click="goToNotificationCenter">
          查看全部
        </el-button>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Calendar,
  CircleCheck,
  CircleClose,
  Warning
} from '@element-plus/icons-vue'
import { notificationAPI } from '@/api'
import { useNotificationNavigation } from '@/composables/useNotificationNavigation'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const { handleNotificationClick } = useNotificationNavigation()
const notifications = ref([])
const unreadCount = ref(0)
const pollingTimer = ref(null)
const isPageActive = ref(true)

const fetchUnreadCount = async () => {
  try {
    // 检查网络状态
    if (!notificationAPI.checkNetworkStatus()) {
      console.warn('网络连接已断开，跳过轮询')
      return
    }

    const response = await notificationAPI.getUnreadCountWithRetry()
    if (response && response.code === 200) {
      unreadCount.value = response.data || 0
    }
  } catch (error) {
    console.error('获取未读消息数量失败:', error)
    // 轮询失败时不显示错误消息，避免干扰用户
  }
}

const fetchNotifications = async () => {
  try {
    // 检查网络状态
    if (!notificationAPI.checkNetworkStatus()) {
      notificationAPI.showNetworkError()
      return
    }

    // 获取最近5条通知（包括已读和未读）
    const response = await notificationAPI.getNotifications({
      pageNum: 1,
      pageSize: 5
    })
    if (response && response.code === 200) {
      notifications.value = response.data?.records || []
    }
  } catch (error) {
    console.error('获取消息列表失败:', error)
    ElMessage.error('获取消息列表失败，请稍后重试')
  }
}

const handleShow = () => {
  fetchNotifications()
}

const handleClick = async (item) => {
  // 使用通知导航处理器
  await handleNotificationClick(item, async (notificationId) => {
    // 检查网络状态
    if (!notificationAPI.checkNetworkStatus()) {
      notificationAPI.showNetworkError()
      return
    }

    await notificationAPI.markAsRead(notificationId)
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  })
}

const markAllAsRead = async () => {
  try {
    // 检查网络状态
    if (!notificationAPI.checkNetworkStatus()) {
      notificationAPI.showNetworkError()
      return
    }

    await notificationAPI.markAllAsRead()
    notifications.value.forEach(item => {
      item.isRead = true
    })
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    console.error('标记全部已读失败:', error)
    ElMessage.error('标记全部已读失败，请稍后重试')
  }
}

const goToNotificationCenter = () => {
  router.push('/notifications')
}

const getIcon = (type) => {
  const iconMap = {
    ACTIVITY_START: Calendar,
    ACTIVITY_ONGOING: Calendar,
    ACTIVITY_END: Calendar,
    REGISTRATION_PENDING: Warning,
    REGISTRATION_APPROVED: CircleCheck,
    REGISTRATION_REJECTED: CircleClose,
    CHECKOUT_PENDING: Warning,
    CHECKOUT_APPROVED: CircleCheck,
    CHECKOUT_REJECTED: CircleClose
  }
  return iconMap[type] || Calendar
}

const getIconColor = (type) => {
  const colorMap = {
    ACTIVITY_START: '#409eff',
    ACTIVITY_ONGOING: '#67c23a',
    ACTIVITY_END: '#67c23a',
    REGISTRATION_PENDING: '#e6a23c',
    REGISTRATION_APPROVED: '#67c23a',
    REGISTRATION_REJECTED: '#f56c6c',
    CHECKOUT_PENDING: '#e6a23c',
    CHECKOUT_APPROVED: '#67c23a',
    CHECKOUT_REJECTED: '#f56c6c'
  }
  return colorMap[type] || '#909399'
}

const formatTime = (time) => {
  return dayjs(time).fromNow()
}

// 页面激活状态检测
const handleVisibilityChange = () => {
  isPageActive.value = !document.hidden

  if (isPageActive.value) {
    // 页面激活时立即获取未读数量并重启轮询
    fetchUnreadCount()
    startPolling()
  } else {
    // 页面非激活时停止轮询
    stopPolling()
  }
}

const startPolling = () => {
  stopPolling() // 先清除现有的定时器

  if (isPageActive.value) {
    pollingTimer.value = setInterval(() => {
      if (isPageActive.value) {
        fetchUnreadCount()
      }
    }, 30000) // 30秒轮询
  }
}

const stopPolling = () => {
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value)
    pollingTimer.value = null
  }
}

onMounted(() => {
  // 初始化获取未读数量
  fetchUnreadCount()

  // 监听页面可见性变化
  document.addEventListener('visibilitychange', handleVisibilityChange)

  // 开始轮询
  startPolling()
})

onUnmounted(() => {
  // 清理定时器和事件监听器
  stopPolling()
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style lang="scss" scoped>
.notification-popover {
  padding: 0;

  .notification-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: var(--space-3, 12px) var(--space-4, 16px);
    border-bottom: 1px solid var(--border-color-lighter, #ebeef5);

    .title {
      font-size: var(--font-size-large, 16px);
      font-weight: 600;
      color: var(--text-color-primary, #303133);
    }

    :deep(.el-button.is-text) {
      &:hover,
      &:focus {
        background-color: transparent !important;
        color: var(--primary-color, #409eff) !important;
      }
    }
  }

  .empty-state {
    padding: var(--space-10, 40px) 0;
  }

  .notification-list {
    .notification-item {
      position: relative;
      display: flex;
      gap: var(--space-3, 12px);
      padding: var(--space-3, 12px) var(--space-4, 16px);
      cursor: pointer;
      transition: background-color var(--transition-duration-fast, 0.2s) var(--transition-timing, ease);
      border-bottom: 1px solid var(--border-color-extra-light, #f2f6fc);

      &:last-child {
        border-bottom: none;
      }

      &:hover {
        background-color: var(--bg-color-hover, #f5f7fa);
      }

      &.unread {
        background-color: var(--notification-unread-bg, #ecf5ff);

        &:hover {
          background-color: var(--notification-unread-bg-hover, #d9ecff);
        }

        .notification-title {
          font-weight: 700;
        }
      }

      .notification-icon {
        flex-shrink: 0;
        width: var(--space-10, 40px);
        height: var(--space-10, 40px);
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: var(--bg-color-hover, #f5f7fa);
        border-radius: 50%;

        :deep(svg) {
          font-size: var(--icon-size-small, 18px);
        }
      }

      .notification-content {
        flex: 1;
        min-width: 0;

        .notification-title {
          font-size: var(--font-size-base, 14px);
          font-weight: 600;
          color: var(--text-color-primary, #303133);
          margin-bottom: var(--space-1, 4px);
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .notification-text {
          font-size: var(--font-size-small, 13px);
          color: var(--text-color-regular, #606266);
          line-height: 1.4;
          margin-bottom: var(--space-1, 4px);
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          line-clamp: 2;
          -webkit-box-orient: vertical;
        }

        .notification-time {
          font-size: var(--font-size-xs, 12px);
          color: var(--text-color-secondary, #909399);
        }
      }

      .unread-dot {
        position: absolute;
        top: var(--space-4, 16px);
        right: var(--space-4, 16px);
        width: var(--space-2, 8px);
        height: var(--space-2, 8px);
        background-color: var(--primary-color, #409eff);
        border-radius: 50%;
        flex-shrink: 0;
      }
    }
  }

  .notification-footer {
    padding: var(--space-2, 8px) var(--space-4, 16px);
    border-top: 1px solid var(--border-color-lighter, #ebeef5);
    text-align: center;

    :deep(.el-button.is-text) {
      &:hover,
      &:focus {
        background-color: transparent !important;
        color: var(--primary-color, #409eff) !important;
      }
    }
  }
}
</style>
