import { createRouter, createWebHistory } from 'vue-router'
import { setupRouterGuards } from './guards'

// 布局组件
import AuthLayout from '@/layouts/AuthLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import TeacherLayout from '@/layouts/TeacherLayout.vue'
import StudentLayout from '@/layouts/StudentLayout.vue'

const routes = [
  {
    path: '/',
    name: 'Landing',
    component: () => import('@/views/Landing.vue'),
    meta: { title: '首页', requiresAuth: false }
  },
  {
    path: '/auth',
    component: AuthLayout,
    redirect: '/auth/login',
    children: [
      {
        path: 'login',
        name: 'Login',
        component: () => import('@/views/auth/Login.vue'),
        meta: { title: '登录', requiresAuth: false }
      },
      {
        path: 'register',
        name: 'Register',
        component: () => import('@/views/auth/Register.vue'),
        meta: { title: '注册', requiresAuth: false }
      },
      {
        path: 'forgot-password',
        name: 'ForgotPassword',
        component: () => import('@/views/auth/ForgotPassword.vue'),
        meta: { title: '忘记密码', requiresAuth: false }
      },
      {
        path: 'reset-password',
        name: 'ResetPassword',
        component: () => import('@/views/auth/ResetPassword.vue'),
        meta: { title: '重置密码', requiresAuth: false }
      }
    ]
  },
  {
    path: '/reset-password',
    component: AuthLayout,
    children: [
      {
        path: '',
        name: 'ResetPasswordStandalone',
        component: () => import('@/views/auth/ResetPassword.vue'),
        meta: { title: '重置密码', requiresAuth: false }
      }
    ]
  },
  {
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/dashboard',
    meta: { requiresAuth: true, roles: ['ADMIN'] },
    children: [
      {
        path: 'dashboard',
        name: 'adminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '管理员仪表板', icon: 'Dashboard' }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/admin/UserManagement.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'activities',
        name: 'AdminActivityManagement',
        component: () => import('@/views/admin/ActivityManagement.vue'),
        meta: { title: '活动管理', icon: 'Calendar' }
      },
      {
        path: 'activities/create/:id?',
        name: 'AdminCreateActivity',
        component: () => import('@/views/admin/CreateActivity.vue'),
        meta: { title: '创建活动', icon: 'Plus' }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/admin/Statistics.vue'),
        meta: { title: '统计分析', icon: 'DataAnalysis' }
      },
      {
        path: 'records',
        name: 'AdminServiceRecordManagement',
        component: () => import('@/views/admin/ServiceRecordManagement.vue'),
        meta: { title: '服务记录管理', icon: 'Document' }
      },
      {
        path: 'points',
        name: 'AdminPointsManagement',
        component: () => import('@/views/admin/PointsManagement.vue'),
        meta: { title: '积分管理', icon: 'Star' }
      },
      {
        path: 'mall/products',
        name: 'ProductManagement',
        component: () => import('@/views/admin/ProductManagement.vue'),
        meta: { title: '商品管理', icon: 'Goods' }
      },
      {
        path: 'mall/categories',
        name: 'CategoryManagement',
        component: () => import('@/views/admin/CategoryManagement.vue'),
        meta: { title: '分类管理', icon: 'Menu' }
      },
      {
        path: 'mall/verify',
        name: 'RedemptionVerify',
        component: () => import('@/views/admin/RedemptionVerify.vue'),
        meta: { title: '兑换核销', icon: 'Finished' }
      },
      {
        path: 'mall/statistics',
        name: 'RedemptionStatistics',
        component: () => import('@/views/admin/RedemptionStatistics.vue'),
        meta: { title: '统计报表', icon: 'DataAnalysis' }
      },
      {
        path: 'activity-approval',
        name: 'ActivityApproval',
        component: () => import('@/views/admin/ActivityApproval.vue'),
        meta: { title: '活动审核', icon: 'CircleCheck' }
      },
      {
        path: 'signups',
        name: 'AdminSignupManagement',
        component: () => import('@/views/admin/SignupManagement.vue'),
        meta: { title: '报名审核', icon: 'UserFilled' }
      },
      {
        path: 'certificate-review',
        name: 'CertificateReview',
        component: () => import('@/views/admin/CertificateReview.vue'),
        meta: { title: '证书审核', icon: 'Medal' }
      },
    ]
  },
  {
    path: '/teacher',
    component: TeacherLayout,
    redirect: '/teacher/dashboard',
    meta: { requiresAuth: true, roles: ['TEACHER', 'ADMIN'] },
    children: [
      {
        path: 'dashboard',
        name: 'teacherDashboard',
        component: () => import('@/views/teacher/Dashboard.vue'),
        meta: { title: '教师仪表板', icon: 'Dashboard' }
      },
      {
        path: 'activities',
        name: 'MyActivities',
        component: () => import('@/views/teacher/MyActivities.vue'),
        meta: { title: '我的活动', icon: 'Calendar' }
      },
      {
        path: 'activities/create/:id?',
        name: 'CreateActivity',
        component: () => import('@/views/teacher/CreateActivity.vue'),
        meta: { title: '创建活动', icon: 'Plus' }
      },
      {
        path: 'activities/:id/qrcode',
        name: 'ActivityQrCode',
        component: () => import('@/views/teacher/ActivityQrCode.vue'),
        meta: { title: '活动二维码', icon: 'Postcard' }
      },
      {
        path: 'signups',
        name: 'SignupManagement',
        component: () => import('@/views/teacher/SignupManagement.vue'),
        meta: { title: '报名管理', icon: 'UserFilled' }
      },
      {
        path: 'records',
        name: 'ServiceRecords',
        component: () => import('@/views/teacher/ServiceRecords.vue'),
        meta: { title: '服务记录', icon: 'Document' }
      },
      {
        path: 'check',
        name: 'TeacherCheck',
        component: () => import('@/views/teacher/TeacherCheckView.vue'),
        meta: { title: '签到二维码', icon: 'Postcard' }
      },
      {
        path: 'review',
        name: 'TeacherReview',
        component: () => import('@/views/teacher/TeacherReviewView.vue'),
        meta: { title: '签退审核', icon: 'Memo' }
      },
      {
        path: 'certificates',
        name: 'CertificateApproval',
        component: () => import('@/views/teacher/CertificateApproval.vue'),
        meta: { title: '证明审核', icon: 'Medal' }
      },
      {
        path: 'points',
        name: 'TeacherPointsManagement',
        component: () => import('@/views/admin/PointsManagement.vue'),
        meta: { title: '积分管理', icon: 'Star' }
      }
    ]
  },
  {
    path: '/student',
    component: StudentLayout,
    redirect: '/student/dashboard',
    meta: { requiresAuth: true, roles: ['STUDENT', 'ADMIN'] },
    children: [
      {
        path: 'dashboard',
        name: 'studentDashboard',
        component: () => import('@/views/student/Dashboard.vue'),
        meta: { title: '学生仪表板', icon: 'Dashboard' }
      },
      {
        path: 'activities',
        name: 'Activities',
        component: () => import('@/views/student/Activities.vue'),
        meta: { title: '活动列表', icon: 'Calendar' }
      },
      {
        path: 'check',
        name: 'StudentCheck',
        component: () => import('@/views/student/StudentCheckView.vue'),
        meta: { title: '扫码签到', icon: 'Aim' }
      },
      {
        path: 'signups',
        name: 'MySignups',
        component: () => import('@/views/student/MySignups.vue'),
        meta: { title: '我的报名', icon: 'UserFilled' }
      },
      {
        path: 'records',
        name: 'MyRecords',
        component: () => import('@/views/student/MyRecords.vue'),
        meta: { title: '我的记录', icon: 'Document' }
      },
      {
        path: 'points',
        name: 'Points',
        component: () => import('@/views/student/Points.vue'),
        meta: { title: '积分中心', icon: 'Star' }
      },
      {
        path: 'certificates',
        name: 'Certificates',
        component: () => import('@/views/student/Certificates.vue'),
        meta: { title: '证明管理', icon: 'Medal' }
      },
      {
        path: 'mall',
        name: 'Mall',
        component: () => import('@/views/student/MallIndex.vue'),
        meta: { title: '积分商城', icon: 'ShoppingCart' }
      },
      {
        path: 'mall/product/:id',
        name: 'ProductDetail',
        component: () => import('@/views/student/ProductDetail.vue'),
        meta: { title: '商品详情' }
      },
      {
        path: 'mall/my-redemptions',
        name: 'MyRedemptions',
        component: () => import('@/views/student/MyRedemptions.vue'),
        meta: { title: '我的兑换', icon: 'List' }
      },
      {
        path: 'mall/voucher/:id',
        name: 'VoucherDetail',
        component: () => import('@/views/student/VoucherDetail.vue'),
        meta: { title: '兑换凭证' }
      }
    ]
  },
  // {
  //   path: '/system/status',
  //   name: 'SystemStatus',
  //   component: () => import('@/views/common/SystemStatus.vue'),
  //   meta: { title: '系统状态', requiresAuth: true, roles: ['ADMIN'] }
  // },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/common/Profile.vue'),
    meta: { title: '个人资料', requiresAuth: true }
  },
  {
    path: '/notifications',
    name: 'NotificationCenter',
    component: () => import('@/views/common/NotificationCenter.vue'),
    meta: { title: '通知中心', requiresAuth: true }
  },
  {
    path: '/activities/:id',
    name: 'ActivityDetail',
    component: () => import('@/views/common/ActivityDetail.vue'),
    meta: { title: '活动详情', requiresAuth: true }
  },
  {
    path: '/activities/:id/review',
    name: 'ActivityReview',
    component: () => import('@/views/teacher/ActivityReview.vue'),
    meta: { title: '活动审核', requiresAuth: true, roles: ['TEACHER', 'ADMIN'] }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/common/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 设置路由守卫
setupRouterGuards(router)

export default router
