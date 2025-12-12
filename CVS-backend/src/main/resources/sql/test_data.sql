-- CVS 测试数据脚本
USE `cvs_db`;

-- 清空现有数据
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `redemption_twb`;
TRUNCATE TABLE `product_twb`;
TRUNCATE TABLE `category_twb`;
TRUNCATE TABLE `cert_twb`;
TRUNCATE TABLE `points_twb`;
TRUNCATE TABLE `record_twb`;
TRUNCATE TABLE `signup_twb`;
TRUNCATE TABLE `activity_twb`;
TRUNCATE TABLE `user_twb`;
SET FOREIGN_KEY_CHECKS = 1;

-- 插入用户数据
INSERT INTO `user_twb` (`id`, `username`, `password`, `name`, `role`, `email`, `phone`, `created_at`, `updated_at`, `deleted`) VALUES
-- 学工处
(1, 'admin', '$2a$10$bJ/7XR8wTltapmgnPjmE2.0./qnxziHQynJC2iv/3T88IUEFVL1pC', '学工处管理员', 'ADMIN', 'admin@university.edu.cn', '13800138000', NOW(), NOW(), 0),

-- 教师
(2, 'teacher001', '$2a$10$bJ/7XR8wTltapmgnPjmE2.0./qnxziHQynJC2iv/3T88IUEFVL1pC', '张老师', 'TEACHER', 'teacher001@university.edu.cn', '13800138001', NOW(), NOW(), 0),
(3, 'teacher002', '$2a$10$bJ/7XR8wTltapmgnPjmE2.0./qnxziHQynJC2iv/3T88IUEFVL1pC', '李老师', 'TEACHER', 'teacher002@university.edu.cn', '13800138002', NOW(), NOW(), 0),

-- 学生
(4, 'student001', '$2a$10$bJ/7XR8wTltapmgnPjmE2.0./qnxziHQynJC2iv/3T88IUEFVL1pC', '王小明', 'STUDENT', 'student001@university.edu.cn', '13800138003', NOW(), NOW(), 0),
(5, 'student002', '$2a$10$bJ/7XR8wTltapmgnPjmE2.0./qnxziHQynJC2iv/3T88IUEFVL1pC', '李小红', 'STUDENT', 'student002@university.edu.cn', '13800138004', NOW(), NOW(), 0),
(6, 'student003', '$2a$10$bJ/7XR8wTltapmgnPjmE2.0./qnxziHQynJC2iv/3T88IUEFVL1pC', '张小华', 'STUDENT', 'student003@university.edu.cn', '13800138005', NOW(), NOW(), 0),
(7, 'student004', '$2a$10$bJ/7XR8wTltapmgnPjmE2.0./qnxziHQynJC2iv/3T88IUEFVL1pC', '刘小强', 'STUDENT', 'student004@university.edu.cn', '13800138006', NOW(), NOW(), 0),
(8, 'student005', '$2a$10$bJ/7XR8wTltapmgnPjmE2.0./qnxziHQynJC2iv/3T88IUEFVL1pC', '陈小美', 'STUDENT', 'student005@university.edu.cn', '13800138007', NOW(), NOW(), 0);

