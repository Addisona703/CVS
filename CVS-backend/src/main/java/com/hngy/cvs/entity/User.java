package com.hngy.cvs.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hngy.cvs.entity.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 
 * @author CVS Team
 */
@Data
@TableName("user_twb")
public class User {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（学号/工号）
     */
    private String username;

    /**
     * 密码（加密）
     */
    private String password;

    /**
     * 姓名
     */
    private String name;

    /**
     * 角色
     */
    private UserRole role;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    private Integer deleted;
}
