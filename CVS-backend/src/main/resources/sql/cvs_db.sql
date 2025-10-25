-- CVS 高校志愿服务数字化系统数据库脚本
-- 创建数据库
DROP DATABASE IF EXISTS `cvs_db`;
CREATE DATABASE `cvs_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `cvs_db`;

-- 1. 用户表
CREATE TABLE `user_twb` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
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

-- 2. 志愿活动表
CREATE TABLE `activity_twb` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(200) NOT NULL COMMENT '活动标题',
  `description` text NOT NULL COMMENT '活动描述',
  `location` varchar(200) NOT NULL COMMENT '活动地点',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `registration_deadline` datetime DEFAULT NULL COMMENT '活动报名截止时间',
  `max_participants` int NOT NULL COMMENT '最大参与人数',
  `status` varchar(20) NOT NULL DEFAULT 'DRAFT' COMMENT '活动状态（DRAFT-草稿/PENDING_APPROVAL-待审核/PUBLISHED-已发布/ONGOING-进行中/COMPLETED-已完成/CANCELLED-已取消/REJECTED-审核拒绝）',
  `organizer_id` bigint NOT NULL COMMENT '组织者ID',
  `requirements` text COMMENT '参与要求',
  `contact_info` varchar(200) COMMENT '联系方式',
  `points` int NOT NULL DEFAULT '0' COMMENT '奖励积分',
  `approver_id` bigint DEFAULT NULL COMMENT '审核人ID',
  `approved_at` datetime DEFAULT NULL COMMENT '审核时间',
  `reject_reason` text COMMENT '拒绝原因',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_organizer_id` (`organizer_id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_approver_id` (`approver_id`),
  CONSTRAINT `fk_activity_organizer` FOREIGN KEY (`organizer_id`) REFERENCES `user_twb` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_activity_approver` FOREIGN KEY (`approver_id`) REFERENCES `user_twb` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿活动表';

-- 3. 活动报名表
CREATE TABLE `signup_twb` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '报名状态',
  `reason` text COMMENT '报名理由',
  `reject_reason` text COMMENT '拒绝原因',
  `signed_in` tinyint NOT NULL DEFAULT '0' COMMENT '是否签到',
  `signed_out` tinyint NOT NULL DEFAULT '0' COMMENT '是否签退',
  `sign_in_time` datetime DEFAULT NULL COMMENT '签到时间',
  `sign_out_time` datetime DEFAULT NULL COMMENT '签退时间',
  `student_rating` int DEFAULT NULL COMMENT '学生自评分（1-5）',
  `student_evaluation` text COMMENT '学生自评内容',
  `teacher_rating` int DEFAULT NULL COMMENT '教师最终评分（触发积分计算）',
  `teacher_evaluation` text COMMENT '教师调整/最终评语',
  `teacher_rating_confirmed_at` datetime DEFAULT NULL COMMENT '教师确认最终评分时间',
  `sign_in_token` varchar(128) DEFAULT NULL COMMENT '签到二维码token',
  `sign_in_token_expires_at` datetime DEFAULT NULL COMMENT '签到token过期时间',
  `sign_out_token` varchar(128) DEFAULT NULL COMMENT '签退二维码token',
  `sign_out_token_expires_at` datetime DEFAULT NULL COMMENT '签退token过期时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_user` (`activity_id`,`user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_signup_activity` FOREIGN KEY (`activity_id`) REFERENCES `activity_twb` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_signup_user` FOREIGN KEY (`user_id`) REFERENCES `user_twb` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动报名表';

-- 4. 服务记录表
CREATE TABLE `record_twb` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `duration_minutes` int NOT NULL COMMENT '服务时长（分钟）',
  `description` text COMMENT '服务描述',
  `evaluation` text COMMENT '服务评价',
  `rating` int DEFAULT NULL COMMENT '评分（1-5）',
  `points_earned` int NOT NULL DEFAULT '0' COMMENT '该服务记录获得的积分数量',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_record_user` FOREIGN KEY (`user_id`) REFERENCES `user_twb` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_record_activity` FOREIGN KEY (`activity_id`) REFERENCES `activity_twb` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务记录表';

-- 5. 积分记录表（仅用于手动调整积分）
CREATE TABLE `points_twb` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `points` int NOT NULL COMMENT '积分数量（正数为增加，负数为扣除）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_points_user` FOREIGN KEY (`user_id`) REFERENCES `user_twb` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分记录表';

-- 6. 志愿证明表
CREATE TABLE `cert_twb` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
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
  CONSTRAINT `fk_certificate_user` FOREIGN KEY (`user_id`) REFERENCES `user_twb` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_certificate_approver` FOREIGN KEY (`approver_id`) REFERENCES `user_twb` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿证明表';

-- 7. 通知表
CREATE TABLE `notification_twb` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `type` varchar(50) NOT NULL COMMENT '通知类型',
  `title` varchar(200) NOT NULL COMMENT '通知标题',
  `content` text COMMENT '通知内容',
  `link_url` varchar(500) COMMENT '跳转链接',
  `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已读 0-未读 1-已读',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_read` (`user_id`, `is_read`),
  KEY `idx_created_time` (`created_time`),
  KEY `idx_type` (`type`),
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user_twb` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- 8. 商品分类表
CREATE TABLE `category_twb` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '分类名称',
  `description` varchar(500) DEFAULT NULL COMMENT '分类描述',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序顺序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标志（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 9. 商品表
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
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标志（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_deleted` (`deleted`),
  KEY `idx_category_status` (`category_id`, `status`),
  KEY `idx_points_stock` (`points_required`, `stock`),
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `category_twb` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 10. 兑换记录表
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
  KEY `idx_created_at` (`created_at`),
  KEY `idx_user_status` (`user_id`, `status`),
  CONSTRAINT `fk_redemption_user` FOREIGN KEY (`user_id`) REFERENCES `user_twb` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_redemption_product` FOREIGN KEY (`product_id`) REFERENCES `product_twb` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_redemption_verifier` FOREIGN KEY (`verified_by`) REFERENCES `user_twb` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='兑换记录表';
