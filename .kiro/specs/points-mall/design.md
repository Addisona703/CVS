# 积分商城设计文档

## Overview

积分商城是CVS志愿服务系统的扩展模块，为学生提供积分兑换实物商品和虚拟权益的功能。系统采用前后端分离架构，后端基于Spring Boot 3 + MyBatis-Plus，前端基于Vue 3 + Element Plus。

核心功能包括：
- 学工处管理商品（CRUD操作、库存管理、分类管理）
- 学生浏览和兑换商品（搜索、筛选、积分兑换）
- 兑换凭证生成与核销（二维码凭证、扫码核销）
- 统计分析（兑换数据、库存预警）

## Architecture

### 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        前端层 (Vue 3)                        │
├─────────────────────────────────────────────────────────────┤
│  学生端                    │         学工处端                │
│  - 商品浏览页面            │  - 商品管理页面                 │
│  - 商品详情页面            │  - 分类管理页面                 │
│  - 兑换确认页面            │  - 核销页面                     │
│  - 我的兑换页面            │  - 统计报表页面                 │
│  - 兑换凭证页面            │                                 │
└─────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    API层 (Spring MVC)                        │
├─────────────────────────────────────────────────────────────┤
│  ProductController  │  RedemptionController  │  CategoryController │
└─────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    业务逻辑层 (Service)                      │
├─────────────────────────────────────────────────────────────┤
│  ProductService  │  RedemptionService  │  CategoryService   │
│  PointsService   │  StatisticsService                       │
└─────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  数据访问层 (MyBatis-Plus)                   │
├─────────────────────────────────────────────────────────────┤
│  ProductMapper  │  RedemptionMapper  │  CategoryMapper      │
└─────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      数据库层 (MySQL)                        │
├─────────────────────────────────────────────────────────────┤
│  product_twb  │  redemption_twb  │  category_twb           │
└─────────────────────────────────────────────────────────────┘
```


## Components and Interfaces

### 后端组件

#### 1. Entity层（实体类）

**Product（商品实体）**
```java
@Data
@TableName("product_twb")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;                    // 主键ID
    private String name;                // 商品名称
    private String description;         // 商品描述
    private Integer pointsRequired;     // 所需积分
    private Integer stock;              // 库存数量
    private String imageUrl;            // 商品图片URL
    private Long categoryId;            // 商品分类ID
    private Integer status;             // 商品状态：0-下架，1-上架
    private Integer stockWarning;       // 库存预警值
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;    // 创建时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;    // 更新时间
    @TableLogic
    private Integer deleted;            // 逻辑删除标志
}
```

**Redemption（兑换记录实体）**
```java
@Data
@TableName("redemption_twb")
public class Redemption {
    @TableId(type = IdType.AUTO)
    private Long id;                    // 主键ID
    private Long userId;                // 用户ID
    private Long productId;             // 商品ID
    private Integer pointsSpent;        // 消耗积分
    private String voucherCode;         // 凭证编号（唯一）
    private Integer status;             // 兑换状态：0-待领取，1-已领取，2-已取消
    private Long verifiedBy;            // 核销人员ID
    private LocalDateTime verifiedAt;   // 核销时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;    // 兑换时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;    // 更新时间
}
```

**Category（商品分类实体）**
```java
@Data
@TableName("category_twb")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;                    // 主键ID
    private String name;                // 分类名称
    private String description;         // 分类描述
    private Integer sortOrder;          // 排序顺序
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;    // 创建时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;    // 更新时间
    @TableLogic
    private Integer deleted;            // 逻辑删除标志
}
```

#### 2. DTO层（数据传输对象）

**ProductCreateRequest**
```java
@Data
public class ProductCreateRequest {
    @NotBlank(message = "商品名称不能为空")
    private String name;
    @NotBlank(message = "商品描述不能为空")
    private String description;
    @NotNull(message = "所需积分不能为空")
    @Min(value = 1, message = "所需积分必须大于0")
    private Integer pointsRequired;
    @NotNull(message = "库存数量不能为空")
    @Min(value = 0, message = "库存数量不能为负数")
    private Integer stock;
    private String imageUrl;
    @NotNull(message = "商品分类不能为空")
    private Long categoryId;
    private Integer stockWarning;
}
```

**RedemptionRequest**
```java
@Data
public class RedemptionRequest {
    @NotNull(message = "商品ID不能为空")
    private Long productId;
}
```

**ProductVO（商品视图对象）**
```java
@Data
public class ProductVO {
    private Long id;
    private String name;
    private String description;
    private Integer pointsRequired;
    private Integer stock;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private Integer status;
    private Boolean canRedeem;          // 当前用户是否可兑换
    private LocalDateTime createdAt;
}
```

**RedemptionVO（兑换记录视图对象）**
```java
@Data
public class RedemptionVO {
    private Long id;
    private Long userId;
    private String userName;
    private Long productId;
    private String productName;
    private String productImage;
    private Integer pointsSpent;
    private String voucherCode;
    private Integer status;
    private String statusText;
    private Long verifiedBy;
    private String verifiedByName;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
}
```

#### 3. Service层（业务逻辑）

**ProductService接口**
```java
public interface ProductService extends IService<Product> {
    // 创建商品
    Long createProduct(ProductCreateRequest request);
    