-- 插入志愿活动数据（包含近90天的活动，用于统计图表展示）
INSERT INTO `activity_twb` (`id`, `title`, `description`, `location`, `start_time`, `end_time`, `registration_deadline`, `max_participants`, `status`, `organizer_id`, `requirements`, `contact_info`, `points`, `created_at`, `updated_at`, `deleted`) VALUES
-- 最近7天的活动
(1, '社区环保志愿活动', '组织学生参与社区环保活动，清理公园垃圾，宣传环保知识', '中央公园', DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 8 HOUR, DATE_SUB(NOW(), INTERVAL 7 DAY), 50, 'COMPLETED', 2, '身体健康，有环保意识，能够坚持8小时工作', '张老师 13800138001', 10, NOW(), NOW(), 0),
(2, '敬老院关爱活动', '前往敬老院陪伴老人，为老人表演节目，帮助打扫卫生', '阳光敬老院', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 4 HOUR, DATE_SUB(NOW(), INTERVAL 6 DAY), 30, 'COMPLETED', 2, '有爱心，善于沟通，尊敬老人', '张老师 13800138001', 8, NOW(), NOW(), 0),
(3, '图书馆志愿服务', '协助图书馆整理书籍，维护阅读秩序，为读者提供咨询服务', '学校图书馆', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 10 HOUR, DATE_SUB(NOW(), INTERVAL 5 DAY), 20, 'COMPLETED', 3, '熟悉图书分类，有责任心，服务态度好', '李老师 13800138002', 6, NOW(), NOW(), 0),
(4, '交通安全宣传活动', '在校门口和周边路口进行交通安全宣传，维护交通秩序', '学校周边路口', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 1 HOUR, DATE_SUB(NOW(), INTERVAL 4 DAY), 15, 'CANCELLED', 3, '形象良好，声音洪亮，有责任心', '李老师 13800138002', 4, NOW(), NOW(), 0),
(5, '校园清洁日活动', '组织全校师生进行校园环境清洁，美化校园环境', '校园各区域', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 5 HOUR, DATE_SUB(NOW(), INTERVAL 3 DAY), 100, 'COMPLETED', 2, '身体健康，不怕脏不怕累', '张老师 13800138001', 5, NOW(), NOW(), 0),
(6, '儿童福利院志愿服务', '陪伴儿童，辅导作业，组织游戏活动', '阳光儿童福利院', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 6 HOUR, DATE_SUB(NOW(), INTERVAL 2 DAY), 25, 'PUBLISHED', 2, '有爱心，善于与儿童沟通', '张老师 13800138001', 8, NOW(), NOW(), 0),
(7, '社区义诊活动', '协助医护人员进行社区义诊，测量血压，发放健康宣传资料', '社区卫生服务中心', NOW(), NOW() + INTERVAL 4 HOUR, DATE_SUB(NOW(), INTERVAL 1 DAY), 20, 'PUBLISHED', 3, '细心负责，有医学常识优先', '李老师 13800138002', 7, NOW(), NOW(), 0),

-- 最近30天的活动
(8, '植树节活动', '参与植树造林，美化环境', '郊区植树基地', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY) + INTERVAL 6 HOUR, DATE_SUB(NOW(), INTERVAL 16 DAY), 80, 'COMPLETED', 2, '身体健康，能够进行体力劳动', '张老师 13800138001', 12, NOW(), NOW(), 0),
(9, '博物馆讲解志愿者', '为游客提供博物馆讲解服务', '市博物馆', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY) + INTERVAL 8 HOUR, DATE_SUB(NOW(), INTERVAL 21 DAY), 15, 'COMPLETED', 3, '普通话标准，有历史文化知识', '李老师 13800138002', 8, NOW(), NOW(), 0),
(10, '地铁站志愿引导', '在地铁站协助乘客购票、引导乘车', '市地铁1号线各站', DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY) + INTERVAL 4 HOUR, DATE_SUB(NOW(), INTERVAL 19 DAY), 40, 'CANCELLED', 2, '服务态度好，善于沟通', '张老师 13800138001', 5, NOW(), NOW(), 0),
(11, '社区文艺演出', '为社区居民表演文艺节目', '社区文化广场', DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY) + INTERVAL 3 HOUR, DATE_SUB(NOW(), INTERVAL 26 DAY), 30, 'COMPLETED', 3, '有文艺特长', '李老师 13800138002', 6, NOW(), NOW(), 0),
(12, '献血宣传活动', '宣传无偿献血知识，鼓励市民参与献血', '市中心广场', DATE_SUB(NOW(), INTERVAL 22 DAY), DATE_SUB(NOW(), INTERVAL 22 DAY) + INTERVAL 5 HOUR, DATE_SUB(NOW(), INTERVAL 23 DAY), 25, 'CANCELLED', 2, '形象良好，善于沟通', '张老师 13800138001', 7, NOW(), NOW(), 0),

