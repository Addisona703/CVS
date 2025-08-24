package com.hngy.cvs.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 证明状态枚举
 * 
 * @author CVS Team
 */
@Getter
@AllArgsConstructor
public enum CertificateStatus {

    PENDING("PENDING", "待审核"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已拒绝");

    private final String code;
    private final String description;

    public static CertificateStatus fromCode(String code) {
        for (CertificateStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的证明状态代码: " + code);
    }
}
