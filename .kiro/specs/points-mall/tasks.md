# 积分商城实现任务清单

- [x] 1. 创建数据库表和枚举类





  - 创建商品表（product_twb）、兑换记录表（redemption_twb）、商品分类表（category_twb）的SQL脚本
  - 编写ProductStatus和RedemptionStatus枚举类
  - _Requirements: 1.1, 1.2, 1.3_

- [x] 2. 实现后端实体类和Mapper




- [x] 2.1 创建实体类


  - 编写Product、Redemption、Category实体类，使用MyBatis-Plus注解
  - 配置字段映射、自动填充和逻辑删除
  - _Requirements: 1.1, 2.1, 3.1_

- [x] 2.2 创建Mapper接口


  - 编写ProductMapper、RedemptionMapper、CategoryMapper接口
  - 实现自定义查询方法（库存扣减、按凭证查询等）
  - 编写对应的XML映射文件
  - _Requirements: 1.1, 3.1, 5.1_

- [x] 3. 实现商品管理功能








- [x] 3.1 实现ProductService





  - 编写商品CRUD方法（创建、更新、删除、查询）
  - 实现商品列表分页查询和筛选功能
  - 实现库存检查和扣减方法（使用乐观锁）


  - 实现商品状态更新方法
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [x] 3.2 实现ProductController





  - 编写商品管理API接口（POST、PUT、DELETE、GET）
  - 添加权限控制注解（@PreAuthorize）
  - 实现请求参数验证
  - _Requirements: 1.1, 1.2, 1.3, 1.4_

- [x] 4. 实现商品分类功能





- [x] 4.1 实现CategoryService


  - 编写分类CRUD方法
  - 实现删除分类前的商品检查逻辑
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [x] 4.2 实现CategoryController


  - 编写分类管理API接口
  - 添加权限控制
  - _Requirements: 8.1, 8.2, 8.3_

- [x] 5. 实现积分兑换功能




- [x] 5.1 扩展PointsService


  - 实现获取用户总积分方法
  - 实现扣除积分方法
  - 实现退还积分方法
  - 添加积分变动记录
  - _Requirements: 3.1, 3.2, 7.1, 7.3_

- [x] 5.2 实现RedemptionService


  - 编写兑换商品核心方法（事务控制）
  - 实现积分检查、库存检查、积分扣除、记录创建的完整流程
  - 实现凭证编号生成方法（唯一性保证）
  - 实现获取兑换记录列表方法（学生端和学工处端）
  - 实现取消兑换方法（退还积分和库存）
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 4.1, 4.2_

- [x] 5.3 实现RedemptionController


  - 编写兑换商品API接口
  - 编写获取我的兑换记录API接口
  - 编写获取兑换详情API接口
  - 编写取消兑换API接口
  - 添加权限控制和参数验证
  - _Requirements: 3.1, 3.2, 4.1, 4.2_

- [x] 6. 实现兑换核销功能




- [x] 6.1 实现核销Service方法


  - 编写核销凭证方法（状态检查、更新核销信息）
  - 实现按凭证编号查询兑换记录方法
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

- [x] 6.2 实现核销Controller接口


  - 编写核销凭证API接口
  - 编写按凭证查询API接口
  - 添加学工处权限控制
  - _Requirements: 5.1, 5.2, 5.3_

- [x] 7. 实现统计报表功能





- [x] 7.1 实现StatisticsService


  - 编写获取兑换统计数据方法（总次数、总积分、时间范围筛选）
  - 编写获取商品兑换排行方法
  - 编写获取库存预警列表方法
  - 编写导出兑换记录Excel方法
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [x] 7.2 实现StatisticsController


  - 编写统计数据API接口
  - 编写商品排行API接口
  - 编写库存预警API接口
  - 编写导出报表API接口
  - 添加学工处权限控制
  - _Requirements: 6.1, 6.2, 6.3, 6.5_

- [x] 8. 实现异常处理




- [x] 8.1 创建异常类


  - 编写MallException自定义异常类
  - 编写MallErrorCode错误码枚举
  - _Requirements: 3.2, 3.5, 5.4_

- [x] 8.2 实现全局异常处理器


  - 编写MallExceptionHandler处理商城异常
  - 处理参数验证异常
  - 统一返回错误格式
  - _Requirements: 3.2, 3.5, 5.4_

- [-] 9. 实现前端学生端页面