-- 最近90天的活动
(13, '春节社区慰问', '春节期间慰问社区困难家庭', '各社区', DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_SUB(NOW(), INTERVAL 60 DAY) + INTERVAL 6 HOUR, DATE_SUB(NOW(), INTERVAL 61 DAY), 50, 'COMPLETED', 2, '有爱心，善于沟通', '张老师 13800138001', 10, NOW(), NOW(), 0),
(14, '寒假支教活动', '前往乡村学校进行支教', '乡村小学', DATE_SUB(NOW(), INTERVAL 70 DAY), DATE_SUB(NOW(), INTERVAL 70 DAY) + INTERVAL 8 HOUR, DATE_SUB(NOW(), INTERVAL 71 DAY), 20, 'COMPLETED', 3, '有教学经验，有耐心', '李老师 13800138002', 15, NOW(), NOW(), 0),
(15, '环保知识讲座', '在社区开展环保知识讲座', '社区活动中心', DATE_SUB(NOW(), INTERVAL 50 DAY), DATE_SUB(NOW(), INTERVAL 50 DAY) + INTERVAL 3 HOUR, DATE_SUB(NOW(), INTERVAL 51 DAY), 30, 'COMPLETED', 2, '有环保知识，善于演讲', '张老师 13800138001', 8, NOW(), NOW(), 0),
(16, '图书捐赠活动', '收集整理图书，捐赠给乡村学校', '学校图书馆', DATE_SUB(NOW(), INTERVAL 45 DAY), DATE_SUB(NOW(), INTERVAL 45 DAY) + INTERVAL 5 HOUR, DATE_SUB(NOW(), INTERVAL 46 DAY), 35, 'COMPLETED', 3, '细心负责，有整理经验', '李老师 13800138002', 6, NOW(), NOW(), 0),
(17, '消防安全演练', '协助消防部门进行消防安全演练', '学校操场', DATE_SUB(NOW(), INTERVAL 80 DAY), DATE_SUB(NOW(), INTERVAL 80 DAY) + INTERVAL 4 HOUR, DATE_SUB(NOW(), INTERVAL 81 DAY), 60, 'CANCELLED', 2, '身体健康，服从指挥', '张老师 13800138001', 7, NOW(), NOW(), 0),
(18, '法律援助宣传', '宣传法律知识，提供法律咨询', '市民广场', DATE_SUB(NOW(), INTERVAL 55 DAY), DATE_SUB(NOW(), INTERVAL 55 DAY) + INTERVAL 6 HOUR, DATE_SUB(NOW(), INTERVAL 56 DAY), 20, 'COMPLETED', 3, '有法律知识，善于沟通', '李老师 13800138002', 9, NOW(), NOW(), 0),
(19, '残疾人运动会志愿者', '协助残疾人运动会的组织和服务', '市体育馆', DATE_SUB(NOW(), INTERVAL 65 DAY), DATE_SUB(NOW(), INTERVAL 65 DAY) + INTERVAL 8 HOUR, DATE_SUB(NOW(), INTERVAL 66 DAY), 50, 'COMPLETED', 2, '有爱心，有耐心，服务态度好', '张老师 13800138001', 10, NOW(), NOW(), 0),
(20, '社区健康讲座', '开展健康知识讲座，普及健康生活方式', '社区卫生服务中心', DATE_SUB(NOW(), INTERVAL 40 DAY), DATE_SUB(NOW(), INTERVAL 40 DAY) + INTERVAL 3 HOUR, DATE_SUB(NOW(), INTERVAL 41 DAY), 25, 'CANCELLED', 3, '有医学或健康知识', '李老师 13800138002', 6, NOW(), NOW(), 0);

