package com.hngy.cvs.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "重置密码请求")
public class ResetPasswordDTO {

    @NotBlank(message = "重置令牌不能为空")
    @Schema(description = "重置令牌", example = "c13af9d6a2b74999a38cbb46da3f35a5")
    private String token;

    @NotBlank(message = "新密码不能为空")
    @Schema(description = "新密码", example = "newPassword456")
    private String newPassword;
}
