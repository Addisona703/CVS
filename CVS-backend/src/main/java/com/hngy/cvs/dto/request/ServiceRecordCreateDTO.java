package com.hngy.cvs.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;

/**
 * 服务记录创建请求DTO
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "服务记录创建请求")
public class ServiceRecordCreateDTO {

    @Schema(description = "用户ID", example = "4")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "活动ID", example = "1")
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @Schema(description = "服务时长（分钟）", example = "480")
    @NotNull(message = "服务时长不能为空")
    @Min(value = 1, message = "服务时长必须大于0")
    @Max(value = 1440, message = "服务时长不能超过24小时")
    private Integer durationMinutes;

    @Schema(description = "服务描述", example = "参与了整天的环保活动，清理了大量垃圾")
    @Size(max = 1000, message = "服务描述长度不能超过1000个字符")
    private String description;

    @Schema(description = "服务评价", example = "表现积极，工作认真负责")
    @Size(max = 1000, message = "服务评价长度不能超过1000个字符")
    private String evaluation;

    @Schema(description = "评分（1-5）", example = "5")
    @Min(value = 1, message = "评分不能低于1")
    @Max(value = 5, message = "评分不能高于5")
    private Integer rating;

    @Schema(description = "获得的积分数量", example = "10")
    @Min(value = 0, message = "积分数量不能为负数")
    @Max(value = 100, message = "积分数量不能超过100")
    private Integer pointsEarned;
}
