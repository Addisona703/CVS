package com.hngy.cvs.common.exception;

import com.hngy.cvs.common.result.ResultCode;

/**
 * 无权访问通知异常
 */
public class UnauthorizedNotificationAccessException extends BusinessException {

    public UnauthorizedNotificationAccessException() {
        super(ResultCode.FORBIDDEN, "无权访问该通知");
    }

    public UnauthorizedNotificationAccessException(String message) {
        super(ResultCode.FORBIDDEN, message);
    }

    public UnauthorizedNotificationAccessException(Long notificationId, Long userId) {
        super(ResultCode.FORBIDDEN, String.format("用户 %d 无权访问通知 %d", userId, notificationId));
    }
}