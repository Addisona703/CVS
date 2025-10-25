package com.hngy.cvs.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 兑换记录实体类
 *
 * @author CVS Team
 */
@Data
@TableName("redemption_twb")
public class Redemption {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 消耗积分
     */
    private Integer pointsSpent;

    /**
     * 凭证编号（唯一）
     */
    private String voucherCode;

    /**
     * 兑换状态：0-待领取，1-已领取，2-已取消
     */
    private Integer status;

    /**
     * 核销人员ID
     */
    private Long verifiedBy;

    /**
     * 核销时间
     */
    private LocalDateTime verifiedAt;

    /**
     * 兑换时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
