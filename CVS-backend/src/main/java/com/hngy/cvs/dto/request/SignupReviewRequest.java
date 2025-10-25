package com.hngy.cvs.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 教师调整评分与评价请求
 */
@Data
@Schema(description = "教师调整评分与评价请求")
public class SignupReviewRequest {

    @NotNull(message = "教师评分不能为空")
    @Schema(description = "教师评分，1~5 分", example = "5")
    @Min(value = 1, message = "评分不能低于1分")
    @Max(value = 5, message = "评分不能高于5分")
    private Integer teacherRating;

    @Schema(description = "教师评价", example = "表现优秀，积极负责")
    private String teacherEvaluation;
}

