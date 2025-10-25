package com.hngy.cvs.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 活动状态枚举
 * 
 * @author CVS Team
 */
@Getter
@AllArgsConstructor
public enum ActivityStatus {

    DRAFT("DRAFT", "草稿"),
    PENDING_APPROVAL("PENDING_APPROVAL", "待审核"),
    PUBLISHED("PUBLISHED", "已发布"),
    ONGOING("ONGOING", "进行中"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消"),
    REJECTED("REJECTED", "审核拒绝");

    private final String code;
    private final String description;
}
