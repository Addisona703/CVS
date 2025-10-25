package com.hngy.cvs.dto.request;

import com.hngy.cvs.entity.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户搜索请求DTO
 * 
 * @author CVS Team
 */
@Data
@Schema(description = "用户搜索请求")
public class UserSearchDTO {

    @Schema(description = "用户名关键词", example = "student")
    private String username;

    @Schema(description = "姓名关键词", example = "张三")
    private String name;

    @Schema(description = "角色筛选", example = "STUDENT")
    private UserRole role;

    @Schema(description = "邮箱关键词", example = "@university.edu.cn")
    private String email;

    @Schema(description = "手机号关键词", example = "138")
    private String phone;
}