package com.hngy.cvs.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 兑换记录查询请求DTO
 *
 * @author CVS Team
 */
@Data
public class RedemptionQueryRequest {

    /**
     * 用户ID（可选，用于筛选特定用户的兑换记录）
     */
    private Long userId;

    /**
     * 商品ID（可选，用于筛选特定商品的兑换记录）
     */
    private Long productId;

    /**
     * 兑换状态（可选，0-待领取，1-已领取，2-已取消）
     */
    private Integer status;

    /**
     * 开始时间（可选，用于时间范围筛选）
     */
    private LocalDateTime startTime;

    /**
     * 结束时间（可选，用于时间范围筛选）
     */
    private LocalDateTime endTime;

    /**
     * 用户姓名（可选，用于模糊搜索）
     */
    private String userName;

    /**
     * 商品名称（可选，用于模糊搜索）
     */
    private String productName;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}