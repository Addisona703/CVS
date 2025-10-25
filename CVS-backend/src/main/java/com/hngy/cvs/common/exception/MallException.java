package com.hngy.cvs.common.exception;

import com.hngy.cvs.common.result.ResultCode;
import lombok.Getter;

/**
 * 积分商城异常类
 * 
 * @author CVS Team
 */
@Getter
public class MallException extends RuntimeException {

    private final Integer code;
    private final String message;

    public MallException(String message) {
        super(message);
        this.code = ResultCode.INTERNAL_SERVER_ERROR.getCode();
        this.message = message;
    }

    public MallException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public MallException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    public MallException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.message = message;
    }

    public MallException(MallErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public MallException(MallErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.message = message;
    }
}