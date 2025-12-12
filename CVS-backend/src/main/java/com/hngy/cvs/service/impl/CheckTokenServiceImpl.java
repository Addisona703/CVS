package com.hngy.cvs.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hngy.cvs.common.result.ResultCode;
import com.hngy.cvs.common.util.AssertUtils;
import com.hngy.cvs.entity.enums.SignActionType;
import com.hngy.cvs.service.CheckTokenService;
import com.hngy.cvs.service.model.CheckToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis的一次性二维码token服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckTokenServiceImpl implements CheckTokenService {

    private static final long DEFAULT_TTL_MINUTES = 5L;
    private static final long MIN_TTL_MINUTES = 1L;
    private static final String KEY_PREFIX = "cvs:checkin:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public CheckToken createToken(Long activityId, SignActionType type, long ttlMinutes) {
        long effectiveTtl = ttlMinutes > 0 ? ttlMinutes : DEFAULT_TTL_MINUTES;

        String token = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(effectiveTtl);
        CheckToken checkToken = CheckToken.builder()
                .token(token)
                .activityId(activityId)
                .type(type)
                .expiresAt(expiresAt)
                .build();

        try {
            String payload = objectMapper.writeValueAsString(checkToken);
            stringRedisTemplate.opsForValue().set(buildKey(token), payload, effectiveTtl, TimeUnit.MINUTES);
            log.debug("Generated {} token {} for activity {} expiring at {}", type, token, activityId, expiresAt);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize check token for activity {}", activityId, e);
            AssertUtils.fail(ResultCode.INTERNAL_SERVER_ERROR);
        }

        return checkToken;
    }

    @Override
    public CheckToken consumeToken(String token, SignActionType expectedType) {
        log.info("=== 开始验证token ===");
        log.info("Token: {}", token);
        log.info("期望类型: {}", expectedType);
        
        AssertUtils.notEmpty(token, ResultCode.TOKEN_INVALID);

        String key = buildKey(token);
        log.info("Redis Key: {}", key);
        
        // 只获取token，不删除，允许在有效期内重复使用
        String payload = stringRedisTemplate.opsForValue().get(key);
        log.info("从Redis获取的payload: {}", payload);
        
        if (payload == null || payload.isEmpty()) {
            log.warn("Token不存在或已过期: {}", token);
        }
        
        AssertUtils.notEmpty(payload, ResultCode.TOKEN_INVALID);
        
        // 注释掉删除操作，允许token在有效期内被多个学生使用
        // stringRedisTemplate.delete(key);

        try {
            CheckToken checkToken = objectMapper.readValue(payload, CheckToken.class);
            log.info("解析后的CheckToken: {}", checkToken);
            
            AssertUtils.notNull(checkToken, ResultCode.TOKEN_INVALID);
            
            log.info("Token类型: {}, 期望类型: {}", checkToken.getType(), expectedType);
            AssertUtils.isTrue(checkToken.getType() == expectedType, ResultCode.TOKEN_INVALID);
            
            log.info("ActivityId: {}", checkToken.getActivityId());
            AssertUtils.isTrue(checkToken.getActivityId() != null, ResultCode.TOKEN_INVALID);
            
            log.info("过期时间: {}, 当前时间: {}", checkToken.getExpiresAt(), LocalDateTime.now());
            AssertUtils.isTrue(checkToken.getExpiresAt() == null || checkToken.getExpiresAt().isAfter(LocalDateTime.now()),
                    ResultCode.TOKEN_EXPIRED);
            
            log.info("=== Token验证成功 ===");
            return checkToken;
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize check token {}", token, e);
            AssertUtils.fail(ResultCode.TOKEN_INVALID);
        }
        return null;
    }

    private String buildKey(String token) {
        return KEY_PREFIX + token;
    }
}
