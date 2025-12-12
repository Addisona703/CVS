# 组合式函数（Composables）

本目录包含可复用的组合式函数，用于封装常见的业务逻辑和功能。

## 目录结构

```
composables/
├── usePermission.js  # 权限控制
├── usePagination.js  # 分页加载
├── useQrCode.js      # 二维码处理
├── useForm.js        # 表单处理
└── index.js          # 统一导出
```

## 使用方法

### 1. usePermission - 权限控制

用于检查用户角色和权限。

```javascript
import { usePermission } from '@/composables/usePermission'

export default {
  setup() {
    const {
      isStudent,
      isTeacher,
      isAdmin,
      hasRole,
      canAccess,
      canEdit,
      requireLogin
    } = usePermission()

    // 检查是否是学生
    if (isStudent.value) {
      console.log('当前用户是学生')
    }

    // 检查是否有特定角色
    if (hasRole('TEACHER')) {
      console.log('当前用户是教师')
    }

    // 检查是否可以访问资源
    const canAccessResource = canAccess({
      roles: ['TEACHER', 'ADMIN'],
      condition: (user) => user.verified === true
    })

    // 检查是否可以编辑资源
    const activity = { organizerId: 123 }
    if (canEdit(activity)) {
      console.log('可以编辑该活动')
    }

    // 要求登录
    const handleAction = () => {
      if (!requireLogin()) {
        return
      }
      // 执行需要登录的操作
    }

    return {
      isStudent,
      canAccessResource,
      handleAction
    }
  }
}
```

**API:**
- `currentRole` - 当前用户角色
- `isStudent` - 是否是学生
- `isTeacher` - 是否是教师
- `isAdmin` - 是否是学工处
- `hasRole(roles)` - 检查是否拥有指定角色
- `canAccess(options)` - 检查是否可以访问资源
- `canEdit(resource)` - 检查是否可以编辑资源
- `canDelete(resource)` - 检查是否可以删除资源
- `canApprove()` - 检查是否可以审核
- `canManageUsers()` - 检查是否可以管理用户
- `canViewStatistics()` - 检查是否可以查看统计
- `requireLogin()` - 要求登录
- `requireRole(roles, message)` - 要求特定角色

### 2. usePagination - 分页加载

用于实现列表的分页加载、下拉刷新、上拉加载更多。

```javascript
import { usePagination } from '@/composables/usePagination'
import { activityApi } from '@/api/activity'

export default {
  setup() {
    const {
      list,
      loading,
      refreshing,
      finished,
      isEmpty,
      refresh,
      loadMore,
      updateParams,
      removeItem
    } = usePagination(activityApi.getActivityList, {
      pageSize: 20,
      params: { status: 'PUBLISHED' },
      immediate: true
    })

    // 下拉刷新
    const handleRefresh = async () => {
      await refresh()
    }

    // 上拉加载更多
    const handleLoadMore = async () => {
      await loadMore()
    }

    // 更新查询参数
    const handleSearch = async (keyword) => {
      await updateParams({ title: keyword })
    }

    // 删除项目
    const handleDelete = (id) => {
      removeItem(id)
    }

    return {
      list,
      loading,
      refreshing,
      finished,
      isEmpty,
      handleRefresh,
      handleLoadMore,
      handleSearch,
      handleDelete
    }
  }
}
```

**API:**
- `list` - 数据列表
- `loading` - 加载状态
- `refreshing` - 刷新状态
- `finished` - 是否已加载完成
- `error` - 错误信息
- `page` - 当前页码
- `total` - 总记录数
- `totalPages` - 总页数
- `hasMore` - 是否有更多数据
- `isEmpty` - 是否为空
- `loadData(isRefresh)` - 加载数据
- `refresh()` - 刷新数据
- `loadMore()` - 加载更多
- `reset()` - 重置状态
- `updateParams(newParams)` - 更新查询参数
- `removeItem(id, idField)` - 删除列表项
- `updateItem(id, data, idField)` - 更新列表项
- `prependItem(item)` - 添加到开头
- `appendItem(item)` - 添加到末尾

### 3. useQrCode - 二维码处理

用于生成、扫描、验证二维码。

```javascript
import { useQrCode } from '@/composables/useQrCode'

export default {
  setup() {
    const {
      qrCodeContent,
      qrCodeUrl,
      loading,
      generateQrCode,
      scanQrCode,
      scanAndCheckIn,
      scanAndCheckOut
    } = useQrCode()

    // 生成签到二维码
    const handleGenerateQrCode = async () => {
      await generateQrCode({
        activityId: 123,
        type: 'CHECKIN'
      })
    }

    // 扫描二维码
    const handleScan = async () => {
      try {
        const result = await scanQrCode()
        console.log('扫描结果:', result)
      } catch (error) {
        console.error('扫描失败:', error)
      }
    }

    // 扫码签到
    const handleCheckIn = async () => {
      try {
        const result = await scanAndCheckIn(123)
        if (result) {
          console.log('签到成功')
        }
      } catch (error) {
        console.error('签到失败:', error)
      }
    }

    // 扫码签退
    const handleCheckOut = async () => {
      try {
        const result = await scanAndCheckOut(123)
        if (result) {
          console.log('签退成功')
        }
      } catch (error) {
        console.error('签退失败:', error)
      }
    }

    return {
      qrCodeContent,
      qrCodeUrl,
      loading,
      handleGenerateQrCode,
      handleScan,
      handleCheckIn,
      handleCheckOut
    }
  }
}
```

