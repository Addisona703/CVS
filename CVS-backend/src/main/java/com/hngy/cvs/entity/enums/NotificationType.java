package com.hngy.cvs.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知类型枚举
 */
@Getter
@AllArgsConstructor
public enum NotificationType {
    ACTIVITY_START("ACTIVITY_START", "活动开始"),
    ACTIVITY_ONGOING("ACTIVITY_ONGOING", "活动进行中"),
    ACTIVITY_END("ACTIVITY_END", "活动结束"),
    REGISTRATION_PENDING("REGISTRATION_PENDING", "报名待审核"),
    REGISTRATION_APPROVED("REGISTRATION_APPROVED", "报名审核通过"),
    REGISTRATION_REJECTED("REGISTRATION_REJECTED", "报名审核拒绝"),
    CHECKOUT_PENDING("CHECKOUT_PENDING", "签退待审核"),
    CHECKOUT_APPROVED("CHECKOUT_APPROVED", "签退审核通过"),
    CHECKOUT_REJECTED("CHECKOUT_REJECTED", "签退审核拒绝"),
    ACTIVITY_CANCEL("ACTIVITY_CANCEL", "活动已取消"),
    SYSTEM("SYSTEM", "系统通知");

    @EnumValue
    @JsonValue
    private final String code;
    private final String description;
}
