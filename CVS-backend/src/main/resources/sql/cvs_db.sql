-- CVS 高校志愿服务数字化系统数据库脚本
-- 创建数据库
DROP DATABASE IF EXISTS `cvs_db`;
CREATE DATABASE `cvs_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `cvs_db`;

-- 1. 用户表
CREATE TABLE `user_twb` (
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

-- 2. 志愿活动表
CREATE TABLE `activity_twb` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `title` varchar(200) NOT NULL COMMENT '活动标题',
  `description` text NOT NULL COMMENT '活动描述',
  `location` varchar(200) NOT NULL COMMENT '活动地点',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `registration_deadline` datetime DEFAULT NULL COMMENT '活动报名截止时间',
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
  CONSTRAINT `fk_activity_organizer` FOREIGN KEY (`organizer_id`) REFERENCES `user_twb` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿活动表';

-- 3. 活动报名表
CREATE TABLE `signup_twb` (
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
  CONSTRAINT `fk_signup_activity` FOREIGN KEY (`activity_id`) REFERENCES `activity_twb` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_signup_user` FOREIGN KEY (`user_id`) REFERENCES `user_twb` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动报名表';

-- 4. 服务记录表
CREATE TABLE `record_twb` (
  `id` bigint NOT NULL COMMENT '主键ID',
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
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `points` int NOT NULL COMMENT '积分数量（正数为增加，负数为扣除）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_points_user` FOREIGN KEY (`user_id`) REFERENCES `user_twb` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分记录表';

-- 6. 志愿证明表
CREATE TABLE `cert_twb` (
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
  CONSTRAINT `fk_certificate_user` FOREIGN KEY (`user_id`) REFERENCES `user_twb` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_certificate_approver` FOREIGN KEY (`approver_id`) REFERENCES `user_twb` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿证明表';
