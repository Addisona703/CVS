package com.hngy.cvs.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 管理员仪表板统计VO
 * 
 * @author CVS Team
 */
@Data
@Builder
@Schema(description = "管理员仪表板统计")
public class AdminDashboardStatsVO {

    @Schema(description = "总用户数", example = "100")
    private Long totalUsers;

    @Schema(description = "总活动数", example = "50")
    private Long totalActivities;

    @Schema(description = "总报名数", example = "200")
    private Long totalSignups;

    @Schema(description = "总服务记录数", example = "150")
    private Long totalServiceRecords;

    @Schema(description = "系统运行时间", example = "30天")
    private String systemUptime;

    @Schema(description = "在线用户数", example = "25")
    private Long onlineUsers;
}
