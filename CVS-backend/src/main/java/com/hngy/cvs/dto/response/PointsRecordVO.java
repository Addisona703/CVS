package com.hngy.cvs.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 积分记录视图对象
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "积分记录信息")
public class PointsRecordVO {

    @Schema(description = "记录ID", example = "1")
    private Long id;

    @Schema(description = "用户ID", example = "4")
    private Long userId;

    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    @Schema(description = "积分数量", example = "10")
    private Integer points;

    @Schema(description = "总积分", example = "120")
    private Long totalPoints;
}
