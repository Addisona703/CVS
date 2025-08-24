package com.hngy.cvs.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 记录实体类
 * 
 * @author CVS Team
 */
@Data
@TableName("record_twb")
public class RecordEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 服务时长（分钟）
     */
    private Integer durationMinutes;

    /**
     * 服务描述
     */
    private String description;

    /**
     * 服务评价
     */
    private String evaluation;

    /**
     * 评分（1-5）
     */
    private Integer rating;

    /**
     * 该服务记录获得的积分数量
     */
    private Integer pointsEarned;

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
