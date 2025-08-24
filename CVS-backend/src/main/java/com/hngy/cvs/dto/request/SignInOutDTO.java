package com.hngy.cvs.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 签到签退请求DTO
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "签到签退请求")
public class SignInOutDTO {

    @Schema(description = "报名ID", example = "1")
    @NotNull(message = "报名ID不能为空")
    private Long signupId;

    @Schema(description = "操作类型", example = "SIGN_IN", allowableValues = {"SIGN_IN", "SIGN_OUT"})
    @NotNull(message = "操作类型不能为空")
    private SignInOutType type;

    public enum SignInOutType {
        SIGN_IN, SIGN_OUT
    }
}
