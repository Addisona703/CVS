// 用户角色定义
export const USER_ROLES = {
  ADMIN: 'ADMIN',
  TEACHER: 'TEACHER',
  STUDENT: 'STUDENT'
}

// 角色权限映射
export const ROLE_PERMISSIONS = {
  [USER_ROLES.ADMIN]: [
    'user:read', 'user:write', 'user:delete',
    'activity:read', 'activity:write', 'activity:delete',
    'signup:read', 'signup:write', 'signup:approve',
    'record:read', 'record:write', 'record:delete',
    'points:read', 'points:write',
    'certificate:read', 'certificate:approve',
    'statistics:read'
  ],
  [USER_ROLES.TEACHER]: [
    'activity:read', 'activity:write',
    'signup:read', 'signup:approve',
    'record:read', 'record:write',
    'certificate:read'
  ],
  [USER_ROLES.STUDENT]: [
    'activity:read',
    'signup:write',
    'record:read',
    'points:read',
    'certificate:write'
  ]
}

// 活动状态
export const ACTIVITY_STATUS = {
  DRAFT: 'DRAFT',
  PENDING_APPROVAL: 'PENDING_APPROVAL',
  PUBLISHED: 'PUBLISHED',
  ONGOING: 'ONGOING',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
  REJECTED: 'REJECTED'
}

// 报名状态
export const SIGNUP_STATUS = {
  PENDING: 'PENDING',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED'
}

// 证明状态
export const CERTIFICATE_STATUS = {
  PENDING: 'PENDING',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED'
}

// 状态标签映射
export const STATUS_LABELS = {
  [ACTIVITY_STATUS.DRAFT]: '草稿',
  [ACTIVITY_STATUS.PENDING_APPROVAL]: '待审核',
  [ACTIVITY_STATUS.PUBLISHED]: '已发布',
  [ACTIVITY_STATUS.ONGOING]: '进行中',
  [ACTIVITY_STATUS.COMPLETED]: '已完成',
  [ACTIVITY_STATUS.CANCELLED]: '已取消',
  [ACTIVITY_STATUS.REJECTED]: '审核拒绝',
  [SIGNUP_STATUS.PENDING]: '待审核',
  [SIGNUP_STATUS.APPROVED]: '已通过',
  [SIGNUP_STATUS.REJECTED]: '已拒绝',
  [CERTIFICATE_STATUS.PENDING]: '待审核',
  [CERTIFICATE_STATUS.APPROVED]: '已通过',
  [CERTIFICATE_STATUS.REJECTED]: '已拒绝'
}

// 状态颜色映射
export const STATUS_COLORS = {
  [ACTIVITY_STATUS.DRAFT]: 'info',
  [ACTIVITY_STATUS.PENDING_APPROVAL]: 'warning',
  [ACTIVITY_STATUS.PUBLISHED]: 'success',
  [ACTIVITY_STATUS.ONGOING]: 'warning',
  [ACTIVITY_STATUS.COMPLETED]: 'info',
  [ACTIVITY_STATUS.CANCELLED]: 'danger',
  [ACTIVITY_STATUS.REJECTED]: 'danger',
  [SIGNUP_STATUS.PENDING]: 'warning',
  [SIGNUP_STATUS.APPROVED]: 'success',
  [SIGNUP_STATUS.REJECTED]: 'danger',
  [CERTIFICATE_STATUS.PENDING]: 'warning',
  [CERTIFICATE_STATUS.APPROVED]: 'success',
  [CERTIFICATE_STATUS.REJECTED]: 'danger'
}

// 角色标签映射
export const ROLE_LABELS = {
  [USER_ROLES.ADMIN]: '管理员',
  [USER_ROLES.TEACHER]: '教师',
  [USER_ROLES.STUDENT]: '学生'
}

// 分页默认配置
export const PAGINATION_CONFIG = {
  PAGE_SIZE: 10,
  PAGE_SIZES: [10, 20, 50, 100]
}

// 日期格式
export const DATE_FORMATS = {
  DATE: 'YYYY-MM-DD',
  DATETIME: 'YYYY-MM-DD HH:mm',
  TIME: 'HH:mm'
}
