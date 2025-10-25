package com.hngy.cvs.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 积分记录视图对象
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "积分记录信息")
public class PointsRecordVO {

    @Schema(description = "记录ID", example = "1")
    private Long id;

    @Schema(description = "用户ID", example = "4")
    private Long userId;

    @Schema(description = "用户真实姓名", example = "张三")
    private String name;

    @Schema(description = "用户学号/工号", example = "2021001")
    private String username;

    @Schema(description = "积分数量", example = "10")
    private Integer points;

    @Schema(description = "总积分", example = "120")
    private Long totalPoints;

    @Schema(description = "活动标题", example = "社区环保志愿活动")
    private String activityTitle;

    @Schema(description = "获得积分方式（SERVICE/ADJUSTMENT）", example = "SERVICE")
    private String pointsSource;

    @Schema(description = "服务时长（分钟）", example = "120")
    private Integer durationMinutes;

    @Schema(description = "评分", example = "5")
    private Integer rating;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
