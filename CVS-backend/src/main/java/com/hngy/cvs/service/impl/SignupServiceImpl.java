package com.hngy.cvs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.cvs.common.result.ResultCode;
import com.hngy.cvs.common.util.AssertUtils;
import com.hngy.cvs.common.util.BeanUtil;
import com.hngy.cvs.common.util.PageUtil;
import com.hngy.cvs.dto.request.PageDTO;
import com.hngy.cvs.dto.request.SignupCreateDTO;
import com.hngy.cvs.dto.request.SignupSearchDTO;
import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.dto.response.SignupVO;
import com.hngy.cvs.entity.Activity;
import com.hngy.cvs.entity.Signup;
import com.hngy.cvs.entity.User;
import com.hngy.cvs.entity.enums.ActivityStatus;
import com.hngy.cvs.entity.enums.SignupStatus;
import com.hngy.cvs.mapper.ActivityMapper;
import com.hngy.cvs.mapper.SignupMapper;
import com.hngy.cvs.mapper.UserMapper;
import com.hngy.cvs.service.NotificationService;
import com.hngy.cvs.service.SignupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报名服务实现类
 * 
 * @author CVS Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignupServiceImpl
        extends ServiceImpl<SignupMapper, Signup>
        implements SignupService {

    private final SignupMapper signupMapper;
    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public SignupVO signupActivity(SignupCreateDTO request, Long userId) {
        // 检查活动是否存在
        Activity activity = activityMapper.selectById(request.getActivityId());
        AssertUtils.notNull(activity, ResultCode.ACTIVITY_NOT_FOUND);

        // 检查活动状态
        AssertUtils.isTrue(activity.getStatus() == ActivityStatus.PUBLISHED,
                          ResultCode.ACTIVITY_NOT_PUBLISHED);

        // 检查报名截止时间
        if (activity.getRegistrationDeadline() != null) {
            AssertUtils.isFalse(activity.getRegistrationDeadline().isBefore(LocalDateTime.now()),
                               ResultCode.BAD_REQUEST);
        }

        // 检查活动是否已过期
        AssertUtils.isFalse(activity.getStartTime().isBefore(LocalDateTime.now()),
                           ResultCode.ACTIVITY_EXPIRED);

        // 检查是否已经报名
        long count = this.count(
                new LambdaQueryWrapper<Signup>()
                        .eq(Signup::getUserId, userId)
                        .eq(Signup::getActivityId, activity.getId())
        );
        AssertUtils.isFalse(count > 0, ResultCode.ALREADY_SIGNED_UP);


        // 创建报名记录
        Signup signup = BeanUtil.to(request, Signup.class);
        signup.setUserId(userId);
        signup.setStatus(SignupStatus.PENDING);
        signup.setSignedIn(false);
        signup.setSignedOut(false);

        this.save(signup);
        log.info("用户 {} 报名活动 {} 成功", userId, request.getActivityId());

        // 发送报名审核通知给教师
        // 需求: 2.1 - WHEN 学生提交活动报名申请, THE CVS系统 SHALL 向活动发起人发送待审核通知
        try {
            notificationService.sendRegistrationPendingNotification(
                    activity.getId(), userId, activity.getOrganizerId());
        } catch (Exception e) {
            log.error("发送报名审核通知失败: activityId={}, studentId={}, teacherId={}", 
                    activity.getId(), userId, activity.getOrganizerId(), e);
        }

        // 转换为VO返回
        return BeanUtil.to(signup, SignupVO.class);
    }

    @Override
    public SignupVO getSignupById(Long id) {
        Signup signup = this.getById(id);
        AssertUtils.notNull(signup, ResultCode.SIGNUP_NOT_FOUND);
        
        return BeanUtil.to(signup, SignupVO.class);
    }

    @Override
    public PageVO<SignupVO> getSignupList(PageDTO<SignupSearchDTO> pageRequest, Long teacherId) {
        // 从 PageDTO 中提取查询条件，确保不为 null
        SignupSearchDTO searchDTO = pageRequest.getParams();
        if (searchDTO == null) {
            searchDTO = new SignupSearchDTO();
        }
        
        // 首先获取该老师发布的所有活动ID
        LambdaQueryWrapper<Activity> teacherActivityWrapper = new LambdaQueryWrapper<Activity>()
                .eq(Activity::getOrganizerId, teacherId)
                .select(Activity::getId);
        List<Long> teacherActivityIds = activityMapper.selectList(teacherActivityWrapper)
                .stream().map(Activity::getId).collect(Collectors.toList());
        
        if (teacherActivityIds.isEmpty()) {
            // 如果该老师没有发布任何活动，返回空结果
            return PageUtil.empty(pageRequest.getPageNum(), pageRequest.getPageSize());
        }
        
        // 使用 MyBatis-Plus 的 LambdaQueryWrapper 进行查询，符合项目规范
        Page<Signup> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        
        LambdaQueryWrapper<Signup> wrapper = new LambdaQueryWrapper<Signup>()
                .in(Signup::getActivityId, teacherActivityIds)  // 只查询该老师的活动
                .eq(searchDTO.getActivityId() != null, Signup::getActivityId, searchDTO.getActivityId())
                .eq(searchDTO.getUserId() != null, Signup::getUserId, searchDTO.getUserId())
                .eq(searchDTO.getStatus() != null, Signup::getStatus, searchDTO.getStatus())
                .orderByDesc(Signup::getCreatedAt);
        
        // 如果需要按活动名称模糊查询，先查询对应的ID（在该老师的活动范围内）
        if (searchDTO.getActivityTitle() != null && !searchDTO.getActivityTitle().trim().isEmpty()) {
            LambdaQueryWrapper<Activity> activityWrapper = new LambdaQueryWrapper<Activity>()
                    .like(Activity::getTitle, searchDTO.getActivityTitle())
                    .in(Activity::getId, teacherActivityIds);  // 只在该老师的活动中查找
            List<Activity> activities = activityMapper.selectList(activityWrapper);
            List<Long> activityIds = activities.stream().map(Activity::getId).collect(Collectors.toList());
            if (activityIds.isEmpty()) {
                // 如果没有匹配的活动，返回空结果
                return PageUtil.empty(pageRequest.getPageNum(), pageRequest.getPageSize());
            }
            wrapper.and(w -> w.in(Signup::getActivityId, activityIds));
        }
        
        if (searchDTO.getUserName() != null && !searchDTO.getUserName().trim().isEmpty()) {
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<User>()
                    .like(User::getName, searchDTO.getUserName());
            List<User> users = userMapper.selectList(userWrapper);
            List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            if (userIds.isEmpty()) {
                // 如果没有匹配的用户，返回空结果
                return PageUtil.empty(pageRequest.getPageNum(), pageRequest.getPageSize());
            }
            wrapper.in(Signup::getUserId, userIds);
        }
        
        IPage<Signup> result = this.page(page, wrapper);
        PageVO<SignupVO> pageVO = PageUtil.convert(result, SignupVO.class);
        
        // 补充关联信息
        enrichSignupVOList(pageVO.getRecords());
        return pageVO;
    }

    @Override
    public PageVO<SignupVO> getMySignups(PageDTO<SignupSearchDTO> pageRequest, Long userId) {
        SignupSearchDTO searchDTO = pageRequest.getParams();
        if (searchDTO == null) {
            searchDTO = new SignupSearchDTO();
        }
        searchDTO.setUserId(userId);
        
        // 使用MyBatis-Plus进行分页查询
        Page<Signup> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        LambdaQueryWrapper<Signup> wrapper = new LambdaQueryWrapper<Signup>()
                .eq(Signup::getUserId, userId)
                .eq(searchDTO.getActivityId() != null, Signup::getActivityId, searchDTO.getActivityId())
                .eq(searchDTO.getStatus() != null, Signup::getStatus, searchDTO.getStatus())
                .orderByDesc(Signup::getCreatedAt);
        
        IPage<Signup> result = this.page(page, wrapper);
        PageVO<SignupVO> pageVO = PageUtil.convert(result, SignupVO.class);
        
        // 补充关联信息
        enrichSignupVOList(pageVO.getRecords());
        return pageVO;
    }

    @Override
    @Transactional
    public void approveSignup(Long signupId, Long operatorId) {
        Signup signup = this.getById(signupId);
        AssertUtils.notNull(signup, ResultCode.SIGNUP_NOT_FOUND);
        
        // 检查权限：验证操作者是否为活动创建者
        Activity activity = activityMapper.selectById(signup.getActivityId());
        AssertUtils.notNull(activity, ResultCode.ACTIVITY_NOT_FOUND);
        AssertUtils.isTrue(activity.getOrganizerId().equals(operatorId), ResultCode.INSUFFICIENT_PERMISSIONS);
        
        // 检查报名状态
        AssertUtils.isTrue(signup.getStatus() == SignupStatus.PENDING, ResultCode.SIGNUP_STATUS_INVALID);
        
        // 更新状态
        signup.setStatus(SignupStatus.APPROVED);
        this.updateById(signup);
        
        // 发送报名审核通过通知给学生
        // 需求: 4.1 - WHEN 活动发起人审核通过报名申请, THE CVS系统 SHALL 向报名学生发送审核通过通知
        try {
            notificationService.sendRegistrationResultNotification(
                    signup.getActivityId(), signup.getUserId(), true, null);
        } catch (Exception e) {
            log.error("发送报名审核通过通知失败: signupId={}, studentId={}", 
                    signupId, signup.getUserId(), e);
        }
        
        log.info("报名审核通过：signupId={}, operatorId={}", signupId, operatorId);
    }

    @Override
    @Transactional
    public void rejectSignup(Long signupId, String rejectReason, Long operatorId) {
        Signup signup = this.getById(signupId);
        AssertUtils.notNull(signup, ResultCode.SIGNUP_NOT_FOUND);

        // 检查权限：验证操作者是否为活动创建者
        Activity activity = activityMapper.selectById(signup.getActivityId());
        AssertUtils.notNull(activity, ResultCode.ACTIVITY_NOT_FOUND);
        AssertUtils.isTrue(activity.getOrganizerId().equals(operatorId), ResultCode.INSUFFICIENT_PERMISSIONS);

        // 检查报名状态
        AssertUtils.isTrue(signup.getStatus() == SignupStatus.PENDING, ResultCode.SIGNUP_STATUS_INVALID);

        // 更新状态和拒绝原因
        signup.setStatus(SignupStatus.REJECTED);
        signup.setRejectReason(rejectReason);
        this.updateById(signup);

        // 发送报名审核拒绝通知给学生
        // 需求: 4.2 - WHEN 活动发起人拒绝报名申请, THE CVS系统 SHALL 向报名学生发送审核拒绝通知
        try {
            notificationService.sendRegistrationResultNotification(
                    signup.getActivityId(), signup.getUserId(), false, rejectReason);
        } catch (Exception e) {
            log.error("发送报名审核拒绝通知失败: signupId={}, studentId={}", 
                    signupId, signup.getUserId(), e);
        }

        log.info("报名审核拒绝：signupId={}, operatorId={}, reason={}", signupId, operatorId, rejectReason);
    }

    @Override
    @Transactional
    public void batchApproveSignups(List<Long> signupIds, Long operatorId) {
        updateSignupStatus(signupIds, operatorId, SignupStatus.APPROVED, null);
    }

    @Override
    @Transactional
    public void batchRejectSignups(List<Long> signupIds, String rejectReason, Long operatorId) {
        updateSignupStatus(signupIds, operatorId, SignupStatus.REJECTED, rejectReason);
    }

    private void updateSignupStatus(List<Long> signupIds, Long operatorId, SignupStatus newStatus, String rejectReason) {
        List<Signup> signups = this.listByIds(signupIds);
        AssertUtils.notEmpty(signups, ResultCode.SIGNUP_NOT_FOUND);

        List<Long> activityIds = signups.stream().map(Signup::getActivityId).distinct().toList();
        List<Activity> activities = activityMapper.selectBatchIds(activityIds);
        AssertUtils.notEmpty(activities, ResultCode.ACTIVITY_NOT_FOUND);

        activities.forEach(activity ->
                AssertUtils.isTrue(activity.getOrganizerId().equals(operatorId),
                        ResultCode.INSUFFICIENT_PERMISSIONS)
        );

        List<Signup> updatedSignups = signups.stream()
                .filter(s -> s.getStatus() == SignupStatus.PENDING)
                .peek(s -> {
                    s.setStatus(newStatus);
                    if (newStatus == SignupStatus.REJECTED) {
                        s.setRejectReason(rejectReason);
                    }
                })
                .collect(Collectors.toList());

        this.updateBatchById(signups);

        // 批量发送通知
        // 需求: 4.1, 4.2 - 批量审核结果通知
        for (Signup signup : updatedSignups) {
            try {
                boolean approved = newStatus == SignupStatus.APPROVED;
                notificationService.sendRegistrationResultNotification(
                        signup.getActivityId(), signup.getUserId(), approved, rejectReason);
            } catch (Exception e) {
                log.error("发送批量审核通知失败: signupId={}, studentId={}, approved={}", 
                        signup.getId(), signup.getUserId(), newStatus == SignupStatus.APPROVED, e);
            }
        }
    }



    /**
     * 补充报名VO列表的关联信息
     */
    private void enrichSignupVOList(List<SignupVO> signupVOs) {
        if (signupVOs == null || signupVOs.isEmpty()) {
            return;
        }
        
        // 获取活动信息
        List<Long> activityIds = signupVOs.stream().map(SignupVO::getActivityId).distinct().collect(Collectors.toList());
        List<Activity> activities = activityMapper.selectBatchIds(activityIds);
        Map<Long, Activity> activityMap = activities.stream().collect(Collectors.toMap(Activity::getId, a -> a));
        
        // 获取用户信息
        List<Long> userIds = signupVOs.stream().map(SignupVO::getUserId).distinct().collect(Collectors.toList());
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        
        // 补充信息
        signupVOs.forEach(vo -> {
            Activity activity = activityMap.get(vo.getActivityId());
            if (activity != null) {
                vo.setActivityTitle(activity.getTitle());
                vo.setLocation(activity.getLocation());
                vo.setStartTime(activity.getStartTime());
                vo.setEndTime(activity.getEndTime());
                vo.setPoints(activity.getPoints());
            }
            
            User user = userMap.get(vo.getUserId());
            if (user != null) {
                vo.setName(user.getName());
                vo.setUsername(user.getUsername());
                vo.setPhone(user.getPhone());
                vo.setEmail(user.getEmail());
            }
        });
    }

    @Override
    @Transactional
    public void cancelSignup(Long signupId, Long userId) {
        // 获取报名记录
        Signup signup = this.getById(signupId);
        AssertUtils.notNull(signup, ResultCode.SIGNUP_NOT_FOUND);
        
        // 验证报名是否属于当前用户
        AssertUtils.isTrue(signup.getUserId().equals(userId), ResultCode.INSUFFICIENT_PERMISSIONS);
        
        // 检查报名状态，只有待审核的报名才能取消，已通过的报名不允许取消
        AssertUtils.isTrue(signup.getStatus() == SignupStatus.PENDING, ResultCode.CANNOT_CANCEL_SIGNUP);
        
        // 检查活动是否已开始
        Activity activity = activityMapper.selectById(signup.getActivityId());
        if (activity != null) {
            AssertUtils.isFalse(activity.getStartTime().isBefore(LocalDateTime.now()),
                               ResultCode.ACTIVITY_ALREADY_STARTED);
        }
        
        // 删除报名记录（物理删除）
        this.removeById(signupId);
        
        log.info("用户 {} 取消报名 {} 成功", userId, signupId);
    }

    @Override
    public PageVO<SignupVO> getCheckedInSignups(PageDTO<SignupSearchDTO> pageRequest, Long teacherId) {
        // 获取教师发布的所有活动ID
        List<Long> teacherActivityIds = activityMapper.selectList(
                new LambdaQueryWrapper<Activity>()
                        .eq(Activity::getOrganizerId, teacherId)
                        .select(Activity::getId)
        ).stream().map(Activity::getId).collect(Collectors.toList());
        
        if (teacherActivityIds.isEmpty()) {
            return PageUtil.empty(pageRequest.getPageNum(), pageRequest.getPageSize());
        }
        
        SignupSearchDTO searchDTO = pageRequest.getParams();
        if (searchDTO == null) {
            searchDTO = new SignupSearchDTO();
        }
        
        Page<Signup> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        LambdaQueryWrapper<Signup> wrapper = new LambdaQueryWrapper<>();
        
        // 筛选教师的活动
        wrapper.in(Signup::getActivityId, teacherActivityIds);
        
        // 筛选已签到但未签退的人员
        wrapper.eq(Signup::getSignedIn, true)
               .eq(Signup::getSignedOut, false)
               .eq(Signup::getStatus, SignupStatus.APPROVED);
        
        // 按活动名称搜索
        if (searchDTO.getActivityTitle() != null && !searchDTO.getActivityTitle().trim().isEmpty()) {
            List<Long> activityIds = activityMapper.selectList(
                    new LambdaQueryWrapper<Activity>()
                            .like(Activity::getTitle, searchDTO.getActivityTitle())
                            .in(Activity::getId, teacherActivityIds)
                            .select(Activity::getId)
            ).stream().map(Activity::getId).collect(Collectors.toList());
            
            if (activityIds.isEmpty()) {
                return PageUtil.empty(pageRequest.getPageNum(), pageRequest.getPageSize());
            }
            wrapper.in(Signup::getActivityId, activityIds);
        }
        
        // 按学生姓名搜索
        if (searchDTO.getUserName() != null && !searchDTO.getUserName().trim().isEmpty()) {
            List<Long> userIds = userMapper.selectList(
                    new LambdaQueryWrapper<User>()
                            .like(User::getName, searchDTO.getUserName())
                            .select(User::getId)
            ).stream().map(User::getId).collect(Collectors.toList());
            
            if (userIds.isEmpty()) {
                return PageUtil.empty(pageRequest.getPageNum(), pageRequest.getPageSize());
            }
            wrapper.in(Signup::getUserId, userIds);
        }
        
        // 按签到时间倒序排列（最新签到的在前）
        wrapper.orderByDesc(Signup::getSignInTime);
        
        IPage<Signup> result = this.page(page, wrapper);
        PageVO<SignupVO> pageVO = PageUtil.convert(result, SignupVO.class);
        
        // 补充关联信息
        enrichSignupVOList(pageVO.getRecords());
        return pageVO;
    }

    @Override
    public Long countSignups() {
        return this.count();
    }

    @Override
    public Long countSignupsByUser(Long userId) {
        LambdaQueryWrapper<Signup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Signup::getUserId, userId);
        return this.count(wrapper);
    }

    @Override
    public Long countApprovedSignupsByOrganizer(Long organizerId) {
        // 首先查询教师创建的所有活动ID
        LambdaQueryWrapper<Activity> activityWrapper = new LambdaQueryWrapper<>();
        activityWrapper.eq(Activity::getOrganizerId, organizerId)
                .eq(Activity::getDeleted, 0)
                .select(Activity::getId);
        List<Activity> activities = activityMapper.selectList(activityWrapper);
        
        if (activities.isEmpty()) {
            return 0L;
        }
        
        List<Long> activityIds = activities.stream().map(Activity::getId).collect(Collectors.toList());
        
        // 统计这些活动的已通过报名数
        LambdaQueryWrapper<Signup> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Signup::getActivityId, activityIds)
                .eq(Signup::getStatus, SignupStatus.APPROVED);
        return this.count(wrapper);
    }

    @Override
    public Long countPendingSignupsByOrganizer(Long organizerId) {
        // 首先查询教师创建的所有活动ID
        LambdaQueryWrapper<Activity> activityWrapper = new LambdaQueryWrapper<>();
        activityWrapper.eq(Activity::getOrganizerId, organizerId)
                .eq(Activity::getDeleted, 0)
                .select(Activity::getId);
        List<Activity> activities = activityMapper.selectList(activityWrapper);
        
        if (activities.isEmpty()) {
            return 0L;
        }
        
        List<Long> activityIds = activities.stream().map(Activity::getId).collect(Collectors.toList());
        
        // 统计这些活动的待审核报名数
        LambdaQueryWrapper<Signup> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Signup::getActivityId, activityIds)
                .eq(Signup::getStatus, SignupStatus.PENDING);
        return this.count(wrapper);
    }

    @Override
    public PageVO<SignupVO> getActivitySignups(Long activityId, PageDTO<SignupSearchDTO> pageRequest) {
        AssertUtils.notNull(activityId, ResultCode.BAD_REQUEST);
        AssertUtils.notNull(pageRequest, ResultCode.BAD_REQUEST);

        // 验证活动是否存在
        Activity activity = activityMapper.selectById(activityId);
        AssertUtils.notNull(activity, ResultCode.ACTIVITY_NOT_FOUND);

        // 构建分页对象
        Page<Signup> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        
        // 构建查询条件
        SignupSearchDTO params = pageRequest.getParams();
        
        // 调用 Mapper 的分页查询方法
        IPage<SignupVO> result = signupMapper.selectSignupPage(
                page,
                activityId,
                params != null ? params.getStatus() : null,
                params != null ? params.getUserName() : null,
                params != null ? params.getSignedIn() : null,
                params != null ? params.getSignedOut() : null
        );

        log.info("查询活动报名列表: activityId={}, pageNum={}, pageSize={}, total={}", 
                activityId, pageRequest.getPageNum(), pageRequest.getPageSize(), result.getTotal());

        return PageUtil.convert(result);
    }
}
