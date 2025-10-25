package com.hngy.cvs.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 审核列表查询请求
 */
@Data
@Schema(description = "审核列表查询请求")
public class ReviewSearchRequest {

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "关键词（学生姓名或学号）")
    private String keyword;

    @Schema(description = "审核状态：PENDING-待审核，REVIEWED-已审核")
    private String reviewStatus;
}
