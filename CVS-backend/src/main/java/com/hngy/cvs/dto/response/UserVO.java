package com.hngy.cvs.dto.response;

import com.hngy.cvs.entity.User;
import com.hngy.cvs.entity.enums.UserRole;
import com.hngy.cvs.common.util.BeanUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图对象
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "用户信息")
public class UserVO {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "student001")
    private String username;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "角色", example = "STUDENT")
    private UserRole role;

    @Schema(description = "邮箱", example = "student001@university.edu.cn")
    private String email;

    @Schema(description = "手机号", example = "13800138001")
    private String phone;

    @Schema(description = "创建时间", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;
}
