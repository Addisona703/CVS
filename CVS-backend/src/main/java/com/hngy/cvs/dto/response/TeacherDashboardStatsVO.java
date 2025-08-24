package com.hngy.cvs.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 教师仪表板统计VO
 * 
 * @author CVS Team
 */
@Data
@Builder
@Schema(description = "教师仪表板统计")
public class TeacherDashboardStatsVO {

    @Schema(description = "我的活动数", example = "10")
    private Long myActivitiesCount;

    @Schema(description = "我的活动总参与人数（已通过报名）", example = "50")
    private Long totalSignupsCount;

    @Schema(description = "待审核报名数", example = "5")
    private Long pendingApprovalsCount;

    @Schema(description = "待审核志愿证明数", example = "3")
    private Long serviceRecordsCount;
}
