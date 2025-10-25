package com.hngy.cvs.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hngy.cvs.entity.enums.CertificateStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 证明审批请求DTO
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "证明审批请求")
public class CertificateApprovalDTO {

    @Schema(description = "证明ID", example = "1")
    @NotNull(message = "证明ID不能为空")
    @JsonProperty(value = "certificateId", access = JsonProperty.Access.WRITE_ONLY)
    private Long certificateId;
    
    @JsonProperty("id")
    private void setId(Long id) {
        this.certificateId = id;
    }

    @Schema(description = "是否批准", example = "true")
    @JsonProperty("approved")
    private Boolean approved;

    @Schema(description = "审批状态", example = "APPROVED")
    private CertificateStatus status;

    @Schema(description = "拒绝原因", example = "服务时长不足")
    @Size(max = 500, message = "拒绝原因长度不能超过500个字符")
    private String rejectReason;
    
    /**
     * 根据approved字段自动设置status
     */
    public CertificateStatus getStatus() {
        if (status != null) {
            return status;
        }
        if (approved != null) {
            return approved ? CertificateStatus.APPROVED : CertificateStatus.REJECTED;
        }
        return null;
    }
}
