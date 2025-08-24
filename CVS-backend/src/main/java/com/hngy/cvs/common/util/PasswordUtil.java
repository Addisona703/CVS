package com.hngy.cvs.common.util;

import lombok.Getter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类
 * 
 * @author CVS Team
 */
public class PasswordUtil {

    /**
     * -- GETTER --
     *  获取密码编码器实例
     */
    @Getter
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 加密密码
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 验证密码
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
