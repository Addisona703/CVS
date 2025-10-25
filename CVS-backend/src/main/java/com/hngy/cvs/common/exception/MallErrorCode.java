package com.hngy.cvs.common.exception;

import com.hngy.cvs.common.result.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 积分商城错误码枚举
 * 
 * @author CVS Team
 */
@Getter
@AllArgsConstructor
public enum MallErrorCode {

    // 商品相关错误
    PRODUCT_NOT_FOUND(ResultCode.PRODUCT_NOT_FOUND.getCode(), ResultCode.PRODUCT_NOT_FOUND.getMessage()),
    PRODUCT_OFFLINE(ResultCode.PRODUCT_OFFLINE.getCode(), ResultCode.PRODUCT_OFFLINE.getMessage()),
    INSUFFICIENT_STOCK(ResultCode.INSUFFICIENT_STOCK.getCode(), ResultCode.INSUFFICIENT_STOCK.getMessage()),
    
    // 积分相关错误
    INSUFFICIENT_POINTS(ResultCode.INSUFFICIENT_POINTS.getCode(), ResultCode.INSUFFICIENT_POINTS.getMessage()),
    
    // 兑换相关错误
    REDEMPTION_NOT_FOUND(ResultCode.REDEMPTION_NOT_FOUND.getCode(), ResultCode.REDEMPTION_NOT_FOUND.getMessage()),
    VOUCHER_ALREADY_USED(ResultCode.VOUCHER_ALREADY_USED.getCode(), ResultCode.VOUCHER_ALREADY_USED.getMessage()),
    VOUCHER_CANCELLED(ResultCode.VOUCHER_CANCELLED.getCode(), ResultCode.VOUCHER_CANCELLED.getMessage()),
    
    // 分类相关错误
    CATEGORY_NOT_FOUND(ResultCode.CATEGORY_NOT_FOUND.getCode(), ResultCode.CATEGORY_NOT_FOUND.getMessage()),
    CATEGORY_HAS_PRODUCTS(ResultCode.CATEGORY_HAS_PRODUCTS.getCode(), ResultCode.CATEGORY_HAS_PRODUCTS.getMessage()),
    
    // 权限相关错误
    UNAUTHORIZED_OPERATION(ResultCode.UNAUTHORIZED_OPERATION.getCode(), ResultCode.UNAUTHORIZED_OPERATION.getMessage());

    private final Integer code;
    private final String message;
}