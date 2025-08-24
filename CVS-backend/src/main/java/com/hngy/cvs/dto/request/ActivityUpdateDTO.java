package com.hngy.cvs.dto.request;

import com.hngy.cvs.entity.enums.ActivityStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 活动更新请求DTO
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "活动更新请求")
public class ActivityUpdateDTO {

    @Schema(description = "活动ID", example = "1")
    @NotNull(message = "活动ID不能为空")
    private Long id;

    @Schema(description = "活动标题", example = "社区环保志愿活动")
    @Size(max = 200, message = "活动标题长度不能超过200个字符")
    private String title;

    @Schema(description = "活动描述", example = "组织学生参与社区环保活动，清理公园垃圾")
    @Size(max = 2000, message = "活动描述长度不能超过2000个字符")
    private String description;

    @Schema(description = "活动地点", example = "中央公园")
    @Size(max = 200, message = "活动地点长度不能超过200个字符")
    private String location;

    @Schema(description = "开始时间", example = "2024-03-01T09:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2024-03-01T17:00:00")
    private LocalDateTime endTime;

    @Schema(description = "活动报名截止时间（可选）", example = "2024-02-28T23:59:59")
    private LocalDateTime registrationDeadline;

    @Schema(description = "最大参与人数", example = "50")
    @Min(value = 1, message = "最大参与人数必须大于0")
    @Max(value = 1000, message = "最大参与人数不能超过1000")
    private Integer maxParticipants;

    @Schema(description = "活动状态", example = "PUBLISHED")
    private ActivityStatus status;

    @Schema(description = "参与要求", example = "身体健康，有环保意识")
    @Size(max = 1000, message = "参与要求长度不能超过1000个字符")
    private String requirements;

    @Schema(description = "联系方式", example = "张老师 13800138001")
    @Size(max = 200, message = "联系方式长度不能超过200个字符")
    private String contactInfo;

    @Schema(description = "奖励积分", example = "10")
    @Min(value = 0, message = "奖励积分不能为负数")
    @Max(value = 100, message = "奖励积分不能超过100")
    private Integer points;
}
