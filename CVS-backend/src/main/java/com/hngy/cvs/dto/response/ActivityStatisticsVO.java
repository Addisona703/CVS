package com.hngy.cvs.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 活动统计数据VO
 *
 * @author CVS Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "活动统计数据")
public class ActivityStatisticsVO {

    @Schema(description = "每日统计数据列表")
    private List<DailyStatisticsVO> dailyStatistics;

    @Schema(description = "统计天数")
    private Integer days;

    @Schema(description = "总活动数")
    private Integer totalActivities;

    @Schema(description = "总参与人次")
    private Integer totalParticipants;
}
