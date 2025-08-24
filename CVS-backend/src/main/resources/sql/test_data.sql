-- CVS 测试数据脚本
USE `cvs_db`;

-- 清空现有数据
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `cert_twb`;
TRUNCATE TABLE `points_twb`;
TRUNCATE TABLE `record_twb`;
TRUNCATE TABLE `signup_twb`;
TRUNCATE TABLE `activity_twb`;
TRUNCATE TABLE `user_twb`;
SET FOREIGN_KEY_CHECKS = 1;

-- 插入用户数据
INSERT INTO `user_twb` (`id`, `username`, `password`, `name`, `role`, `email`, `phone`, `created_at`, `updated_at`, `deleted`) VALUES
-- 管理员
(1, 'admin', '$2a$10$bJ/7XR8wTltapmgnPjmE2.0./qnxziHQynJC2iv/3T88IUEFVL1pC', '系统管理员', 'ADMIN', 'admin@university.edu.cn', '13800138000', NOW(), NOW(), 0),

-- 教师
(2, 'teacher001', '$2a$10$bJ/7XR8wTltapmgnPjmE2.0./qnxziHQynJC2iv/3T88IUEFVL1pC', '张老师', 'TEACHER', 'teacher001@university.edu.cn', '13800138001', NOW(), NOW(), 0),
(3, 'teacher002', '$2a$10$bJ/7XR8wTltapmgnPjmE2.0./qnxziHQynJC2iv/3T88IUEFVL1pC', '李老师', 'TEACHER', 'teacher002@university.edu.cn', '13800138002', NOW(), NOW(), 0),

-- 学生
(4, 'student001', '$2a$10$bJ/7XR8wTltapmgnPjmE2.0./qnxziHQynJC2iv/3T88IUEFVL1pC', '王小明', 'STUDENT', 'student001@university.edu.cn', '13800138003', NOW(), NOW(), 0),
(5, 'student002', '$2a$10$bJ/7XR8wTltapmgnPjmE2.0./qnxziHQynJC2iv/3T88IUEFVL1pC', '李小红', 'STUDENT', 'student002@university.edu.cn', '13800138004', NOW(), NOW(), 0),
(6, 'student003', '$2a$10$bJ/7XR8wTltapmgnPjmE2.0./qnxziHQynJC2iv/3T88IUEFVL1pC', '张小华', 'STUDENT', 'student003@university.edu.cn', '13800138005', NOW(), NOW(), 0),
(7, 'student004', '$2a$10$bJ/7XR8wTltapmgnPjmE2.0./qnxziHQynJC2iv/3T88IUEFVL1pC', '刘小强', 'STUDENT', 'student004@university.edu.cn', '13800138006', NOW(), NOW(), 0),
(8, 'student005', '$2a$10$bJ/7XR8wTltapmgnPjmE2.0./qnxziHQynJC2iv/3T88IUEFVL1pC', '陈小美', 'STUDENT', 'student005@university.edu.cn', '13800138007', NOW(), NOW(), 0);

-- 插入志愿活动数据
INSERT INTO `activity_twb` (`id`, `title`, `description`, `location`, `start_time`, `end_time`, `registration_deadline`, `max_participants`, `status`, `organizer_id`, `requirements`, `contact_info`, `points`, `created_at`, `updated_at`, `deleted`) VALUES
(1, '社区环保志愿活动', '组织学生参与社区环保活动，清理公园垃圾，宣传环保知识', '中央公园', '2024-03-01 09:00:00', '2024-03-01 17:00:00', '2024-02-28 23:59:59', 50, 'PUBLISHED', 2, '身体健康，有环保意识，能够坚持8小时工作', '张老师 13800138001', 10, NOW(), NOW(), 0),
(2, '敬老院关爱活动', '前往敬老院陪伴老人，为老人表演节目，帮助打扫卫生', '阳光敬老院', '2024-03-05 14:00:00', '2024-03-05 18:00:00', '2024-03-04 12:00:00', 30, 'PUBLISHED', 2, '有爱心，善于沟通，尊敬老人', '张老师 13800138001', 8, NOW(), NOW(), 0),
(3, '图书馆志愿服务', '协助图书馆整理书籍，维护阅读秩序，为读者提供咨询服务', '学校图书馆', '2024-03-10 08:00:00', '2024-03-10 18:00:00', NULL, 20, 'PUBLISHED', 3, '熟悉图书分类，有责任心，服务态度好', '李老师 13800138002', 6, NOW(), NOW(), 0),
(4, '校园清洁日活动', '组织全校师生进行校园环境清洁，美化校园环境', '校园各区域', '2024-03-15 07:00:00', '2024-03-15 12:00:00', '2024-03-14 18:00:00', 100, 'DRAFT', 2, '身体健康，不怕脏不怕累', '张老师 13800138001', 5, NOW(), NOW(), 0),
(5, '交通安全宣传活动', '在校门口和周边路口进行交通安全宣传，维护交通秩序', '学校周边路口', '2024-03-20 07:30:00', '2024-03-20 08:30:00', NULL, 15, 'PUBLISHED', 3, '形象良好，声音洪亮，有责任心', '李老师 13800138002', 4, NOW(), NOW(), 0);

