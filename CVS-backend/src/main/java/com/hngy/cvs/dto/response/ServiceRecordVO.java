package com.hngy.cvs.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 服务记录视图对象
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "服务记录信息")
public class ServiceRecordVO {

    @Schema(description = "记录ID", example = "1")
    private Long id;

    @Schema(description = "用户ID", example = "4")
    private Long userId;

    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    @Schema(description = "活动ID", example = "1")
    private Long activityId;

    @Schema(description = "活动标题", example = "社区环保志愿活动")
    private String activityTitle;

    @Schema(description = "服务时长（分钟）", example = "480")
    private Integer durationMinutes;

    @Schema(description = "服务描述", example = "参与了整天的环保活动")
    private String description;

    @Schema(description = "服务评价", example = "表现积极，工作认真负责")
    private String evaluation;

    @Schema(description = "评分（1-5）", example = "5")
    private Integer rating;

    @Schema(description = "获得的积分数量", example = "10")
    private Integer pointsEarned;

    @Schema(description = "创建时间", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;
}
