/**
 * 通知API服务
 * 提供系统通知相关的接口
 */
import http from '@/utils/request'

/**
 * 获取未读消息数量
 */
export const getUnreadCount = () => {
  return http.get('/notifications/unread-count')
}

/**
 * 获取消息列表（分页）
 */
export const getNotificationList = (params) => {
  return http.get('/notifications', params)
}

/**
 * 获取未读消息列表
 */
export const getUnreadNotifications = () => {
  return http.get('/notifications/unread')
}

/**
 * 标记消息已读
 */
export const markAsRead = (id) => {
  return http.put(`/notifications/${id}/read`)
}

/**
 * 标记所有消息已读
 */
export const markAllAsRead = () => {
  return http.put('/notifications/mark-all-read')
}

/**
 * 删除通知
 */
export const deleteNotification = (id) => {
  return http.delete(`/notifications/${id}`)
}

/**
 * 清空已读通知
 */
export const clearReadNotifications = () => {
  return http.delete('/notifications/clear-read')
}

/**
 * 批量删除通知
 */
export const batchDelete = (ids) => {
  return http.delete('/notifications/batch-delete', { data: { ids } })
}

/**
 * 获取通知详情
 */
export const getNotificationById = (id) => {
  return http.get(`/notifications/${id}`)
}
