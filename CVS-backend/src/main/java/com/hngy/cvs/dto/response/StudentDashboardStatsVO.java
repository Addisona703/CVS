package com.hngy.cvs.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 学生仪表板统计VO
 * 
 * @author CVS Team
 */
@Data
@Builder
@Schema(description = "学生仪表板统计")
public class StudentDashboardStatsVO {

    @Schema(description = "我的报名数", example = "8")
    private Long mySignupsCount;

    @Schema(description = "总服务时长（小时）", example = "24.5")
    private Double totalServiceHours;

    @Schema(description = "总积分", example = "120")
    private Long totalPoints;

    @Schema(description = "证明数量", example = "2")
    private Long certificatesCount;
}
