package com.hngy.cvs.service;

import com.hngy.cvs.entity.enums.SignActionType;
import com.hngy.cvs.service.model.CheckToken;

/**
 * 二维码token生成与校验服务
 */
public interface CheckTokenService {

    /**
     * 生成指定活动的二维码token
     *
     * @param activityId 活动ID
     * @param type       签到或签退
     * @param ttlMinutes token有效期（分钟）
     * @return token信息
     */
    CheckToken createToken(Long activityId, SignActionType type, long ttlMinutes);

    /**
     * 校验并消费token，使用后立即作废
     *
     * @param token        token字符串
     * @param expectedType 期望的类型
     * @return token信息
     */
    CheckToken consumeToken(String token, SignActionType expectedType);
}

