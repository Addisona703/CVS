# 工具函数文档

本目录包含CVS移动端应用的所有工具函数和辅助类。

## 文件说明

### request.js - HTTP请求客户端
基于uni.request封装的HTTP客户端，提供以下功能：
- 请求/响应拦截器
- 自动添加token和设备ID
- 错误处理和重试机制
- 支持GET、POST、PUT、DELETE方法

**使用示例：**
```javascript
import http from '@/utils/request'

// GET请求
const data = await http.get('/api/activities', { page: 1, size: 20 })

// POST请求
const result = await http.post('/api/activities', { title: '活动标题' })
```

### auth.js - 认证工具
提供token管理和设备ID生成功能：
- token加密存储
- token过期检查
- 设备ID生成和验证
- 登录状态检查

**使用示例：**
```javascript
import { tokenManager, isLoggedIn, requireAuth } from '@/utils/auth'

// 保存token
tokenManager.setToken(accessToken, refreshToken)

// 获取token
const token = tokenManager.getAccessToken()

// 检查是否登录
if (isLoggedIn()) {
  // 已登录
}

// 需要登录的页面守卫
requireAuth(() => {
  // 登录后执行的逻辑
})
```

### format.js - 格式化工具
提供各种数据格式化功能：
- 日期时间格式化
- 时长格式化
- 数字格式化
- 手机号/邮箱脱敏
- 状态文本转换

**使用示例：**
```javascript
import { formatDate, formatDuration, formatPhone } from '@/utils/format'

// 格式化日期
formatDate('2025-01-15T10:30:00', 'YYYY-MM-DD HH:mm') // "2025-01-15 10:30"

// 格式化时长
formatDuration(90) // "1小时30分钟"

// 格式化手机号
formatPhone('13800138000') // "138****8000"
```

### validate.js - 验证工具
提供各种数据验证功能：
- 邮箱/手机号验证
- 密码强度验证
- 表单验证
- XSS/SQL注入过滤

**使用示例：**
```javascript
import { validateEmail, validatePassword, validateForm } from '@/utils/validate'

// 验证邮箱
validateEmail('test@example.com') // true

// 验证密码
const result = validatePassword('Abc123!@#')
// { valid: true, message: '密码强度良好', strength: 4 }

// 表单验证
const { valid, errors } = validateForm(formData, {
  email: [{ type: 'required' }, { type: 'email' }],
  phone: [{ type: 'required' }, { type: 'phone' }]
})
```

### storage.js - 存储工具
提供缓存管理功能：
- 分类缓存（永久/短期/临时）
- 缓存过期检查
- 缓存清理

**使用示例：**
```javascript
import { CacheManager, CacheType, cacheActivityList } from '@/utils/storage'

// 设置缓存
CacheManager.set('key', data, CacheType.SHORT_TERM)

// 获取缓存
const data = CacheManager.get('key')

// 快捷方法
cacheActivityList(activities)
const cachedActivities = getCachedActivityList()
```

### errorHandler.js - 错误处理
提供统一的错误处理：
- 错误代码定义
- 错误消息映射
- 全局错误处理器
- 错误恢复建议

**使用示例：**
```javascript
import { ErrorHandler, ErrorCode, createError } from '@/utils/errorHandler'

// 处理错误
try {
  await someAsyncOperation()
} catch (error) {
  ErrorHandler.handle(error, {
    showToast: true,
    callback: (errorInfo) => {
      console.log('错误信息:', errorInfo)
    }
  })
}

// 创建业务错误
throw createError(ErrorCode.ACTIVITY_FULL, '活动名额已满')
```

### logger.js - 日志管理
提供日志记录和性能监控：
- 分级日志（DEBUG/INFO/WARN/ERROR）
- 日志存储和上报
- 性能监控

**使用示例：**
```javascript
import { logger, performanceMonitor } from '@/utils/logger'

// 记录日志
logger.info('用户登录成功', { userId: 123 })
logger.error('请求失败', error)

// 性能监控
performanceMonitor.start('pageLoad')
// ... 执行操作
const duration = performanceMonitor.end('pageLoad')
logger.info('页面加载时间', { duration })
```

## 统一导出

所有工具函数都可以从 `@/utils` 统一导入：

```javascript
import {
  http,
  tokenManager,
  formatDate,
  validateEmail,
  CacheManager,
  ErrorHandler,
  logger
} from '@/utils'
```

## 注意事项

1. **token管理**：token会自动加密存储，并在请求时自动添加到请求头
2. **错误处理**：所有HTTP请求的错误都会被自动处理并显示提示
3. **缓存管理**：缓存会自动检查过期时间，过期的缓存会被自动清除
4. **日志记录**：生产环境只记录INFO及以上级别的日志
5. **性能监控**：关键操作应该使用性能监控记录执行时间

## 开发建议

1. 使用 `http` 进行所有网络请求，不要直接使用 `uni.request`
2. 使用 `ErrorHandler` 处理所有错误，保持错误提示的一致性
3. 使用 `CacheManager` 管理缓存，避免手动操作 `uni.storage`
4. 使用 `logger` 记录关键操作和错误，便于调试和问题追踪
5. 使用验证工具进行数据验证，提高数据质量和安全性
