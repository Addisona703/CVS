package com.hngy.cvs.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 发送验证码请求DTO
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "发送验证码请求")
public class SendCodeDTO {

    @Schema(description = "邮箱地址", example = "student@university.edu.cn")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "验证码类型，如：register, reset_password, login", example = "register")
    @NotBlank(message = "验证码类型不能为空")
    private String type;
}