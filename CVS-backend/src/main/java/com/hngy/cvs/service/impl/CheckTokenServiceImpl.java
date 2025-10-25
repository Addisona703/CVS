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
        AssertUtils.notEmpty(token, ResultCode.TOKEN_INVALID);

        String key = buildKey(token);
        // 使用 GET + DELETE 组合替代 GETDEL（兼容 Redis 6.2 以下版本）
        String payload = stringRedisTemplate.opsForValue().get(key);
        AssertUtils.notEmpty(payload, ResultCode.TOKEN_INVALID);
        
        // 获取成功后立即删除，确保一次性使用
        stringRedisTemplate.delete(key);

        try {
            CheckToken checkToken = objectMapper.readValue(payload, CheckToken.class);
            AssertUtils.notNull(checkToken, ResultCode.TOKEN_INVALID);
            AssertUtils.isTrue(checkToken.getType() == expectedType, ResultCode.TOKEN_INVALID);
            AssertUtils.isTrue(checkToken.getActivityId() != null, ResultCode.TOKEN_INVALID);
            AssertUtils.isTrue(checkToken.getExpiresAt() == null || checkToken.getExpiresAt().isAfter(LocalDateTime.now()),
                    ResultCode.TOKEN_EXPIRED);
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
