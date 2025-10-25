package com.hngy.cvs.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 * 
 * @author CVS Team
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "资源冲突"),
    VALIDATION_ERROR(422, "参数校验失败"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    PARAM_ERROR(430, "请求参数错误"),

    // 服务器错误 5xx
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    // 业务错误 1xxx
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    INVALID_CREDENTIALS(1003, "用户名或密码错误"),
    ACCOUNT_DISABLED(1004, "账户已被禁用"),
    TOKEN_EXPIRED(1005, "令牌已过期"),
    TOKEN_INVALID(1006, "令牌无效"),
    INSUFFICIENT_PERMISSIONS(1007, "权限不足"),

    // 活动相关错误 2xxx
    ACTIVITY_NOT_FOUND(2001, "活动不存在"),
    ACTIVITY_FULL(2002, "活动报名人数已满"),
    ACTIVITY_EXPIRED(2003, "活动已过期"),
    ACTIVITY_NOT_PUBLISHED(2004, "活动未发布"),
    ALREADY_SIGNED_UP(2005, "已经报名过该活动"),
    SIGNUP_NOT_FOUND(2006, "报名记录不存在"),
    SIGNUP_NOT_APPROVED(2007, "报名未通过审核"),
    CANNOT_CANCEL_ACTIVITY(2008, "无法取消活动"),
    ACTIVITY_STATUS_INVALID_FOR_PUBLISH(2009, "活动状态错误，只有草稿状态的活动才能发布"),
    ACTIVITY_START_TIME_INVALID(2010, "活动开始时间不能早于当前时间"),
    ACTIVITY_END_TIME_INVALID(2011, "活动结束时间不能早于开始时间"),
    ACTIVITY_REGISTRATION_DEADLINE_INVALID(2012, "报名截止时间不能晚于活动开始时间"),
    ACTIVITY_STATUS_INVALID_FOR_EDIT(2013, "活动状态错误，进行中或已完成的活动无法编辑"),
    ACTIVITY_STATUS_INVALID_FOR_DELETE(2014, "活动状态错误，进行中或已完成的活动无法删除"),
    ACTIVITY_STATUS_INVALID_FOR_CANCEL(2015, "活动状态错误，已完成的活动无法取消"),
    ACTIVITY_REGISTRATION_DEADLINE_PASSED(2016, "活动报名时间已过，无法取消"),
    SIGNUP_STATUS_INVALID(2017, "报名状态错误"),
    ALREADY_SIGNED_IN(2018, "已经签到过了"),
    NOT_SIGNED_IN(2019, "尚未签到"),
    ALREADY_SIGNED_OUT(2020, "已经签退过了"),
    SIGNUP_NOT_SIGNED_IN(2023, "报名记录尚未签到"),
    SIGNUP_NOT_SIGNED_OUT(2024, "报名记录尚未签退"),
    CANNOT_CANCEL_SIGNUP(2021, "无法取消报名"),
    ACTIVITY_ALREADY_STARTED(2022, "活动已开始，无法取消报名"),
    ACTIVITY_STATUS_INVALID_FOR_APPROVAL(2025, "活动状态错误，只有待审核的活动才能审核"),
    REJECT_REASON_REQUIRED(2026, "拒绝原因不能为空"),

    // 证明相关错误 3xxx
    CERTIFICATE_NOT_FOUND(3001, "证明不存在"),
    CERTIFICATE_ALREADY_APPROVED(3002, "证明已审批"),
    INSUFFICIENT_SERVICE_HOURS(3003, "服务时长不足"),

    // 文件相关错误 4xxx
    FILE_UPLOAD_ERROR(4001, "文件上传失败"),
    FILE_TYPE_NOT_SUPPORTED(4002, "文件类型不支持"),
    FILE_SIZE_EXCEEDED(4003, "文件大小超出限制"),

    // 积分商城相关错误 5xxx
    PRODUCT_NOT_FOUND(5001, "商品不存在"),
    PRODUCT_OFFLINE(5002, "商品已下架"),
    INSUFFICIENT_STOCK(5003, "商品库存不足"),
    INSUFFICIENT_POINTS(5004, "积分不足"),
    REDEMPTION_NOT_FOUND(5005, "兑换记录不存在"),
    VOUCHER_ALREADY_USED(5006, "凭证已被使用"),
    VOUCHER_CANCELLED(5007, "兑换已取消"),
    CATEGORY_NOT_FOUND(5008, "分类不存在"),
    CATEGORY_HAS_PRODUCTS(5009, "分类下存在商品，无法删除"),
    UNAUTHORIZED_OPERATION(5010, "无权限操作");

    private final Integer code;
    private final String message;
}
