# 通知系统数据库性能优化文档

## 概述

本文档详细说明了通知系统数据库性能优化的实施方案，包括索引优化、查询优化和连接池配置优化。

## 优化内容

### 1. 索引效果验证和优化

#### 1.1 现有索引分析

原有索引结构：
```sql
-- 主键索引
PRIMARY KEY (id)

-- 基础索引
KEY idx_user_id (user_id)
KEY idx_user_read (user_id, is_read)  
KEY idx_created_time (created_time)
KEY idx_type (type)
```

#### 1.2 索引优化策略

**删除冗余索引：**
- 删除单独的 `idx_user_id` 索引，因为 `idx_user_read` 复合索引已包含 `user_id`

**新增优化索引：**
```sql
-- 覆盖索引 - 优化未读数量查询和分页查询
CREATE INDEX idx_user_read_created ON notification_twb (user_id, is_read, created_time DESC);

-- 类型筛选索引 - 优化按类型筛选查询
CREATE INDEX idx_user_type_read ON notification_twb (user_id, type, is_read);

-- 时间范围查询索引 - 优化时间范围筛选
CREATE INDEX idx_user_created_read ON notification_twb (user_id, created_time, is_read);

-- 批量操作索引 - 优化删除已读通知等操作
CREATE INDEX idx_read_user_created ON notification_twb (is_read, user_id, created_time);
```

#### 1.3 索引选择策略

不同查询场景使用不同的最优索引：

| 查询场景 | 推荐索引 | 说明 |
|---------|---------|------|
| 未读数量查询 | `idx_user_read_created` | 覆盖索引，避免回表 |
| 分页查询（无筛选） | `idx_user_read_created` | 支持排序和筛选 |
| 按类型筛选 | `idx_user_type_read` | 类型筛选优化 |
| 时间范围查询 | `idx_user_created_read` | 时间范围优化 |
| 批量删除已读 | `idx_read_user_created` | 批量操作优化 |

### 2. 查询语句性能优化

#### 2.1 查询提示优化

在关键查询中添加索引提示，确保使用最优索引：

```sql
-- 未读数量查询
SELECT /*+ USE_INDEX(notification_twb, idx_user_read_created) */ COUNT(*)
FROM notification_twb
WHERE user_id = ? AND is_read = 0;

-- 分页查询
SELECT /*+ USE_INDEX(notification_twb, idx_user_read_created) */ *
FROM notification_twb
WHERE user_id = ?
ORDER BY CASE WHEN is_read = 0 THEN 0 ELSE 1 END, created_time DESC;
```

#### 2.2 动态查询优化

根据查询条件动态选择最优索引：

- **有类型筛选时**：使用 `idx_user_type_read`
- **有时间范围时**：使用 `idx_user_created_read`  
- **默认查询**：使用 `idx_user_read_created`

#### 2.3 排序优化

优化排序逻辑，未读通知优先显示：
```sql
ORDER BY 
    CASE WHEN is_read = 0 THEN 0 ELSE 1 END,
    created_time DESC
```

### 3. 数据库连接池参数优化

#### 3.1 开发环境配置

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 25              # 适中的连接池大小
      minimum-idle: 8                    # 保持足够的空闲连接
      connection-timeout: 20000          # 20秒连接超时
      idle-timeout: 300000               # 5分钟空闲超时
      max-lifetime: 1200000              # 20分钟最大生命周期
      validation-timeout: 5000           # 连接验证超时
      leak-detection-threshold: 60000    # 连接泄漏检测
```

#### 3.2 生产环境配置

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50              # 更大的连接池
      minimum-idle: 15                   # 更多空闲连接
      # 其他参数与开发环境相同
```

#### 3.3 MySQL连接参数优化

在JDBC URL中添加性能优化参数：
```
jdbc:mysql://localhost:3306/cvs_db?
  useUnicode=true&
  characterEncoding=utf8&
  cachePrepStmts=true&
  useServerPrepStmts=true&
  prepStmtCacheSize=250&
  prepStmtCacheSqlLimit=2048&
  rewriteBatchedStatements=true
```

## 性能指标

关键性能指标和阈值：

| 指标 | 正常范围 | 警告阈值 | 说明 |
|------|---------|---------|------|
| 未读数量查询 | < 50ms | > 50ms | 高频查询，需要快速响应 |
| 分页查询 | < 200ms | > 200ms | 用户体验相关 |
| 表大小 | < 1GB | > 1GB | 考虑数据归档 |
| 记录数 | < 100万 | > 100万 | 考虑数据清理 |
| 连接池使用率 | < 80% | > 80% | 连接池压力监控 |

## 维护建议

### 1. 定期维护

```sql
-- 每月执行一次表优化
OPTIMIZE TABLE notification_twb;

-- 每周执行一次统计信息更新
ANALYZE TABLE notification_twb;
```

### 2. 数据清理

使用提供的存储过程定期清理过期数据：
```sql
-- 清理90天前的已读通知
CALL CleanupOldNotifications(90);
```

### 3. 性能监控

定期执行性能分析：
```sql
-- 执行查询性能分析
CALL AnalyzeNotificationQueries();

-- 获取性能优化建议
CALL GetNotificationPerformanceRecommendations();
```

## 预期效果

通过以上优化措施，预期达到以下性能提升：

1. **查询性能提升 50-80%**：通过覆盖索引减少回表操作
2. **并发处理能力提升 30-50%**：通过连接池优化
3. **系统稳定性提升**：通过索引优化和连接池配置
4. **维护效率提升**：通过自动化清理工具

## 注意事项

1. **索引维护成本**：新增索引会增加写操作的开销，需要权衡
2. **连接池配置**：需要根据实际并发量调整连接池参数
3. **数据归档**：制定合理的数据保留和归档策略

## 验证方法

1. **执行性能测试脚本**：运行 SQL 优化脚本
2. **监控关键指标**：观察查询响应时间和系统资源使用
3. **压力测试**：模拟高并发场景验证优化效果