    // 更新商品
    void updateProduct(Long id, ProductUpdateRequest request);
    
    // 删除商品（逻辑删除）
    void deleteProduct(Long id);
    
    // 获取商品列表（分页、筛选）
    IPage<ProductVO> getProductList(ProductQueryRequest request);
    
    // 获取商品详情
    ProductVO getProductDetail(Long id, Long userId);
    
    // 上架/下架商品
    void updateProductStatus(Long id, Integer status);
    
    // 检查库存
    boolean checkStock(Long productId);
    
    // 扣减库存
    void decreaseStock(Long productId);
}
```

**RedemptionService接口**
```java
public interface RedemptionService extends IService<Redemption> {
    // 兑换商品
    RedemptionVO redeemProduct(Long userId, RedemptionRequest request);
    
    // 获取用户兑换记录
    IPage<RedemptionVO> getUserRedemptions(Long userId, PageRequest pageRequest);
    
    // 获取兑换详情
    RedemptionVO getRedemptionDetail(Long id);
    
    // 核销兑换凭证
    void verifyRedemption(String voucherCode, Long staffId);
    
    // 取消兑换（退还积分）
    void cancelRedemption(Long id, Long userId);
    
    // 获取所有兑换记录（学工处）
    IPage<RedemptionVO> getAllRedemptions(RedemptionQueryRequest request);
    
    // 生成唯一凭证编号
    String generateVoucherCode();
}
```

**CategoryService接口**
```java
public interface CategoryService extends IService<Category> {
    // 创建分类
    Long createCategory(CategoryCreateRequest request);
    
    // 更新分类
    void updateCategory(Long id, CategoryUpdateRequest request);
    
    // 删除分类
    void deleteCategory(Long id);
    
    // 获取所有分类
    List<CategoryVO> getAllCategories();
}
```

**StatisticsService接口**
```java
public interface StatisticsService {
    // 获取兑换统计数据
    RedemptionStatisticsVO getRedemptionStatistics(LocalDate startDate, LocalDate endDate);
    
    // 获取商品兑换排行
    List<ProductRankingVO> getProductRanking(Integer limit);
    
    // 获取库存预警列表
    List<ProductVO> getLowStockProducts();
    
