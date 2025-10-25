package com.hngy.cvs.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 每日统计数据VO
 *
 * @author CVS Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "每日统计数据")
public class DailyStatisticsVO {

    @Schema(description = "日期 (YYYY-MM-DD)")
    private String date;

    @Schema(description = "当日活动数")
    private Integer activityCount;

    @Schema(description = "当日参与人数")
    private Integer participantCount;
}
