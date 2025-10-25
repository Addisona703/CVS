-- 集成测试基础数据
-- 插入测试用户
INSERT INTO user_twb (id, username, password, email, phone, real_name, student_id, role, status) VALUES
(1, 'teacher1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8imdZMfin5HjUa9SjIVqpNlitNXie', 'teacher1@test.com', '13800000001', '张老师', NULL, 'TEACHER', 'ACTIVE'),
(2, 'teacher2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8imdZMfin5HjUa9SjIVqpNlitNXie', 'teacher2@test.com', '13800000002', '李老师', NULL, 'TEACHER', 'ACTIVE'),
(3, 'student1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8imdZMfin5HjUa9SjIVqpNlitNXie', 'student1@test.com', '13800000003', '王同学', '2021001', 'STUDENT', 'ACTIVE'),
(4, 'student2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8imdZMfin5HjUa9SjIVqpNlitNXie', 'student2@test.com', '13800000004', '赵同学', '2021002', 'STUDENT', 'ACTIVE'),
(5, 'student3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8imdZMfin5HjUa9SjIVqpNlitNXie', 'student3@test.com', '13800000005', '刘同学', '2021003', 'STUDENT', 'ACTIVE');

-- 插入测试活动
INSERT INTO activity_twb (id, title, description, location, start_time, end_time, registration_deadline, max_participants, current_participants, status, organizer_id, requirements, contact_info, points) VALUES
(1, '环保志愿活动', '清理校园垃圾，保护环境', '校园内', '2024-12-20 09:00:00', '2024-12-20 17:00:00', '2024-12-19 23:59:59', 20, 2, 'PUBLISHED', 1, '身体健康，有环保意识', '张老师 13800000001', 8),
(2, '图书馆志愿服务', '协助图书馆整理书籍', '图书馆', '2024-12-21 14:00:00', '2024-12-21 18:00:00', '2024-12-20 23:59:59', 15, 1, 'PUBLISHED', 1, '细心负责', '张老师 13800000001', 4),
(3, '社区服务活动', '为社区老人提供帮助', '附近社区', '2024-12-22 08:00:00', '2024-12-22 12:00:00', '2024-12-21 23:59:59', 10, 0, 'PUBLISHED', 2, '有爱心，善于沟通', '李老师 13800000002', 6);

-- 插入测试报名记录
INSERT INTO registration_twb (id, activity_id, user_id, status, application_reason, review_reason, reviewed_by, reviewed_at) VALUES
(1, 1, 3, 'APPROVED', '我对环保很感兴趣', '符合要求', 1, '2024-12-18 10:00:00'),
(2, 1, 4, 'PENDING', '希望为环保贡献力量', NULL, NULL, NULL),
(3, 2, 3, 'REJECTED', '想要帮助整理图书', '时间冲突', 1, '2024-12-18 11:00:00');

-- 插入测试签退记录
INSERT INTO checkout_twb (id, registration_id, checkout_time, checkout_reason, status, review_reason, reviewed_by, reviewed_at) VALUES
(1, 1, '2024-12-20 16:00:00', '身体不适需要提前离开', 'PENDING', NULL, NULL, NULL);

-- 插入测试通知数据
INSERT INTO notification_twb (id, user_id, type, title, content, link_url, is_read, created_time, read_time) VALUES
-- 教师通知
(1, 1, 'REGISTRATION_PENDING', '新的报名申请', '学生赵同学申请参加"环保志愿活动"', '/activities/1/signups', 0, '2024-12-18 09:30:00', NULL),
(2, 1, 'CHECKOUT_PENDING', '签退审核申请', '学生王同学提交了签退申请', '/activities/1/checkouts', 0, '2024-12-20 16:05:00', NULL),
(3, 1, 'ACTIVITY_START', '活动即将开始', '您组织的"环保志愿活动"将在30分钟后开始', '/activities/1', 1, '2024-12-20 08:30:00', '2024-12-20 08:35:00'),

-- 学生通知
(4, 3, 'REGISTRATION_APPROVED', '报名审核通过', '您的"环保志愿活动"报名申请已通过', '/activities/1', 0, '2024-12-18 10:05:00', NULL),
(5, 3, 'REGISTRATION_REJECTED', '报名审核拒绝', '很抱歉，您的"图书馆志愿服务"报名申请未通过，原因：时间冲突', '/activities/2', 1, '2024-12-18 11:05:00', '2024-12-18 11:10:00'),
(6, 4, 'REGISTRATION_PENDING', '报名申请已提交', '您已成功提交"环保志愿活动"的报名申请，请等待审核', '/activities/1', 0, '2024-12-18 09:25:00', NULL);