-- 插入活动报名数据
INSERT INTO `signup_twb` (`id`, `activity_id`, `user_id`, `status`, `reason`, `reject_reason`, `signed_in`, `signed_out`, `sign_in_time`, `sign_out_time`, `created_at`, `updated_at`) VALUES
-- 已完成的报名（有签到签退）
(1, 1, 4, 'APPROVED', '我对环保很感兴趣，希望能为环境保护贡献自己的力量', NULL, 1, 1, DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 8 HOUR, DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 16 HOUR, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
(2, 2, 7, 'APPROVED', '想要关爱老人，传递温暖', NULL, 1, 1, DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 13 HOUR, DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 17 HOUR, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 5, 4, 'APPROVED', '希望参与校园清洁，美化校园环境', NULL, 1, 1, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 8 HOUR, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 13 HOUR, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 8, 5, 'APPROVED', '想参与植树活动，为环保做贡献', NULL, 1, 1, DATE_SUB(NOW(), INTERVAL 15 DAY) + INTERVAL 8 HOUR, DATE_SUB(NOW(), INTERVAL 15 DAY) + INTERVAL 14 HOUR, DATE_SUB(NOW(), INTERVAL 16 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY)),

-- 已签到但未签退的报名
(5, 1, 5, 'APPROVED', '想通过志愿活动提高自己的环保意识', NULL, 1, 0, DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 8 HOUR, NULL, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),

-- 已通过但未签到的报名
(6, 6, 6, 'APPROVED', '我喜欢和小朋友相处，希望能给他们带来快乐', NULL, 0, 0, NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(7, 7, 8, 'APPROVED', '想参与社区义诊，学习医疗知识', NULL, 0, 0, NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- 待审核的报名
(8, 6, 4, 'PENDING', '我很喜欢和小朋友相处，希望能陪伴他们', NULL, 0, 0, NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9, 7, 5, 'PENDING', '想参与社区义诊活动', NULL, 0, 0, NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- 被拒绝的报名
(10, 3, 6, 'REJECTED', '希望参与图书馆志愿服务', '名额已满', 0, 0, NULL, NULL, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
(11, 4, 8, 'REJECTED', '想参与交通安全宣传', '活动已取消', 0, 0, NULL, NULL, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY));

-- 插入服务记录数据（只有完成签到签退的才有服务记录）
INSERT INTO `record_twb` (`id`, `user_id`, `activity_id`, `duration_minutes`, `description`, `evaluation`, `rating`, `points_earned`, `created_at`, `updated_at`) VALUES
(1, 4, 1, 480, '参与了整天的环保活动，清理了大量垃圾，向市民宣传环保知识', '表现积极，工作认真负责，是优秀的志愿者', 5, 10, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
(2, 7, 2, 240, '在敬老院陪伴老人，为老人表演了节目，帮助打扫卫生', '与老人相处融洽，服务态度很好', 4, 8, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 4, 5, 300, '参与校园清洁日活动，清理校园垃圾，维护校园环境', '工作积极主动，不怕脏不怕累', 5, 5, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 5, 8, 360, '参与植树节活动，种植树苗，为绿化环境做贡献', '认真负责，体力劳动表现出色', 5, 12, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY));

-- 插入积分数据（总积分 = 服务记录积分 + 额外奖励/扣除 - 兑换消耗）
INSERT INTO `points_twb` (`id`, `user_id`, `points`) VALUES
(1, 4, 185),  -- 王小明：服务积分15(10+5) + 额外奖励200 - 兑换消耗30(50-20退回) = 185
(2, 5, 12),   -- 李小红：服务积分12
(3, 6, 50),   -- 张小华：额外奖励50（用于测试兑换）
(4, 7, 8),    -- 刘小强：服务积分8
(5, 8, 100);  -- 陈小美：额外奖励100（用于测试兑换）

-- 插入志愿证明数据
INSERT INTO `cert_twb` (`id`, `user_id`, `purpose`, `start_date`, `end_date`, `status`, `reject_reason`, `approver_id`, `approved_at`, `certificate_number`, `created_at`, `updated_at`) VALUES
-- 已批准的证明
(1, 4, '申请奖学金需要', DATE_SUB(NOW(), INTERVAL 90 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY), 'APPROVED', NULL, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), 'CVS202400001', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 5, '保研材料使用', DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY), 'APPROVED', NULL, 1, DATE_SUB(NOW(), INTERVAL 14 DAY), 'CVS202400002', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY)),

-- 待审核的证明
(3, 7, '求职简历使用', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), 'PENDING', NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 4, '实习证明材料', DATE_SUB(NOW(), INTERVAL 20 DAY), NOW(), 'PENDING', NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- 被拒绝的证明
(5, 6, '申请奖学金', DATE_SUB(NOW(), INTERVAL 30 DAY), NOW(), 'REJECTED', '服务时长不足，需要至少20小时志愿服务时长', 1, DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY));

