# CVS 高校志愿服务数字化系统 - 详细设计文档

## 📋 目录
1. [系统概述](#系统概述)
2. [技术架构](#技术架构)
3. [前端设计](#前端设计)
4. [后端设计](#后端设计)
5. [数据模型设计](#数据模型设计)
6. [API设计](#api设计)
7. [安全设计](#安全设计)
8. [部署架构](#部署架构)

---

## 🎯 系统概述

### 项目简介
CVS (College Volunteer Service) 是一个基于 Vue3 + Spring Boot 3 的高校志愿服务管理平台，支持三种用户角色：学生、教师、管理员。系统提供志愿活动管理、报名审核、服务记录、积分系统和证明管理等功能。

### 核心功能
- **用户管理**: 多角色注册、登录、权限控制
- **活动管理**: 活动创建、发布、报名、审核
- **服务记录**: 签到签退、时长统计、评价管理
- **积分系统**: 积分奖励、排行榜、记录查询
- **证明管理**: 志愿证明申请、审核、签发

---

## 🏗️ 技术架构

### 前端技术栈
```
Vue 3.3+                    // 渐进式JavaScript框架
├── Element Plus 2.4+       // UI组件库
├── Vue Router 4.2+         // 路由管理
├── Pinia 2.1+             // 状态管理
├── Axios 1.5+             // HTTP客户端
├── Vite 4.4+              // 构建工具
└── JavaScript
```

### 后端技术栈
```
Spring Boot 3.0.2           // 应用框架
├── Spring Security 6.0+    // 安全框架
├── Spring Data JPA         // 数据访问
├── MyBatis-Plus 3.5.3+    // ORM框架
├── JWT 0.11.5             // 令牌认证
├── MySQL 8.0+             // 关系数据库
├── Redis 7.0+             // 缓存数据库
├── Swagger/OpenAPI 3      // API文档
└── Maven 3.6+             // 构建工具
```

### 开发环境
- **JDK**: 17+
- **Node.js**: 18+
- **MySQL**: 8.0+
- **Redis**: 7.0+ (可选)

---

## 🎨 前端设计

### 项目结构
```
cvs-frontend/
├── public/                 // 静态资源
├── src/
│   ├── api/               // API接口定义
│   │   ├── auth.js        // 认证相关
│   │   ├── user.js        // 用户管理
│   │   ├── activity.js    // 活动管理
│   │   ├── signup.js      // 报名管理
│   │   ├── points.js      // 积分系统
│   │   ├── certificate.js // 证明管理
│   │   └── statistics.js  // 统计分析
│   ├── assets/            // 静态资源
│   ├── components/        // 公共组件
│   │   ├── common/        // 通用组件
│   │   ├── forms/         // 表单组件
│   │   └── charts/        // 图表组件
│   ├── composables/       // 组合式函数
│   │   ├── useAuth.js     // 认证逻辑
│   │   ├── usePermission.js // 权限控制
│   │   └── useApi.js      // API调用
│   ├── layouts/           // 布局组件
│   │   ├── AuthLayout.vue // 认证布局
│   │   ├── AdminLayout.vue // 管理员布局
│   │   ├── TeacherLayout.vue // 教师布局
│   │   └── StudentLayout.vue // 学生布局
│   ├── router/            // 路由配置
│   │   ├── index.js       // 主路由
│   │   └── guards.js      // 路由守卫
│   ├── stores/            // 状态管理
│   │   ├── auth.js        // 认证状态
│   │   ├── user.js        // 用户状态
│   │   └── app.js         // 应用状态
│   ├── utils/             // 工具函数
│   │   ├── request.js     // HTTP请求
│   │   ├── constants.js   // 常量定义
│   │   └── helpers.js     // 辅助函数
│   ├── views/             // 页面组件
│   │   ├── auth/          // 认证页面
│   │   ├── admin/         // 管理员页面
│   │   ├── teacher/       // 教师页面
│   │   ├── student/       // 学生页面
│   │   └── common/        // 公共页面
│   ├── App.vue            // 根组件
│   └── main.js            // 入口文件
├── package.json           // 依赖配置
└── vite.config.js         // 构建配置
```

### 用户界面分类

#### 🔐 认证模块 (AuthLayout)
```
/auth/
├── login                  // 登录页面
├── register               // 注册页面
├── forgot-password        // 忘记密码
└── reset-password         // 重置密码
```

#### 👨‍💼 管理员模块 (AdminLayout)
```
/admin/
├── dashboard              // 仪表板
├── users                  // 用户管理
├── activities             // 活动管理
├── statistics             // 统计分析
├── records                // 服务记录管理
└── roles                  // 角色管理
```

#### 👨‍🏫 教师模块 (TeacherLayout)
```
/teacher/
├── dashboard              // 工作台
├── activities             // 我的活动
├── activities/create      // 创建活动
├── signups                // 报名管理
├── records                // 服务记录
└── certificates           // 证明审核
```

#### 👨‍🎓 学生模块 (StudentLayout)
```
/student/
├── dashboard              // 个人中心
├── activities             // 活动列表
├── signups                // 我的报名
├── records                // 我的记录
├── points                 // 积分中心
└── certificates           // 证明管理
```

#### 🔄 公共模块
```
/common/
├── profile                // 个人资料
├── activity-detail        // 活动详情
└── not-found              // 404页面
```

### 核心组件设计

#### 1. 布局组件
```vue
<!-- AdminLayout.vue -->
<template>
  <el-container class="admin-layout">
    <el-aside width="250px">
      <AdminSidebar />
    </el-aside>
    <el-container>
      <el-header height="60px">
        <AdminHeader />
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>
```

#### 2. 数据表格组件
```vue
<!-- DataTable.vue -->
<template>
  <div class="data-table">
    <el-table 
      :data="data" 
      :loading="loading"
      @selection-change="handleSelectionChange">
      <slot name="columns"></slot>
    </el-table>
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      @current-change="handlePageChange" />
  </div>
</template>
```

#### 3. 表单组件
```vue
<!-- FormDialog.vue -->
<template>
  <el-dialog 
    v-model="visible" 
    :title="title"
    @close="handleClose">
    <el-form 
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px">
      <slot name="form-items"></slot>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>
```

### 路由设计

#### 路由配置
```javascript
// router/index.js
const routes = [
  {
    path: '/auth',
    component: AuthLayout,
    children: [
      { path: 'login', component: Login },
      { path: 'register', component: Register }
    ]
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, roles: ['ADMIN'] },
    children: [
      { path: 'dashboard', component: AdminDashboard },
      { path: 'users', component: UserManagement }
    ]
  }
]
```

#### 路由守卫
```javascript
// router/guards.js
export function setupRouterGuards(router) {
  router.beforeEach((to, from, next) => {
    const authStore = useAuthStore()
    
    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
      next('/auth/login')
    } else if (to.meta.roles && !hasRole(to.meta.roles)) {
      next('/403')
    } else {
      next()
    }
  })
}
```

---

## ⚙️ 后端设计

### 项目结构
```
CVS/
├── src/main/java/com/hngy/cvs/
│   ├── common/                    // 公共模块
│   │   ├── config/               // 配置类
│   │   │   ├── SecurityConfig.java
│   │   │   ├── SwaggerConfig.java
│   │   │   └── CorsConfig.java
│   │   ├── exception/            // 异常处理
│   │   │   ├── BusinessException.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── result/               // 响应结果
│   │   │   ├── Result.java
│   │   │   ├── ResultCode.java
│   │   │   └── PageResult.java
│   │   ├── security/             // 安全相关
│   │   │   ├── UserPrincipal.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── JwtAuthenticationEntryPoint.java
│   │   └── util/                 // 工具类
│   │       ├── JwtUtil.java
│   │       ├── PasswordUtil.java
│   │       └── DateUtil.java
│   ├── controller/               // 控制器层
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   ├── VolunteerActivityController.java
│   │   ├── ActivitySignupController.java
│   │   ├── ServiceRecordController.java
│   │   ├── PointsController.java
│   │   ├── VolunteerCertificateController.java
│   │   ├── StatisticsController.java
│   │   └── RoleController.java
│   ├── dto/                      // 数据传输对象
│   │   ├── request/              // 请求DTO
│   │   │   ├── LoginDTO.java
│   │   │   ├── RegisterDTO.java
│   │   │   ├── ActivityCreateDTO.java
│   │   │   └── ...
│   │   └── response/             // 响应DTO
│   │       ├── UserVO.java
│   │       ├── ActivityVO.java
│   │       ├── LoginVO.java
│   │       └── ...
│   ├── entity/                   // 实体类
│   │   ├── User.java
│   │   ├── VolunteerActivity.java
│   │   ├── ActivitySignup.java
│   │   ├── ServiceRecord.java
│   │   ├── PointsRecord.java
│   │   ├── VolunteerCertificate.java
│   │   └── enums/                // 枚举类
│   │       ├── UserRole.java
│   │       ├── ActivityStatus.java
│   │       └── SignupStatus.java
│   ├── mapper/                   // 数据访问层
│   │   ├── UserMapper.java
│   │   ├── VolunteerActivityMapper.java
│   │   ├── ActivitySignupMapper.java
│   │   ├── ServiceRecordMapper.java
│   │   ├── PointsRecordMapper.java
│   │   ├── VolunteerCertificateMapper.java
│   │   └── StatisticsMapper.java
│   ├── service/                  // 服务接口
│   │   ├── UserService.java
│   │   ├── VolunteerActivityService.java
│   │   ├── ActivitySignupService.java
│   │   ├── ServiceRecordService.java
│   │   ├── PointsService.java
│   │   ├── VolunteerCertificateService.java
│   │   ├── StatisticsService.java
│   │   └── RoleService.java
│   ├── service/impl/             // 服务实现
│   │   ├── UserServiceImpl.java
│   │   ├── VolunteerActivityServiceImpl.java
│   │   └── ...
│   └── CvsApplication.java       // 启动类
├── src/main/resources/
│   ├── application.yml           // 配置文件
│   ├── application-dev.yml       // 开发环境配置
│   ├── application-prod.yml      // 生产环境配置
│   └── sql/
│       ├── cvs_db.sql           // 数据库脚本
│       └── test_data.sql        // 测试数据
└── pom.xml                      // Maven配置
```

### 控制器层设计

#### 1. 认证控制器 (AuthController)
```java
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto);

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO dto);

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public Result<Void> logout();

    @PostMapping("/send-code")
    @Operation(summary = "发送验证码")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeDTO dto);

    @PostMapping("/verify-code")
    @Operation(summary = "验证验证码")
    public Result<VerifyCodeVO> verifyCode(@Valid @RequestBody VerifyCodeDTO dto);

    @PostMapping("/reset-password")
    @Operation(summary = "重置密码")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto);
}
```

#### 2. 用户管理控制器 (UserController)
```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户信息管理相关接口")
public class UserController {

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #userPrincipal.userId == #id")
    @Operation(summary = "获取用户信息")
    public Result<UserVO> getUserById(@PathVariable Long id,
                                     @AuthenticationPrincipal UserPrincipal userPrincipal);

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #userPrincipal.userId == #id")
    @Operation(summary = "更新用户信息")
    public Result<UserVO> updateUser(@PathVariable Long id,
                                    @Valid @RequestBody UserUpdateDTO dto,
                                    @AuthenticationPrincipal UserPrincipal userPrincipal);

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "分页查询用户列表")
    public Result<PageResult<UserVO>> getUserList(@Valid @ModelAttribute UserQueryDTO queryDTO);

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除用户")
    public Result<Void> deleteUser(@PathVariable Long id);
}
```

#### 3. 活动管理控制器 (VolunteerActivityController)
```java
@RestController
@RequestMapping("/api/activities")
@Tag(name = "志愿活动管理", description = "志愿活动相关接口")
public class VolunteerActivityController {

    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "创建活动")
    public Result<ActivityVO> createActivity(@Valid @RequestBody ActivityCreateDTO dto,
                                           @AuthenticationPrincipal UserPrincipal principal);

    @GetMapping("/{id}")
    @Operation(summary = "获取活动详情")
    public Result<ActivityVO> getActivityById(@PathVariable Long id);

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "更新活动")
    public Result<ActivityVO> updateActivity(@PathVariable Long id,
                                           @Valid @RequestBody ActivityUpdateDTO dto,
                                           @AuthenticationPrincipal UserPrincipal principal);

    @GetMapping
    @Operation(summary = "分页查询活动列表")
    public Result<PageResult<ActivityVO>> getActivityList(@Valid @ModelAttribute ActivitySearchDTO searchDTO);

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "发布活动")
    public Result<Void> publishActivity(@PathVariable Long id,
                                      @AuthenticationPrincipal UserPrincipal principal);

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "取消活动")
    public Result<Void> cancelActivity(@PathVariable Long id,
                                     @AuthenticationPrincipal UserPrincipal principal);

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除活动")
    public Result<Void> deleteActivity(@PathVariable Long id);

    @GetMapping("/my")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取我创建的活动列表")
    public Result<PageResult<ActivityVO>> getMyActivities(@Valid @ModelAttribute ActivitySearchDTO searchDTO,
                                                        @AuthenticationPrincipal UserPrincipal principal);
}
```

#### 4. 统计分析控制器 (StatisticsController)
```java
@RestController
@RequestMapping("/api/statistics")
@Tag(name = "统计分析", description = "角色特定的仪表板统计接口")
public class StatisticsController {

    @GetMapping("/admin-dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取管理员仪表板统计数据")
    public Result<AdminDashboardStatsVO> getAdminDashboardStats();

    @GetMapping("/teacher-dashboard")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取教师仪表板统计数据")
    public Result<TeacherDashboardStatsVO> getTeacherDashboardStats(
            @AuthenticationPrincipal UserPrincipal principal);

    @GetMapping("/student-dashboard")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(summary = "获取学生仪表板统计数据")
    public Result<StudentDashboardStatsVO> getStudentDashboardStats(
            @AuthenticationPrincipal UserPrincipal principal);
}
```

### 服务层设计

#### 1. 用户服务接口 (UserService)
```java
public interface UserService {

    /**
     * 用户注册
     */
    UserVO register(RegisterDTO request);

    /**
     * 用户登录
     */
    LoginVO login(LoginDTO request);

    /**
     * 根据ID获取用户信息
     */
    UserVO getUserById(Long id);

    /**
     * 更新用户信息
     */
    UserVO updateUser(Long id, UserUpdateDTO dto);

    /**
     * 分页查询用户列表
     */
    IPage<UserVO> getUserList(int page, int size);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 根据用户名获取用户
     */
    User getUserByUsername(String username);

    /**
     * 发送验证码
     */
    void sendVerificationCode(SendCodeDTO dto);

    /**
     * 验证验证码
     */
    VerifyCodeVO verifyCode(VerifyCodeDTO dto);

    /**
     * 重置密码
     */
    void resetPassword(ResetPasswordDTO dto);
}
```

#### 2. 活动服务接口 (VolunteerActivityService)
```java
public interface VolunteerActivityService {

    /**
     * 创建活动
     */
    ActivityVO createActivity(ActivityCreateDTO dto, Long organizerId);

    /**
     * 根据ID获取活动详情
     */
    ActivityVO getActivityById(Long id);

    /**
     * 更新活动
     */
    ActivityVO updateActivity(Long id, ActivityUpdateDTO dto, Long organizerId);

    /**
     * 分页查询活动列表
     */
    IPage<ActivityVO> getActivityList(ActivitySearchDTO searchDTO);

    /**
     * 发布活动
     */
    void publishActivity(Long id, Long organizerId);

    /**
     * 取消活动
     */
    void cancelActivity(Long id, Long organizerId);

    /**
     * 删除活动
     */
    void deleteActivity(Long id);

    /**
     * 获取用户创建的活动列表
     */
    IPage<ActivityVO> getMyActivities(ActivitySearchDTO searchDTO, Long organizerId);

    /**
     * 检查用户是否为活动组织者
     */
    boolean isOrganizer(Long activityId, Long userId);
}
```

### 数据访问层设计

#### 1. 用户Mapper (UserMapper)
```java
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM user WHERE username = #{username} AND deleted = 0")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM user WHERE email = #{email} AND deleted = 0")
    User selectByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM user WHERE phone = #{phone} AND deleted = 0")
    User selectByPhone(@Param("phone") String phone);

    /**
     * 统计各角色用户数量
     */
    @Select("SELECT role, COUNT(*) as count FROM user WHERE deleted = 0 GROUP BY role")
    List<Map<String, Object>> countByRole();
}
```

#### 2. 活动Mapper (VolunteerActivityMapper)
```java
@Mapper
public interface VolunteerActivityMapper extends BaseMapper<VolunteerActivity> {

    /**
     * 分页查询活动列表（带搜索条件）
     */
    IPage<ActivityVO> selectActivityPage(IPage<ActivityVO> page, @Param("searchDTO") ActivitySearchDTO searchDTO);

    /**
     * 获取用户创建的活动列表
     */
    IPage<ActivityVO> selectMyActivities(IPage<ActivityVO> page,
                                       @Param("searchDTO") ActivitySearchDTO searchDTO,
                                       @Param("organizerId") Long organizerId);

    /**
     * 统计活动状态分布
     */
    @Select("SELECT status, COUNT(*) as count FROM volunteer_activity WHERE deleted = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus();

    /**
     * 获取热门活动（按报名人数排序）
     */
    @Select("SELECT va.*, COUNT(asu.id) as signup_count " +
            "FROM volunteer_activity va " +
            "LEFT JOIN activity_signup asu ON va.id = asu.activity_id " +
            "WHERE va.deleted = 0 AND va.status = 'PUBLISHED' " +
            "GROUP BY va.id " +
            "ORDER BY signup_count DESC " +
            "LIMIT #{limit}")
    List<ActivityVO> selectPopularActivities(@Param("limit") int limit);
}
```

---

## 📊 数据模型设计

### 实体类设计

#### 1. 用户实体 (User)
```java
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;                    // 主键ID

    private String username;            // 用户名（学号/工号）
    private String password;            // 密码（加密）
    private String name;                // 姓名
    private UserRole role;              // 角色
    private String email;               // 邮箱
    private String phone;               // 手机号

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;    // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;    // 更新时间

    @TableLogic
    private Integer deleted;            // 逻辑删除标志
}
```

#### 2. 志愿活动实体 (VolunteerActivity)
```java
@Data
@TableName("volunteer_activity")
public class VolunteerActivity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;                    // 主键ID

    private String title;               // 活动标题
    private String description;         // 活动描述
    private String location;            // 活动地点
    private LocalDateTime startTime;    // 开始时间
    private LocalDateTime endTime;      // 结束时间
    private Integer maxParticipants;    // 最大参与人数
    private ActivityStatus status;      // 活动状态
    private Long organizerId;           // 组织者ID
    private String requirements;        // 参与要求
    private String contactInfo;         // 联系方式
    private Integer points;             // 奖励积分

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;    // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;    // 更新时间

    @TableLogic
    private Integer deleted;            // 逻辑删除标志
}
```

#### 3. 活动报名实体 (ActivitySignup)
```java
@Data
@TableName("activity_signup")
public class ActivitySignup {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;                    // 主键ID

    private Long activityId;            // 活动ID
    private Long userId;                // 用户ID
    private SignupStatus status;        // 报名状态
    private String reason;              // 报名理由
    private String rejectReason;        // 拒绝原因
    private Boolean signedIn;           // 是否签到
    private Boolean signedOut;          // 是否签退
    private LocalDateTime signInTime;   // 签到时间
    private LocalDateTime signOutTime;  // 签退时间

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;    // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;    // 更新时间
}
```

#### 4. 服务记录实体 (ServiceRecord)
```java
@Data
@TableName("service_record")
public class ServiceRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;                    // 主键ID

    private Long userId;                // 用户ID
    private Long activityId;            // 活动ID
    private Integer durationMinutes;    // 服务时长（分钟）
    private String description;         // 服务描述
    private String evaluation;          // 服务评价
    private Integer rating;             // 评分（1-5）

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;    // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;    // 更新时间
}
```

#### 5. 积分记录实体 (PointsRecord)
```java
@Data
@TableName("points_record")
public class PointsRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;                    // 主键ID

    private Long userId;                // 用户ID
    private Integer points;             // 积分数量
    private PointsType type;            // 积分类型
    private String description;         // 积分描述
    private Long relatedId;             // 关联ID（活动ID等）

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;    // 创建时间
}
```

#### 6. 志愿证明实体 (VolunteerCertificate)
```java
@Data
@TableName("volunteer_certificate")
public class VolunteerCertificate {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;                    // 主键ID

    private Long userId;                // 用户ID
    private String purpose;             // 申请目的
    private LocalDate startDate;        // 服务开始日期
    private LocalDate endDate;          // 服务结束日期
    private CertificateStatus status;   // 证明状态
    private String rejectReason;        // 拒绝原因
    private Long approverId;            // 审批人ID
    private LocalDateTime approvedAt;   // 审批时间
    private String certificateNumber;   // 证明编号

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;    // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;    // 更新时间
}
```

### 枚举类设计

#### 1. 用户角色枚举 (UserRole)
```java
@Getter
@AllArgsConstructor
public enum UserRole {

    STUDENT("STUDENT", "学生"),
    TEACHER("TEACHER", "教师"),
    ADMIN("ADMIN", "管理员");

    private final String code;
    private final String description;

    public static UserRole fromCode(String code) {
        for (UserRole role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("未知的角色代码: " + code);
    }
}
```

#### 2. 活动状态枚举 (ActivityStatus)
```java
@Getter
@AllArgsConstructor
public enum ActivityStatus {

    DRAFT("DRAFT", "草稿"),
    PUBLISHED("PUBLISHED", "已发布"),
    ONGOING("ONGOING", "进行中"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String description;
}
```

#### 3. 报名状态枚举 (SignupStatus)
```java
@Getter
@AllArgsConstructor
public enum SignupStatus {

    PENDING("PENDING", "待审核"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已拒绝"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String description;
}
```

#### 4. 证明状态枚举 (CertificateStatus)
```java
@Getter
@AllArgsConstructor
public enum CertificateStatus {

    PENDING("PENDING", "待审核"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已拒绝");

    private final String code;
    private final String description;
}
```

#### 5. 积分类型枚举 (PointsType)
```java
@Getter
@AllArgsConstructor
public enum PointsType {

    ACTIVITY_PARTICIPATION("ACTIVITY_PARTICIPATION", "活动参与"),
    ACTIVITY_COMPLETION("ACTIVITY_COMPLETION", "活动完成"),
    EXCELLENT_PERFORMANCE("EXCELLENT_PERFORMANCE", "优秀表现"),
    MANUAL_ADJUSTMENT("MANUAL_ADJUSTMENT", "手动调整");

    private final String code;
    private final String description;
}
```

### DTO类设计

#### 1. 请求DTO

##### 登录请求DTO (LoginDTO)
```java
@Data
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private Boolean rememberMe = false;
}
```

##### 注册请求DTO (RegisterDTO)
```java
@Data
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 100, message = "姓名长度不能超过100个字符")
    private String name;

    @NotNull(message = "角色不能为空")
    private UserRole role;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
```

##### 活动创建DTO (ActivityCreateDTO)
```java
@Data
public class ActivityCreateDTO {

    @NotBlank(message = "活动标题不能为空")
    @Size(max = 200, message = "活动标题长度不能超过200个字符")
    private String title;

    @NotBlank(message = "活动描述不能为空")
    @Size(max = 2000, message = "活动描述长度不能超过2000个字符")
    private String description;

    @NotBlank(message = "活动地点不能为空")
    @Size(max = 200, message = "活动地点长度不能超过200个字符")
    private String location;

    @NotNull(message = "开始时间不能为空")
    @Future(message = "开始时间必须是未来时间")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @NotNull(message = "最大参与人数不能为空")
    @Min(value = 1, message = "最大参与人数必须大于0")
    @Max(value = 1000, message = "最大参与人数不能超过1000")
    private Integer maxParticipants;

    @Size(max = 1000, message = "参与要求长度不能超过1000个字符")
    private String requirements;

    @Size(max = 200, message = "联系方式长度不能超过200个字符")
    private String contactInfo;

    @Min(value = 0, message = "奖励积分不能为负数")
    @Max(value = 100, message = "奖励积分不能超过100")
    private Integer points = 0;
}
```

#### 2. 响应DTO

##### 用户视图对象 (UserVO)
```java
@Data
public class UserVO {

    private Long id;
    private String username;
    private String name;
    private UserRole role;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserVO from(User user) {
        return BeanUtil.copyProperties(user, UserVO.class);
    }
}
```

##### 登录响应VO (LoginVO)
```java
@Data
@Builder
public class LoginVO {

    private String token;           // JWT令牌
    private Long expiresIn;         // 过期时间（毫秒）
    private UserVO user;            // 用户信息
}
```

##### 活动视图对象 (ActivityVO)
```java
@Data
public class ActivityVO {

    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer maxParticipants;
    private ActivityStatus status;
    private Long organizerId;
    private String organizerName;
    private String requirements;
    private String contactInfo;
    private Integer points;
    private Integer currentParticipants;    // 当前报名人数
    private Boolean canSignup;              // 是否可以报名
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ActivityVO from(VolunteerActivity activity) {
        return BeanUtil.copyProperties(activity, ActivityVO.class);
    }
}
```

##### 仪表板统计VO
```java
// 管理员仪表板统计
@Data
@Builder
public class AdminDashboardStatsVO {
    private Long totalUsers;
    private Long totalActivities;
    private Long totalSignups;
    private Long totalServiceRecords;
    private String systemUptime;
    private Long onlineUsers;
}

// 教师仪表板统计
@Data
@Builder
public class TeacherDashboardStatsVO {
    private Long myActivitiesCount;
    private Long totalSignupsCount;
    private Long pendingApprovalsCount;
    private Long serviceRecordsCount;
}

// 学生仪表板统计
@Data
@Builder
public class StudentDashboardStatsVO {
    private Long mySignupsCount;
    private Double totalServiceHours;
    private Long totalPoints;
    private Long certificatesCount;
}
```

### 数据库表结构

#### 1. 用户表 (user)
```sql
CREATE TABLE `user` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名（学号/工号）',
  `password` varchar(255) NOT NULL COMMENT '密码（加密）',
  `name` varchar(100) NOT NULL COMMENT '姓名',
  `role` varchar(20) NOT NULL COMMENT '角色（STUDENT/TEACHER/ADMIN）',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标志（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role` (`role`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
```

#### 2. 志愿活动表 (volunteer_activity)
```sql
CREATE TABLE `volunteer_activity` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `title` varchar(200) NOT NULL COMMENT '活动标题',
  `description` text NOT NULL COMMENT '活动描述',
  `location` varchar(200) NOT NULL COMMENT '活动地点',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `max_participants` int NOT NULL COMMENT '最大参与人数',
  `status` varchar(20) NOT NULL DEFAULT 'DRAFT' COMMENT '活动状态',
  `organizer_id` bigint NOT NULL COMMENT '组织者ID',
  `requirements` text COMMENT '参与要求',
  `contact_info` varchar(200) COMMENT '联系方式',
  `points` int NOT NULL DEFAULT '0' COMMENT '奖励积分',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_organizer_id` (`organizer_id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_activity_organizer` FOREIGN KEY (`organizer_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿活动表';
```

#### 3. 活动报名表 (activity_signup)
```sql
CREATE TABLE `activity_signup` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '报名状态',
  `reason` text COMMENT '报名理由',
  `reject_reason` text COMMENT '拒绝原因',
  `signed_in` tinyint NOT NULL DEFAULT '0' COMMENT '是否签到',
  `signed_out` tinyint NOT NULL DEFAULT '0' COMMENT '是否签退',
  `sign_in_time` datetime DEFAULT NULL COMMENT '签到时间',
  `sign_out_time` datetime DEFAULT NULL COMMENT '签退时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_user` (`activity_id`,`user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_signup_activity` FOREIGN KEY (`activity_id`) REFERENCES `volunteer_activity` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_signup_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动报名表';
```

#### 4. 服务记录表 (service_record)
```sql
CREATE TABLE `service_record` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `duration_minutes` int NOT NULL COMMENT '服务时长（分钟）',
  `description` text COMMENT '服务描述',
  `evaluation` text COMMENT '服务评价',
  `rating` int DEFAULT NULL COMMENT '评分（1-5）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_record_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_record_activity` FOREIGN KEY (`activity_id`) REFERENCES `volunteer_activity` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务记录表';
```

#### 5. 积分记录表 (points_record)
```sql
CREATE TABLE `points_record` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `points` int NOT NULL COMMENT '积分数量',
  `type` varchar(50) NOT NULL COMMENT '积分类型（ACTIVITY_PARTICIPATION/ACTIVITY_COMPLETION/EXCELLENT_PERFORMANCE/MANUAL_ADJUSTMENT）',
  `description` varchar(500) COMMENT '积分描述',
  `related_id` bigint DEFAULT NULL COMMENT '关联ID（活动ID等）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_related_id` (`related_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_points_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分记录表';
```

#### 6. 志愿证明表 (volunteer_certificate)
```sql
CREATE TABLE `volunteer_certificate` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `purpose` varchar(500) NOT NULL COMMENT '申请目的',
  `start_date` date NOT NULL COMMENT '服务开始日期',
  `end_date` date NOT NULL COMMENT '服务结束日期',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '证明状态（PENDING/APPROVED/REJECTED）',
  `reject_reason` text COMMENT '拒绝原因',
  `approver_id` bigint DEFAULT NULL COMMENT '审批人ID',
  `approved_at` datetime DEFAULT NULL COMMENT '审批时间',
  `certificate_number` varchar(50) DEFAULT NULL COMMENT '证明编号',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_approver_id` (`approver_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_certificate_number` (`certificate_number`),
  CONSTRAINT `fk_certificate_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_certificate_approver` FOREIGN KEY (`approver_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿证明表';
```

### 数据库关系图

```
用户表 (user)
├── 1:N → 志愿活动表 (volunteer_activity) [organizer_id]
├── 1:N → 活动报名表 (activity_signup) [user_id]
├── 1:N → 服务记录表 (service_record) [user_id]
├── 1:N → 积分记录表 (points_record) [user_id]
├── 1:N → 志愿证明表 (volunteer_certificate) [user_id]
└── 1:N → 志愿证明表 (volunteer_certificate) [approver_id]

志愿活动表 (volunteer_activity)
├── 1:N → 活动报名表 (activity_signup) [activity_id]
└── 1:N → 服务记录表 (service_record) [activity_id]
```

### 索引设计说明

#### 1. 主键索引
- 所有表都使用 `bigint` 类型的主键，支持大数据量
- 使用 `ASSIGN_ID` 策略生成分布式唯一ID

#### 2. 唯一索引
- `user.username`: 确保用户名唯一性
- `activity_signup(activity_id, user_id)`: 确保用户不能重复报名同一活动
- `volunteer_certificate.certificate_number`: 确保证明编号唯一性

#### 3. 普通索引
- **时间索引**: `created_at` 字段用于时间范围查询和排序
- **状态索引**: `status` 字段用于状态筛选
- **外键索引**: 所有外键字段都建立索引，提高关联查询性能
- **业务索引**: 根据常用查询条件建立的复合索引

#### 4. 外键约束
- **CASCADE删除**: 用户删除时，相关的活动、报名、记录等数据一并删除
- **SET NULL**: 审批人删除时，证明记录的审批人字段设为NULL，保留历史记录

---

## 🔌 API设计

### RESTful API规范

#### 1. 基础规范
- **Base URL**: `http://localhost:8080/api`
- **Content-Type**: `application/json`
- **Authorization**: `Bearer <JWT_TOKEN>`
- **HTTP状态码**: 遵循RESTful标准

#### 2. 统一响应格式
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2024-01-12T10:30:00"
}
```

#### 3. 分页响应格式
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

### 核心API接口

#### 1. 认证接口

##### 用户登录
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "student001",
  "password": "123456",
  "rememberMe": false
}

Response:
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "username": "student001",
      "name": "张三",
      "role": "STUDENT",
      "email": "student001@university.edu.cn"
    }
  }
}
```

##### 用户注册
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "student002",
  "password": "123456",
  "name": "李四",
  "role": "STUDENT",
  "email": "student002@university.edu.cn",
  "phone": "13800138002"
}

Response:
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 2,
    "username": "student002",
    "name": "李四",
    "role": "STUDENT",
    "email": "student002@university.edu.cn",
    "phone": "13800138002"
  }
}
```

#### 2. 活动管理接口

##### 创建活动
```http
POST /api/activities
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "title": "社区环保志愿活动",
  "description": "组织学生参与社区环保活动，清理公园垃圾",
  "location": "中央公园",
  "startTime": "2024-02-01T09:00:00",
  "endTime": "2024-02-01T17:00:00",
  "maxParticipants": 50,
  "requirements": "身体健康，有环保意识",
  "contactInfo": "张老师 13800138001",
  "points": 10
}
```

##### 获取活动列表
```http
GET /api/activities?page=1&size=10&title=环保&status=PUBLISHED
Authorization: Bearer <JWT_TOKEN>

Response:
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "社区环保志愿活动",
        "description": "组织学生参与社区环保活动",
        "location": "中央公园",
        "startTime": "2024-02-01T09:00:00",
        "endTime": "2024-02-01T17:00:00",
        "maxParticipants": 50,
        "status": "PUBLISHED",
        "organizerId": 2,
        "organizerName": "张老师",
        "currentParticipants": 25,
        "canSignup": true,
        "points": 10
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

#### 3. 报名管理接口

##### 报名活动
```http
POST /api/activities/1/signup
Authorization: Bearer <JWT_TOKEN>

Response:
{
  "code": 200,
  "message": "报名成功",
  "data": null
}
```

##### 获取我的报名
```http
GET /api/signups/my?page=1&size=10&status=APPROVED
Authorization: Bearer <JWT_TOKEN>

Response:
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "id": 1,
        "activityId": 1,
        "activityTitle": "社区环保志愿活动",
        "status": "APPROVED",
        "signedIn": true,
        "signedOut": false,
        "signInTime": "2024-02-01T09:00:00",
        "createdAt": "2024-01-15T10:00:00"
      }
    ]
  }
}
```

#### 4. 统计接口

##### 学生仪表板统计
```http
GET /api/statistics/student-dashboard
Authorization: Bearer <JWT_TOKEN>

Response:
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "mySignupsCount": 5,
    "totalServiceHours": 24.5,
    "totalPoints": 120,
    "certificatesCount": 2
  }
}
```

---

## 🔒 安全设计

### JWT认证机制

#### 1. JWT配置
```yaml
jwt:
  secret: cvs-secret-key-2024
  expiration: 86400000          # 24小时（毫秒）
  expiration-short: 86400000    # 1天（不记住我）
  expiration-long: 2592000000   # 30天（记住我）
```

#### 2. JWT工具类 (JwtUtil)
```java
@Component
public class JwtUtil {

    /**
     * 生成JWT Token（支持记住我功能）
     */
    public String generateToken(Long userId, String username, String role, Boolean rememberMe) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        Long tokenExpiration = Boolean.TRUE.equals(rememberMe) ? expirationLong : expirationShort;
        return createToken(claims, username, tokenExpiration);
    }

    /**
     * 验证Token
     */
    public Boolean validateToken(String token, String username) {
        final String tokenUsername = getUsernameFromToken(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从Token中获取角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("role", String.class);
    }
}
```

#### 3. 安全配置 (SecurityConfig)
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 公开接口
                .requestMatchers("/api/auth/**", "/", "/health", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 管理员接口
                .requestMatchers("/api/admin/**", "/api/statistics/admin-dashboard").hasRole("ADMIN")
                // 教师接口
                .requestMatchers("/api/teacher/**", "/api/statistics/teacher-dashboard").hasAnyRole("TEACHER", "ADMIN")
                // 学生接口
                .requestMatchers("/api/student/**", "/api/statistics/student-dashboard").hasAnyRole("STUDENT", "ADMIN")
                // 其他接口需要认证
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

#### 4. JWT认证过滤器 (JwtAuthenticationFilter)
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                String username = jwtUtil.getUsernameFromToken(jwt);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    if (jwtUtil.validateToken(jwt, username)) {
                        Long userId = jwtUtil.getUserIdFromToken(jwt);
                        String role = jwtUtil.getRoleFromToken(jwt);

                        // 创建认证对象
                        UserPrincipal userPrincipal = new UserPrincipal(userId, username, role);
                        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("无法设置用户认证", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

### 权限控制

#### 1. 方法级权限控制
```java
// 只有管理员可以访问
@PreAuthorize("hasRole('ADMIN')")
public Result<Void> deleteUser(Long id);

// 教师和管理员可以访问
@PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
public Result<ActivityVO> createActivity(ActivityCreateDTO dto);

// 用户只能访问自己的数据或管理员可以访问所有数据
@PreAuthorize("hasRole('ADMIN') or #userPrincipal.userId == #id")
public Result<UserVO> getUserById(Long id, UserPrincipal userPrincipal);
```

#### 2. 前端权限控制
```javascript
// 权限检查组合式函数
export function usePermission() {
  const authStore = useAuthStore()

  const hasRole = (role) => {
    return authStore.user?.role === role
  }

  const hasAnyRole = (roles) => {
    return roles.includes(authStore.user?.role)
  }

  const hasPermission = (permission) => {
    const userRole = authStore.user?.role
    const permissions = ROLE_PERMISSIONS[userRole] || []
    return permissions.includes(permission)
  }

  return { hasRole, hasAnyRole, hasPermission }
}

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/auth/login')
  } else if (to.meta.roles && !hasAnyRole(to.meta.roles)) {
    next('/403')
  } else {
    next()
  }
})
```

### 数据安全

#### 1. 密码加密
```java
@Component
public class PasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 加密密码
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 验证密码
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
```

#### 2. 敏感数据处理
```java
@Data
public class UserVO {
    private Long id;
    private String username;
    private String name;
    private UserRole role;
    private String email;
    private String phone;

    // 密码字段不包含在VO中，确保不会泄露
    // private String password; // 不包含此字段
}
```

---

## 🚀 部署架构

### 开发环境部署

#### 1. 环境要求
```bash
# 后端环境
JDK 17+
Maven 3.6+
MySQL 8.0+
Redis 7.0+ (可选)

# 前端环境
Node.js 18+
npm 9+ 或 yarn 1.22+
```

#### 2. 后端部署步骤
```bash
# 1. 克隆项目
git clone <repository-url>
cd CVS

# 2. 配置数据库
# 创建数据库
mysql -u root -p
CREATE DATABASE cvs_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据库脚本
mysql -u root -p cvs_db < src/main/resources/sql/cvs_db.sql

# 3. 配置application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cvs_db
    username: root
    password: your_password

# 4. 编译运行
mvn clean compile
mvn spring-boot:run
```

#### 3. 前端部署步骤
```bash
# 1. 进入前端目录
cd cvs-frontend

# 2. 安装依赖
npm install

# 3. 配置环境变量
# .env.development
VITE_API_BASE_URL=http://localhost:8080

# 4. 启动开发服务器
npm run dev
```

### 生产环境部署

#### 1. Docker部署
```dockerfile
# 后端Dockerfile
FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/cvs-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

```dockerfile
# 前端Dockerfile
FROM node:18-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

#### 2. Docker Compose配置
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: cvs_db
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql:/docker-entrypoint-initdb.d

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  backend:
    build: ./CVS
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/cvs_db
      SPRING_REDIS_HOST: redis

  frontend:
    build: ./cvs-frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql_data:
```

#### 3. Nginx配置
```nginx
server {
    listen 80;
    server_name localhost;

    # 前端静态文件
    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 监控与日志

#### 1. 应用监控
```yaml
# application-prod.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always

logging:
  level:
    com.hngy.cvs: INFO
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/cvs.log
```

#### 2. 性能优化建议
- **数据库优化**: 添加适当索引，使用连接池
- **缓存策略**: 使用Redis缓存热点数据
- **前端优化**: 代码分割，懒加载，CDN加速
- **API优化**: 分页查询，避免N+1问题

---

## 📝 总结

本文档详细描述了CVS高校志愿服务数字化系统的完整设计方案，包括：

1. **前端设计**: Vue3 + Element Plus的现代化UI架构
2. **后端设计**: Spring Boot 3 + MyBatis-Plus的微服务架构
3. **数据模型**: 完整的实体关系和数据库设计
4. **API设计**: RESTful风格的接口规范
5. **安全设计**: JWT认证和角色权限控制
6. **部署架构**: 开发和生产环境的部署方案

该系统支持学生、教师、管理员三种角色，提供完整的志愿服务管理功能，具有良好的扩展性和维护性。

