package com.hngy.cvs.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 兑换状态枚举
 * 
 * @author CVS Team
 */
@Getter
@AllArgsConstructor
public enum RedemptionStatus {

    PENDING(0, "待领取"),
    VERIFIED(1, "已领取"),
    CANCELLED(2, "已取消");

    private final Integer code;
    private final String description;

    /**
     * 根据code获取枚举
     * 
     * @param code 状态码
     * @return 对应的枚举值
     */
    public static RedemptionStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RedemptionStatus status : RedemptionStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
