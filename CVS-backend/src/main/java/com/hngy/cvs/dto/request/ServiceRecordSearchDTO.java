package com.hngy.cvs.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 服务记录搜索DTO
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "服务记录搜索条件")
public class ServiceRecordSearchDTO {

    @Schema(description = "活动ID", example = "1")
    private Long activityId;

    @Schema(description = "活动名称（模糊查询）", example = "环保志愿活动")
    private String activityTitle;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户姓名（模糊查询）", example = "张三")
    private String userName;

    @Schema(description = "服务记录描述（模糊查询）", example = "志愿服务")
    private String description;

    @Schema(description = "最小服务时长（分钟）", example = "60")
    private Integer minDurationMinutes;

    @Schema(description = "最大服务时长（分钟）", example = "480")
    private Integer maxDurationMinutes;

    @Schema(description = "最小积分", example = "5")
    private Integer minPointsEarned;

    @Schema(description = "最大积分", example = "20")
    private Integer maxPointsEarned;
}