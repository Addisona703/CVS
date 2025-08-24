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
    @TableId(type = IdType.ASSIGN_ID)
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
    private Boolean signedIn;

    /**
     * 是否签退
     */
    private Boolean signedOut;

    /**
     * 签到时间
     */
    private LocalDateTime signInTime;

    /**
     * 签退时间
     */
    private LocalDateTime signOutTime;

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
