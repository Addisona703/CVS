package com.hngy.cvs.common.util;

import com.hngy.cvs.common.exception.BusinessException;
import com.hngy.cvs.common.result.ResultCode;
import cn.hutool.core.util.StrUtil;

import java.util.Collection;
import java.util.Map;

/**
 * 断言工具类：用于简化业务逻辑校验，统一抛出业务异常。
 * 推荐在 CVS 志愿服务系统的 Service 层做参数检查、状态校验、前置判断等。
 *
 * @author CVS Team
 */
public class AssertUtils {

    /**
     * 直接抛出业务异常
     *
     * @param resultCode 错误码
     */
    public static void fail(ResultCode resultCode) {
        throw new BusinessException(resultCode);
    }

    /**
     * 直接抛出业务异常
     *
     * @param message 错误消息
     */
    public static void fail(String message) {
        throw new BusinessException(message);
    }

    /**
     * 直接抛出业务异常
     *
     * @param resultCode 错误码
     * @param message 自定义错误消息
     */
    public static void fail(ResultCode resultCode, String message) {
        throw new BusinessException(resultCode, message);
    }

    /**
     * 断言表达式为真
     *
     * @param expression 表达式
     * @param message 错误消息
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new BusinessException(message);
        }
    }

    /**
     * 断言表达式为假
     *
     * @param expression 表达式
     * @param message 错误消息
     */
    public static void isFalse(boolean expression, String message) {
        if (expression) {
            throw new BusinessException(message);
        }
    }

    /**
     * 断言对象不为空
     *
     * @param obj 对象
     * @param message 错误消息
     */
    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new BusinessException(message);
        }
    }

    /**
     * 断言对象为空
     *
     * @param obj 对象
     * @param message 错误消息
     */
    public static void isNull(Object obj, String message) {
        if (obj != null) {
            throw new BusinessException(message);
        }
    }

    /**
     * 断言字符串不为空
     *
     * @param str 字符串
     * @param message 错误消息
     */
    public static void notEmpty(String str, String message) {
        if (StrUtil.isBlank(str)) {
            throw new BusinessException(message);
        }
    }

    /**
     * 断言集合不为空
     *
     * @param collection 集合
     * @param message 错误消息
     */
    public static void notEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new BusinessException(message);
        }
    }

    /**
     * 断言Map不为空
     *
     * @param map Map对象
     * @param message 错误消息
     */
    public static void notEmpty(Map<?, ?> map, String message) {
        if (map == null || map.isEmpty()) {
            throw new BusinessException(message);
        }
    }

    // --- 推荐使用的方法，使用标准错误码 ---

    /**
     * 断言字符串不为空
     *
     * @param str 字符串
     * @param resultCode 错误码
     */
    public static void notEmpty(String str, ResultCode resultCode) {
        if (StrUtil.isBlank(str)) {
            throw new BusinessException(resultCode);
        }
    }

    /**
     * 断言集合不为空
     *
     * @param collection 集合
     * @param resultCode 错误码
     */
    public static void notEmpty(Collection<?> collection, ResultCode resultCode) {
        if (collection == null || collection.isEmpty()) {
            throw new BusinessException(resultCode);
        }
    }

    /**
     * 断言Map不为空
     *
     * @param map Map对象
     * @param resultCode 错误码
     */
    public static void notEmpty(Map<?, ?> map, ResultCode resultCode) {
        if (map == null || map.isEmpty()) {
            throw new BusinessException(resultCode);
        }
    }

    /**
     * 断言表达式为真
     *
     * @param expression 表达式
     * @param resultCode 错误码
     */
    public static void isTrue(boolean expression, ResultCode resultCode) {
        if (!expression) {
            throw new BusinessException(resultCode);
        }
    }

    /**
     * 断言表达式为假
     *
     * @param expression 表达式
     * @param resultCode 错误码
     */
    public static void isFalse(boolean expression, ResultCode resultCode) {
        if (expression) {
            throw new BusinessException(resultCode);
        }
    }

    /**
     * 断言对象不为空
     *
     * @param object 对象
     * @param resultCode 错误码
     */
    public static void notNull(Object object, ResultCode resultCode) {
        if (object == null) {
            throw new BusinessException(resultCode);
        }
    }

    /**
     * 断言对象为空
     *
     * @param object 对象
     * @param resultCode 错误码
     */
    public static void isNull(Object object, ResultCode resultCode) {
        if (object != null) {
            throw new BusinessException(resultCode);
        }
    }
}