    // 导出兑换记录
    byte[] exportRedemptions(LocalDate startDate, LocalDate endDate);
}
```

#### 4. Controller层（API接口）

**ProductController**
- `POST /api/mall/products` - 创建商品（学工处）
- `PUT /api/mall/products/{id}` - 更新商品（学工处）
- `DELETE /api/mall/products/{id}` - 删除商品（学工处）
- `GET /api/mall/products` - 获取商品列表（分页、筛选）
- `GET /api/mall/products/{id}` - 获取商品详情
- `PUT /api/mall/products/{id}/status` - 更新商品状态（学工处）

**RedemptionController**
- `POST /api/mall/redemptions` - 兑换商品（学生）
- `GET /api/mall/redemptions/my` - 获取我的兑换记录（学生）
- `GET /api/mall/redemptions/{id}` - 获取兑换详情
- `POST /api/mall/redemptions/verify` - 核销凭证（学工处）
- `PUT /api/mall/redemptions/{id}/cancel` - 取消兑换（学生）
- `GET /api/mall/redemptions` - 获取所有兑换记录（学工处）

**CategoryController**
- `POST /api/mall/categories` - 创建分类（学工处）
- `PUT /api/mall/categories/{id}` - 更新分类（学工处）
- `DELETE /api/mall/categories/{id}` - 删除分类（学工处）
- `GET /api/mall/categories` - 获取所有分类

**StatisticsController**
- `GET /api/mall/statistics/redemptions` - 获取兑换统计（学工处）
- `GET /api/mall/statistics/ranking` - 获取商品排行（学工处）
- `GET /api/mall/statistics/low-stock` - 获取库存预警（学工处）
- `GET /api/mall/statistics/export` - 导出兑换记录（学工处）

### 前端组件

#### 学生端页面组件

**MallIndex.vue（商城首页）**
- 商品列表展示（网格布局）
- 分类筛选
- 搜索功能
- 积分余额显示
- 商品卡片（图片、名称、积分、库存）

**ProductDetail.vue（商品详情）**
- 商品完整信息展示
- 兑换按钮
- 积分充足性提示
- 领取说明

**MyRedemptions.vue（我的兑换）**
- 兑换记录列表
- 状态筛选
- 凭证查看入口

**VoucherDetail.vue（兑换凭证）**
- 凭证二维码
- 凭证编号
- 商品信息
- 领取地点和说明
- 兑换状态

#### 学工处端页面组件

**ProductManagement.vue（商品管理）**
- 商品列表表格
- 添加/编辑商品对话框
- 商品状态切换
- 库存管理
- 删除确认

**CategoryManagement.vue（分类管理）**
- 分类列表
- 添加/编辑分类对话框
- 删除确认

**RedemptionVerify.vue（兑换核销）**
- 扫码核销功能
- 手动输入凭证编号
- 核销确认对话框
- 核销记录

**RedemptionStatistics.vue（统计报表）**
- 统计数据卡片
- 商品兑换排行图表
- 库存预警列表
- 时间范围筛选
- 导出功能


## Data Models

### 数据库表设计

#### 1. product_twb（商品表）

```sql
CREATE TABLE `product_twb` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(200) NOT NULL COMMENT '商品名称',
  `description` text NOT NULL COMMENT '商品描述',
  `points_required` int NOT NULL COMMENT '所需积分',
  `stock` int NOT NULL DEFAULT '0' COMMENT '库存数量',
  `image_url` varchar(500) DEFAULT NULL COMMENT '商品图片URL',
  `category_id` bigint NOT NULL COMMENT '商品分类ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '商品状态：0-下架，1-上架',
  `stock_warning` int DEFAULT '10' COMMENT '库存预警值',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';
```

#### 2. redemption_twb（兑换记录表）

```sql
CREATE TABLE `redemption_twb` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `points_spent` int NOT NULL COMMENT '消耗积分',
  `voucher_code` varchar(50) NOT NULL COMMENT '凭证编号',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '兑换状态：0-待领取，1-已领取，2-已取消',
  `verified_by` bigint DEFAULT NULL COMMENT '核销人员ID',
  `verified_at` datetime DEFAULT NULL COMMENT '核销时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '兑换时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_voucher_code` (`voucher_code`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='兑换记录表';
```

#### 3. category_twb（商品分类表）

```sql
CREATE TABLE `category_twb` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '分类名称',
  `description` varchar(500) DEFAULT NULL COMMENT '分类描述',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序顺序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';
```

### 数据关系图

```
user_twb (用户表)
    ├── 1:N → redemption_twb (兑换记录) [user_id]
    └── 1:N → redemption_twb (核销记录) [verified_by]

product_twb (商品表)
    ├── N:1 → category_twb (商品分类) [category_id]
    └── 1:N → redemption_twb (兑换记录) [product_id]

points_twb (积分记录表)
    └── N:1 → user_twb (用户) [user_id]
```