-- 插入通知数据
INSERT INTO `notification_twb` (`id`, `user_id`, `type`, `title`, `content`, `link_url`, `is_read`, `created_time`, `read_time`) VALUES
(1, 4, 'ACTIVITY_START', '活动开始提醒', '您报名的“社区环保志愿活动”将在30分钟后开始，请准时到达集合地点。', '/activities/1', 1, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY + INTERVAL 10 MINUTE),
(2, 4, 'REGISTRATION_APPROVED', '报名审核通过', '恭喜，您报名的“敬老院关爱活动”已通过审核，请留意活动时间。', '/activities/2', 0, NOW() - INTERVAL 2 DAY, NULL),
(3, 5, 'REGISTRATION_PENDING', '报名待审核', '您报名的“图书馆志愿服务”正在审核中，请耐心等待。', '/activities/3', 0, NOW() - INTERVAL 1 DAY, NULL),
(4, 5, 'ACTIVITY_END', '活动结束反馈', '感谢参加“社区环保志愿活动”，欢迎填写活动反馈表。', '/activities/1/feedback', 1, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY + INTERVAL 15 MINUTE),
(5, 7, 'CHECKOUT_APPROVED', '签退审核通过', '您在“敬老院关爱活动”的签退申请已通过审核。', '/records/2', 1, NOW() - INTERVAL 12 HOUR, NOW() - INTERVAL 11 HOUR),
(6, 8, 'SYSTEM_NOTICE', '系统维护通知', '系统将在本周日凌晨进行维护，届时将暂停服务，请提前安排好您的使用计划。', NULL, 0, NOW() - INTERVAL 6 HOUR, NULL),
(7, 8, 'REGISTRATION_REJECTED', '报名审核未通过', '很抱歉，您报名的“图书馆志愿服务”因名额已满未能通过审核。', '/activities/3', 0, NOW() - INTERVAL 20 HOUR, NULL),
(8, 5, 'CHECKOUT_PENDING', '签退待审核', '您提交的“社区环保志愿活动”签退申请已收到，正在审核中。', '/activities/1', 0, NOW() - INTERVAL 10 HOUR, NULL),
(9, 6, 'CHECKOUT_REJECTED', '签退审核未通过', '您在“社区环保志愿活动”的签退申请因缺少签退照片未通过审核，请补充材料。', '/activities/1', 0, NOW() - INTERVAL 8 HOUR, NULL),
(10, 4, 'SYSTEM_NOTICE', '积分政策更新', '新的志愿者积分政策已上线，请前往积分中心查看详情。', '/points/policy', 1, NOW() - INTERVAL 5 HOUR, NOW() - INTERVAL 4 HOUR + INTERVAL 20 MINUTE),
(11, 7, 'ACTIVITY_START', '活动开始提醒', '您报名的“交通安全宣传活动”将于明日早上开始，请提前集合。', '/activities/5', 0, NOW() - INTERVAL 3 HOUR, NULL),
(12, 2, 'SYSTEM_NOTICE', '审批提醒', '有新的志愿活动报名等待审核，请及时处理。', '/admin/review/signups', 0, NOW() - INTERVAL 90 MINUTE, NULL),
(13, 6, 'REGISTRATION_APPROVED', '报名审核通过', '恭喜，您报名的“交通安全宣传活动”已通过审核。', '/activities/5', 1, NOW() - INTERVAL 70 MINUTE, NOW() - INTERVAL 60 MINUTE),
(14, 5, 'ACTIVITY_END', '活动总结通知', '“敬老院关爱活动”已圆满结束，请填写志愿服务总结表。', '/activities/2/summary', 0, NOW() - INTERVAL 45 MINUTE, NULL),
(15, 4, 'CHECKOUT_PENDING', '签退待审核', '您提交的“敬老院关爱活动”签退信息已收到，审核通过后会第一时间通知您。', '/records/2', 1, NOW() - INTERVAL 30 MINUTE, NOW() - INTERVAL 20 MINUTE);

