package com.hngy.cvs.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 兑换商品请求DTO
 *
 * @author CVS Team
 */
@Data
public class RedemptionRequest {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;
}