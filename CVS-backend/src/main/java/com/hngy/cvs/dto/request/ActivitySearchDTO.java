package com.hngy.cvs.dto.request;

import com.hngy.cvs.entity.enums.ActivityStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动搜索请求DTO
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "活动搜索请求")
public class ActivitySearchDTO {

    @Schema(description = "活动标题关键词", example = "环保")
    private String title;

    @Schema(description = "组织者名字关键词", example = "张老师")
    private String organizerName;

    @Schema(description = "活动状态", example = "PUBLISHED")
    private ActivityStatus status;

    @Schema(description = "活动地点关键词", example = "公园")
    private String location;

    @Schema(description = "开始时间范围-开始", example = "2024-03-01T00:00")
    private LocalDateTime startTimeFrom;

    @Schema(description = "开始时间范围-结束", example = "2024-03-31T23:59")
    private LocalDateTime startTimeTo;

    @Schema(description = "组织者ID", example = "2")
    private Long organizerId;
}