-- 插入活动报名数据
INSERT INTO `signup_twb` (`id`, `activity_id`, `user_id`, `status`, `reason`, `reject_reason`, `signed_in`, `signed_out`, `sign_in_time`, `sign_out_time`, `created_at`, `updated_at`) VALUES
(1, 1, 4, 'APPROVED', '我对环保很感兴趣，希望能为环境保护贡献自己的力量', NULL, 1, 1, '2024-03-01 08:50:00', '2024-03-01 17:10:00', NOW(), NOW()),
(2, 1, 5, 'APPROVED', '想通过志愿活动提高自己的环保意识', NULL, 1, 0, '2024-03-01 08:55:00', NULL, NOW(), NOW()),
(3, 1, 6, 'PENDING', '希望参与环保活动，学习环保知识', NULL, 0, 0, NULL, NULL, NOW(), NOW()),
(4, 2, 4, 'APPROVED', '我很喜欢和老人聊天，希望能给他们带来快乐', NULL, 0, 0, NULL, NULL, NOW(), NOW()),
(5, 2, 7, 'APPROVED', '想要关爱老人，传递温暖', NULL, 1, 1, '2024-03-05 13:50:00', '2024-03-05 18:05:00', NOW(), NOW()),
(6, 3, 5, 'APPROVED', '我经常去图书馆，熟悉图书分类', NULL, 0, 0, NULL, NULL, NOW(), NOW()),
(7, 3, 8, 'REJECTED', '想要帮助图书馆整理书籍', '时间冲突，无法参加', 0, 0, NULL, NULL, NOW(), NOW()),
(8, 5, 6, 'PENDING', '希望参与交通安全宣传，提高大家的安全意识', NULL, 0, 0, NULL, NULL, NOW(), NOW());

-- 插入服务记录数据
INSERT INTO `record_twb` (`id`, `user_id`, `activity_id`, `duration_minutes`, `description`, `evaluation`, `rating`, `points_earned`, `created_at`, `updated_at`) VALUES
(1, 4, 1, 480, '参与了整天的环保活动，清理了大量垃圾，向市民宣传环保知识', '表现积极，工作认真负责，是优秀的志愿者', 5, 10, NOW(), NOW()),
(2, 7, 2, 240, '在敬老院陪伴老人，为老人表演了节目，帮助打扫卫生', '与老人相处融洽，服务态度很好', 4, 8, NOW(), NOW());

-- 插入积分记录数据（手动调整积分）
INSERT INTO `points_twb` (`id`, `user_id`, `points`) VALUES
(1, 4, 5),  -- 张三获得额外奖励积分
(2, 7, -2); -- 李四因某种原因扣除积分

-- 插入志愿证明数据
INSERT INTO `cert_twb` (`id`, `user_id`, `purpose`, `start_date`, `end_date`, `status`, `reject_reason`, `approver_id`, `approved_at`, `certificate_number`, `created_at`, `updated_at`) VALUES
(1, 4, '申请奖学金需要', '2024-01-01', '2024-03-01', 'APPROVED', NULL, 1, NOW(), 'CVS202400001', NOW(), NOW()),
(2, 7, '求职简历使用', '2024-02-01', '2024-03-05', 'PENDING', NULL, NULL, NULL, NULL, NOW(), NOW());

-- 注意：所有密码都是 "123456" 的BCrypt加密结果