- [x] 9.1 创建商城首页组件


  - 编写MallIndex.vue组件
  - 实现商品网格布局展示
  - 实现分类筛选功能
  - 实现搜索功能
  - 显示用户积分余额
  - 实现商品卡片（图片、名称、积分、库存、兑换按钮）
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 7.1, 7.2_


- [x] 9.2 创建商品详情页组件




  - 编写ProductDetail.vue组件
  - 显示商品完整信息
  - 实现兑换确认对话框
  - 显示积分充足性提示
  - 显示领取说明
  - _Requirements: 2.3, 7.5_

- [x] 9.3 创建我的兑换页面组件




  - 编写MyRedemptions.vue组件
  - 显示兑换记录列表
  - 实现状态筛选
  - 实现查看凭证详情功能
  - _Requirements: 4.1, 4.2_

- [x] 9.4 创建兑换凭证页面组件





  - 编写VoucherDetail.vue组件
  - 集成vue-qrcode生成二维码
  - 显示凭证编号和商品信息
  - 显示领取地点和说明
  - 实现取消兑换功能
  - 根据状态显示不同内容
  - _Requirements: 4.3, 4.4, 4.5_

- [x] 10. 实现前端学工处端页面





- [x] 10.1 创建商品管理页面组件


  - 编写ProductManagement.vue组件
  - 实现商品列表表格展示
  - 实现添加商品对话框
  - 实现编辑商品对话框
  - 实现商品状态切换功能
  - 实现删除商品确认对话框
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [x] 10.2 创建分类管理页面组件


  - 编写CategoryManagement.vue组件
  - 实现分类列表展示
  - 实现添加/编辑分类对话框
  - 实现删除分类确认（检查是否有商品）
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [x] 10.3 创建兑换核销页面组件


  - 编写RedemptionVerify.vue组件
  - 集成vue-qrcode-reader实现扫码功能
  - 实现手动输入凭证编号功能
  - 实现核销确认对话框
  - 显示学生和商品信息
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

- [x] 10.4 创建统计报表页面组件


  - 编写RedemptionStatistics.vue组件
  - 实现统计数据卡片展示
  - 集成ECharts显示商品兑换排行图表
  - 实现库存预警列表
  - 实现时间范围筛选
  - 实现导出Excel功能
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [-] 11. 实现前端API接口

- [x] 11.1 创建商城API模块



  - 编写src/api/mall.js文件
  - 实现商品相关API方法（getProductList、getProductDetail、createProduct等）
  - 实现兑换相关API方法（redeemProduct、getMyRedemptions、verifyRedemption等）
  - 实现分类相关API方法（getCategories、createCategory等）
  - 实现统计相关API方法（getStatistics、exportRedemptions等）
  - _Requirements: 所有需求_

- [-] 12. 配置前端路由

- [x] 12.1 添加商城路由



  - 在router/index.js中添加学生端商城路由
  - 在router/index.js中添加学工处端商城管理路由
  - 配置路由守卫（权限检查）
  - _Requirements: 所有需求_

- [x] 13. 集成到现有系统




- [x] 13.1 更新导航菜单


  - 在学生端布局中添加"积分商城"菜单项
  - 在学工处端布局中添加"商城管理"菜单项
  - _Requirements: 所有需求_

- [x] 13.2 更新用户角色枚举


  - 确认UserRole枚举中包含ADMIN角色（学工处）
  - 更新权限配置，将原管理员权限映射到学工处
  - _Requirements: 所有需求_

- [ ]* 14. 编写测试用例
- [ ]* 14.1 编写后端单元测试
  - 编写ProductServiceTest测试商品管理功能
  - 编写RedemptionServiceTest测试兑换流程
  - 编写CategoryServiceTest测试分类管理
  - 测试异常场景（积分不足、库存不足、重复核销等）
  - _Requirements: 3.2, 3.5, 5.4_

- [ ]* 14.2 编写后端集成测试
  - 编写ProductControllerTest测试商品API
  - 编写RedemptionControllerTest测试兑换API
  - 测试权限控制
  - _Requirements: 所有需求_

- [ ]* 14.3 编写前端组件测试
  - 测试商品列表组件渲染
  - 测试兑换流程交互
  - 测试核销功能
  - _Requirements: 所有需求_

- [ ] 15. 准备测试数据
- [ ] 15.1 编写测试数据SQL脚本
  - 创建测试商品分类数据
  - 创建测试商品数据
  - 创建测试用户积分数据
  - _Requirements: 所有需求_
