-- H2数据库集成测试表结构
-- 用户表
CREATE TABLE IF NOT EXISTS user_twb (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    real_name VARCHAR(50) NOT NULL,
    student_id VARCHAR(20),
    role ENUM('STUDENT', 'TEACHER', 'ADMIN') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'BANNED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) DEFAULT 0
);

-- 活动表
CREATE TABLE IF NOT EXISTS activity_twb (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    location VARCHAR(200) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    registration_deadline DATETIME NOT NULL,
    max_participants INT NOT NULL,
    current_participants INT DEFAULT 0,
    status ENUM('DRAFT', 'PUBLISHED', 'CANCELLED', 'COMPLETED') DEFAULT 'DRAFT',
    organizer_id BIGINT NOT NULL,
    requirements TEXT,
    contact_info VARCHAR(200),
    points INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) DEFAULT 0,
    FOREIGN KEY (organizer_id) REFERENCES user_twb(id)
);

-- 通知表
CREATE TABLE IF NOT EXISTS notification_twb (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    link_url VARCHAR(500),
    is_read TINYINT(1) DEFAULT 0,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    read_time DATETIME,
    deleted TINYINT(1) DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user_twb(id)
);

-- 报名表
CREATE TABLE IF NOT EXISTS registration_twb (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    activity_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    application_reason TEXT,
    review_reason TEXT,
    reviewed_by BIGINT,
    reviewed_at DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) DEFAULT 0,
    FOREIGN KEY (activity_id) REFERENCES activity_twb(id),
    FOREIGN KEY (user_id) REFERENCES user_twb(id),
    FOREIGN KEY (reviewed_by) REFERENCES user_twb(id),
    UNIQUE KEY uk_activity_user (activity_id, user_id, deleted)
);

-- 签退表
CREATE TABLE IF NOT EXISTS checkout_twb (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    registration_id BIGINT NOT NULL,
    checkout_time DATETIME NOT NULL,
    checkout_reason TEXT,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    review_reason TEXT,
    reviewed_by BIGINT,
    reviewed_at DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) DEFAULT 0,
    FOREIGN KEY (registration_id) REFERENCES registration_twb(id),
    FOREIGN KEY (reviewed_by) REFERENCES user_twb(id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_notification_user_id ON notification_twb(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_user_read ON notification_twb(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_notification_created_time ON notification_twb(created_time);
CREATE INDEX IF NOT EXISTS idx_activity_organizer ON activity_twb(organizer_id);
CREATE INDEX IF NOT EXISTS idx_activity_status ON activity_twb(status);
CREATE INDEX IF NOT EXISTS idx_registration_activity ON registration_twb(activity_id);
CREATE INDEX IF NOT EXISTS idx_registration_user ON registration_twb(user_id);
CREATE INDEX IF NOT EXISTS idx_checkout_registration ON checkout_twb(registration_id);