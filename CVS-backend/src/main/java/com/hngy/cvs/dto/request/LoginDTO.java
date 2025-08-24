package com.hngy.cvs.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "登录请求")
public class LoginDTO {

    @Schema(description = "用户名", example = "student001")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码", example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "记住我", example = "false")
    private Boolean rememberMe = false;
}
