# API 接口对照表

本文档记录前端 API 与后端 Controller 的对应关系。

## 积分相关 API (PointsController)

| 前端 API | 后端接口 | 方法 | 说明 |
|---------|---------|------|------|
| `getCurrentUserPointsStats()` | `/api/points/stats` | GET | 获取当前用户积分统计 |
| `getAllPointsRecords(params)` | `/api/points/records` | POST | 获取积分明细（分页）|
| `getPointsRankingPage(page, size)` | `/api/points/ranking` | GET | 获取积分排行榜 |
| `awardPoints(userId, points)` | `/api/points/award` | POST | 奖励积分（管理员）|

## 统计相关 API (StatisticsController)

| 前端 API | 后端接口 | 方法 | 说明 | 返回字段 |
|---------|---------|------|------|---------|
| `getStudentDashboardStats()` | `/api/statistics/student-dashboard` | GET | 学生仪表板统计 | mySignupsCount, totalServiceHours, totalPoints, certificatesCount |
| `getTeacherDashboardStats()` | `/api/statistics/teacher-dashboard` | GET | 教师仪表板统计 | - |
| `getAdminDashboardStats()` | `/api/statistics/admin-dashboard` | GET | 管理员仪表板统计 | - |
| `getActivityStatistics(days)` | `/api/activities/statistics` | GET | 活动统计 | - |
| `getRedemptionStatistics(startDate, endDate)` | `/api/statistics/redemptions` | GET | 兑换统计 | - |
| `getProductRanking(limit)` | `/api/statistics/ranking` | GET | 商品排行榜 | - |
| `getLowStockProducts()` | `/api/statistics/low-stock` | GET | 低库存商品 | - |
| `exportRedemptions(startDate, endDate)` | `/api/statistics/export` | GET | 导出兑换统计 | - |

## 服务记录 API (RecordController)

| 前端 API | 后端接口 | 方法 | 说明 |
|---------|---------|------|------|
| `getAllServiceRecords(params)` | `/api/service-records/all` | POST | 所有服务记录（管理员）|
| `getMyServiceRecords(params)` | `/api/service-records/my` | POST | 我的服务记录（学生）|
| `getManagedServiceRecords(params)` | `/api/service-records/managed` | POST | 我管理的记录（教师）|
| `getServiceRecordById(id)` | `/api/service-records/{id}` | GET | 服务记录详情 |
| `getMyServiceStats()` | `/api/service-records/stats/my` | GET | 我的服务统计 |
| `getUserServiceStats(userId)` | `/api/service-records/stats/user/{userId}` | GET | 用户服务统计 |

## 活动相关 API (ActivityController)

| 前端 API | 后端接口 | 方法 | 说明 |
|---------|---------|------|------|
| `getActivityList(params)` | `/api/activities/search` | POST | 活动列表（分页）|
| `getActivityById(id)` | `/api/activities/{id}` | GET | 活动详情 |
| `createActivity(data)` | `/api/activities` | POST | 创建活动 |
| `updateActivity(id, data)` | `/api/activities/{id}` | PUT | 更新活动 |
| `deleteActivity(id)` | `/api/activities/{id}` | DELETE | 删除活动 |
| `publishActivity(id)` | `/api/activities/{id}/publish` | POST | 发布活动 |
| `cancelActivity(id)` | `/api/activities/{id}/cancel` | POST | 取消活动 |
| `getMyActivities(params)` | `/api/activities/my` | POST | 我的活动（教师）|
| `approveActivity(id, data)` | `/api/activities/{id}/approve` | POST | 审核活动（管理员）|
| `getActivityStatistics(days)` | `/api/activities/statistics` | GET | 活动统计 |

## 使用示例

### 学生仪表板

```javascript
import * as statisticsApi from '@/api/statistics'

// 获取学生统计数据
const stats = await statisticsApi.getStudentDashboardStats()
// 返回: { totalPoints, activityCount, totalHours, ... }
```

### 积分查询

```javascript
import * as pointsApi from '@/api/points'

// 获取当前用户积分统计
const pointsStats = await pointsApi.getCurrentUserPointsStats()
// 返回: { balance, rank, totalEarned, ... }

// 获取积分排行榜
const ranking = await pointsApi.getPointsRankingPage(1, 20)
// 返回: { records, total, current, size, pages }
```

### 服务记录

```javascript
import * as serviceRecordApi from '@/api/serviceRecord'

// 获取我的服务记录
const records = await serviceRecordApi.getMyServiceRecords({
  pageNum: 1,
  pageSize: 10
})

// 获取我的服务统计
const stats = await serviceRecordApi.getMyServiceStats()
// 返回: { totalHours, activityCount, averageRating, ... }
```

## 注意事项

1. **分页参数**: 后端使用 `pageNum` 和 `pageSize`，不是 `current` 和 `size`
2. **查询参数**: 复杂查询使用 POST 方法，参数放在 `params` 对象中
3. **认证**: 所有接口都需要 JWT token，会自动在请求拦截器中添加
4. **响应格式**: 后端统一返回 `{ code, message, data }` 格式，响应拦截器会自动提取 `data`

## 更新日志

- 2025-10-29: 修复积分、统计、服务记录 API 对接问题
- 2025-10-29: 更新学生仪表板使用正确的统计 API
