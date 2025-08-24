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

    // 证明相关错误 3xxx
    CERTIFICATE_NOT_FOUND(3001, "证明不存在"),
    CERTIFICATE_ALREADY_APPROVED(3002, "证明已审批"),
    INSUFFICIENT_SERVICE_HOURS(3003, "服务时长不足"),

    // 文件相关错误 4xxx
    FILE_UPLOAD_ERROR(4001, "文件上传失败"),
    FILE_TYPE_NOT_SUPPORTED(4002, "文件类型不支持"),
    FILE_SIZE_EXCEEDED(4003, "文件大小超出限制");

    private final Integer code;
    private final String message;
}
