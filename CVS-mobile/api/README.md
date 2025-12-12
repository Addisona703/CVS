# API服务层

本目录包含所有与后端API交互的服务模块。每个模块负责特定功能域的API调用。

## 目录结构

```
api/
├── auth.js           # 认证服务（登录、注册、密码重置等）
├── activity.js       # 活动服务（活动CRUD、发布等）
├── signup.js         # 报名服务（报名、取消、审核等）
├── check.js          # 签到服务（签到、签退、二维码等）
├── serviceRecord.js  # 服务记录服务
├── points.js         # 积分服务（余额、明细、排行榜等）
├── mall.js           # 商城服务（商品、兑换、分类等）
├── certificate.js    # 证明服务（申请、审核、下载等）
├── user.js           # 用户服务（用户信息、管理等）
├── statistics.js     # 统计服务（各类统计数据）
├── notification.js   # 通知服务（通知列表、已读等）
└── index.js          # 统一导出
```

## 使用方法

### 1. 导入API服务

```javascript
// 导入单个服务
import { authApi } from '@/api/auth'
import { activityApi } from '@/api/activity'

// 或从统一导出文件导入
import { authApi, activityApi } from '@/api'
```

### 2. 调用API

所有API方法都返回Promise，可以使用async/await或.then()/.catch()处理：

```javascript
// 使用 async/await
async function login() {
  try {
    const response = await authApi.login({
      username: 'student1',
      password: 'password123'
    })
    console.log('登录成功:', response)
  } catch (error) {
    console.error('登录失败:', error)
  }
}

// 使用 .then()/.catch()
authApi.login({ username: 'student1', password: 'password123' })
  .then(response => {
    console.log('登录成功:', response)
  })
  .catch(error => {
    console.error('登录失败:', error)
  })
```

### 3. 在Store中使用

```javascript
// stores/auth.js
import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  actions: {
    async login(credentials) {
      const response = await authApi.login(credentials)
      this.token = response.token
      this.userInfo = response.userInfo
      return response
    }
  }
})
```

### 4. 在页面中使用

```javascript
// pages/index/index.vue
<script>
import { authApi } from '@/api/auth'

export default {
  methods: {
    async handleLogin() {
      try {
        uni.showLoading({ title: '登录中...' })
        
        const response = await authApi.login({
          username: this.username,
          password: this.password
        })
        
        // 保存token和用户信息
        uni.setStorageSync('token', response.token)
        uni.setStorageSync('userInfo', response.userInfo)
        
        uni.showToast({
          title: '登录成功',
          icon: 'success'
        })
        
        // 跳转到首页
        uni.switchTab({ url: '/pages/student-sub/dashboard/dashboard' })
        
      } catch (error) {
        uni.showToast({
          title: error.message || '登录失败',
          icon: 'none'
        })
      } finally {
        uni.hideLoading()
      }
    }
  }
}
</script>
```

## API服务说明

### authApi - 认证服务

- `login(data)` - 用户登录
- `register(data)` - 用户注册
- `sendCode(data)` - 发送验证码
- `resetPassword(data)` - 重置密码
- `refreshToken(data)` - 刷新令牌
- `logout()` - 退出登录

### activityApi - 活动服务

- `getActivityList(params)` - 获取活动列表（分页）
- `getActivityById(id)` - 获取活动详情
- `createActivity(data)` - 创建活动
- `updateActivity(data)` - 更新活动
- `deleteActivity(id)` - 删除活动
- `publishActivity(id)` - 发布活动
- `cancelActivity(id)` - 取消活动
- `getMyActivities(params)` - 获取我的活动列表

### signupApi - 报名服务

- `createSignup(data)` - 创建报名
- `cancelSignup(id)` - 取消报名
- `getSignupList(params)` - 获取报名列表
- `getMySignups(params)` - 获取我的报名列表
- `approveSignup(data)` - 审核报名
- `batchApprove(data)` - 批量审核报名
- `getSignupById(id)` - 获取报名详情

### checkApi - 签到服务

- `checkIn(data)` - 签到
- `checkOut(data)` - 签退
- `getCheckStatus(activityId)` - 获取签到状态
- `generateQrCode(data)` - 生成签到二维码
- `validateQrCode(data)` - 验证二维码
- `getPendingCheckouts(params)` - 获取待审核签退列表
- `approveCheckout(data)` - 审核签退

