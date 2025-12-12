package com.hngy.cvs.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 核销统计VO
 *
 * @author CVS Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "核销统计VO")
public class VerifyStatisticsVO {

    @Schema(description = "今日核销数量")
    private Long todayCount;

    @Schema(description = "累计核销数量")
    private Long totalCount;

    @Schema(description = "待核销数量")
    private Long pendingCount;
}
