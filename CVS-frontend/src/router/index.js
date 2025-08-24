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
    redirect: '/auth/login'
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
        path: 'roles',
        name: 'RoleManagement',
        component: () => import('@/views/admin/RoleManagement.vue'),
        meta: { title: '角色管理', icon: 'User' }
      }
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
        path: 'activities/create',
        name: 'CreateActivity',
        component: () => import('@/views/teacher/CreateActivity.vue'),
        meta: { title: '创建活动', icon: 'Plus' }
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
        path: 'certificates',
        name: 'CertificateApproval',
        component: () => import('@/views/teacher/CertificateApproval.vue'),
        meta: { title: '证明审核', icon: 'Medal' }
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
    path: '/activities/:id',
    name: 'ActivityDetail',
    component: () => import('@/views/common/ActivityDetail.vue'),
    meta: { title: '活动详情', requiresAuth: true }
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
// setupRouterGuards(router)

export default router