### 枚举类型

**ProductStatus（商品状态）**
```java
public enum ProductStatus {
    OFFLINE(0, "下架"),
    ONLINE(1, "上架");
    
    private final Integer code;
    private final String description;
}
```

**RedemptionStatus（兑换状态）**
```java
public enum RedemptionStatus {
    PENDING(0, "待领取"),
    VERIFIED(1, "已领取"),
    CANCELLED(2, "已取消");
    
    private final Integer code;
    private final String description;
}
```

## Error Handling

### 异常类型定义

**MallException（商城业务异常）**
```java
public class MallException extends RuntimeException {
    private final Integer code;
    private final String message;
    
    public MallException(MallErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
}
```

**MallErrorCode（错误码枚举）**
```java
public enum MallErrorCode {
    PRODUCT_NOT_FOUND(40001, "商品不存在"),
    PRODUCT_OFFLINE(40002, "商品已下架"),
    INSUFFICIENT_STOCK(40003, "商品库存不足"),
    INSUFFICIENT_POINTS(40004, "积分不足"),
    REDEMPTION_NOT_FOUND(40005, "兑换记录不存在"),
    VOUCHER_ALREADY_USED(40006, "凭证已被使用"),
    VOUCHER_CANCELLED(40007, "兑换已取消"),
    CATEGORY_NOT_FOUND(40008, "分类不存在"),
    CATEGORY_HAS_PRODUCTS(40009, "分类下存在商品，无法删除"),
    UNAUTHORIZED_OPERATION(40010, "无权限操作");
    
    private final Integer code;
    private final String message;
}
```

### 异常处理流程

1. **积分不足异常**
   - 检查时机：兑换商品前
   - 处理方式：返回错误提示，阻止兑换
   - 前端展示：弹窗提示"积分不足，无法兑换"

2. **库存不足异常**
   - 检查时机：兑换商品前（加锁检查）
   - 处理方式：返回错误提示，阻止兑换
   - 前端展示：弹窗提示"商品已兑完"

3. **凭证已使用异常**
   - 检查时机：核销凭证时
   - 处理方式：返回错误提示，阻止重复核销
   - 前端展示：弹窗提示"该凭证已被使用"

4. **权限异常**
   - 检查时机：所有需要权限的操作
   - 处理方式：返回401/403状态码
   - 前端展示：跳转登录页或提示无权限

### 全局异常处理器

```java
@RestControllerAdvice
public class MallExceptionHandler {
    
    @ExceptionHandler(MallException.class)
    public Result<Void> handleMallException(MallException e) {
        return Result.error(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.error(40000, message);
    }
}
```

## Testing Strategy

### 单元测试

#### Service层测试

**ProductServiceTest**
- 测试创建商品
- 测试更新商品
- 测试删除商品
- 测试获取商品列表
- 测试库存检查和扣减
- 测试商品状态更新

**RedemptionServiceTest**
- 测试兑换商品（正常流程）
- 测试积分不足场景
- 测试库存不足场景
- 测试凭证生成唯一性
- 测试核销凭证
- 测试重复核销场景
- 测试取消兑换

**CategoryServiceTest**
- 测试创建分类
- 测试更新分类
- 测试删除空分类
- 测试删除有商品的分类

#### Mapper层测试

**ProductMapperTest**
- 测试商品CRUD操作
- 测试分页查询
- 测试条件筛选
- 测试库存更新

**RedemptionMapperTest**
- 测试兑换记录CRUD操作
- 测试按用户查询
- 测试按状态查询
- 测试统计查询

### 集成测试

#### Controller层测试

**ProductControllerTest**
- 测试创建商品接口（需要学工处权限）
- 测试获取商品列表接口
- 测试获取商品详情接口
- 测试更新商品状态接口

**RedemptionControllerTest**
- 测试兑换商品接口（需要学生权限）
- 测试获取我的兑换记录接口
- 测试核销凭证接口（需要学工处权限）

### 性能测试

