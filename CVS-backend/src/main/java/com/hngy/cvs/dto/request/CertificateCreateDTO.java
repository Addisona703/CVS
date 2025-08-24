package com.hngy.cvs.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 志愿证明申请请求DTO
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "志愿证明申请请求")
public class CertificateCreateDTO {

    @Schema(description = "申请目的", example = "申请奖学金需要")
    @NotBlank(message = "申请目的不能为空")
    @Size(max = 500, message = "申请目的长度不能超过500个字符")
    private String purpose;

    @Schema(description = "服务开始日期", example = "2024-01-01")
    @NotNull(message = "服务开始日期不能为空")
    private LocalDate startDate;

    @Schema(description = "服务结束日期", example = "2024-03-01")
    @NotNull(message = "服务结束日期不能为空")
    private LocalDate endDate;
}
