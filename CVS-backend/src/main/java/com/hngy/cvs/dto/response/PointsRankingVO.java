package com.hngy.cvs.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 积分排行榜VO
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "积分排行榜信息")
public class PointsRankingVO {

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "用户姓名", example = "张三")
    private String name;

    @Schema(description = "总积分", example = "280")
    private Long totalPoints;

    @Schema(description = "排名", example = "1")
    private Long ranking;

    @Schema(description = "是否为当前用户", example = "false")
    private Boolean isCurrentUser;
}