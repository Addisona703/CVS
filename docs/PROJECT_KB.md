# CVS-志愿服务数字化：项目索引（给新会话用）

> 目的：把“去哪找什么”固定下来；不记录大段代码，只记录入口文件/关键路径/关键业务链路，方便你随时让我按需抽取毕业设计书片段。

## 1. 仓库总览

- 后端（Spring Boot）：`CVS-backend/`
- PC 前端（Vue3/Vite）：`CVS-frontend/`
- 移动端（uni-app/Vue3）：`CVS-mobile/`

## 2. 技术栈速记

### 后端

- Maven 工程：`CVS-backend/pom.xml`
- Spring Boot + Security + JWT：`CVS-backend/src/main/java/com/hngy/cvs/common/config/SecurityConfig.java`
- MyBatis-Plus：`CVS-backend/src/main/java/com/hngy/cvs/common/config/MyBatisPlusConfig.java`
- Redis：`CVS-backend/src/main/java/com/hngy/cvs/common/config/RedisConfig.java`
- Swagger/OpenAPI：`CVS-backend/src/main/java/com/hngy/cvs/common/config/SwaggerConfig.java`
- PDF 证书（PDFBox）：`CVS-backend/src/main/java/com/hngy/cvs/service/impl/PdfServiceImpl.java`
- Excel 导出（POI）：统计/导出相关实现通常在 `CVS-backend/src/main/java/com/hngy/cvs/service/impl/StatisticsServiceImpl.java`

### PC 前端

- Vite 配置：`CVS-frontend/vite.config.js`
- 环境变量：`CVS-frontend/.env.development`、`CVS-frontend/.env.production`
- Axios 封装：`CVS-frontend/src/utils/request.js`
- 路由与页面结构：`CVS-frontend/src/router/index.js`、`CVS-frontend/src/views/`
- 状态管理（登录态）：`CVS-frontend/src/stores/auth.js`

### 移动端

- uni-app 页面路由：`CVS-mobile/pages.json`
- 环境变量：`CVS-mobile/.env.development`、`CVS-mobile/.env.production`
- 请求封装（uni.request）：`CVS-mobile/utils/request.js`
- API 服务层：`CVS-mobile/api/`
- 全局配置：`CVS-mobile/config/index.js`

## 3. 后端目录结构（读代码的入口）

- 启动类：`CVS-backend/src/main/java/com/hngy/cvs/CvsApplication.java`
- 控制器层（API 入口）：`CVS-backend/src/main/java/com/hngy/cvs/controller/`
- Service 接口：`CVS-backend/src/main/java/com/hngy/cvs/service/`
- Service 实现：`CVS-backend/src/main/java/com/hngy/cvs/service/impl/`
- 实体类：`CVS-backend/src/main/java/com/hngy/cvs/entity/`
- Mapper：`CVS-backend/src/main/java/com/hngy/cvs/mapper/`
- 通用组件：`CVS-backend/src/main/java/com/hngy/cvs/common/`
- 配置：`CVS-backend/src/main/resources/application.yml`、`CVS-backend/src/main/resources/application-dev.yml`
- 数据库脚本：`CVS-backend/src/main/resources/sql/cvs_db.sql`、`CVS-backend/src/main/resources/sql/test_data.sql`

## 4. 后端 API 模块索引（按 controller 找功能）

> 说明：毕业设计书里的“功能模块与接口设计/权限设计”，一般从这些控制器入手。

