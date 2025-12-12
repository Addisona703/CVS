# 教师端页面使用指南

## 概述

本文档介绍教师端页面的功能和使用方法。教师端包含7个核心页面，涵盖活动管理、报名审核、签到签退、证明审核等功能。

## 页面列表

### 1. 教师仪表板 (dashboard)
**路径**: `/pages/teacher/dashboard/dashboard`

**功能**:
- 显示统计数据（创建活动数、待审核报名数、待审核签退数）
- 提供快捷入口（我的活动、创建活动、报名管理、签到二维码）
- 显示最近创建的活动列表

**使用方法**:
```javascript
uni.navigateTo({
  url: '/pages/teacher/dashboard/dashboard'
})
```

### 2. 我的活动 (my-activities)
**路径**: `/pages/teacher/my-activities/my-activities`

**功能**:
- 显示教师创建的所有活动
- 支持按状态筛选（全部、草稿、已发布、进行中、已完成）
- 支持编辑、删除、发布操作
- 支持分页加载

**操作**:
- 点击活动卡片：查看活动详情
- 点击编辑按钮：跳转到编辑页面
- 点击删除按钮：删除活动（需确认）
- 点击发布按钮：发布草稿活动（需确认）
- 点击右下角FAB按钮：创建新活动

### 3. 创建活动 (create-activity)
**路径**: `/pages/teacher/create-activity/create-activity`

**功能**:
- 创建新活动或编辑已有活动
- 表单字段：标题、描述、地点、开始时间、结束时间、报名截止时间、最大人数、积分奖励、活动要求
- 支持保存草稿和直接发布

**表单验证**:
- 所有必填字段不能为空
- 结束时间必须晚于开始时间
- 报名截止时间必须早于开始时间
- 最大人数和积分必须为正数

**使用方法**:
```javascript
// 创建新活动
uni.navigateTo({
  url: '/pages/teacher/create-activity/create-activity'
})

// 编辑活动
uni.navigateTo({
  url: `/pages/teacher/create-activity/create-activity?id=${activityId}`
})
```

### 4. 报名管理 (signup-management)
**路径**: `/pages/teacher/signup-management/signup-management`

**功能**:
- 显示所有报名记录
- 支持按状态筛选（全部、待审核、已通过、已拒绝）
- 审核报名申请（通过/拒绝）
- 支持分页加载

**审核操作**:
- 通过：点击"通过"按钮，确认后通过报名
- 拒绝：点击"拒绝"按钮，输入拒绝原因后提交

### 5. 签到二维码 (check-qrcode)
**路径**: `/pages/teacher/check-qrcode/check-qrcode`

**功能**:
- 选择活动生成签到/签退二维码
- 二维码包含HMAC签名，防止伪造
- 二维码有效期5分钟
- 支持手动刷新和自动刷新（每5分钟）

**二维码数据结构**:
```javascript
{
  activityId: number,      // 活动ID
  type: string,            // CHECK_IN 或 CHECK_OUT
  timestamp: number,       // 生成时间戳
  expiresAt: number,       // 过期时间戳
  signature: string        // HMAC-SHA256签名
}
```

**安全特性**:
- 使用HMAC-SHA256签名验证二维码真实性
- 时效性控制（5分钟过期）
- 防止重放攻击

### 6. 签退审核 (review-signout)
**路径**: `/pages/teacher/review-signout/review-signout`

**功能**:
- 显示待审核的签退记录
- 显示学生签到签退时间和服务时长
- 支持添加教师评价
- 审核签退（通过/拒绝）

**审核流程**:
1. 查看学生签到签退时间
2. 计算服务时长
3. 输入教师评价（可选）
4. 点击"通过"或"拒绝"按钮
5. 通过后生成服务记录

### 7. 证明审核 (certificate-approval)
**路径**: `/pages/teacher/certificate-approval/certificate-approval`

**功能**:
- 显示待审核的证明申请
- 显示学生服务记录汇总（活动数、服务时长、累计积分）
- 审核证明申请（通过/拒绝）

**证明类型**:
- SERVICE_HOURS: 服务时长证明
- PARTICIPATION: 活动参与证明
- VOLUNTEER: 志愿者证明

**审核流程**:
1. 查看学生基本信息
2. 查看服务记录汇总
3. 查看申请说明
4. 点击"通过"或"拒绝"按钮
5. 拒绝时需输入拒绝原因

## API依赖

教师端页面依赖以下API服务：

- `@/api/activity`: 活动管理相关接口
- `@/api/signup`: 报名管理相关接口
- `@/api/check`: 签到签退相关接口
- `@/api/certificate`: 证明管理相关接口

## 组件依赖

教师端页面使用以下组件：

- `activity-card`: 活动卡片组件
- `signup-card`: 报名卡片组件
- `qr-display`: 二维码展示组件
- `loading-state`: 加载状态组件
- `empty-state`: 空状态组件

## 工具函数依赖

- `@/utils/format`: 格式化工具（日期时间、时长）
- `crypto-js`: 加密库（HMAC签名）

## 注意事项

1. **权限控制**: 所有教师端页面需要教师角色权限才能访问
2. **数据刷新**: 大部分页面支持下拉刷新功能
3. **分页加载**: 列表页面支持上拉加载更多
4. **表单验证**: 创建活动页面有完整的表单验证
5. **二维码安全**: 签到二维码包含签名和时效性验证
6. **审核操作**: 审核操作不可撤销，请谨慎操作

## 开发建议

1. 在开发环境中测试所有功能
2. 确保后端API接口已实现
3. 测试各种边界情况和异常场景
4. 优化页面加载性能
5. 添加适当的错误处理和用户提示

## 后续优化

1. 添加活动统计图表
2. 支持批量审核操作
3. 添加消息推送功能
4. 优化二维码生成性能
5. 添加离线缓存功能
