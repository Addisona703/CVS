package com.hngy.cvs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.cvs.common.result.ResultCode;
import com.hngy.cvs.common.util.AssertUtils;
import com.hngy.cvs.common.util.BeanUtil;
import com.hngy.cvs.common.util.PageUtil;
import com.hngy.cvs.dto.request.ActivityCreateDTO;
import com.hngy.cvs.dto.request.ActivitySearchDTO;
import com.hngy.cvs.dto.request.ActivityUpdateDTO;
import com.hngy.cvs.dto.request.PageDTO;
import com.hngy.cvs.dto.response.ActivityStatisticsVO;
import com.hngy.cvs.dto.response.ActivityVO;
import com.hngy.cvs.dto.response.DailyStatisticsVO;
import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.entity.Activity;
import com.hngy.cvs.entity.Signup;
import com.hngy.cvs.entity.User;
import com.hngy.cvs.entity.enums.ActivityStatus;
import com.hngy.cvs.entity.enums.SignupStatus;
import com.hngy.cvs.mapper.ActivityMapper;
import com.hngy.cvs.mapper.SignupMapper;
import com.hngy.cvs.mapper.UserMapper;
import com.hngy.cvs.service.ActivityService;
import com.hngy.cvs.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;

/**
 * 活动服务实现类
 * 
 * @author CVS Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityMapper activityMapper;
    private final SignupMapper signupMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ActivityVO createActivity(ActivityCreateDTO request, Long organizerId) {
        // 验证活动时间
        validateActivityTimes(request.getStartTime(), request.getEndTime(), request.getRegistrationDeadline());

        // 创建活动
        Activity activity = BeanUtil.to(request, Activity.class);
        activity.setOrganizerId(organizerId);

        activityMapper.insert(activity);
        log.info("创建活动成功: {}, 组织者: {}", activity.getTitle(), organizerId);

        ActivityVO activityVO = BeanUtil.to(activity, ActivityVO.class);
        // 为创建的活动也补充组织者名字
        enrichActivityVOList(List.of(activityVO));
        return activityVO;
    }

    @Override
    @Transactional
    public ActivityVO updateActivity(ActivityUpdateDTO request, Long organizerId) {
        Activity activity = activityMapper.selectById(request.getId());
        AssertUtils.notNull(activity, ResultCode.ACTIVITY_NOT_FOUND);

        // 检查权限
        AssertUtils.isTrue(activity.getOrganizerId().equals(organizerId),
                          ResultCode.INSUFFICIENT_PERMISSIONS);

        // 检查活动状态
        AssertUtils.isFalse(activity.getStatus() == ActivityStatus.ONGOING || activity.getStatus() == ActivityStatus.COMPLETED,
                           ResultCode.ACTIVITY_STATUS_INVALID_FOR_EDIT);

        // 验证活动时间
        validateActivityTimes(request.getStartTime(), request.getEndTime(), request.getRegistrationDeadline());

        // 更新活动信息
        BeanUtil.to(request, activity, "id", "organizerId", "createdAt");
        activityMapper.updateById(activity);
        log.info("更新活动成功: {}", activity.getId());

        ActivityVO activityVO = BeanUtil.to(activity, ActivityVO.class);
        // 为更新的活动也补充组织者名字
        enrichActivityVOList(List.of(activityVO));
        return activityVO;
    }

    @Override
    @Transactional
    public void deleteActivity(Long id, Long organizerId) {
        Activity activity = activityMapper.selectById(id);
        AssertUtils.notNull(activity, ResultCode.ACTIVITY_NOT_FOUND);

        // 检查权限
        AssertUtils.isTrue(activity.getOrganizerId().equals(organizerId),
                          ResultCode.INSUFFICIENT_PERMISSIONS);

        // 检查活动状态
        AssertUtils.isFalse(activity.getStatus() == ActivityStatus.ONGOING || activity.getStatus() == ActivityStatus.COMPLETED,
                           ResultCode.ACTIVITY_STATUS_INVALID_FOR_DELETE);

        // 使用MyBatis-Plus的逻辑删除功能
        activityMapper.deleteById(id);
        log.info("删除活动成功: {}", id);
    }

    @Override
    public ActivityVO getActivityById(Long id) {
        Activity activity = activityMapper.selectById(id);
        AssertUtils.notNull(activity, ResultCode.ACTIVITY_NOT_FOUND);
        
        // 根据时间自动更新活动状态
        updateActivityStatusByTime(activity);
        
        ActivityVO activityVO = BeanUtil.to(activity, ActivityVO.class);
        // 为单个活动也补充组织者名字
        enrichActivityVOList(List.of(activityVO));
        return activityVO;
    }
    
    /**
     * 根据当前时间自动更新活动状态
     */
    private void updateActivityStatusByTime(Activity activity) {
        LocalDateTime now = LocalDateTime.now();
        ActivityStatus currentStatus = activity.getStatus();
        ActivityStatus newStatus = currentStatus;
        
        // 只有已发布的活动才需要根据时间更新状态
        if (currentStatus == ActivityStatus.PUBLISHED) {
            if (now.isAfter(activity.getEndTime())) {
                // 活动已结束
                newStatus = ActivityStatus.COMPLETED;
            } else if (now.isAfter(activity.getStartTime())) {
                // 活动进行中
                newStatus = ActivityStatus.ONGOING;
            }
        } else if (currentStatus == ActivityStatus.ONGOING) {
            if (now.isAfter(activity.getEndTime())) {
                // 活动已结束
                newStatus = ActivityStatus.COMPLETED;
            }
        }
        
        // 如果状态发生变化，更新数据库
        if (newStatus != currentStatus) {
            activity.setStatus(newStatus);
            activityMapper.updateById(activity);
            log.info("自动更新活动状态: 活动ID={}, 原状态={}, 新状态={}", 
                    activity.getId(), currentStatus, newStatus);
        }
    }

    @Override
    public PageVO<ActivityVO> getActivityList(PageDTO<ActivitySearchDTO> pageRequest) {
        ActivitySearchDTO searchDTO = pageRequest.getParams();
        Page<Activity> page = PageUtil.toPage(pageRequest);

        // 构建活动查询条件
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        if (searchDTO != null) {
            wrapper.like(StringUtils.isNotBlank(searchDTO.getTitle()), Activity::getTitle, searchDTO.getTitle())
                    .eq(searchDTO.getStatus() != null, Activity::getStatus, searchDTO.getStatus())
                    .like(StringUtils.isNotBlank(searchDTO.getLocation()), Activity::getLocation, searchDTO.getLocation())
                    .ge(searchDTO.getStartTimeFrom() != null, Activity::getStartTime, searchDTO.getStartTimeFrom())
                    .le(searchDTO.getStartTimeTo() != null, Activity::getStartTime, searchDTO.getStartTimeTo())
                    .eq(searchDTO.getOrganizerId() != null, Activity::getOrganizerId, searchDTO.getOrganizerId());

            // 按组织者姓名筛选
            if (StringUtils.isNotBlank(searchDTO.getOrganizerName())) {
                List<Long> organizerIds = userMapper.selectList(
                        new LambdaQueryWrapper<User>()
                                .like(User::getName, searchDTO.getOrganizerName())
                                .select(User::getId)
                ).stream().map(User::getId).toList();

                if (organizerIds.isEmpty()) {
                    wrapper.eq(Activity::getId, -1L); // 保证查不到
                } else {
                    wrapper.in(Activity::getOrganizerId, organizerIds);
                }
            }
        }

        wrapper.orderByDesc(Activity::getCreatedAt);

        // 分页查询
        IPage<Activity> result = activityMapper.selectPage(page, wrapper);
        PageVO<ActivityVO> pageVO = PageUtil.convert(result, ActivityVO.class);

        enrichActivityVOList(pageVO.getRecords());
        return pageVO;
    }

    @Override
    public PageVO<ActivityVO> getMyActivitiesList(PageDTO<ActivitySearchDTO> pageRequest, Long organizerId) {
        ActivitySearchDTO searchDTO = pageRequest.getParams();
        System.out.println(pageRequest);
        // 创建分页对象
        Page<Activity> page = PageUtil.toPage(pageRequest);

        // 条件构造器，利用 MyBatis-Plus 的 condition 参数写法
        LambdaQueryWrapper<Activity> wrapper = null;
        if (searchDTO != null) {
            wrapper = new LambdaQueryWrapper<Activity>()
                    .eq(Activity::getOrganizerId, organizerId)
                    .like(StringUtils.isNotBlank(searchDTO.getTitle()),
                            Activity::getTitle, searchDTO.getTitle())
                    .eq(searchDTO.getStatus() != null,
                            Activity::getStatus, searchDTO.getStatus())
                    .like(StringUtils.isNotBlank(searchDTO.getLocation()),
                            Activity::getLocation, searchDTO.getLocation())
                    .ge(searchDTO.getStartTimeFrom() != null,
                            Activity::getStartTime, searchDTO.getStartTimeFrom())
                    .le(searchDTO.getStartTimeTo() != null,
                            Activity::getStartTime, searchDTO.getStartTimeTo())
                    .orderByDesc(Activity::getCreatedAt);
        }

        IPage<Activity> result = activityMapper.selectPage(page, wrapper);
        PageVO<ActivityVO> pageVO = PageUtil.convert(result, ActivityVO.class);
        
        // 补充组织者名字和报名数量信息
        enrichActivityVOList(pageVO.getRecords());
        
        return pageVO;
    }

    @Override
    @Transactional
    public void publishActivity(Long id, Long currentUserId) {
        Activity activity = activityMapper.selectById(id);
        AssertUtils.notNull(activity, ResultCode.ACTIVITY_NOT_FOUND);

        // 检查权限：只有活动创建者才能发布
        AssertUtils.isTrue(activity.getOrganizerId().equals(currentUserId),
                          ResultCode.INSUFFICIENT_PERMISSIONS);

        // 检查活动状态
        AssertUtils.isTrue(activity.getStatus() == ActivityStatus.DRAFT || activity.getStatus() == ActivityStatus.REJECTED,
                          ResultCode.ACTIVITY_STATUS_INVALID_FOR_PUBLISH);

        // 检查活动时间（允许过去的时间用于测试）
        // AssertUtils.isFalse(activity.getStartTime().isBefore(LocalDateTime.now()),
        //                    ResultCode.ACTIVITY_START_TIME_INVALID);

        // 获取当前用户信息，判断是否为管理员
        com.hngy.cvs.entity.User currentUser = userMapper.selectById(currentUserId);
        boolean isAdmin = currentUser != null && currentUser.getRole() == com.hngy.cvs.entity.enums.UserRole.ADMIN;

        if (isAdmin) {
            // 管理员创建的活动直接发布，不需要审核
            activity.setStatus(ActivityStatus.PUBLISHED);
            activity.setApproverId(currentUserId); // 管理员自己审核
            activity.setApprovedAt(LocalDateTime.now());
            activity.setRejectReason(null);
            activityMapper.updateById(activity);
            log.info("管理员直接发布活动: {}, 活动ID: {}", currentUserId, id);
        } else {
            // 教师创建的活动需要提交审核
            activity.setStatus(ActivityStatus.PENDING_APPROVAL);
            activity.setRejectReason(null); // 清空之前的拒绝原因
            activityMapper.updateById(activity);
            log.info("教师提交活动审核: {}, 活动ID: {}", currentUserId, id);

            // 发送通知给管理员
            notificationService.sendActivityApprovalNotification(id);
        }
    }

    @Override
    @Transactional
    public void approveActivity(Long id, Boolean approved, String rejectReason, Long approverId) {
        Activity activity = activityMapper.selectById(id);
        AssertUtils.notNull(activity, ResultCode.ACTIVITY_NOT_FOUND);

        // 检查活动状态
        AssertUtils.isTrue(activity.getStatus() == ActivityStatus.PENDING_APPROVAL,
                          ResultCode.ACTIVITY_STATUS_INVALID_FOR_APPROVAL);

        if (approved) {
            // 审核通过，发布活动
            activity.setStatus(ActivityStatus.PUBLISHED);
            activity.setApproverId(approverId);
            activity.setApprovedAt(LocalDateTime.now());
            activity.setRejectReason(null);
            log.info("活动审核通过: {}, 审核人: {}", id, approverId);
        } else {
            // 审核拒绝
            AssertUtils.notBlank(rejectReason, ResultCode.REJECT_REASON_REQUIRED);
            activity.setStatus(ActivityStatus.REJECTED);
            activity.setApproverId(approverId);
            activity.setApprovedAt(LocalDateTime.now());
            activity.setRejectReason(rejectReason);
            log.info("活动审核拒绝: {}, 审核人: {}, 原因: {}", id, approverId, rejectReason);
        }

        activityMapper.updateById(activity);

        // 发送通知给活动创建者
        notificationService.sendActivityApprovalResultNotification(id, approved);
    }

    @Override
    @Transactional
    public void cancelActivity(Long id, Long organizerId) {
        Activity activity = activityMapper.selectById(id);
        AssertUtils.notNull(activity, ResultCode.ACTIVITY_NOT_FOUND);

        // 检查权限
        AssertUtils.isTrue(activity.getOrganizerId().equals(organizerId),
                          ResultCode.INSUFFICIENT_PERMISSIONS);

        // 检查活动状态
        AssertUtils.isFalse(activity.getStatus() == ActivityStatus.COMPLETED,
                           ResultCode.ACTIVITY_STATUS_INVALID_FOR_CANCEL);

        // 检查是否在报名时间内才可以取消
        LocalDateTime now = LocalDateTime.now();
        AssertUtils.isTrue(activity.getRegistrationDeadline() == null || now.isBefore(activity.getRegistrationDeadline()),
                          ResultCode.ACTIVITY_REGISTRATION_DEADLINE_PASSED);

        // 发送取消通知
        notificationService.sendActivityCancelNotification(id);

        // 批量删除该活动的所有报名记录
        LambdaQueryWrapper<Signup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Signup::getActivityId, id);
        int deletedCount = signupMapper.delete(wrapper);

        activity.setStatus(ActivityStatus.CANCELLED);
        activityMapper.updateById(activity);
        log.info("取消活动成功: {}, 已删除 {} 条相关报名记录", id, deletedCount);
    }

    @Override
    public Long countActivities() {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getDeleted, 0);
        return activityMapper.selectCount(wrapper);
    }

    @Override
    public Long countActivitiesByOrganizer(Long organizerId) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getOrganizerId, organizerId)
                .eq(Activity::getDeleted, 0);
        return activityMapper.selectCount(wrapper);
    }

    /**
     * 批量补充组织者姓名、报名人数、签到人数、签退人数
     */
    private void enrichActivityVOList(List<ActivityVO> activityVOs) {
        if (activityVOs == null || activityVOs.isEmpty()) return;

        List<Long> activityIds = activityVOs.stream().map(ActivityVO::getId).toList();

        // 组织者ID -> 姓名
        Map<Long, String> organizerNameMap = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .in(User::getId, activityVOs.stream().map(ActivityVO::getOrganizerId).toList())
                        .select(User::getId, User::getName)
        ).stream().collect(Collectors.toMap(User::getId, User::getName));

        // 查询所有相关的报名记录
        List<Signup> signups = signupMapper.selectList(
                new LambdaQueryWrapper<Signup>()
                        .in(Signup::getActivityId, activityIds)
                        .eq(Signup::getStatus, SignupStatus.APPROVED)
        );

        // 活动ID -> 报名人数（已批准）
        Map<Long, Long> signupCountMap = signups.stream()
                .collect(Collectors.groupingBy(Signup::getActivityId, Collectors.counting()));

        // 活动ID -> 签到人数
        Map<Long, Long> checkinCountMap = signups.stream()
                .filter(s -> Boolean.TRUE.equals(s.getSignedIn()))
                .collect(Collectors.groupingBy(Signup::getActivityId, Collectors.counting()));

        // 活动ID -> 签退人数
        Map<Long, Long> checkoutCountMap = signups.stream()
                .filter(s -> Boolean.TRUE.equals(s.getSignedOut()))
                .collect(Collectors.groupingBy(Signup::getActivityId, Collectors.counting()));

        // 填充 VO
        activityVOs.forEach(vo -> {
            String organizerName = organizerNameMap.getOrDefault(vo.getOrganizerId(), "未知");
            vo.setOrganizerName(organizerName);
            vo.setCurrentParticipants(signupCountMap.getOrDefault(vo.getId(), 0L).intValue());
            vo.setCheckinCount(checkinCountMap.getOrDefault(vo.getId(), 0L).intValue());
            vo.setCheckoutCount(checkoutCountMap.getOrDefault(vo.getId(), 0L).intValue());
        });
    }

    /**
     * 验证活动时间的合法性
     *
     * @param startTime 活动开始时间
     * @param endTime 活动结束时间
     * @param registrationDeadline 报名截止时间（可选）
     */
    private void validateActivityTimes(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime registrationDeadline) {
        // 验证结束时间不能早于开始时间
        AssertUtils.isFalse(endTime.isBefore(startTime),
                           ResultCode.ACTIVITY_END_TIME_INVALID);

        // 验证报名截止时间（如果设置了）
        if (registrationDeadline != null) {
            AssertUtils.isFalse(registrationDeadline.isAfter(startTime),
                               ResultCode.ACTIVITY_REGISTRATION_DEADLINE_INVALID);
        }
    }

    @Override
    public ActivityStatisticsVO getActivityStatistics(Integer days) {
        // 验证天数参数
        AssertUtils.isTrue(days != null && (days == 7 || days == 30 || days == 90),
                          ResultCode.PARAM_ERROR);

        // 计算开始和结束日期
        LocalDateTime endDate = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        LocalDateTime startDate = endDate.minusDays(days - 1).withHour(0).withMinute(0).withSecond(0);

        // 获取每日统计数据
        List<DailyStatisticsVO> dailyStatistics = activityMapper.getDailyStatistics(startDate, endDate);

        // 计算总计
        int totalActivities = dailyStatistics.stream()
                .mapToInt(DailyStatisticsVO::getActivityCount)
                .sum();
        int totalParticipants = dailyStatistics.stream()
                .mapToInt(DailyStatisticsVO::getParticipantCount)
                .sum();

        return ActivityStatisticsVO.builder()
                .dailyStatistics(dailyStatistics)
                .days(days)
                .totalActivities(totalActivities)
                .totalParticipants(totalParticipants)
                .build();
    }
}
