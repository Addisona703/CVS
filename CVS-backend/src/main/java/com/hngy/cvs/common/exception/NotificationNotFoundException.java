package com.hngy.cvs.common.exception;

import com.hngy.cvs.common.result.ResultCode;

/**
 * 通知不存在异常
 */
public class NotificationNotFoundException extends BusinessException {

    public NotificationNotFoundException() {
        super(ResultCode.NOT_FOUND, "通知不存在");
    }

    public NotificationNotFoundException(String message) {
        super(ResultCode.NOT_FOUND, message);
    }

    public NotificationNotFoundException(Long notificationId) {
        super(ResultCode.NOT_FOUND, "通知不存在，ID: " + notificationId);
    }
}