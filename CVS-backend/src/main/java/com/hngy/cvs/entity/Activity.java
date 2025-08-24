package com.hngy.cvs.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hngy.cvs.entity.enums.ActivityStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动实体类
 *
 * @author CVS Team
 */
@Data
@TableName("activity_twb")
public class Activity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 活动标题
     */
    private String title;

    /**
     * 活动描述
     */
    private String description;

    /**
     * 活动地点
     */
    private String location;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 活动报名截止时间
     */
    private LocalDateTime registrationDeadline;

    /**
     * 最大参与人数
     */
    private Integer maxParticipants;

    /**
     * 活动状态
     */
    private ActivityStatus status;

    /**
     * 组织者ID
     */
    private Long organizerId;

    /**
     * 参与要求
     */
    private String requirements;

    /**
     * 联系方式
     */
    private String contactInfo;

    /**
     * 奖励积分
     */
    private Integer points;

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

    /**
     * 逻辑删除标志
     */
    @TableLogic
    private Integer deleted;
}
