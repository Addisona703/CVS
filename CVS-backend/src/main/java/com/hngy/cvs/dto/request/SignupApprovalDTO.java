package com.hngy.cvs.dto.request;

import com.hngy.cvs.entity.enums.SignupStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 报名审批请求DTO
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "报名审批请求")
public class SignupApprovalDTO {

    @Schema(description = "报名ID", example = "1")
    @NotNull(message = "报名ID不能为空")
    private Long signupId;

    @Schema(description = "审批状态", example = "APPROVED")
    @NotNull(message = "审批状态不能为空")
    private SignupStatus status;

    @Schema(description = "拒绝原因", example = "时间冲突，无法参加")
    @Size(max = 500, message = "拒绝原因长度不能超过500个字符")
    private String rejectReason;
}
