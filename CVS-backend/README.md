# CVS 高校志愿服务数字化系统

## 📋 项目简介

CVS (College Volunteer Service) 是一个基于 Vue3 + Spring Boot 3 的高校志愿服务管理平台，支持三种用户角色：学生、教师、管理员。系统提供志愿活动管理、报名审核、服务记录、积分系统和证明管理等功能。

## 🚀 技术栈

### 前端技术栈
- **Vue 3.3+** - 渐进式JavaScript框架
- **Element Plus 2.4+** - UI组件库
- **Vue Router 4.2+** - 路由管理
- **Pinia 2.1+** - 状态管理
- **Axios 1.5+** - HTTP客户端
- **Vite 4.4+** - 构建工具

### 后端技术栈
- **Spring Boot 3.0.2** - 应用框架
- **Spring Security 6.0+** - 安全框架
- **MyBatis-Plus 3.5.3+** - ORM框架
- **JWT 0.11.5** - 令牌认证
- **MySQL 8.0+** - 关系数据库
- **Redis 7.0+** - 缓存数据库
- **Swagger/OpenAPI 3** - API文档

## 📁 项目结构

```
CVS-学生志愿服务数字化/
├── src/                           # 后端源码
│   ├── main/java/com/hngy/cvs/
│   │   ├── common/               # 公共模块
│   │   ├── controller/           # 控制器层
│   │   ├── dto/                  # 数据传输对象
│   │   ├── entity/               # 实体类
│   │   ├── mapper/               # 数据访问层
│   │   ├── service/              # 服务层
│   │   └── CvsApplication.java   # 启动类
│   └── main/resources/
│       ├── application.yml       # 配置文件
│       └── sql/                  # 数据库脚本
├── cvs-frontend/                 # 前端源码
│   ├── src/
│   │   ├── api/                  # API接口
│   │   ├── assets/               # 静态资源
│   │   ├── components/           # 公共组件
│   │   ├── layouts/              # 布局组件
│   │   ├── router/               # 路由配置
│   │   ├── stores/               # 状态管理
│   │   ├── utils/                # 工具函数
│   │   └── views/                # 页面组件
│   ├── package.json              # 依赖配置
│   └── vite.config.js            # 构建配置
├── docker-compose.yml            # Docker编排
├── Dockerfile                    # 后端Docker文件
└── README.md                     # 项目文档
```

## 🛠️ 环境要求

### 开发环境
- **JDK**: 17+
- **Node.js**: 18+
- **MySQL**: 8.0+
- **Redis**: 7.0+ (可选)
- **Maven**: 3.6+

### 生产环境
- **Docker**: 20.10+
- **Docker Compose**: 2.0+

## 🚀 快速开始

### 方式一：Docker 部署（推荐）

1. **克隆项目**
```bash
git clone <repository-url>
cd CVS-学生志愿服务数字化
```

2. **启动服务**
```bash
docker-compose up -d
```

3. **访问应用**
- 前端地址: http://localhost
- 后端API: http://localhost:8080
- API文档: http://localhost:8080/swagger-ui.html

### 方式二：本地开发

#### 后端启动

1. **配置数据库**
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE cvs_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据库脚本
mysql -u root -p cvs_db < src/main/resources/sql/cvs_db.sql
mysql -u root -p cvs_db < src/main/resources/sql/test_data.sql
```

2. **修改配置**
```yaml
# src/main/resources/application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cvs_db
    username: root
    password: your_password
```

3. **启动后端**
```bash
mvn clean compile
mvn spring-boot:run
```

#### 前端启动

1. **安装依赖**
```bash
cd cvs-frontend
npm install
```

2. **启动开发服务器**
```bash
npm run dev
```

3. **访问应用**
- 前端地址: http://localhost:3000
- 后端API: http://localhost:8080

## 👥 默认账号

系统预置了以下测试账号：

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | 123456 | 系统管理员 |
| 教师 | teacher001 | 123456 | 张老师 |
| 教师 | teacher002 | 123456 | 李老师 |
| 学生 | student001 | 123456 | 王小明 |
| 学生 | student002 | 123456 | 李小红 |

## 📖 功能特性

### 🔐 用户管理
- 多角色注册登录（学生、教师、管理员）
- JWT令牌认证
- 角色权限控制
- 用户信息管理

### 📅 活动管理
- 活动创建与发布
- 活动报名与审核
- 活动状态管理
- 活动搜索与筛选

### 📝 服务记录
- 签到签退管理
- 服务时长统计
- 服务评价管理
- 服务记录查询

### ⭐ 积分系统
- 积分奖励机制
- 积分排行榜
- 积分记录查询
- 积分类型管理

### 📜 证明管理
- 志愿证明申请
- 证明审核流程
- 证明编号生成
- 证明查询验证

## 🔧 开发指南

### API 文档
启动后端服务后，访问 http://localhost:8080/swagger-ui.html 查看完整的API文档。

### 数据库设计
详细的数据库设计请参考 `src/main/resources/sql/cvs_db.sql` 文件。

### 前端开发
```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 代码检查
npm run lint
```

### 后端开发
```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 打包项目
mvn clean package

# 启动应用
mvn spring-boot:run
```

## 📦 部署说明

### Docker 部署
```bash
# 构建并启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 生产环境配置
1. 修改 `application-prod.yml` 中的数据库连接信息
2. 配置 Redis 连接信息
3. 设置 JWT 密钥
4. 配置文件上传路径

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系我们

- 项目维护者: CVS Team
- 邮箱: cvs@university.edu.cn
- 项目地址: [GitHub Repository](https://github.com/cvs-team/cvs)

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者和用户！
