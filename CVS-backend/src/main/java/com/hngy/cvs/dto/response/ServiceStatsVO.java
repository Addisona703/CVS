package com.hngy.cvs.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 服务统计数据VO
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "服务统计数据")
public class ServiceStatsVO {

    @Schema(description = "总记录数", example = "15")
    private Long totalRecords;

    @Schema(description = "总服务时长（小时）", example = "48.5")
    private Double totalServiceHours;

    @Schema(description = "总获得积分", example = "120")
    private Integer totalPointsEarned;

    @Schema(description = "平均每次服务时长（小时）", example = "3.2")
    private Double averageServiceHours;

    @Schema(description = "最长单次服务时长（小时）", example = "8.0")
    private Double maxServiceHours;

    @Schema(description = "最短单次服务时长（小时）", example = "1.0")
    private Double minServiceHours;

    @Schema(description = "参与活动数量", example = "8")
    private Long totalActivities;
}