package com.hngy.cvs.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证验证码响应VO
 * 
 * @author CVS Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "验证验证码响应")
public class VerifyCodeVO {

    @Schema(description = "是否验证成功", example = "true")
    private Boolean verified;

    @Schema(description = "消息", example = "验证码验证成功")
    private String message;
}