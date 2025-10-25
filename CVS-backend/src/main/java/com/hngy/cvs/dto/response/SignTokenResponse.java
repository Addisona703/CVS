package com.hngy.cvs.dto.response;

import com.hngy.cvs.entity.enums.SignActionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 生成二维码token响应
 */
@Data
@Builder
@Schema(description = "二维码token响应")
public class SignTokenResponse {

    @Schema(description = "二维码token", example = "abcdef123456")
    private String token;

    @Schema(description = "有效期截止时间", example = "2024-05-10T10:15:00")
    private LocalDateTime expiresAt;

    @Schema(description = "二维码类型", example = "SIGN_IN", allowableValues = {"SIGN_IN", "SIGN_OUT"})
    private SignActionType action;
}
