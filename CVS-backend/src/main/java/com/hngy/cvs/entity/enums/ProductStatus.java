package com.hngy.cvs.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商品状态枚举
 * 
 * @author CVS Team
 */
@Getter
@AllArgsConstructor
public enum ProductStatus {

    OFFLINE(0, "下架"),
    ONLINE(1, "上架");

    private final Integer code;
    private final String description;

    /**
     * 根据code获取枚举
     * 
     * @param code 状态码
     * @return 对应的枚举值
     */
    public static ProductStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProductStatus status : ProductStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
