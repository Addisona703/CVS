package com.hngy.cvs.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 未签到学生简单信息
 */
@Data
@Schema(description = "未签到学生信息")
public class PendingSignStudentVO {

    @Schema(description = "学生姓名")
    private String name;

    @Schema(description = "学生学号")
    private String username;

    @Schema(description = "学生头像地址（预留字段）")
    private String avatarUrl;
}