**并发兑换测试**
- 模拟多个用户同时兑换同一商品
- 验证库存扣减的准确性
- 验证不会出现超卖情况

**压力测试**
- 测试商品列表查询性能
- 测试兑换接口响应时间
- 测试数据库连接池配置

### 测试数据准备

**初始化测试数据**
```sql
-- 插入测试分类
INSERT INTO category_twb (name, description, sort_order) VALUES
('文具用品', '学习办公文具', 1),
('生活用品', '日常生活用品', 2),
('电子产品', '数码电子产品', 3);

-- 插入测试商品
INSERT INTO product_twb (name, description, points_required, stock, category_id, status) VALUES
('中性笔套装', '10支装黑色中性笔', 50, 100, 1, 1),
('笔记本', 'A5大小笔记本', 80, 50, 1, 1),
('保温杯', '500ml不锈钢保温杯', 200, 30, 2, 1);

-- 插入测试用户积分
INSERT INTO points_twb (user_id, points) VALUES
(1, 500),  -- 学生1有500积分
(2, 300);  -- 学生2有300积分
```


## Implementation Details

### 核心业务逻辑实现

#### 1. 积分兑换流程

```java
@Transactional(rollbackFor = Exception.class)
public RedemptionVO redeemProduct(Long userId, RedemptionRequest request) {
    // 1. 查询商品信息
    Product product = productMapper.selectById(request.getProductId());
    if (product == null || product.getDeleted() == 1) {
        throw new MallException(MallErrorCode.PRODUCT_NOT_FOUND);
    }
    
    // 2. 检查商品状态
    if (product.getStatus() == ProductStatus.OFFLINE.getCode()) {
        throw new MallException(MallErrorCode.PRODUCT_OFFLINE);
    }
    
    // 3. 检查库存（加锁）
    if (!checkAndDecreaseStock(product.getId())) {
        throw new MallException(MallErrorCode.INSUFFICIENT_STOCK);
    }
    
    // 4. 检查用户积分
    Integer userPoints = pointsService.getUserTotalPoints(userId);
    if (userPoints < product.getPointsRequired()) {
        // 回滚库存
        increaseStock(product.getId());
        throw new MallException(MallErrorCode.INSUFFICIENT_POINTS);
    }
    
    // 5. 扣除积分
    pointsService.deductPoints(userId, product.getPointsRequired(), 
        "兑换商品：" + product.getName());
    
    // 6. 创建兑换记录
    Redemption redemption = new Redemption();
    redemption.setUserId(userId);
    redemption.setProductId(product.getId());
    redemption.setPointsSpent(product.getPointsRequired());
    redemption.setVoucherCode(generateVoucherCode());
    redemption.setStatus(RedemptionStatus.PENDING.getCode());
    redemptionMapper.insert(redemption);
    
    // 7. 返回兑换结果
    return buildRedemptionVO(redemption);
}

// 检查并扣减库存（使用乐观锁）
private boolean checkAndDecreaseStock(Long productId) {
    int updated = productMapper.decreaseStock(productId);
    return updated > 0;
}

// 生成唯一凭证编号
private String generateVoucherCode() {
    // 格式：MALL + 时间戳 + 随机数
    String timestamp = String.valueOf(System.currentTimeMillis());
    String random = String.format("%04d", new Random().nextInt(10000));
    return "MALL" + timestamp + random;
}
```

**库存扣减SQL（乐观锁）**
```xml
<update id="decreaseStock">
    UPDATE product_twb 
    SET stock = stock - 1,
        updated_at = NOW()
    WHERE id = #{productId} 
      AND stock > 0
      AND deleted = 0
</update>
```

#### 2. 凭证核销流程

