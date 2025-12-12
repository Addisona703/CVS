package com.hngy.cvs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.cvs.common.result.ResultCode;
import com.hngy.cvs.common.util.AssertUtils;
import com.hngy.cvs.common.util.BeanUtil;
import com.hngy.cvs.common.util.JwtUtil;
import com.hngy.cvs.common.util.PageUtil;
import com.hngy.cvs.common.util.PasswordUtil;
import com.hngy.cvs.dto.request.LoginDTO;
import com.hngy.cvs.dto.request.PageDTO;
import com.hngy.cvs.dto.request.RegisterDTO;
import com.hngy.cvs.dto.request.UserSearchDTO;
import com.hngy.cvs.dto.request.UserUpdateDTO;
import com.hngy.cvs.dto.response.LoginVO;
import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.dto.response.UserVO;
import com.hngy.cvs.entity.Points;
import com.hngy.cvs.entity.User;
import com.hngy.cvs.entity.enums.UserRole;
import com.hngy.cvs.mapper.PointsMapper;
import com.hngy.cvs.mapper.UserMapper;
import com.hngy.cvs.service.EmailService;
import com.hngy.cvs.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import java.time.Duration;
import java.util.UUID;

/**
 * 用户服务实现类
 * 
 * @author CVS Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String PASSWORD_RESET_TOKEN_PREFIX = "password_reset:";

    private final UserMapper userMapper;
    private final PointsMapper pointsMapper;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Value("${app.password-reset.token-expiration-minutes:30}")
    private long resetTokenExpirationMinutes;

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

        // 仅为学生初始化积分记录
        if (user.getRole() == UserRole.STUDENT) {
            Points points = new Points();
            points.setUserId(user.getId());
            points.setPoints(0);
            pointsMapper.insert(points);
            log.info("初始化学生 {} 积分记录，初始积分为0", user.getUsername());
        }

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

    @Override
    public PageVO<UserVO> getUserList(PageDTO<UserSearchDTO> pageRequest) {
        UserSearchDTO searchDTO = pageRequest.getParams();
        Page<User> page = PageUtil.toPage(pageRequest);

        // 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        // 排除学工处账户
        wrapper.ne(User::getRole, UserRole.ADMIN);
        
        if (searchDTO != null) {
            // 按用户名模糊查询
            if (StringUtils.isNotBlank(searchDTO.getUsername())) {
                wrapper.like(User::getUsername, searchDTO.getUsername());
            }
            
            // 按姓名模糊查询
            if (StringUtils.isNotBlank(searchDTO.getName())) {
                wrapper.like(User::getName, searchDTO.getName());
            }
            
            // 按角色筛选
            if (searchDTO.getRole() != null) {
                wrapper.eq(User::getRole, searchDTO.getRole());
            }
            
            // 按邮箱模糊查询
            if (StringUtils.isNotBlank(searchDTO.getEmail())) {
                wrapper.like(User::getEmail, searchDTO.getEmail());
            }
            
            // 按手机号模糊查询
            if (StringUtils.isNotBlank(searchDTO.getPhone())) {
                wrapper.like(User::getPhone, searchDTO.getPhone());
            }
        }
        
        // 按创建时间倒序排列
        wrapper.orderByDesc(User::getCreatedAt);
        
        // 分页查询
        IPage<User> result = userMapper.selectPage(page, wrapper);
        return PageUtil.convert(result, UserVO.class);
    }

    @Override
    @Transactional
    public UserVO updateUser(UserUpdateDTO request, Long currentUserId) {
        User user = userMapper.selectById(request.getId());
        AssertUtils.notNull(user, ResultCode.USER_NOT_FOUND);
        
        // 权限检查：只有学工处或用户本人可以修改
        User currentUser = userMapper.selectById(currentUserId);
        boolean isAdmin = currentUser != null && currentUser.getRole() == UserRole.ADMIN;
        boolean isSelf = user.getId().equals(currentUserId);
        AssertUtils.isTrue(isAdmin || isSelf, ResultCode.INSUFFICIENT_PERMISSIONS);
        
        // 检查唯一性
        if (StringUtils.isNotBlank(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
            checkFieldUnique(User::getEmail, request.getEmail(), "邮箱已被使用");
        }
        if (StringUtils.isNotBlank(request.getPhone()) && !request.getPhone().equals(user.getPhone())) {
            checkFieldUnique(User::getPhone, request.getPhone(), "手机号已被使用");
        }
        
        // 更新用户信息
        if (StringUtils.isNotBlank(request.getName())) {
            user.setName(request.getName());
        }
        if (StringUtils.isNotBlank(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        if (StringUtils.isNotBlank(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        
        userMapper.updateById(user);
        log.info("更新用户信息成功: {}", user.getUsername());
        
        return BeanUtil.to(user, UserVO.class);
    }

    @Override
    @Transactional
    public void deleteUser(Long id, Long currentUserId) {
        User user = userMapper.selectById(id);
        AssertUtils.notNull(user, ResultCode.USER_NOT_FOUND);
        
        // 防止删除学工处账户
        AssertUtils.isFalse(user.getRole() == UserRole.ADMIN, ResultCode.INSUFFICIENT_PERMISSIONS);
        
        // 防止学工处删除自己
        AssertUtils.isFalse(user.getId().equals(currentUserId), ResultCode.INSUFFICIENT_PERMISSIONS);

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
    @Transactional
    public void resetPassword(String token, String newPassword) {
        AssertUtils.notEmpty(token, "重置令牌不能为空");
        AssertUtils.notEmpty(newPassword, "新密码不能为空");

        String redisKey = PASSWORD_RESET_TOKEN_PREFIX + token;
        String userIdStr = stringRedisTemplate.opsForValue().get(redisKey);
        AssertUtils.notEmpty(userIdStr, ResultCode.TOKEN_INVALID);

        long userId;
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException ex) {
            stringRedisTemplate.delete(redisKey);
            log.warn("密码重置令牌 {} 存储的用户ID无效: {}", token, userIdStr);
            AssertUtils.fail(ResultCode.TOKEN_INVALID);
            return;
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            stringRedisTemplate.delete(redisKey);
            AssertUtils.fail(ResultCode.USER_NOT_FOUND);
        }

        AssertUtils.isFalse(PasswordUtil.matches(newPassword, user.getPassword()), "新密码不能与原密码相同");

        user.setPassword(PasswordUtil.encode(newPassword));
        userMapper.updateById(user);
        stringRedisTemplate.delete(redisKey);
        log.info("用户 {} 通过重置链接更新密码成功", user.getUsername());
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        AssertUtils.notEmpty(email, "邮箱不能为空");

        User user = getUserByEmail(email);
        AssertUtils.notNull(user, ResultCode.USER_NOT_FOUND);

        String token = generateResetToken();
        cacheResetToken(token, user.getId());

        String resetLink = buildResetLink(token);
        sendPasswordResetEmail(user, resetLink);

        log.info("用户 {} 请求密码重置，重置链接已发送", user.getUsername());
    }

    private String generateResetToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void cacheResetToken(String token, Long userId) {
        Duration expiration = Duration.ofMinutes(getResetTokenExpirationMinutes());
        String redisKey = PASSWORD_RESET_TOKEN_PREFIX + token;
        stringRedisTemplate.opsForValue().set(redisKey, String.valueOf(userId), expiration);
    }

    private String buildResetLink(String token) {
        String baseUrl = StringUtils.isBlank(frontendBaseUrl) ? "http://localhost:5173" : frontendBaseUrl.trim();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl);
        builder.pathSegment("reset-password");
        builder.replaceQueryParam("token", token);
        return builder.build(true).toUriString();
    }

    private void sendPasswordResetEmail(User user, String resetLink) {
        String displayName = StringUtils.isNotBlank(user.getName()) ? user.getName() : user.getUsername();
        String subject = "CVS系统 - 重置密码链接";
        String content = buildResetPasswordEmailContent(displayName, resetLink);
        emailService.sendHtmlMessage(user.getEmail(), subject, content);
    }

    private String buildResetPasswordEmailContent(String displayName, String resetLink) {
        return String.format(
                "<!DOCTYPE html>" +
                "<html><body>" +
                "<p>您好，%s：</p>" +
                "<p>我们收到了您账户的密码重置请求，请点击以下按钮进行重置：</p>" +
                "<p><a href=\"%s\" style=\"display:inline-block;padding:10px 16px;background-color:#409EFF;color:#ffffff;text-decoration:none;border-radius:4px;\">重置密码</a></p>" +
                "<p>如果按钮无法点击，请复制以下链接到浏览器中打开：</p>" +
                "<p>%s</p>" +
                "<p>重置链接将在 %d 分钟后失效。</p>" +
                "<p>如果这不是您的操作，请忽略此邮件。</p>" +
                "<p>CVS 高校志愿服务数字化系统</p>" +
                "</body></html>",
                displayName,
                resetLink,
                resetLink,
                getResetTokenExpirationMinutes()
        );
    }

    private long getResetTokenExpirationMinutes() {
        return resetTokenExpirationMinutes > 0 ? resetTokenExpirationMinutes : 30;
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
