package com.hngy.cvs.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 签退审核视图对象
 */
@Data
@Schema(description = "签退审核视图对象")
public class SignupReviewVO {

    @Schema(description = "报名ID")
    private Long signupId;

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "活动标题")
    private String activityTitle;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "签到时间")
    private LocalDateTime checkInTime;

    @Schema(description = "签退时间")
    private LocalDateTime checkOutTime;

    @Schema(description = "学生自评分")
    private Integer studentRating;

    @Schema(description = "学生自评内容")
    private String studentEvaluation;

    @Schema(description = "教师评分")
    private Integer teacherRating;

    @Schema(description = "教师评语")
    private String teacherEvaluation;

    @Schema(description = "审核状态：PENDING-待审核，REVIEWED-已审核")
    private String reviewStatus;

    @Schema(description = "教师确认评分时间")
    private LocalDateTime teacherRatingConfirmedAt;
}