```java
@Transactional(rollbackFor = Exception.class)
public void verifyRedemption(String voucherCode, Long staffId) {
    // 1. 查询兑换记录
    Redemption redemption = redemptionMapper.selectByVoucherCode(voucherCode);
    if (redemption == null) {
        throw new MallException(MallErrorCode.REDEMPTION_NOT_FOUND);
    }
    
    // 2. 检查兑换状态
    if (redemption.getStatus() == RedemptionStatus.VERIFIED.getCode()) {
        throw new MallException(MallErrorCode.VOUCHER_ALREADY_USED);
    }
    if (redemption.getStatus() == RedemptionStatus.CANCELLED.getCode()) {
        throw new MallException(MallErrorCode.VOUCHER_CANCELLED);
    }
    
    // 3. 更新兑换状态
    redemption.setStatus(RedemptionStatus.VERIFIED.getCode());
    redemption.setVerifiedBy(staffId);
    redemption.setVerifiedAt(LocalDateTime.now());
    redemptionMapper.updateById(redemption);
}
```

#### 3. 取消兑换流程

```java
@Transactional(rollbackFor = Exception.class)
public void cancelRedemption(Long id, Long userId) {
    // 1. 查询兑换记录
    Redemption redemption = redemptionMapper.selectById(id);
    if (redemption == null) {
        throw new MallException(MallErrorCode.REDEMPTION_NOT_FOUND);
    }
    
    // 2. 验证权限
    if (!redemption.getUserId().equals(userId)) {
        throw new MallException(MallErrorCode.UNAUTHORIZED_OPERATION);
    }
    
    // 3. 检查状态（只能取消待领取的兑换）
    if (redemption.getStatus() != RedemptionStatus.PENDING.getCode()) {
        throw new MallException(MallErrorCode.VOUCHER_ALREADY_USED);
    }
    
    // 4. 退还积分
    pointsService.addPoints(userId, redemption.getPointsSpent(), 
        "取消兑换退还积分");
    
    // 5. 恢复库存
    productMapper.increaseStock(redemption.getProductId());
    
    // 6. 更新兑换状态
    redemption.setStatus(RedemptionStatus.CANCELLED.getCode());
    redemptionMapper.updateById(redemption);
}
```

#### 4. 积分余额计算

```java
public Integer getUserTotalPoints(Long userId) {
    // 查询用户所有积分记录，计算总和
    List<Points> pointsList = pointsMapper.selectList(
        new LambdaQueryWrapper<Points>()
            .eq(Points::getUserId, userId)
    );
    
    return pointsList.stream()
        .mapToInt(Points::getPoints)
        .sum();
}
```

### 前端关键实现

#### 1. 商品列表组件

```vue
<template>
  <div class="mall-index">
    <!-- 积分余额显示 -->
    <div class="points-banner">
      <span>我的积分：</span>
      <span class="points-value">{{ userPoints }}</span>
    </div>
    
    <!-- 分类筛选 -->
    <el-tabs v-model="activeCategory" @tab-change="handleCategoryChange">
      <el-tab-pane label="全部" name="all"></el-tab-pane>
      <el-tab-pane 
        v-for="category in categories" 
        :key="category.id"
        :label="category.name" 
        :name="category.id.toString()">
      </el-tab-pane>
    </el-tabs>
    
    <!-- 搜索框 -->
    <el-input 
      v-model="searchKeyword" 
      placeholder="搜索商品"
      @input="handleSearch">
    </el-input>
    
    <!-- 商品网格 -->
    <div class="product-grid">
      <el-card 
        v-for="product in products" 
        :key="product.id"
        class="product-card"
        @click="goToDetail(product.id)">
        <img :src="product.imageUrl" class="product-image" />
        <div class="product-info">
          <h3>{{ product.name }}</h3>
          <div class="product-points">
            <span class="points">{{ product.pointsRequired }}积分</span>
            <span class="stock">剩余{{ product.stock }}</span>
          </div>
          <el-button 
            type="primary" 
            :disabled="!product.canRedeem"
            @click.stop="handleRedeem(product)">
            {{ product.canRedeem ? '立即兑换' : '积分不足' }}
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProductList, getUserPoints } from '@/api/mall'

const userPoints = ref(0)
const activeCategory = ref('all')
const searchKeyword = ref('')
const products = ref([])
const categories = ref([])

const loadProducts = async () => {
  const params = {
    categoryId: activeCategory.value === 'all' ? null : activeCategory.value,
    keyword: searchKeyword.value
  }
  const res = await getProductList(params)
  products.value = res.data
}

const loadUserPoints = async () => {
  const res = await getUserPoints()
  userPoints.value = res.data
}

onMounted(() => {
  loadUserPoints()
  loadProducts()
})
</script>
```

