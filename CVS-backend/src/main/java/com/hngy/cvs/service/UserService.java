package com.hngy.cvs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hngy.cvs.dto.request.LoginDTO;
import com.hngy.cvs.dto.request.RegisterDTO;
import com.hngy.cvs.dto.response.LoginVO;
import com.hngy.cvs.dto.response.UserVO;
import com.hngy.cvs.entity.User;

/**
 * 用户服务接口
 * 
 * @author CVS Team
 */
public interface UserService {

    /**
     * 用户注册
     */
    UserVO register(RegisterDTO request);

    /**
     * 用户登录
     */
    LoginVO login(LoginDTO request);

    /**
     * 根据ID获取用户信息
     */
    UserVO getUserById(Long id);

    /**
     * 分页查询用户列表
     */
//    IPage<UserVO> getUserList(int page, int size);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 根据用户名获取用户
     */
    User getUserByUsername(String username);

    /**
     * 统计总用户数
     */
    Long countTotal();
}
