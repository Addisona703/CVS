package com.hngy.cvs.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 活动报名请求DTO
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "活动报名请求")
public class SignupCreateDTO {

    @Schema(description = "活动ID", example = "1")
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @Schema(description = "报名理由", example = "我对环保很感兴趣，希望能为环境保护贡献自己的力量")
    @Size(max = 1000, message = "报名理由长度不能超过1000个字符")
    private String reason;
}