**API:**
- `qrCodeContent` - 二维码内容
- `qrCodeUrl` - 二维码图片URL
- `scanResult` - 扫描结果
- `loading` - 加载状态
- `generateQrCode(options)` - 生成二维码
- `scanQrCode(options)` - 扫描二维码
- `validateQrCode(qrCode)` - 验证二维码
- `scanAndCheckIn(activityId)` - 扫码并签到
- `scanAndCheckOut(activityId)` - 扫码并签退
- `scanAndVerifyRedemption()` - 扫码核销兑换
- `clearQrCode()` - 清除二维码数据

### 4. useForm - 表单处理

用于表单验证、防重复提交、表单重置。

```javascript
import { useForm, createFormRules } from '@/composables/useForm'

export default {
  setup() {
    // 定义表单规则
    const rules = createFormRules({
      username: {
        required: true,
        minLength: 3,
        maxLength: 20,
        message: '用户名长度为3-20个字符'
      },
      email: {
        required: true,
        type: 'email',
        message: '请输入正确的邮箱地址'
      },
      password: {
        required: true,
        type: 'password'
      },
      phone: {
        required: true,
        type: 'phone'
      },
      age: {
        required: true,
        type: 'number',
        min: 18,
        max: 100,
        message: '年龄必须在18-100之间'
      },
      confirmPassword: {
        required: true,
        validator: (value, formData) => {
          if (value !== formData.password) {
            return '两次密码输入不一致'
          }
          return true
        }
      }
    })

    // 初始化表单
    const {
      formData,
      errors,
      submitting,
      canSubmit,
      validateField,
      handleSubmit,
      resetForm
    } = useForm(
      {
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
        phone: '',
        age: ''
      },
      rules
    )

    // 字段失焦时验证
    const handleBlur = (field) => {
      validateField(field)
    }

    // 提交表单
    const onSubmit = async () => {
      await handleSubmit(async (values) => {
        // 调用API提交数据
        console.log('提交数据:', values)
        await authApi.register(values)
        
        uni.showToast({
          title: '注册成功',
          icon: 'success'
        })
      })
    }

    // 重置表单
    const handleReset = () => {
      resetForm()
    }

    return {
      formData,
      errors,
      submitting,
      canSubmit,
      handleBlur,
      onSubmit,
      handleReset
    }
  }
}
```

**API:**
- `formData` - 表单数据
- `errors` - 表单错误
- `submitting` - 提交状态
- `validated` - 是否已验证
- `hasErrors` - 是否有错误
- `canSubmit` - 是否可以提交
- `validateField(field)` - 验证单个字段
- `validate()` - 验证所有字段
- `clearValidation(field)` - 清除验证错误
- `resetForm()` - 重置表单
- `setValues(values)` - 设置表单值
- `setValue(field, value)` - 设置单个字段值
- `getValues()` - 获取表单值
- `handleSubmit(onSubmit)` - 提交表单
- `setError(field, message)` - 设置字段错误
- `setErrors(fieldErrors)` - 设置多个字段错误

**表单规则配置:**

```javascript
const rules = {
  fieldName: {
    required: true,           // 必填
    type: 'email',           // 类型（email/phone/password/number/url）
    minLength: 3,            // 最小长度
    maxLength: 20,           // 最大长度
    min: 0,                  // 最小值（数字）
    max: 100,                // 最大值（数字）
    pattern: /^[a-z]+$/,     // 正则表达式
    message: '错误提示',      // 错误消息
    validator: (value, formData) => {  // 自定义验证函数
      // 返回 true 表示验证通过
      // 返回字符串表示验证失败，字符串为错误消息
      return true
    }
  }
}
```

## 在模板中使用

```vue
<template>
  <view class="page">
    <!-- 分页列表示例 -->
    <scroll-view
      scroll-y
      @scrolltolower="handleLoadMore"
      @refresherrefresh="handleRefresh"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
    >
      <view v-for="item in list" :key="item.id">
        {{ item.title }}
      </view>
      
      <view v-if="loading">加载中...</view>
      <view v-if="finished">没有更多了</view>
      <view v-if="isEmpty">暂无数据</view>
    </scroll-view>

    <!-- 表单示例 -->
    <view class="form">
      <input
        v-model="formData.username"
        @blur="handleBlur('username')"
        placeholder="请输入用户名"
      />
      <text v-if="errors.username" class="error">
        {{ errors.username }}
      </text>

      <button
        :disabled="!canSubmit"
        @click="onSubmit"
      >
        {{ submitting ? '提交中...' : '提交' }}
      </button>
    </view>
  </view>
</template>
```

## 注意事项

1. 组合式函数应该在 `setup()` 函数中调用
2. 返回的响应式数据需要使用 `.value` 访问（在模板中会自动解包）
3. 组合式函数可以相互组合使用
4. 避免在循环或条件语句中调用组合式函数
5. 组合式函数的命名应该以 `use` 开头

## 开发建议

1. 将可复用的逻辑封装成组合式函数
2. 保持组合式函数的单一职责
3. 为组合式函数添加详细的注释和类型说明
4. 提供清晰的API和使用示例
5. 考虑错误处理和边界情况
