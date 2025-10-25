package com.hngy.cvs.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hngy.cvs.entity.enums.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知实体类
 */
@Data
@TableName("notification_twb")
public class Notification {

    /**
     * 通知ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 通知类型
     */
    private NotificationType type;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 跳转链接
     */
    private String linkUrl;

    /**
     * 是否已读 0-未读 1-已读
     */
    private Boolean isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;
}
