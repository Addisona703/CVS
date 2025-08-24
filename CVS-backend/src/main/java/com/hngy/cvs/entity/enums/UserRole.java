package com.hngy.cvs.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 * 
 * @author CVS Team
 */
@Getter
@AllArgsConstructor
public enum UserRole {

    STUDENT("STUDENT", "学生"),
    TEACHER("TEACHER", "教师"),
    ADMIN("ADMIN", "管理员");

    private final String code;
    private final String description;

    public static UserRole fromCode(String code) {
        for (UserRole role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("未知的角色代码: " + code);
    }
}
