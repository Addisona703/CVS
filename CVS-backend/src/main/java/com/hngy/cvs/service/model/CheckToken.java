package com.hngy.cvs.service.model;

import com.hngy.cvs.entity.enums.SignActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Redis中存储的签到/签退二维码token信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckToken {

    /**
     * 一次性token字符串
     */
    private String token;

    /**
     * 所属活动ID
     */
    private Long activityId;

    /**
     * 二维码类型：签到或签退
     */
    private SignActionType type;

    /**
     * 到期时间（用于返回给前端展示）
     */
    private LocalDateTime expiresAt;
}

