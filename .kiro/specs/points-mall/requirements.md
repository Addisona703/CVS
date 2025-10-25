# 积分商城需求文档

## Introduction

积分商城是CVS志愿服务系统的扩展功能，允许学生使用参与志愿活动获得的积分兑换实物商品或虚拟权益。学生通过积分兑换商品后获得电子凭证，凭此凭证在线下指定地点领取商品。学工处负责商品的上架、库存管理和兑换审核。

## Glossary

- **System**: CVS积分商城系统
- **Student**: 学生用户，可以使用积分兑换商品
- **Staff**: 学工处工作人员，负责商品管理和兑换审核
- **Product**: 可兑换的商品，包括实物商品和虚拟权益
- **Points**: 学生通过参与志愿活动获得的积分
- **Redemption**: 积分兑换记录，包含兑换的商品、消耗的积分和兑换凭证
- **Voucher**: 兑换凭证，学生凭此在线下领取商品
- **Stock**: 商品库存数量
- **Category**: 商品分类，如文具、生活用品、电子产品等

## Requirements

### Requirement 1: 商品管理

**User Story:** 作为学工处工作人员，我希望能够管理积分商城的商品，以便学生可以兑换各种奖励

#### Acceptance Criteria

1. WHEN Staff登录系统，THE System SHALL 显示商品管理界面，包含商品列表、添加商品、编辑商品和删除商品功能
2. WHEN Staff创建新商品，THE System SHALL 要求输入商品名称、描述、所需积分、库存数量、商品图片和商品分类
3. WHEN Staff提交商品信息且所有必填字段有效，THE System SHALL 保存商品信息并在商品列表中显示该商品
4. WHEN Staff编辑商品信息，THE System SHALL 允许修改商品名称、描述、所需积分、库存数量、商品图片、商品分类和商品状态（上架/下架）
5. WHEN Staff删除商品，THE System SHALL 将商品标记为已删除状态而不是物理删除，并且不再在学生端显示

### Requirement 2: 商品浏览与搜索

**User Story:** 作为学生，我希望能够浏览和搜索积分商城的商品，以便找到我想要兑换的商品

#### Acceptance Criteria

1. WHEN Student访问积分商城页面，THE System SHALL 显示所有上架状态且库存大于0的商品列表
2. WHEN Student查看商品列表，THE System SHALL 显示每个商品的名称、图片、所需积分、剩余库存和商品分类
3. WHEN Student点击商品，THE System SHALL 显示商品详情页面，包含完整的商品描述、兑换规则和领取说明
4. WHEN Student在搜索框输入关键词，THE System SHALL 根据商品名称和描述进行模糊搜索并显示匹配结果
5. WHEN Student选择商品分类筛选，THE System SHALL 仅显示该分类下的商品

### Requirement 3: 积分兑换

**User Story:** 作为学生，我希望能够使用积分兑换商品，以便获得志愿服务的实物奖励

#### Acceptance Criteria

1. WHEN Student点击兑换按钮，THE System SHALL 检查Student的当前积分是否大于或等于商品所需积分
2. IF Student积分不足，THEN THE System SHALL 显示错误提示"积分不足，无法兑换"并阻止兑换操作
3. WHEN Student积分充足且商品库存大于0，THE System SHALL 扣除相应积分、减少商品库存数量并创建兑换记录
4. WHEN 兑换成功，THE System SHALL 生成唯一的兑换凭证编号并显示兑换成功页面
5. IF 商品库存为0，THEN THE System SHALL 显示"商品已兑完"提示并禁用兑换按钮

### Requirement 4: 兑换凭证管理

**User Story:** 作为学生，我希望能够查看我的兑换凭证，以便在线下领取商品

#### Acceptance Criteria

1. WHEN Student访问"我的兑换"页面，THE System SHALL 显示Student所有的兑换记录列表
2. WHEN Student查看兑换记录，THE System SHALL 显示商品名称、兑换时间、消耗积分、凭证编号和兑换状态（待领取/已领取/已取消）
3. WHEN Student点击兑换记录，THE System SHALL 显示兑换凭证详情，包含二维码、凭证编号和领取地点信息
4. WHEN Student的兑换状态为"待领取"，THE System SHALL 在凭证页面显示可用于扫码核销的二维码
5. WHEN Student的兑换状态为"已领取"，THE System SHALL 显示领取时间和核销人员信息

### Requirement 5: 兑换核销

**User Story:** 作为学工处工作人员，我希望能够核销学生的兑换凭证，以便学生领取商品

#### Acceptance Criteria

1. WHEN Staff访问核销页面，THE System SHALL 提供扫码核销和手动输入凭证编号两种核销方式
2. WHEN Staff扫描Student的兑换凭证二维码，THE System SHALL 显示该兑换记录的详细信息，包括学生姓名、商品名称和兑换时间
3. WHEN Staff确认核销且兑换状态为"待领取"，THE System SHALL 将兑换状态更新为"已领取"并记录核销时间和核销人员
4. IF 兑换凭证已被核销，THEN THE System SHALL 显示"该凭证已被使用"提示并阻止重复核销
5. WHEN Staff输入无效的凭证编号，THE System SHALL 显示"凭证不存在"错误提示

### Requirement 6: 兑换统计与报表

**User Story:** 作为学工处工作人员，我希望能够查看兑换统计数据，以便了解商品受欢迎程度和库存情况

#### Acceptance Criteria

1. WHEN Staff访问统计页面，THE System SHALL 显示总兑换次数、总消耗积分和各商品的兑换排行
2. WHEN Staff查看商品统计，THE System SHALL 显示每个商品的总兑换次数、剩余库存和库存预警状态
3. WHEN 商品库存低于设定的预警值，THE System SHALL 在统计页面标记该商品为"库存不足"状态
4. WHEN Staff选择时间范围筛选，THE System SHALL 显示该时间段内的兑换统计数据
5. WHEN Staff导出报表，THE System SHALL 生成包含兑换记录详情的Excel文件供下载

### Requirement 7: 积分余额显示

**User Story:** 作为学生，我希望能够随时查看我的积分余额，以便了解我可以兑换哪些商品

#### Acceptance Criteria

1. WHEN Student登录系统，THE System SHALL 在页面顶部显示Student的当前积分余额
2. WHEN Student访问积分商城页面，THE System SHALL 在每个商品上标识Student是否有足够积分兑换
3. WHEN Student查看积分明细，THE System SHALL 显示积分获取记录和积分消耗记录，包括时间、来源/用途和积分变化
4. WHEN Student的积分发生变化，THE System SHALL 实时更新页面上显示的积分余额
5. WHEN Student查看商品详情，THE System SHALL 显示"还需X积分可兑换"提示，如果积分不足

### Requirement 8: 商品分类管理

**User Story:** 作为学工处工作人员，我希望能够管理商品分类，以便更好地组织商品

#### Acceptance Criteria

1. WHEN Staff访问分类管理页面，THE System SHALL 显示所有商品分类列表
2. WHEN Staff创建新分类，THE System SHALL 要求输入分类名称和分类描述
3. WHEN Staff编辑分类，THE System SHALL 允许修改分类名称和分类描述
4. WHEN Staff删除分类且该分类下存在商品，THE System SHALL 显示警告提示并要求先将商品移至其他分类
5. WHEN Staff删除分类且该分类下无商品，THE System SHALL 删除该分类并从分类列表中移除