#### 2. 兑换凭证组件

```vue
<template>
  <div class="voucher-detail">
    <div class="voucher-header">
      <h2>兑换凭证</h2>
      <el-tag :type="statusType">{{ statusText }}</el-tag>
    </div>
    
    <!-- 二维码 -->
    <div v-if="redemption.status === 0" class="qrcode-container">
      <vue-qrcode 
        :value="redemption.voucherCode" 
        :options="{ width: 200 }">
      </vue-qrcode>
      <p class="voucher-code">凭证编号：{{ redemption.voucherCode }}</p>
    </div>
    
    <!-- 商品信息 -->
    <div class="product-info">
      <img :src="redemption.productImage" />
      <div>
        <h3>{{ redemption.productName }}</h3>
        <p>消耗积分：{{ redemption.pointsSpent }}</p>
        <p>兑换时间：{{ formatDate(redemption.createdAt) }}</p>
      </div>
    </div>
    
    <!-- 领取说明 -->
    <div v-if="redemption.status === 0" class="pickup-info">
      <h4>领取说明</h4>
      <p>请携带此凭证到学工处办公室领取商品</p>
      <p>领取地点：行政楼201室</p>
      <p>领取时间：周一至周五 9:00-17:00</p>
    </div>
    
    <!-- 已领取信息 -->
    <div v-if="redemption.status === 1" class="verified-info">
      <p>领取时间：{{ formatDate(redemption.verifiedAt) }}</p>
      <p>核销人员：{{ redemption.verifiedByName }}</p>
    </div>
    
    <!-- 取消按钮 -->
    <el-button 
      v-if="redemption.status === 0"
      type="danger" 
      @click="handleCancel">
      取消兑换
    </el-button>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import VueQrcode from '@chenfengyuan/vue-qrcode'
import { getRedemptionDetail, cancelRedemption } from '@/api/mall'

const redemption = ref({})

const statusType = computed(() => {
  const types = { 0: 'warning', 1: 'success', 2: 'info' }
  return types[redemption.value.status]
})

const statusText = computed(() => {
  const texts = { 0: '待领取', 1: '已领取', 2: '已取消' }
  return texts[redemption.value.status]
})
</script>
```

#### 3. 核销页面组件

```vue
<template>
  <div class="verify-page">
    <h2>兑换核销</h2>
    
    <!-- 扫码核销 -->
    <div class="scan-section">
      <el-button type="primary" @click="startScan">
        <el-icon><Camera /></el-icon>
        扫码核销
      </el-button>
      <qrcode-stream @decode="onDecode"></qrcode-stream>
    </div>
    
    <!-- 手动输入 -->
    <div class="manual-section">
      <el-input 
        v-model="voucherCode" 
        placeholder="请输入凭证编号">
      </el-input>
      <el-button type="primary" @click="handleVerify">
        确认核销
      </el-button>
    </div>
    
    <!-- 核销确认对话框 -->
    <el-dialog v-model="showConfirm" title="确认核销">
      <div class="confirm-content">
        <p>学生姓名：{{ redemptionInfo.userName }}</p>
        <p>商品名称：{{ redemptionInfo.productName }}</p>
        <p>兑换时间：{{ formatDate(redemptionInfo.createdAt) }}</p>
      </div>
      <template #footer>
        <el-button @click="showConfirm = false">取消</el-button>
        <el-button type="primary" @click="confirmVerify">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { QrcodeStream } from 'vue-qrcode-reader'
import { verifyRedemption, getRedemptionByVoucher } from '@/api/mall'

const voucherCode = ref('')
const showConfirm = ref(false)
const redemptionInfo = ref({})

const onDecode = (code) => {
  voucherCode.value = code
  handleVerify()
}

const handleVerify = async () => {
  const res = await getRedemptionByVoucher(voucherCode.value)
  redemptionInfo.value = res.data
  showConfirm.value = true
}

const confirmVerify = async () => {
  await verifyRedemption(voucherCode.value)
  ElMessage.success('核销成功')
  showConfirm.value = false
  voucherCode.value = ''
}
</script>
```

