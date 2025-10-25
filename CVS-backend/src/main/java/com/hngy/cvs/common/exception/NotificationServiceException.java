package com.hngy.cvs.common.exception;

import com.hngy.cvs.common.result.ResultCode;

/**
 * 通知服务异常
 */
public class NotificationServiceException extends BusinessException {

    public NotificationServiceException() {
        super(ResultCode.INTERNAL_SERVER_ERROR, "通知服务异常");
    }

    public NotificationServiceException(String message) {
        super(ResultCode.INTERNAL_SERVER_ERROR, message);
    }

    public NotificationServiceException(String message, Throwable cause) {
        super(ResultCode.INTERNAL_SERVER_ERROR, message);
        initCause(cause);
    }

    public NotificationServiceException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }
}