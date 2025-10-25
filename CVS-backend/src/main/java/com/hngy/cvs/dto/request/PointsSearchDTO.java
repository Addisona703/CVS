package com.hngy.cvs.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 积分记录搜索DTO
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "积分记录搜索条件")
public class PointsSearchDTO {

    @Schema(description = "用户姓名", example = "张三")
    private String name;

    @Schema(description = "用户学号/工号", example = "2021001")
    private String username;

    @Schema(description = "积分来源（SERVICE/ADJUSTMENT）", example = "SERVICE")
    private String pointsSource;

    @Schema(description = "最小积分", example = "10")
    private Integer minPoints;

    @Schema(description = "最大积分", example = "100")
    private Integer maxPoints;
}