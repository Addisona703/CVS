package com.hngy.cvs.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hngy.cvs.entity.enums.CertificateStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 证明实体类
 * 
 * @author CVS Team
 */
@Data
@TableName("cert_twb")
public class Certificate {

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
     * 申请目的
     */
    private String purpose;

    /**
     * 服务开始日期
     */
    private LocalDate startDate;

    /**
     * 服务结束日期
     */
    private LocalDate endDate;

    /**
     * 证明状态
     */
    private CertificateStatus status;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批时间
     */
    private LocalDateTime approvedAt;

    /**
     * 证明编号
     */
    private String certificateNumber;

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