### serviceRecordApi - 服务记录服务

- `getServiceRecordList(params)` - 获取服务记录列表
- `getServiceRecordById(id)` - 获取服务记录详情
- `getMyServiceRecords(params)` - 获取我的服务记录
- `getServiceRecordStats(params)` - 获取服务记录统计
- `exportServiceRecords(params)` - 导出服务记录

### pointsApi - 积分服务

- `getPointsBalance(userId)` - 获取积分余额
- `getPointsRecords(params)` - 获取积分明细
- `getPointsRanking(params)` - 获取积分排行榜
- `adjustPoints(data)` - 调整积分（管理员）
- `getPointsStats(params)` - 获取积分统计

### mallApi - 商城服务

- `getProductList(params)` - 获取商品列表
- `getProductById(id)` - 获取商品详情
- `createProduct(data)` - 创建商品（管理员）
- `updateProduct(data)` - 更新商品（管理员）
- `deleteProduct(id)` - 删除商品（管理员）
- `updateProductStatus(data)` - 上下架商品（管理员）
- `createRedemption(data)` - 创建兑换
- `getRedemptionList(params)` - 获取兑换列表
- `getMyRedemptions(params)` - 获取我的兑换列表
- `getRedemptionById(id)` - 获取兑换详情
- `verifyRedemption(data)` - 核销兑换（管理员）
- `getCategoryList()` - 获取商品分类列表
- `createCategory(data)` - 创建分类（管理员）
- `updateCategory(data)` - 更新分类（管理员）
- `deleteCategory(id)` - 删除分类（管理员）

### certificateApi - 证明服务

- `createCertificate(data)` - 申请证明
- `getCertificateList(params)` - 获取证明列表
- `getMyCertificates(params)` - 获取我的证明列表
- `getCertificateById(id)` - 获取证明详情
- `approveCertificate(data)` - 审核证明
- `downloadCertificate(id)` - 下载证明文件
- `getPendingCertificates(params)` - 获取待审核证明列表

### userApi - 用户服务

- `getUserInfo(userId)` - 获取用户信息
- `updateUserInfo(data)` - 更新用户信息
- `changePassword(data)` - 修改密码
- `getUserList(params)` - 获取用户列表（管理员）
- `createUser(data)` - 创建用户（管理员）
- `updateUser(data)` - 更新用户（管理员）
- `deleteUser(id)` - 删除用户（管理员）
- `resetUserPassword(data)` - 重置用户密码（管理员）
- `uploadAvatar(file)` - 上传头像

### statisticsApi - 统计服务

- `getStatisticsOverview()` - 获取统计概览
- `getActivityStatistics(params)` - 获取活动统计
- `getUserStatistics(params)` - 获取用户统计
- `getRedemptionStatistics(params)` - 获取兑换统计
- `getServiceTimeStatistics(params)` - 获取服务时长统计
- `getPointsStatistics(params)` - 获取积分统计
- `exportStatistics(params)` - 导出统计报表

### notificationApi - 通知服务

- `getNotificationList(params)` - 获取通知列表
- `getUnreadCount()` - 获取未读通知数量
- `markAsRead(id)` - 标记为已读
- `markAllAsRead()` - 标记全部为已读
- `deleteNotification(id)` - 删除通知
- `batchDelete(ids)` - 批量删除通知
- `getNotificationById(id)` - 获取通知详情

## 错误处理

所有API调用的错误都会被统一的错误处理器捕获和处理。错误处理器位于 `utils/request.js` 中。

常见错误码：
- `401` - 未授权，需要重新登录
- `403` - 权限不足
- `404` - 资源不存在
- `409` - 冲突（如重复报名）
- `500` - 服务器错误

## 注意事项

1. 所有API调用都会自动添加认证token（在请求拦截器中处理）
2. 所有API响应都会经过响应拦截器处理，自动提取data字段
3. 网络错误和业务错误都会被统一处理并显示提示
4. 支持请求重试机制（网络错误时自动重试）
5. 所有接口都支持取消请求（防止重复请求）

## 开发建议

1. 在开发新功能时，先在对应的API服务文件中添加接口方法
2. 为每个接口方法添加详细的JSDoc注释
3. 在Store中调用API，不要在页面中直接调用
4. 使用try-catch处理异步错误
5. 合理使用loading状态提示用户
