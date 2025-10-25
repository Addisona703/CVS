package com.hngy.cvs.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 兑换统计数据视图对象
 *
 * @author CVS Team
 */
@Data
@Schema(description = "兑换统计数据视图对象")
public class RedemptionStatisticsVO {

    @Schema(description = "总兑换次数", example = "150")
    private Long totalRedemptions;

    @Schema(description = "总消耗积分", example = "12500")
    private Long totalPointsSpent;

    @Schema(description = "待领取兑换数", example = "25")
    private Long pendingRedemptions;

    @Schema(description = "已领取兑换数", example = "120")
    private Long verifiedRedemptions;

    @Schema(description = "已取消兑换数", example = "5")
    private Long cancelledRedemptions;

    @Schema(description = "今日兑换次数", example = "8")
    private Long todayRedemptions;

    @Schema(description = "本周兑换次数", example = "45")
    private Long weekRedemptions;

    @Schema(description = "本月兑换次数", example = "120")
    private Long monthRedemptions;
}