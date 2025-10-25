// API Mock 数据
export const mockNotifications = [
  {
    id: 1,
    type: 'REGISTRATION_PENDING',
    title: '新的报名申请',
    content: '学生张三申请参加"环保志愿活动"',
    linkUrl: '/activities/1/signups',
    isRead: false,
    createdTime: '2024-12-18 09:30:00'
  },
  {
    id: 2,
    type: 'CHECKOUT_PENDING',
    title: '签退审核申请',
    content: '学生李四提交了签退申请',
    linkUrl: '/activities/2/checkouts',
    isRead: false,
    createdTime: '2024-12-18 10:00:00'
  },
  {
    id: 3,
    type: 'ACTIVITY_START',
    title: '活动即将开始',
    content: '您组织的"社区服务活动"将在30分钟后开始',
    linkUrl: '/activities/3',
    isRead: true,
    createdTime: '2024-12-18 08:30:00'
  },
  {
    id: 4,
    type: 'REGISTRATION_APPROVED',
    title: '报名审核通过',
    content: '您的"环保志愿活动"报名申请已通过',
    linkUrl: '/activities/1',
    isRead: false,
    createdTime: '2024-12-18 10:05:00'
  },
  {
    id: 5,
    type: 'REGISTRATION_REJECTED',
    title: '报名审核拒绝',
    content: '很抱歉，您的"图书馆志愿服务"报名申请未通过',
    linkUrl: '/activities/2',
    isRead: true,
    createdTime: '2024-12-18 11:05:00'
  }
]

export const mockApiResponses = {
  getUnreadCount: {
    code: 200,
    message: '成功',
    data: 3
  },
  getNotifications: {
    code: 200,
    message: '成功',
    data: {
      records: mockNotifications,
      total: mockNotifications.length,
      current: 1,
      size: 10,
      pages: 1
    }
  },
  markAsRead: {
    code: 200,
    message: '操作成功'
  },
  markAllAsRead: {
    code: 200,
    message: '操作成功'
  },
  deleteNotification: {
    code: 200,
    message: '删除成功'
  },
  clearReadNotifications: {
    code: 200,
    message: '清空成功'
  }
}

// Mock API 服务
export const createMockNotificationService = () => {
  return {
    getUnreadCount: vi.fn().mockResolvedValue(mockApiResponses.getUnreadCount),
    getNotifications: vi.fn().mockResolvedValue(mockApiResponses.getNotifications),
    markAsRead: vi.fn().mockResolvedValue(mockApiResponses.markAsRead),
    markAllAsRead: vi.fn().mockResolvedValue(mockApiResponses.markAllAsRead),
    deleteNotification: vi.fn().mockResolvedValue(mockApiResponses.deleteNotification),
    clearReadNotifications: vi.fn().mockResolvedValue(mockApiResponses.clearReadNotifications)
  }
}