- 认证/注册/找回密码：`CVS-backend/src/main/java/com/hngy/cvs/controller/AuthController.java`
- 用户管理：`CVS-backend/src/main/java/com/hngy/cvs/controller/UserController.java`
- 活动管理：`CVS-backend/src/main/java/com/hngy/cvs/controller/ActivityController.java`
- 报名与审核：`CVS-backend/src/main/java/com/hngy/cvs/controller/SignupController.java`
- 签到/签退（二维码 token、学生签到签退、教师评分审核）：`CVS-backend/src/main/java/com/hngy/cvs/controller/CheckController.java`
- 服务记录：`CVS-backend/src/main/java/com/hngy/cvs/controller/RecordController.java`
- 积分：`CVS-backend/src/main/java/com/hngy/cvs/controller/PointsController.java`
- 证明/证书：`CVS-backend/src/main/java/com/hngy/cvs/controller/CertificateController.java`
- 通知中心：`CVS-backend/src/main/java/com/hngy/cvs/controller/NotificationController.java`
- 统计报表（含导出）：`CVS-backend/src/main/java/com/hngy/cvs/controller/StatisticsController.java`
- 文件上传/静态资源：`CVS-backend/src/main/java/com/hngy/cvs/controller/FileController.java`、`CVS-backend/src/main/java/com/hngy/cvs/controller/StaticResourceController.java`
- 商城：商品：`CVS-backend/src/main/java/com/hngy/cvs/controller/ProductController.java`
- 商城：分类：`CVS-backend/src/main/java/com/hngy/cvs/controller/CategoryController.java`
- 商城：兑换/核销：`CVS-backend/src/main/java/com/hngy/cvs/controller/RedemptionController.java`

## 5. 权限与认证（毕业设计书常用段落）

- Spring Security 入口：`CVS-backend/src/main/java/com/hngy/cvs/common/config/SecurityConfig.java`
  - 采用 `@PreAuthorize(...)` 做方法级权限（控制器里很常见）
- JWT 工具：`CVS-backend/src/main/java/com/hngy/cvs/common/util/JwtUtil.java`
- JWT 过滤器：`CVS-backend/src/main/java/com/hngy/cvs/common/security/JwtAuthenticationFilter.java`
  - 支持从 `Authorization: Bearer <token>` 获取，也支持 URL 参数 `token`（用于 H5 预览/下载场景）
- 登录实现：`CVS-backend/src/main/java/com/hngy/cvs/service/impl/UserServiceImpl.java`（`login()`、`register()`、`forgotPassword()`、`resetPassword()`）

## 6. 核心业务链路（按“流程图/时序图”写设计书）

### 6.1 活动发布与审核

- 教师/管理员创建/更新/删除：`CVS-backend/src/main/java/com/hngy/cvs/controller/ActivityController.java`
- 发布与审核接口：同上（`/publish`、`/approve`）

### 6.2 报名与报名审核

- 学生报名、学生取消：`CVS-backend/src/main/java/com/hngy/cvs/controller/SignupController.java`
- 教师/管理员审核（含批量）：同上（`approve/reject/batch`）

### 6.3 二维码签到/签退 + 教师审核评分 + 积分发放 + 服务记录生成

- API 入口：`CVS-backend/src/main/java/com/hngy/cvs/controller/CheckController.java`
- 业务实现（关键）：`CVS-backend/src/main/java/com/hngy/cvs/service/impl/CheckServiceImpl.java`
  - 生成签到/签退 token（TTL=5分钟）
  - 学生签到/签退
  - 教师审核（评分/评语）：
    - 首次确认会创建服务记录：`CVS-backend/src/main/java/com/hngy/cvs/service/impl/RecordServiceImpl.java`（`createServiceRecordFromSignup()`）
    - 依据活动基础积分与评分比例发放积分：`CheckServiceImpl.handlePointsAward()` → `PointsServiceImpl.awardPoints()`

### 6.4 证明/证书（资格验证 + PDF 生成）

- 资格策略（按累计服务时长）：`CVS-backend/src/main/java/com/hngy/cvs/service/strategy/ServiceHoursEligibilityStrategy.java`
- 申请/审批/编号/生成PDF：`CVS-backend/src/main/java/com/hngy/cvs/service/impl/CertificateServiceImpl.java`
- PDF 模板填充：`CVS-backend/src/main/java/com/hngy/cvs/service/impl/PdfServiceImpl.java`
- 模板与字体（resources）：`CVS-backend/src/main/resources/assets/`、`CVS-backend/src/main/resources/fonts/`

### 6.5 积分商城（商品-兑换-核销）

