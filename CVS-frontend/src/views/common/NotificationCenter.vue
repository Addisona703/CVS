<template>
  <div class="notification-center">
    <div class="page-header">
      <h1 class="page-title">通知中心</h1>
      <p class="page-description">查看和管理您的所有通知消息</p>
    </div>

    <div class="notification-content">
      <!-- 筛选和操作栏 -->
      <div class="filter-bar">
        <div class="filter-left">
          <t-select v-model="filterType" placeholder="选择通知类型" clearable style="width: 180px"
            @change="handleFilterChange">
            <t-option label="全部类型" value="" />
            <t-option label="活动开始" value="ACTIVITY_START" />
            <t-option label="活动进行中" value="ACTIVITY_ONGOING" />
            <t-option label="活动结束" value="ACTIVITY_END" />
            <t-option label="报名待审核" value="REGISTRATION_PENDING" />
            <t-option label="报名审核通过" value="REGISTRATION_APPROVED" />
            <t-option label="报名审核拒绝" value="REGISTRATION_REJECTED" />
            <t-option label="签退待审核" value="CHECKOUT_PENDING" />
            <t-option label="签退审核通过" value="CHECKOUT_APPROVED" />
            <t-option label="签退审核拒绝" value="CHECKOUT_REJECTED" />
          </t-select>

          <t-select v-model="filterRead" placeholder="选择状态" clearable style="width: 120px" @change="handleFilterChange">
            <t-option label="全部" value="" />
            <t-option label="未读" :value="false" />
            <t-option label="已读" :value="true" />
          </t-select>
        </div>

        <div class="filter-right">
          <t-button v-if="unreadCount > 0" theme="primary" @click="markAllAsRead">
            全部标记为已读
          </t-button>
          <t-button theme="danger" variant="outline" @click="clearReadNotifications">
            清空已读通知
          </t-button>
        </div>
      </div>

      <!-- 通知列表 -->
      <div class="notification-list">
        <t-empty v-if="notifications.length === 0 && !loading" description="暂无通知消息" />

        <t-list v-else :split="true">
          <t-list-item v-for="item in notifications" :key="item.id">
            <template #content>
              <t-comment :avatar="getAvatar(item.type)" :author="getTypeLabel(item.type)"
                :datetime="formatTime(item.createdTime)" :content="item.content">
                <template #actions>
                  <t-space :size="6" @click="handleNotificationItemClick(item)">
                    <t-icon name="browse" />
                    <span>查看详情</span>
                  </t-space>
                  <t-space v-if="!item.isRead" :size="6" @click="markAsRead(item)">
                    <t-icon name="check-circle" />
                    <span>标记已读</span>
                  </t-space>
                  <t-space :size="6" @click="deleteNotification(item)">
                    <t-icon name="delete" />
                    <span>删除</span>
                  </t-space>
                </template>
              </t-comment>
              <div v-if="!item.isRead" class="unread-badge">
                <t-tag theme="primary" size="small">未读</t-tag>
              </div>
            </template>
          </t-list-item>
        </t-list>
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <t-pagination v-if="total > 0" v-model="currentPage" v-model:page-size="pageSize" :total="total"
          :page-size-options="[10, 20, 50, 100]" show-page-size show-jumper @change="handleCurrentChange"
          @page-size-change="handleSizeChange" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import { notificationAPI } from '@/api'
import { useNotificationNavigation } from '@/composables/useNotificationNavigation'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const { handleNotificationClick } = useNotificationNavigation()

// 响应式数据
const notifications = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterType = ref('')
const filterRead = ref('')

// 计算属性
const unreadCount = computed(() => {
  return notifications.value.filter(item => !item.isRead).length
})

// 获取通知列表
const fetchNotifications = async () => {
  loading.value = true
  try {
    // 检查网络状态
    if (!notificationAPI.checkNetworkStatus()) {
      notificationAPI.showNetworkError()
      return
    }

    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }

    if (filterType.value) {
      params.type = filterType.value
    }

    if (filterRead.value !== '') {
      params.isRead = filterRead.value
    }

    const response = await notificationAPI.getNotifications(params)
    if (response && response.code === 200) {
      notifications.value = response.data?.records || []
      total.value = response.data?.total || 0
    }
  } catch (error) {
    console.error('获取通知列表失败:', error)
    MessagePlugin.error('获取通知列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 处理筛选变化
const handleFilterChange = () => {
  currentPage.value = 1
  fetchNotifications()
}

// 处理分页变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchNotifications()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchNotifications()
}

// 通知点击处理
const handleNotificationItemClick = async (item) => {
  // 使用通知导航处理器
  await handleNotificationClick(item, async (notificationId) => {
    // 检查网络状态
    if (!notificationAPI.checkNetworkStatus()) {
      notificationAPI.showNetworkError()
      return
    }

    try {
      await notificationAPI.markAsRead(notificationId)
    } catch (error) {
      console.error('通过查看详情标记已读失败:', error)
      MessagePlugin.error('标记已读失败，请稍后重试')
      throw error
    }
  })
}

// 标记单个通知为已读
const markAsRead = async (item) => {
  try {
    // 检查网络状态
    if (!notificationAPI.checkNetworkStatus()) {
      notificationAPI.showNetworkError()
      return
    }

    await notificationAPI.markAsRead(item.id)
    item.isRead = true
    MessagePlugin.success('已标记为已读')
  } catch (error) {
    console.error('标记已读失败:', error)
    MessagePlugin.error('标记已读失败，请稍后重试')
  }
}

// 标记所有通知为已读
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
    MessagePlugin.success('已全部标记为已读')
  } catch (error) {
    console.error('标记全部已读失败:', error)
    MessagePlugin.error('标记全部已读失败，请稍后重试')
  }
}

