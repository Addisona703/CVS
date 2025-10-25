package com.hngy.cvs.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 学生扫码签退请求
 */
@Data
@Schema(description = "学生扫码签退请求")
public class CheckOutRequest {

    @NotBlank(message = "token不能为空")
    @Schema(description = "二维码token", example = "c3c3190c5a414c02bd0c3442c3dbe420")
    private String token;

    @Schema(description = "学生自评分，1~5 分", example = "4")
    @Min(value = 1, message = "评分不能低于1分")
    @Max(value = 5, message = "评分不能高于5分")
    private Integer studentRating;

    @Schema(description = "学生自评内容", example = "参与志愿服务，负责会场秩序维护")
    private String studentEvaluation;
}

