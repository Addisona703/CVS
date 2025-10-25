package com.hngy.cvs.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 核销兑换凭证请求DTO
 *
 * @author CVS Team
 */
@Data
public class VerifyRedemptionRequest {

    /**
     * 凭证编号
     */
    @NotBlank(message = "凭证编号不能为空")
    private String voucherCode;
}