// 删除通知
const deleteNotification = async (item) => {
  const dialog = DialogPlugin.confirm({
    header: '确认删除',
    body: '确定要删除这条通知吗？',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async () => {
      try {
        // 检查网络状态
        if (!notificationAPI.checkNetworkStatus()) {
          notificationAPI.showNetworkError()
          dialog.destroy()
          return
        }

        await notificationAPI.deleteNotification(item.id)

        // 从列表中移除
        const index = notifications.value.findIndex(n => n.id === item.id)
        if (index > -1) {
          notifications.value.splice(index, 1)
          total.value--
        }

        MessagePlugin.success('删除成功')
        dialog.destroy()
      } catch (error) {
        console.error('删除通知失败:', error)
        MessagePlugin.error('删除失败，请稍后重试')
        dialog.destroy()
      }
    },
    onCancel: () => {
      dialog.destroy()
    }
  })
}

// 清空已读通知
const clearReadNotifications = async () => {
  const dialog = DialogPlugin.confirm({
    header: '确认清空',
    body: '确定要清空所有已读通知吗？',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async () => {
      try {
        // 检查网络状态
        if (!notificationAPI.checkNetworkStatus()) {
          notificationAPI.showNetworkError()
          dialog.destroy()
          return
        }

        await notificationAPI.clearReadNotifications()

        // 重新获取通知列表
        fetchNotifications()

        MessagePlugin.success('清空成功')
        dialog.destroy()
      } catch (error) {
        console.error('清空已读通知失败:', error)
        MessagePlugin.error('清空失败，请稍后重试')
        dialog.destroy()
      }
    },
    onCancel: () => {
      dialog.destroy()
    }
  })
}

// 获取头像
const getAvatar = (type) => {
  // 根据通知类型返回不同的头像
  const avatarMap = {
    ACTIVITY_START: 'https://tdesign.gtimg.com/site/avatar.jpg',
    ACTIVITY_ONGOING: 'https://tdesign.gtimg.com/site/avatar.jpg',
    ACTIVITY_END: 'https://tdesign.gtimg.com/site/avatar.jpg',
    REGISTRATION_PENDING: 'https://tdesign.gtimg.com/site/avatar.jpg',
    REGISTRATION_APPROVED: 'https://tdesign.gtimg.com/site/avatar.jpg',
    REGISTRATION_REJECTED: 'https://tdesign.gtimg.com/site/avatar.jpg',
    CHECKOUT_PENDING: 'https://tdesign.gtimg.com/site/avatar.jpg',
    CHECKOUT_APPROVED: 'https://tdesign.gtimg.com/site/avatar.jpg',
    CHECKOUT_REJECTED: 'https://tdesign.gtimg.com/site/avatar.jpg'
  }
  return avatarMap[type] || 'https://tdesign.gtimg.com/site/avatar.jpg'
}

// 获取通知类型标签
const getTypeLabel = (type) => {
  const labelMap = {
    ACTIVITY_START: '活动开始',
    ACTIVITY_ONGOING: '活动进行中',
    ACTIVITY_END: '活动结束',
    REGISTRATION_PENDING: '报名待审核',
    REGISTRATION_APPROVED: '报名审核通过',
    REGISTRATION_REJECTED: '报名审核拒绝',
    CHECKOUT_PENDING: '签退待审核',
    CHECKOUT_APPROVED: '签退审核通过',
    CHECKOUT_REJECTED: '签退审核拒绝'
  }
  return labelMap[type] || '系统通知'
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).fromNow()
}

// 组件挂载时获取数据
onMounted(() => {
  fetchNotifications()
})
</script>

<style lang="scss" scoped>
.notification-center {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 32px;

  .page-title {
    font-size: 28px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 8px 0;
  }

  .page-description {
    font-size: 16px;
    color: #606266;
    margin: 0;
  }
}

.notification-content {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  height: 600px;
  display: flex;
  flex-direction: column;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
  background: #fafafa;

  .filter-left {
    display: flex;
    gap: 16px;
  }

  .filter-right {
    display: flex;
    gap: 12px;
  }
}

.notification-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 24px;
}

:deep(.t-list-item) {
  position: relative;

  .unread-badge {
    position: absolute;
    top: 16px;
    right: 16px;
  }

  .t-comment__actions {
    .t-space {
      cursor: pointer;
      transition: color 0.2s;

      &:hover {
        color: var(--td-brand-color);
      }
    }
  }
}

.pagination {
  padding: 20px 24px;
  display: flex;
  justify-content: center;
  border-top: 1px solid #ebeef5;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .notification-center {
    padding: 16px;
  }

  .filter-bar {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;

    .filter-left,
    .filter-right {
      justify-content: center;
    }
  }

  .notification-items .notification-item {
    padding: 16px;
    flex-direction: column;
    gap: 12px;

    .notification-actions {
      flex-direction: row;
      justify-content: center;
    }

    .unread-indicator {
      top: 16px;
      right: 16px;
    }
  }
}
</style>
