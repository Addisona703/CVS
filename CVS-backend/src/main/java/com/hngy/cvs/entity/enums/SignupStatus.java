package com.hngy.cvs.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 报名状态枚举
 * 
 * @author CVS Team
 */
@Getter
@AllArgsConstructor
public enum SignupStatus {

    PENDING("PENDING", "待审核"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已拒绝"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String description;

    public static SignupStatus fromCode(String code) {
        for (SignupStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的报名状态代码: " + code);
    }
}
