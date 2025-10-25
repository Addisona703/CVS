package com.hngy.cvs.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 学生扫码签到请求
 */
@Data
@Schema(description = "学生扫码签到请求")
public class CheckInRequest {

    @NotBlank(message = "token不能为空")
    @Schema(description = "二维码token", example = "04cf7bc2714e4c5d9c31c9b47572c983")
    private String token;
}

