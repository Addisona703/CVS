package com.hngy.cvs.common.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Principal;

/**
 * 用户主体信息类
 * 
 * @author CVS Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements Principal {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户角色
     */
    private String role;

    @Override
    public String getName() {
        return username;
    }
}
