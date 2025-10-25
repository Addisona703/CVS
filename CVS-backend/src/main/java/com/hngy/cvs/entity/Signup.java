package com.hngy.cvs.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hngy.cvs.entity.enums.SignupStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报名实体类
 * 
 * @author CVS Team
 */
@Data
@TableName("signup_twb")
public class Signup {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 报名状态
     */
    private SignupStatus status;

    /**
     * 报名理由
     */
    private String reason;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 是否签到
     */
    @TableField("signed_in")
    private Boolean signedIn;

    /**
     * 是否签退
     */
    @TableField("signed_out")
    private Boolean signedOut;

    /**
     * 签到时间
     */
    @TableField("sign_in_time")
    private LocalDateTime signInTime;

    /**
     * 签退时间
     */
    @TableField("sign_out_time")
    private LocalDateTime signOutTime;

    /**
     * 学生自评分（1-5）
     */
    @TableField("student_rating")
    private Integer studentRating;

    /**
     * 学生自评内容
     */
    @TableField("student_evaluation")
    private String studentEvaluation;

    /**
     * 教师最终评分（触发积分计算）
     */
    @TableField("teacher_rating")
    private Integer teacherRating;

    /**
     * 教师最终评语
     */
    @TableField("teacher_evaluation")
    private String teacherEvaluation;

    /**
     * 教师确认最终评分时间
     */
    @TableField("teacher_rating_confirmed_at")
    private LocalDateTime teacherRatingConfirmedAt;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
