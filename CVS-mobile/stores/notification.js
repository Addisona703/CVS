import { defineStore } from 'pinia'

export const useNotificationStore = defineStore('notification', {
  state: () => ({
    notifications: [],
    unreadCount: 0
  }),
  
  getters: {
    unreadNotifications: (state) => {
      return state.notifications.filter(n => !n.isRead)
    },
    
    hasUnread: (state) => {
      return state.unreadCount > 0
    }
  },
  
  actions: {
    // 设置通知列表
    setNotifications(notifications) {
      this.notifications = notifications
      this.updateUnreadCount()
    },
    
    // 添加通知
    addNotification(notification) {
      this.notifications.unshift(notification)
      if (!notification.isRead) {
        this.unreadCount++
      }
    },
    
    // 标记为已读
    markAsRead(notificationId) {
      const notification = this.notifications.find(n => n.id === notificationId)
      if (notification && !notification.isRead) {
        notification.isRead = true
        this.unreadCount = Math.max(0, this.unreadCount - 1)
      }
    },
    
    // 标记全部为已读
    markAllAsRead() {
      this.notifications.forEach(n => {
        n.isRead = true
      })
      this.unreadCount = 0
    },
    
    // 删除通知
    deleteNotification(notificationId) {
      const index = this.notifications.findIndex(n => n.id === notificationId)
      if (index !== -1) {
        const notification = this.notifications[index]
        if (!notification.isRead) {
          this.unreadCount = Math.max(0, this.unreadCount - 1)
        }
        this.notifications.splice(index, 1)
      }
    },
    
    // 更新未读数量
    updateUnreadCount() {
      this.unreadCount = this.notifications.filter(n => !n.isRead).length
    },
    
    // 设置未读数量
    setUnreadCount(count) {
      this.unreadCount = count
    },
    
    // 清空通知
    clearNotifications() {
      this.notifications = []
      this.unreadCount = 0
    }
  }
})
