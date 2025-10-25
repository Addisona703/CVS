package com.hngy.cvs.service;

import com.hngy.cvs.dto.request.LoginDTO;
import com.hngy.cvs.dto.request.PageDTO;
import com.hngy.cvs.dto.request.RegisterDTO;
import com.hngy.cvs.dto.request.UserSearchDTO;
import com.hngy.cvs.dto.request.UserUpdateDTO;
import com.hngy.cvs.dto.response.LoginVO;
import com.hngy.cvs.dto.response.PageVO;
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
     * 分页查询用户列表（管理员端）
     * 不包含管理员账户，可以筛选老师或学生
     */
    PageVO<UserVO> getUserList(PageDTO<UserSearchDTO> pageRequest);

    /**
     * 更新用户信息
     */
    UserVO updateUser(UserUpdateDTO request, Long currentUserId);

    /**
     * 删除用户（仅管理员）
     */
    void deleteUser(Long id, Long currentUserId);

    /**
     * 重置用户密码
     */
    void resetPassword(String token, String newPassword);

    /**
     * 忘记密码（通过邮箱重置）
     */
    void forgotPassword(String email);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 根据用户名获取用户
     */
    User getUserByUsername(String username);

    /**
     * 根据邮箱获取用户
     */
    User getUserByEmail(String email);

    /**
     * 统计总用户数
     */
    Long countTotal();
}
