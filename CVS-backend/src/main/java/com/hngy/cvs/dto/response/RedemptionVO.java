package com.hngy.cvs.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 兑换记录视图对象
 *
 * @author CVS Team
 */
@Data
public class RedemptionVO {

    /**
     * 兑换记录ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 用户学号
     */
    private String userUsername;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片URL
     */
    private String productImageUrl;

    /**
     * 消耗积分
     */
    private Integer pointsSpent;

    /**
     * 凭证编号
     */
    private String voucherCode;

    /**
     * 兑换状态（0-待领取，1-已领取，2-已取消）
     */
    private Integer status;

    /**
     * 兑换状态文本
     */
    private String statusText;

    /**
     * 核销人员ID
     */
    private Long verifiedBy;

    /**
     * 核销人员姓名
     */
    private String verifiedByName;

    /**
     * 核销时间
     */
    private LocalDateTime verifiedAt;

    /**
     * 兑换时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 当前用户是否可以取消兑换
     */
    private Boolean canCancel;
}