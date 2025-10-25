package com.hngy.cvs.dto.response;

import com.hngy.cvs.entity.enums.SignupStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报名视图对象
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "报名信息")
public class SignupVO {

    @Schema(description = "报名ID", example = "1")
    private Long id;

    @Schema(description = "活动ID", example = "1")
    private Long activityId;

    @Schema(description = "活动标题", example = "社区环保志愿活动")
    private String activityTitle;
    
    @Schema(description = "活动地点", example = "中央公园")
    private String location;
    
    @Schema(description = "活动开始时间", example = "2024-03-01T09:00:00")
    private LocalDateTime startTime;
    
    @Schema(description = "活动结束时间", example = "2024-03-01T17:00:00")
    private LocalDateTime endTime;
    
    @Schema(description = "奖励积分", example = "10")
    private Integer points;

    @Schema(description = "用户ID", example = "4")
    private Long userId;

    @Schema(description = "用户姓名", example = "张三")
    private String name;
    
    @Schema(description = "学号/用户名", example = "2021001")
    private String username;
    
    @Schema(description = "联系电话", example = "13800138000")
    private String phone;
    
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "报名状态", example = "APPROVED")
    private SignupStatus status;

    @Schema(description = "报名理由", example = "我对环保很感兴趣")
    private String reason;

    @Schema(description = "拒绝原因", example = "时间冲突")
    private String rejectReason;

    @Schema(description = "是否签到", example = "true")
    private Boolean signedIn;

    @Schema(description = "是否签退", example = "false")
    private Boolean signedOut;

    @Schema(description = "签到时间", example = "2024-03-01T08:50:00")
    private LocalDateTime signInTime;

    @Schema(description = "签退时间", example = "2024-03-01T17:10:00")
    private LocalDateTime signOutTime;

    @Schema(description = "学生自评分（1-5）", example = "4")
    private Integer studentRating;

    @Schema(description = "学生自评内容", example = "认真完成志愿服务任务")
    private String studentEvaluation;

    @Schema(description = "教师最终评分", example = "5")
    private Integer teacherRating;

    @Schema(description = "教师最终评语", example = "表现优秀，组织协调能力强")
    private String teacherEvaluation;

    @Schema(description = "教师评分确认时间", example = "2024-03-02T09:30:00")
    private LocalDateTime teacherRatingConfirmedAt;

    @Schema(description = "创建时间", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;
}