### 安全性考虑

#### 1. 权限控制

**后端权限注解**
```java
@RestController
@RequestMapping("/api/mall/products")
public class ProductController {
    
    // 只有学工处可以创建商品
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> createProduct(@RequestBody ProductCreateRequest request) {
        // ...
    }
    
    // 所有人都可以查看商品列表
    @GetMapping
    public Result<IPage<ProductVO>> getProductList(ProductQueryRequest request) {
        // ...
    }
}
```

**前端路由守卫**
```javascript
router.beforeEach((to, from, next) => {
  const userRole = store.state.user.role
  
  if (to.path.startsWith('/mall/manage')) {
    // 商城管理页面只允许学工处访问
    if (userRole !== 'ADMIN') {
      next('/403')
      return
    }
  }
  
  next()
})
```

#### 2. 并发控制

**使用乐观锁防止超卖**
```sql
-- 库存扣减使用WHERE条件确保库存充足
UPDATE product_twb 
SET stock = stock - 1
WHERE id = #{productId} AND stock > 0
```

**使用唯一索引防止重复兑换**
```sql
-- voucher_code字段添加唯一索引
UNIQUE KEY `uk_voucher_code` (`voucher_code`)
```

#### 3. 数据验证

**后端参数校验**
```java
@Data
public class ProductCreateRequest {
    @NotBlank(message = "商品名称不能为空")
    @Length(max = 200, message = "商品名称不能超过200字符")
    private String name;
    
    @NotNull(message = "所需积分不能为空")
    @Min(value = 1, message = "所需积分必须大于0")
    @Max(value = 100000, message = "所需积分不能超过100000")
    private Integer pointsRequired;
}
```

**前端表单验证**
```javascript
const rules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { max: 200, message: '商品名称不能超过200字符', trigger: 'blur' }
  ],
  pointsRequired: [
    { required: true, message: '请输入所需积分', trigger: 'blur' },
    { type: 'number', min: 1, max: 100000, message: '积分范围1-100000', trigger: 'blur' }
  ]
}
```

### 性能优化

#### 1. 数据库优化

**添加索引**
```sql
-- 商品表索引
CREATE INDEX idx_category_status ON product_twb(category_id, status);
CREATE INDEX idx_points_stock ON product_twb(points_required, stock);

-- 兑换记录表索引
CREATE INDEX idx_user_status ON redemption_twb(user_id, status);
CREATE INDEX idx_created_at ON redemption_twb(created_at);
```

**查询优化**
```java
// 使用分页查询避免一次性加载大量数据
IPage<Product> page = new Page<>(pageNum, pageSize);
productMapper.selectPage(page, queryWrapper);

// 使用连接查询减少数据库访问次数
List<ProductVO> products = productMapper.selectProductsWithCategory(params);
```

#### 2. 缓存策略

**Redis缓存商品列表**
```java
@Cacheable(value = "products", key = "#categoryId + '_' + #status")
public List<ProductVO> getProductsByCategory(Long categoryId, Integer status) {
    // 查询数据库
}

@CacheEvict(value = "products", allEntries = true)
public void updateProduct(Long id, ProductUpdateRequest request) {
    // 更新商品，清除缓存
}
```

**缓存用户积分**
```java
@Cacheable(value = "userPoints", key = "#userId")
public Integer getUserTotalPoints(Long userId) {
    // 计算用户总积分
}

@CacheEvict(value = "userPoints", key = "#userId")
public void updateUserPoints(Long userId, Integer points) {
    // 更新积分，清除缓存
}
```

#### 3. 前端优化

**图片懒加载**
```vue
<img v-lazy="product.imageUrl" class="product-image" />
```

**虚拟滚动（大量商品时）**
```vue
<virtual-list 
  :data-sources="products"
  :data-component="ProductCard"
  :estimate-size="200">
</virtual-list>
```