-- 注意：所有密码都是 "123456" 的BCrypt加密结果

-- 插入商品分类数据
INSERT INTO `category_twb` (`id`, `name`, `description`, `sort_order`, `created_at`, `updated_at`, `deleted`) VALUES
(1, '学习用品', '各类学习相关的文具和用品', 1, NOW(), NOW(), 0),
(2, '生活用品', '日常生活所需的各类用品', 2, NOW(), NOW(), 0),
(3, '电子产品', '各类电子设备和配件', 3, NOW(), NOW(), 0),
(4, '图书音像', '图书、音乐、影视等文化产品', 4, NOW(), NOW(), 0),
(5, '优惠券', '各类商家优惠券和代金券', 5, NOW(), NOW(), 0);

-- 插入商品数据
INSERT INTO `product_twb` (`id`, `name`, `description`, `points_required`, `stock`, `image_url`, `category_id`, `status`, `stock_warning`, `created_at`, `updated_at`, `deleted`) VALUES
-- 学习用品
(1, '中性笔套装（10支装）', '优质中性笔，书写流畅，适合日常学习使用', 50, 100, '/images/products/pen-set.jpg', 1, 1, 10, NOW(), NOW(), 0),
(2, 'A4笔记本（5本装）', '高品质笔记本，纸张厚实，不易渗透', 80, 80, '/images/products/notebook.jpg', 1, 1, 10, NOW(), NOW(), 0),
(3, '文具礼盒套装', '包含笔、橡皮、尺子等常用文具', 150, 50, '/images/products/stationery-set.jpg', 1, 1, 10, NOW(), NOW(), 0),
(4, '计算器', '科学计算器，适合理工科学生使用', 120, 30, '/images/products/calculator.jpg', 1, 1, 5, NOW(), NOW(), 0),

-- 生活用品
(5, '保温杯', '304不锈钢保温杯，保温效果好', 200, 60, '/images/products/thermos.jpg', 2, 1, 10, NOW(), NOW(), 0),
(6, '雨伞', '三折自动伞，防风防雨', 180, 40, '/images/products/umbrella.jpg', 2, 1, 10, NOW(), NOW(), 0),
(7, '帆布袋', '环保帆布袋，结实耐用', 100, 70, '/images/products/canvas-bag.jpg', 2, 1, 10, NOW(), NOW(), 0),
(8, '毛巾套装（3条装）', '纯棉毛巾，吸水性好', 150, 50, '/images/products/towel-set.jpg', 2, 1, 10, NOW(), NOW(), 0),