- 兑换扣库存、扣积分、生成凭证：`CVS-backend/src/main/java/com/hngy/cvs/service/impl/RedemptionServiceImpl.java`
- 商品管理/库存：`CVS-backend/src/main/java/com/hngy/cvs/service/impl/ProductServiceImpl.java`
- 管理端统计与导出：`CVS-backend/src/main/java/com/hngy/cvs/controller/StatisticsController.java`

### 6.6 通知中心

- API：`CVS-backend/src/main/java/com/hngy/cvs/controller/NotificationController.java`
- 发送通知的调用点：签退申请/签退结果等在 `CVS-backend/src/main/java/com/hngy/cvs/service/impl/CheckServiceImpl.java` 可直接追踪

## 7. 数据库索引（写“数据库设计/表结构说明”）

SQL 脚本：`CVS-backend/src/main/resources/sql/cvs_db.sql`

主要表（脚本里按编号定义）：

- `user_twb`：用户（`role`：STUDENT/TEACHER/ADMIN；逻辑删除字段 `deleted`）
- `activity_twb`：活动（状态：DRAFT/PENDING_APPROVAL/PUBLISHED/ONGOING/COMPLETED/CANCELLED/REJECTED；含 `points` 奖励积分）
- `signup_twb`：报名（状态、签到签退、教师评分确认时间等；含 `sign_in_token`/`sign_out_token` 字段但业务上还有独立 token 服务）
- `record_twb`：服务记录（时长、评分、`points_earned`）
- `points_twb`：积分记录/当前积分（代码里倾向把它当“用户总积分表”，见 `PointsServiceImpl`）
- `cert_twb`：证明申请（PENDING/APPROVED/REJECTED；证书编号 `certificate_number`）
- `notification_twb`：通知（类型、标题、内容、已读状态）
- `category_twb`：商品分类
- `product_twb`：商品（`points_required`、`stock`、`stock_warning`、`status` 上架/下架）
- `redemption_twb`：兑换记录（`voucher_code`、核销人、核销时间、状态）

## 8. PC 前端页面/角色导航（从路由直接定位 UI 功能）

路由定义：`CVS-frontend/src/router/index.js`

- `/admin/*`：学工处（管理员）仪表板、用户/活动/统计/记录/积分/商城/审核等
- `/teacher/*`：教师仪表板、我的活动、二维码、签退审核、报名管理、记录等
- `/student/*`：学生仪表板、活动列表、报名、签到相关、积分中心、证明、商城等
- 通用：`/notifications` 通知中心、`/profile` 个人资料、活动详情页等

路由守卫：`CVS-frontend/src/router/guards.js`（基于 `meta.requiresAuth` 与 `meta.roles` 控制访问）

## 9. 三端与后端对接（“接口对接与环境配置”常用）

### PC 前端

- API baseURL：`CVS-frontend/.env.development`（`VITE_API_BASE_URL=.../api`）
- Axios：`CVS-frontend/src/utils/request.js`（自动加 `Bearer token`，统一处理 `res.code===200`）

### 移动端

- API baseURL：`CVS-mobile/.env.development`（手机调试通常写局域网 IP）
- 请求封装：`CVS-mobile/utils/request.js`
- API 服务：`CVS-mobile/api/*`

### 后端端口/配置位置（不要死记端口，直接看配置）

- Spring profile 与通用配置：`CVS-backend/src/main/resources/application.yml`
- 开发环境端口与 DB/Redis/mail：`CVS-backend/src/main/resources/application-dev.yml`

## 10. 需要我帮你写“片段”时的提问模板

你可以直接这样问（我会按本索引去抓材料，不会从头扫项目）：

- “写一段：系统角色与权限控制（JWT+Spring Security），要有实现依据，引用到关键类路径。”
- “写一段：二维码签到签退的业务流程（含教师审核、积分发放、服务记录生成），要能画时序图。”
- “写一段：数据库设计说明，按表逐个解释字段与关系（至少 user/activity/signup/record/points/cert/product/redemption）。”
