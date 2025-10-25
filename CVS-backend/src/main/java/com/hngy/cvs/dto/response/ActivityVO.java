package com.hngy.cvs.dto.response;

import com.hngy.cvs.entity.Activity;
import com.hngy.cvs.entity.enums.ActivityStatus;
import com.hngy.cvs.common.util.BeanUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

import java.time.LocalDateTime;

/**
 * 活动视图对象
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "活动信息")
public class ActivityVO {

    @Schema(description = "活动ID", example = "1")
    private Long id;

    @Schema(description = "活动标题", example = "社区环保志愿活动")
    private String title;

    @Schema(description = "活动描述", example = "组织学生参与社区环保活动")
    private String description;

    @Schema(description = "活动地点", example = "中央公园")
    private String location;

    @Schema(description = "开始时间", example = "2024-03-01T09:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2024-03-01T17:00:00")
    private LocalDateTime endTime;

    @Schema(description = "活动报名截止时间", example = "2024-02-28T23:59:59")
    private LocalDateTime registrationDeadline;

    @Schema(description = "最大参与人数", example = "50")
    private Integer maxParticipants;

    @Schema(description = "活动状态", example = "PUBLISHED")
    private ActivityStatus status;

    @Schema(description = "组织者ID", example = "2")
    private Long organizerId;

    @Schema(description = "组织者姓名", example = "张老师")
    private String organizerName;

    @Schema(description = "参与要求", example = "身体健康，有环保意识")
    private String requirements;

    @Schema(description = "联系方式", example = "张老师 13800138001")
    private String contactInfo;

    @Schema(description = "奖励积分", example = "10")
    private Integer points;

    @Schema(description = "当前报名人数", example = "25")
    private Integer currentParticipants;

    @Schema(description = "已签到人数", example = "20")
    private Integer checkinCount;

    @Schema(description = "已签退人数", example = "18")
    private Integer checkoutCount;

    @Schema(description = "是否可以报名", example = "true")
    private Boolean canSignup;

    @Schema(description = "审核人ID", example = "1")
    private Long approverId;

    @Schema(description = "审核时间", example = "2024-01-01T12:00:00")
    private LocalDateTime approvedAt;

    @Schema(description = "拒绝原因", example = "活动内容不符合要求")
    private String rejectReason;

    @Schema(description = "创建时间", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;
}
