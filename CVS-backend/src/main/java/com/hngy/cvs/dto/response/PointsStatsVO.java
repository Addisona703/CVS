package com.hngy.cvs.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 积分统计信息VO
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "积分统计信息")
public class PointsStatsVO {

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    @Schema(description = "总积分", example = "180")
    private Long totalPoints;

    @Schema(description = "当前排名", example = "5")
    private Long currentRanking;

    @Schema(description = "从服务记录获得的积分", example = "160")
    private Long servicePoints;

    @Schema(description = "手动调整的积分", example = "20")
    private Long adjustmentPoints;

    @Schema(description = "总服务时长（小时）", example = "24.5")
    private Double totalServiceHours;

    @Schema(description = "服务记录数量", example = "12")
    private Long serviceRecordsCount;
}