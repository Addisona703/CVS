package com.hngy.cvs.dto.request;

import com.hngy.cvs.entity.enums.SignupStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 报名搜索请求DTO
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "报名搜索请求")
public class SignupSearchDTO {

    @Schema(description = "活动名称关键词", example = "环保")
    private String activityTitle;

    @Schema(description = "学生名称关键词", example = "张三")
    private String userName;

    @Schema(description = "报名状态", example = "PENDING")
    private SignupStatus status;

    @Schema(description = "活动ID", example = "1")
    private Long activityId;

    @Schema(description = "用户ID", example = "1")
    private Long userId;
}