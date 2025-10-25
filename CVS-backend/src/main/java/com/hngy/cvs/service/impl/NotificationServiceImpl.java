package com.hngy.cvs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.cvs.common.result.ResultCode;
import com.hngy.cvs.common.util.AssertUtils;
import com.hngy.cvs.common.util.BeanUtil;
import com.hngy.cvs.dto.request.NotificationDTO;
import com.hngy.cvs.dto.request.NotificationQuery;
import com.hngy.cvs.dto.response.NotificationVO;
import com.hngy.cvs.entity.Activity;
import com.hngy.cvs.entity.Notification;
import com.hngy.cvs.entity.Signup;
import com.hngy.cvs.entity.enums.NotificationType;
import com.hngy.cvs.entity.enums.SignupStatus;
import com.hngy.cvs.mapper.ActivityMapper;
import com.hngy.cvs.mapper.NotificationMapper;
import com.hngy.cvs.mapper.SignupMapper;
import com.hngy.cvs.mapper.UserMapper;
import com.hngy.cvs.service.NotificationService;
import com.hngy.cvs.service.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息通知服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    private final ActivityMapper activityMapper;
    private final SignupMapper signupMapper;
    private final UserMapper userMapper;
    private final NotificationTemplateService templateService;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("MM月dd日 HH:mm");

    @Override
    @Transactional
    public Long createNotification(NotificationDTO dto) {
        AssertUtils.notNull(dto, ResultCode.BAD_REQUEST);
        AssertUtils.notNull(dto.getUserId(), ResultCode.BAD_REQUEST);
        AssertUtils.notNull(dto.getType(), ResultCode.BAD_REQUEST);

        // 使用模板数据生成内容，如果没有提供则使用模板服务生成
        Map<String, Object> templateData = dto.getTemplateData() != null ? dto.getTemplateData() : new HashMap<>();
        
        String title = dto.getTitle();
        if (!StringUtils.hasText(title)) {
            title = templateService.generateTitle(dto.getType(), templateData);
        }
        
        String content = dto.getContent();
        if (!StringUtils.hasText(content)) {
            content = templateService.generateContent(dto.getType(), templateData);
        }
        
        String linkUrl = dto.getLinkUrl();
        if (!StringUtils.hasText(linkUrl)) {
            linkUrl = templateService.generateLinkUrl(dto.getType(), templateData);
        }

        Notification notification = new Notification();
        notification.setUserId(dto.getUserId());
        notification.setType(dto.getType());
        notification.setTitle(title);
        notification.setContent(content);
        notification.setLinkUrl(linkUrl);
        notification.setIsRead(false);
        notification.setCreatedTime(LocalDateTime.now());

        this.save(notification);
        log.info("创建通知成功: 用户ID={}, 类型={}, 标题={}", dto.getUserId(), dto.getType(), title);
        return notification.getId();
    }

    @Override
    public Long getUnreadCount(Long userId) {
        AssertUtils.notNull(userId, ResultCode.BAD_REQUEST);
        return baseMapper.countUnreadByUserId(userId);
    }

    @Override
    public IPage<NotificationVO> getNotificationList(Long userId, NotificationQuery query) {
        AssertUtils.notNull(userId, ResultCode.BAD_REQUEST);
        AssertUtils.notNull(query, ResultCode.BAD_REQUEST);

        Page<Notification> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<Notification> result = baseMapper.selectNotificationPage(page, userId, query.getType(), query.getIsRead());

        Page<NotificationVO> voPage = new Page<>(query.getPageNum(), query.getPageSize());
        voPage.setRecords(
                result.getRecords().stream()
                        .map(n -> BeanUtil.to(n, NotificationVO.class))
                        .collect(Collectors.toList())
        );
        voPage.setTotal(result.getTotal());
        voPage.setSize(result.getSize());
        voPage.setCurrent(result.getCurrent());
        voPage.setPages(result.getPages());

        return voPage;
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        AssertUtils.notNull(notificationId, ResultCode.BAD_REQUEST);
        AssertUtils.notNull(userId, ResultCode.BAD_REQUEST);

        Notification notification = this.getById(notificationId);
        AssertUtils.notNull(notification, ResultCode.NOT_FOUND);
        AssertUtils.isTrue(notification.getUserId().equals(userId), ResultCode.INSUFFICIENT_PERMISSIONS);

        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadTime(LocalDateTime.now());
            this.updateById(notification);
            log.info("标记通知已读: 通知ID={}, 用户ID={}", notificationId, userId);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        AssertUtils.notNull(userId, ResultCode.BAD_REQUEST);

        int count = baseMapper.markAllAsReadByUserId(userId);
        log.info("批量标记通知已读: 用户ID={}, 更新数量={}", userId, count);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        AssertUtils.notNull(notificationId, ResultCode.BAD_REQUEST);
        AssertUtils.notNull(userId, ResultCode.BAD_REQUEST);

        Notification notification = this.getById(notificationId);
        AssertUtils.notNull(notification, ResultCode.NOT_FOUND);
        AssertUtils.isTrue(notification.getUserId().equals(userId), ResultCode.INSUFFICIENT_PERMISSIONS);

        this.removeById(notificationId);
        log.info("删除通知: 通知ID={}, 用户ID={}", notificationId, userId);
    }

    @Override
    @Transactional
    public void clearReadNotifications(Long userId) {
        AssertUtils.notNull(userId, ResultCode.BAD_REQUEST);

        int count = baseMapper.deleteReadNotificationsByUserId(userId);
        log.info("清空已读通知: 用户ID={}, 删除数量={}", userId, count);
    }

    @Override
    @Transactional
    public void sendActivityStartNotification(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.warn("活动不存在，无法发送开始通知: {}", activityId);
            return;
        }

        // 检查教师是否已收到该活动的开始通知（去重）
        long existingTeacherNotification = this.count(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, activity.getOrganizerId())
                        .eq(Notification::getType, NotificationType.ACTIVITY_START)
                        .like(Notification::getContent, activity.getTitle())
        );
        
        if (existingTeacherNotification > 0) {
            log.info("活动开始通知已存在，跳过发送: 活动ID={}, 标题={}", activityId, activity.getTitle());
            return;
        }

        // 获取已批准的报名学生
        List<Signup> signups = signupMapper.selectList(
                new LambdaQueryWrapper<Signup>()
                        .eq(Signup::getActivityId, activityId)
                        .eq(Signup::getStatus, SignupStatus.APPROVED)
        );

        String timeStr = activity.getStartTime().format(TIME_FORMATTER);
        
        // 创建模板数据
        Map<String, Object> templateData = templateService.createActivityStatusData(
                activityId, activity.getTitle(), timeStr, activity.getLocation()
        );

        // 通知教师
        NotificationDTO teacherDto = new NotificationDTO();
        teacherDto.setUserId(activity.getOrganizerId());
        teacherDto.setType(NotificationType.ACTIVITY_START);
        teacherDto.setTemplateData(templateData);
        createNotification(teacherDto);

        int studentNotificationCount = 0;
        // 通知学生（检查去重）
        for (Signup signup : signups) {
            // 检查该学生是否已收到该活动的开始通知
            long existingStudentNotification = this.count(
                    new LambdaQueryWrapper<Notification>()
                            .eq(Notification::getUserId, signup.getUserId())
                            .eq(Notification::getType, NotificationType.ACTIVITY_START)
                            .like(Notification::getContent, activity.getTitle())
            );
            
            if (existingStudentNotification == 0) {
                NotificationDTO studentDto = new NotificationDTO();
                studentDto.setUserId(signup.getUserId());
                studentDto.setType(NotificationType.ACTIVITY_START);
                studentDto.setTemplateData(templateData);
                createNotification(studentDto);
                studentNotificationCount++;
            }
        }

        log.info("发送活动开始通知: 活动ID={}, 通知数量={}", activityId, studentNotificationCount + 1);
    }

    @Override
    @Transactional
    public void sendActivityOngoingNotification(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.warn("活动不存在，无法发送进行中通知: {}", activityId);
            return;
        }

        long existingTeacherNotification = this.count(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, activity.getOrganizerId())
                        .eq(Notification::getType, NotificationType.ACTIVITY_ONGOING)
                        .like(Notification::getContent, activity.getTitle())
        );

        if (existingTeacherNotification > 0) {
            log.info("活动进行中通知已存在，跳过发送: 活动ID={}, 标题={}", activityId, activity.getTitle());
            return;
        }

        List<Signup> signups = signupMapper.selectList(
                new LambdaQueryWrapper<Signup>()
                        .eq(Signup::getActivityId, activityId)
                        .eq(Signup::getStatus, SignupStatus.APPROVED)
        );

        String timeStr = activity.getStartTime() != null ? activity.getStartTime().format(TIME_FORMATTER) : null;
        Map<String, Object> templateData = templateService.createActivityStatusData(
                activityId, activity.getTitle(), timeStr, activity.getLocation()
        );

        NotificationDTO teacherDto = new NotificationDTO();
        teacherDto.setUserId(activity.getOrganizerId());
        teacherDto.setType(NotificationType.ACTIVITY_ONGOING);
        teacherDto.setTemplateData(templateData);
        createNotification(teacherDto);

        int studentNotificationCount = 0;
        for (Signup signup : signups) {
            long existingStudentNotification = this.count(
                    new LambdaQueryWrapper<Notification>()
                            .eq(Notification::getUserId, signup.getUserId())
                            .eq(Notification::getType, NotificationType.ACTIVITY_ONGOING)
                            .like(Notification::getContent, activity.getTitle())
            );

            if (existingStudentNotification == 0) {
                NotificationDTO studentDto = new NotificationDTO();
                studentDto.setUserId(signup.getUserId());
                studentDto.setType(NotificationType.ACTIVITY_ONGOING);
                studentDto.setTemplateData(templateData);
                createNotification(studentDto);
                studentNotificationCount++;
            }
        }

        log.info("发送活动进行中通知: 活动ID={}, 通知数量={}", activityId, studentNotificationCount + 1);
    }

    @Override
    @Transactional
    public void sendActivityEndNotification(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.warn("活动不存在，无法发送结束通知: {}", activityId);
            return;
        }

        // 检查教师是否已收到该活动的结束通知（去重）
        long existingTeacherNotification = this.count(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, activity.getOrganizerId())
                        .eq(Notification::getType, NotificationType.ACTIVITY_END)
                        .like(Notification::getContent, activity.getTitle())
        );
        
        if (existingTeacherNotification > 0) {
            log.info("活动结束通知已存在，跳过发送: 活动ID={}, 标题={}", activityId, activity.getTitle());
            return;
        }

        // 获取已签到的学生
        List<Signup> signups = signupMapper.selectList(
                new LambdaQueryWrapper<Signup>()
                        .eq(Signup::getActivityId, activityId)
                        .eq(Signup::getStatus, SignupStatus.APPROVED)
                        .eq(Signup::getSignedIn, true)
        );

        // 创建模板数据
        Map<String, Object> templateData = templateService.createActivityStatusData(
                activityId, activity.getTitle(), null, null
        );

        // 通知教师
        NotificationDTO teacherDto = new NotificationDTO();
        teacherDto.setUserId(activity.getOrganizerId());
        teacherDto.setType(NotificationType.ACTIVITY_END);
        teacherDto.setTemplateData(templateData);
        createNotification(teacherDto);

        int studentNotificationCount = 0;
        // 通知已签到但未签退的学生（检查去重）
        for (Signup signup : signups) {
            if (!Boolean.TRUE.equals(signup.getSignedOut())) {
                // 检查该学生是否已收到该活动的结束通知
                long existingStudentNotification = this.count(
                        new LambdaQueryWrapper<Notification>()
                                .eq(Notification::getUserId, signup.getUserId())
                                .eq(Notification::getType, NotificationType.ACTIVITY_END)
                                .like(Notification::getContent, activity.getTitle())
                );
                
                if (existingStudentNotification == 0) {
                    NotificationDTO studentDto = new NotificationDTO();
                    studentDto.setUserId(signup.getUserId());
                    studentDto.setType(NotificationType.ACTIVITY_END);
                    studentDto.setTemplateData(templateData);
                    createNotification(studentDto);
                    studentNotificationCount++;
                }
            }
        }

        log.info("发送活动结束通知: 活动ID={}, 通知数量={}", activityId, studentNotificationCount + 1);
    }

    @Override
    @Transactional
    public void sendActivityCancelNotification(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.warn("活动不存在，无法发送取消通知: {}", activityId);
            return;
        }

        // 获取所有已报名的学生
        List<Signup> signups = signupMapper.selectList(
                new LambdaQueryWrapper<Signup>()
                        .eq(Signup::getActivityId, activityId)
        );

        // 创建模板数据
        Map<String, Object> templateData = templateService.createActivityStatusData(
                activityId, activity.getTitle(), null, null
        );

        // 通知所有报名学生 - 使用ACTIVITY_END作为取消通知的类型
        for (Signup signup : signups) {
            NotificationDTO dto = new NotificationDTO();
            dto.setUserId(signup.getUserId());
            dto.setType(NotificationType.ACTIVITY_END);
            dto.setTitle("活动已取消");
            dto.setContent(String.format("很抱歉，活动《%s》已被取消。", activity.getTitle()));
            dto.setTemplateData(templateData);
            createNotification(dto);
        }

        log.info("发送活动取消通知: 活动ID={}, 通知数量={}", activityId, signups.size());
    }

    @Override
    @Transactional
    public void sendRegistrationPendingNotification(Long activityId, Long studentId, Long teacherId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.warn("活动不存在，无法发送报名审核通知: {}", activityId);
            return;
        }

        var student = userMapper.selectById(studentId);
        if (student == null) {
            log.warn("学生不存在，无法发送报名审核通知: {}", studentId);
            return;
        }

        // 创建模板数据
        Map<String, Object> templateData = templateService.createRegistrationPendingData(
                activityId, activity.getTitle(), student.getName(), student.getUsername()
        );

        // 通知教师
        NotificationDTO dto = new NotificationDTO();
        dto.setUserId(teacherId);
        dto.setType(NotificationType.REGISTRATION_PENDING);
        dto.setTemplateData(templateData);
        createNotification(dto);

        log.info("发送报名审核通知: 活动ID={}, 学生ID={}, 教师ID={}", activityId, studentId, teacherId);
    }

    @Override
    @Transactional
    public void sendRegistrationResultNotification(Long activityId, Long studentId, boolean approved, String reason) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.warn("活动不存在，无法发送报名结果通知: {}", activityId);
            return;
        }

        String timeStr = activity.getStartTime() != null ? activity.getStartTime().format(TIME_FORMATTER) : null;
        
        // 创建模板数据
        Map<String, Object> templateData = templateService.createApprovalResultData(
                activityId, activity.getTitle(), approved, reason
        );
        
        // 添加活动时间和地点信息
        templateData.put("activityTime", timeStr);
        templateData.put("activityLocation", activity.getLocation());

        // 通知学生
        NotificationDTO dto = new NotificationDTO();
        dto.setUserId(studentId);
        dto.setType(approved ? NotificationType.REGISTRATION_APPROVED : NotificationType.REGISTRATION_REJECTED);
        dto.setTemplateData(templateData);
        createNotification(dto);

        log.info("发送报名结果通知: 活动ID={}, 学生ID={}, 结果={}", activityId, studentId, approved ? "通过" : "拒绝");
    }

    @Override
    @Transactional
    public void sendCheckoutPendingNotification(Long activityId, Long studentId, Long teacherId, String checkoutReason) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.warn("活动不存在，无法发送签退审核通知: {}", activityId);
            return;
        }

        var student = userMapper.selectById(studentId);
        if (student == null) {
            log.warn("学生不存在，无法发送签退审核通知: {}", studentId);
            return;
        }

        // 创建模板数据
        Map<String, Object> templateData = templateService.createCheckoutPendingData(
                activityId, activity.getTitle(), student.getName(), student.getUsername(), checkoutReason
        );

        // 通知教师
        NotificationDTO dto = new NotificationDTO();
        dto.setUserId(teacherId);
        dto.setType(NotificationType.CHECKOUT_PENDING);
        dto.setTemplateData(templateData);
        createNotification(dto);

        log.info("发送签退审核通知: 活动ID={}, 学生ID={}, 教师ID={}", activityId, studentId, teacherId);
    }

    @Override
    @Transactional
    public void sendCheckoutResultNotification(Long activityId, Long studentId, boolean approved, String reason) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.warn("活动不存在，无法发送签退结果通知: {}", activityId);
            return;
        }

        // 创建模板数据
        Map<String, Object> templateData = templateService.createApprovalResultData(
                activityId, activity.getTitle(), approved, reason
        );

        // 通知学生
        NotificationDTO dto = new NotificationDTO();
        dto.setUserId(studentId);
        dto.setType(approved ? NotificationType.CHECKOUT_APPROVED : NotificationType.CHECKOUT_REJECTED);
        dto.setTemplateData(templateData);
        createNotification(dto);

        log.info("发送签退结果通知: 活动ID={}, 学生ID={}, 结果={}", activityId, studentId, approved ? "通过" : "拒绝");
    }

    @Override
    public List<NotificationVO> getUnreadNotifications(Long userId) {
        List<Notification> notifications = this.list(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, false)
                        .orderByDesc(Notification::getCreatedTime)
                        .last("LIMIT 10")
        );

        return notifications.stream()
                .map(n -> BeanUtil.to(n, NotificationVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public IPage<NotificationVO> getUserNotifications(Long userId, int pageNum, int pageSize, Boolean isRead) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        
        if (isRead != null) {
            wrapper.eq(Notification::getIsRead, isRead);
        }
        
        wrapper.orderByDesc(Notification::getCreatedTime);

        Page<Notification> page = new Page<>(pageNum, pageSize);
        IPage<Notification> result = this.page(page, wrapper);

        Page<NotificationVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setRecords(
                result.getRecords().stream()
                        .map(n -> BeanUtil.to(n, NotificationVO.class))
                        .collect(Collectors.toList())
        );
        voPage.setTotal(result.getTotal());
        voPage.setSize(result.getSize());
        voPage.setCurrent(result.getCurrent());
        voPage.setPages(result.getPages());

        return voPage;
    }

    @Override
    @Transactional
    public void sendActivityApprovalNotification(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.warn("活动不存在，无法发送审核通知: {}", activityId);
            return;
        }

        // 获取所有管理员
        List<com.hngy.cvs.entity.User> admins = userMapper.selectList(
                new LambdaQueryWrapper<com.hngy.cvs.entity.User>()
                        .eq(com.hngy.cvs.entity.User::getRole, com.hngy.cvs.entity.enums.UserRole.ADMIN)
        );

        // 获取活动创建者信息
        com.hngy.cvs.entity.User organizer = userMapper.selectById(activity.getOrganizerId());
        String organizerName = organizer != null ? organizer.getName() : "未知";

        // 通知所有管理员
        for (com.hngy.cvs.entity.User admin : admins) {
            NotificationDTO dto = new NotificationDTO();
            dto.setUserId(admin.getId());
            dto.setType(NotificationType.SYSTEM);
            dto.setTitle("活动待审核");
            dto.setContent(String.format("教师 %s 提交了活动《%s》，请及时审核。", organizerName, activity.getTitle()));
            dto.setLinkUrl("/admin/activities");
            createNotification(dto);
        }

        log.info("发送活动审核通知: 活动ID={}, 通知管理员数量={}", activityId, admins.size());
    }

    @Override
    @Transactional
    public void sendActivityApprovalResultNotification(Long activityId, Boolean approved) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.warn("活动不存在，无法发送审核结果通知: {}", activityId);
            return;
        }

        // 通知活动创建者
        NotificationDTO dto = new NotificationDTO();
        dto.setUserId(activity.getOrganizerId());
        dto.setType(NotificationType.SYSTEM);
        
        if (approved) {
            dto.setTitle("活动审核通过");
            dto.setContent(String.format("您的活动《%s》已通过审核，现已发布。", activity.getTitle()));
        } else {
            dto.setTitle("活动审核未通过");
            String reason = activity.getRejectReason() != null ? activity.getRejectReason() : "未说明";
            dto.setContent(String.format("您的活动《%s》未通过审核。原因：%s", activity.getTitle(), reason));
        }
        
        dto.setLinkUrl("/teacher/activities");
        createNotification(dto);

        log.info("发送活动审核结果通知: 活动ID={}, 结果={}", activityId, approved ? "通过" : "拒绝");
    }

    /**
     * 创建带有增强链接生成的通知
     * @param dto 通知DTO
     * @param additionalParams 额外的URL参数
     * @return 通知ID
     */
    @Transactional
    public Long createNotificationWithEnhancedLink(NotificationDTO dto, Map<String, String> additionalParams) {
        AssertUtils.notNull(dto, ResultCode.BAD_REQUEST);
        AssertUtils.notNull(dto.getUserId(), ResultCode.BAD_REQUEST);
        AssertUtils.notNull(dto.getType(), ResultCode.BAD_REQUEST);

        // 使用模板数据生成内容
        Map<String, Object> templateData = dto.getTemplateData() != null ? dto.getTemplateData() : new HashMap<>();
        
        String title = dto.getTitle();
        if (!StringUtils.hasText(title)) {
            title = templateService.generateTitle(dto.getType(), templateData);
        }
        
        String content = dto.getContent();
        if (!StringUtils.hasText(content)) {
            content = templateService.generateContent(dto.getType(), templateData);
        }
        
        // 使用增强的链接生成方法
        String linkUrl = dto.getLinkUrl();
        if (!StringUtils.hasText(linkUrl)) {
            if (additionalParams != null && !additionalParams.isEmpty()) {
                linkUrl = templateService.generateLinkUrlWithParams(dto.getType(), templateData, additionalParams);
            } else {
                linkUrl = templateService.generateLinkUrl(dto.getType(), templateData);
            }
        }

        Notification notification = new Notification();
        notification.setUserId(dto.getUserId());
        notification.setType(dto.getType());
        notification.setTitle(title);
        notification.setContent(content);
        notification.setLinkUrl(linkUrl);
        notification.setIsRead(false);
        notification.setCreatedTime(LocalDateTime.now());

        this.save(notification);
        log.info("创建增强链接通知成功: 用户ID={}, 类型={}, 标题={}, 链接={}", 
                dto.getUserId(), dto.getType(), title, linkUrl);
        return notification.getId();
    }




}