-- 电子产品
(9, '无线鼠标', '2.4G无线鼠标，静音设计', 300, 25, '/images/products/wireless-mouse.jpg', 3, 1, 5, NOW(), NOW(), 0),
(10, '蓝牙耳机', '入耳式蓝牙耳机，音质清晰', 500, 20, '/images/products/bluetooth-earphone.jpg', 3, 1, 5, NOW(), NOW(), 0),
(11, '移动电源（10000mAh）', '大容量移动电源，支持快充', 400, 15, '/images/products/power-bank.jpg', 3, 1, 5, NOW(), NOW(), 0),
(12, 'U盘（32GB）', '高速USB 3.0 U盘', 250, 35, '/images/products/usb-drive.jpg', 3, 1, 5, NOW(), NOW(), 0),

-- 图书音像
(13, '畅销书籍', '当季热门畅销书籍（随机发放）', 300, 40, '/images/products/bestseller-book.jpg', 4, 1, 10, NOW(), NOW(), 0),
(14, '专业书籍', '各专业经典教材和参考书', 400, 30, '/images/products/textbook.jpg', 4, 1, 10, NOW(), NOW(), 0),
(15, '英语学习资料', '新概念英语（全4册）', 200, 50, '/images/products/english-material.jpg', 4, 1, 10, NOW(), NOW(), 0),

-- 优惠券
(16, '校园书店20元代金券', '可在校园书店使用，满50元可用', 150, 100, '/images/products/bookstore-coupon.jpg', 5, 1, 20, NOW(), NOW(), 0),
(17, '食堂餐券（10元）', '校园食堂通用餐券', 80, 200, '/images/products/canteen-coupon.jpg', 5, 1, 30, NOW(), NOW(), 0),
(18, '咖啡店优惠券', '校园咖啡店饮品8折券', 100, 150, '/images/products/coffee-coupon.jpg', 5, 1, 20, NOW(), NOW(), 0),
(19, '电影票兑换券', '校园周边影院电影票兑换券', 350, 50, '/images/products/movie-ticket.jpg', 5, 1, 10, NOW(), NOW(), 0),
(20, '健身房月卡', '校园健身房一个月使用权', 600, 10, '/images/products/gym-card.jpg', 5, 1, 3, NOW(), NOW(), 0),

-- 库存不足的商品（用于测试库存预警）
(21, '限量版纪念T恤', '学校周年纪念限量版T恤', 280, 5, '/images/products/tshirt.jpg', 2, 1, 10, NOW(), NOW(), 0),
(22, '精美台历', '学校定制精美台历', 120, 8, '/images/products/calendar.jpg', 1, 1, 10, NOW(), NOW(), 0),

-- 已下架的商品
(23, '过期优惠券', '已过期的优惠券，不再提供兑换', 50, 0, '/images/products/expired-coupon.jpg', 5, 0, 10, NOW(), NOW(), 0);

-- 插入兑换记录数据
INSERT INTO `redemption_twb` (`id`, `user_id`, `product_id`, `points_spent`, `voucher_code`, `status`, `verified_by`, `verified_at`, `created_at`, `updated_at`) VALUES
-- 已核销的兑换记录（今日核销 - 用于测试今日核销统计）
(1, 4, 17, 80, 'MALL20240001', 1, 1, NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 2 HOUR),
(2, 5, 2, 80, 'MALL20240002', 1, 1, NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 1 HOUR),

-- 已核销的兑换记录（历史核销 - 用于测试累计核销统计）
(3, 4, 1, 50, 'MALL20240003', 1, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 7, 18, 100, 'MALL20240004', 1, 1, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, 6, 7, 100, 'MALL20240005', 1, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),

-- 待核销的兑换记录（用于测试待核销统计和核销功能）
(6, 4, 16, 150, 'MALL20240006', 0, NULL, NULL, NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 3 HOUR),
(7, 8, 5, 200, 'MALL20240007', 0, NULL, NULL, NOW() - INTERVAL 5 HOUR, NOW() - INTERVAL 5 HOUR),
(8, 6, 18, 100, 'MALL20240008', 0, NULL, NULL, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),

-- 已取消的兑换记录（积分已退回）
(9, 4, 10, 500, 'MALL20240009', 2, NULL, NULL, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY));
