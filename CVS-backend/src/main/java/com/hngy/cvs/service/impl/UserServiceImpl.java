package com.hngy.cvs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.cvs.common.exception.BusinessException;
import com.hngy.cvs.common.result.ResultCode;
import com.hngy.cvs.common.util.AssertUtils;
import com.hngy.cvs.common.util.BeanUtil;
import com.hngy.cvs.common.util.JwtUtil;
import com.hngy.cvs.common.util.PasswordUtil;
import com.hngy.cvs.dto.request.LoginDTO;
import com.hngy.cvs.dto.request.RegisterDTO;
import com.hngy.cvs.dto.response.LoginVO;
import com.hngy.cvs.dto.response.UserVO;
import com.hngy.cvs.entity.User;
import com.hngy.cvs.mapper.UserMapper;
import com.hngy.cvs.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现类
 * 
 * @author CVS Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public UserVO register(RegisterDTO request) {
        checkFieldUnique(User::getUsername, request.getUsername(), "重复的用户名");
        checkFieldUnique(User::getEmail, request.getEmail(), "邮箱已被使用");
        checkFieldUnique(User::getPhone, request.getPhone(), "手机号已被使用");

        // 创建用户
        User user = BeanUtil.to(request, User.class);
        user.setPassword(PasswordUtil.encode(request.getPassword()));

        userMapper.insert(user);
        log.info("用户注册成功: {}", user.getUsername());

        return BeanUtil.to(user, UserVO.class);
    }

    /**
     * 检查某字段是否唯一
     */
    private <T> void checkFieldUnique(SFunction<User, T> column, T value, String message) {
        if (value == null) {
            return;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(column, value).eq(User::getDeleted, 0);
        AssertUtils.isTrue(userMapper.selectCount(wrapper) == 0,
                          ResultCode.USER_ALREADY_EXISTS);
    }

    @Override
    public LoginVO login(LoginDTO request) {
        // 根据用户名查找用户
        User user = getUserByUsername(request.getUsername());
        AssertUtils.notNull(user, ResultCode.USER_NOT_FOUND);

        // 验证密码
        AssertUtils.isTrue(PasswordUtil.matches(request.getPassword(), user.getPassword()),
                          ResultCode.INVALID_CREDENTIALS);

        // 生成JWT Token
        String token = jwtUtil.generateToken(
            user.getId(), 
            user.getUsername(), 
            user.getRole().getCode(), 
            request.getRememberMe()
        );

        log.info("用户登录成功: {}", user.getUsername());

        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        AssertUtils.notNull(user, ResultCode.USER_NOT_FOUND);
        return BeanUtil.to(user, UserVO.class);
    }

//    @Override
//    public IPage<UserVO> getUserList(int page, int size) {
//        Page<User> userPage = new Page<>(page, size);
//        IPage<User> result = userMapper.selectPage(userPage, null);
//
//        return result.convert(UserVO::from);
//    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userMapper.selectById(id);
        AssertUtils.notNull(user, ResultCode.USER_NOT_FOUND);

        userMapper.deleteById(id);
        log.info("删除用户: {}", user.getUsername());
    }

    @Override
    public boolean existsByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username)
               .eq(User::getDeleted, 0);
        return userMapper.selectCount(wrapper) > 0;
    }

    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username)
               .eq(User::getDeleted, 0);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public Long countTotal() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0);
        return userMapper.selectCount(wrapper);
    }

    /**
     * 根据邮箱查询用户
     */
    public User getUserByEmail(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email)
               .eq(User::getDeleted, 0);
        return userMapper.selectOne(wrapper);
    }

    /**
     * 根据手机号查询用户
     */
    public User getUserByPhone(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone)
               .eq(User::getDeleted, 0);
        return userMapper.selectOne(wrapper);
    }
}
