package com.hngy.cvs.dto.response;

import com.hngy.cvs.entity.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 登录响应VO
 * 
 * @author CVS Team
 */
@Data
@Builder
@Schema(description = "登录响应")
public class LoginVO {

    @Schema(description = "JWT令牌", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String token;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "student001")
    private String username;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "角色", example = "STUDENT")
    private UserRole role;
}
