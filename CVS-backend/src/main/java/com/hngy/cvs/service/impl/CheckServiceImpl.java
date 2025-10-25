package com.hngy.cvs.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.cvs.common.result.ResultCode;
import com.hngy.cvs.common.util.AssertUtils;
import com.hngy.cvs.dto.request.CheckInRequest;
import com.hngy.cvs.dto.request.CheckOutRequest;
import com.hngy.cvs.dto.request.ReviewSearchRequest;
import com.hngy.cvs.dto.request.SignupReviewRequest;
import com.hngy.cvs.dto.response.PendingSignStudentVO;
import com.hngy.cvs.dto.response.SignTokenResponse;
import com.hngy.cvs.dto.response.SignupReviewVO;
import com.hngy.cvs.entity.Activity;
import com.hngy.cvs.entity.Signup;
import com.hngy.cvs.entity.User;
import com.hngy.cvs.entity.enums.ActivityStatus;
import com.hngy.cvs.entity.enums.SignActionType;
import com.hngy.cvs.entity.enums.SignupStatus;
import com.hngy.cvs.mapper.ActivityMapper;
import com.hngy.cvs.mapper.SignupMapper;
import com.hngy.cvs.mapper.UserMapper;
import com.hngy.cvs.service.CheckService;
import com.hngy.cvs.service.CheckTokenService;
import com.hngy.cvs.service.NotificationService;
import com.hngy.cvs.service.PointsService;
import com.hngy.cvs.service.RecordService;
import com.hngy.cvs.service.model.CheckToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 签到/签退业务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckServiceImpl extends ServiceImpl<SignupMapper, Signup> implements CheckService {

    private static final long TOKEN_TTL_MINUTES = 5L;

    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;
    private final CheckTokenService checkTokenService;
    private final RecordService recordService;
    private final PointsService pointsService;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public SignTokenResponse createCheckInToken(Long activityId, Long teacherId) {
        Activity activity = validateActivityForToken(activityId, teacherId);
        CheckToken token = checkTokenService.createToken(activity.getId(), SignActionType.SIGN_IN, TOKEN_TTL_MINUTES);
        return toResponse(token);
    }

    @Override
    @Transactional(readOnly = true)
    public SignTokenResponse createCheckOutToken(Long activityId, Long teacherId) {
        Activity activity = validateActivityForToken(activityId, teacherId);
        CheckToken token = checkTokenService.createToken(activity.getId(), SignActionType.SIGN_OUT, TOKEN_TTL_MINUTES);
        return toResponse(token);
    }

    @Override
    @Transactional
    public void checkIn(Long studentId, CheckInRequest request) {
        CheckToken token = checkTokenService.consumeToken(request.getToken(), SignActionType.SIGN_IN);
        Signup signup = fetchApprovedSignup(token.getActivityId(), studentId);

        AssertUtils.isFalse(Boolean.TRUE.equals(signup.getSignedIn()), ResultCode.ALREADY_SIGNED_IN);

        signup.setSignedIn(true);
        signup.setSignInTime(LocalDateTime.now());
        this.updateById(signup);

        log.info("Student {} checked in to activity {} via token {}", studentId, token.getActivityId(), request.getToken());
    }

    @Override
    @Transactional
    public void checkOut(Long studentId, CheckOutRequest request) {
        CheckToken token = checkTokenService.consumeToken(request.getToken(), SignActionType.SIGN_OUT);
        Signup signup = fetchApprovedSignup(token.getActivityId(), studentId);

        AssertUtils.isTrue(Boolean.TRUE.equals(signup.getSignedIn()), ResultCode.NOT_SIGNED_IN);
        AssertUtils.isFalse(Boolean.TRUE.equals(signup.getSignedOut()), ResultCode.ALREADY_SIGNED_OUT);

        signup.setSignedOut(true);
        signup.setSignOutTime(LocalDateTime.now());

        if (request.getStudentRating() != null) {
            signup.setStudentRating(request.getStudentRating());
        }
        if (StrUtil.isNotBlank(request.getStudentEvaluation())) {
            signup.setStudentEvaluation(request.getStudentEvaluation().trim());
        }

        this.updateById(signup);

        // 发送签退审核通知给教师
        // 需求: 3.1 - WHEN 学生提交签退申请, THE CVS系统 SHALL 向活动发起人发送签退待审核通知
        try {
            Activity activity = activityMapper.selectById(token.getActivityId());
            if (activity != null) {
                String checkoutReason = request.getStudentEvaluation(); // 使用学生自评作为签退原因
                notificationService.sendCheckoutPendingNotification(
                        token.getActivityId(), studentId, activity.getOrganizerId(), checkoutReason);
            }
        } catch (Exception e) {
            log.error("发送签退审核通知失败: activityId={}, studentId={}", 
                    token.getActivityId(), studentId, e);
        }

        log.info("Student {} checked out from activity {} via token {}", studentId, token.getActivityId(), request.getToken());
    }

    @Override
    @Transactional
    public void review(Long signupId, SignupReviewRequest request, Long teacherId) {
        Signup signup = this.getById(signupId);
        AssertUtils.notNull(signup, ResultCode.SIGNUP_NOT_FOUND);

        Activity activity = activityMapper.selectById(signup.getActivityId());
        AssertUtils.notNull(activity, ResultCode.ACTIVITY_NOT_FOUND);
        AssertUtils.isTrue(activity.getOrganizerId().equals(teacherId), ResultCode.INSUFFICIENT_PERMISSIONS);
        AssertUtils.isTrue(Boolean.TRUE.equals(signup.getSignedIn()), ResultCode.NOT_SIGNED_IN);
        AssertUtils.isTrue(Boolean.TRUE.equals(signup.getSignedOut()), ResultCode.SIGNUP_NOT_SIGNED_OUT);

        Integer previousRating = signup.getTeacherRating();
        boolean firstFinalize = signup.getTeacherRatingConfirmedAt() == null;

        signup.setTeacherRating(request.getTeacherRating());
        signup.setTeacherEvaluation(request.getTeacherEvaluation());
        signup.setTeacherRatingConfirmedAt(LocalDateTime.now());

        this.updateById(signup);

        if (firstFinalize) {
            recordService.createServiceRecordFromSignup(
                    signup.getId(),
                    request.getTeacherRating(),
                    request.getTeacherEvaluation(),
                    signup.getStudentEvaluation()
            );
        }

        handlePointsAward(activity, signup, previousRating, request.getTeacherRating(), firstFinalize);

        // 发送签退审核结果通知给学生
        // 需求: 5.1, 5.2 - 签退审核结果通知
        if (firstFinalize) {
            try {
                // 如果教师评分大于等于3分，认为是通过，否则是拒绝
                boolean approved = request.getTeacherRating() >= 3;
                String reason = approved ? null : request.getTeacherEvaluation();
                notificationService.sendCheckoutResultNotification(
                        activity.getId(), signup.getUserId(), approved, reason);
            } catch (Exception e) {
                log.error("发送签退审核结果通知失败: signupId={}, studentId={}", 
                        signupId, signup.getUserId(), e);
            }
        }

        log.info("Teacher {} reviewed signup {} with rating {}", teacherId, signupId, request.getTeacherRating());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PendingSignStudentVO> listPendingCheckInStudents(Long activityId, Long teacherId) {
        validateActivityForToken(activityId, teacherId);
        return this.baseMapper.selectPendingCheckInStudents(activityId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PendingSignStudentVO> listPendingCheckOutStudents(Long activityId, Long teacherId) {
        validateActivityForToken(activityId, teacherId);
        return this.baseMapper.selectPendingCheckOutStudents(activityId);
    }

    private Activity validateActivityForToken(Long activityId, Long teacherId) {
        Activity activity = activityMapper.selectById(activityId);
        AssertUtils.notNull(activity, ResultCode.ACTIVITY_NOT_FOUND);
        AssertUtils.isTrue(activity.getOrganizerId().equals(teacherId), ResultCode.INSUFFICIENT_PERMISSIONS);
        AssertUtils.isTrue(
                activity.getStatus() == ActivityStatus.PUBLISHED || activity.getStatus() == ActivityStatus.ONGOING,
                ResultCode.BAD_REQUEST
        );
        return activity;
    }

    /**
     * 获取已批准的报名记录
     * 验证用户是否参加了该活动且报名已通过审核
     */
    private Signup fetchApprovedSignup(Long activityId, Long studentId) {
        Signup signup = this.getOne(new LambdaQueryWrapper<Signup>()
                .eq(Signup::getActivityId, activityId)
                .eq(Signup::getUserId, studentId));
        
        // 验证报名记录存在
        AssertUtils.notNull(signup, ResultCode.SIGNUP_NOT_FOUND);
        
        // 验证报名已通过审核，只有审核通过的用户才能签到签退
        AssertUtils.isTrue(signup.getStatus() == SignupStatus.APPROVED, ResultCode.SIGNUP_NOT_APPROVED);
        
        return signup;
    }

    private void handlePointsAward(Activity activity,
                                   Signup signup,
                                   Integer previousRating,
                                   Integer currentRating,
                                   boolean firstFinalize) {
        if (activity.getPoints() == null || activity.getPoints() <= 0 || currentRating == null) {
            return;
        }

        int newPoints = computeAwardPoints(activity.getPoints(), currentRating);
        if (firstFinalize) {
            if (newPoints > 0) {
                pointsService.awardPoints(signup.getUserId(), newPoints);
            }
            return;
        }

        if (previousRating != null && currentRating > previousRating) {
            int previousPoints = computeAwardPoints(activity.getPoints(), previousRating);
            int delta = newPoints - previousPoints;
            if (delta > 0) {
                pointsService.awardPoints(signup.getUserId(), delta);
            }
        }
    }

    private int computeAwardPoints(Integer basePoints, Integer rating) {
        if (basePoints == null || basePoints <= 0 || rating == null) {
            return 0;
        }
        return (int) Math.round(basePoints * (rating / 5.0));
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<SignupReviewVO> searchReviews(ReviewSearchRequest request, Long teacherId, int pageNum, int pageSize) {
        // 构建查询条件
        LambdaQueryWrapper<Signup> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询已签退的记录
        wrapper.eq(Signup::getSignedOut, true);
        wrapper.eq(Signup::getStatus, SignupStatus.APPROVED);
        
        // 根据审核状态筛选
        if (StrUtil.isNotBlank(request.getReviewStatus())) {
            if ("PENDING".equals(request.getReviewStatus())) {
                // 待审核：教师评分为空或教师确认时间为空
                wrapper.and(w -> w.isNull(Signup::getTeacherRating)
                        .or()
                        .isNull(Signup::getTeacherRatingConfirmedAt));
            } else if ("REVIEWED".equals(request.getReviewStatus())) {
                // 已审核：教师评分和确认时间都不为空
                wrapper.isNotNull(Signup::getTeacherRating);
                wrapper.isNotNull(Signup::getTeacherRatingConfirmedAt);
            }
        }
        
        // 按活动ID筛选
        if (request.getActivityId() != null) {
            wrapper.eq(Signup::getActivityId, request.getActivityId());
        }
        
        // 只查询该教师组织的活动
        List<Long> teacherActivityIds = activityMapper.selectList(
                new LambdaQueryWrapper<Activity>()
                        .eq(Activity::getOrganizerId, teacherId)
                        .select(Activity::getId)
        ).stream().map(Activity::getId).collect(Collectors.toList());
        
        if (teacherActivityIds.isEmpty()) {
            return new Page<>(pageNum, pageSize);
        }
        
        wrapper.in(Signup::getActivityId, teacherActivityIds);
        
        // 按签退时间倒序
        wrapper.orderByDesc(Signup::getSignOutTime);
        
        // 分页查询
        Page<Signup> page = new Page<>(pageNum, pageSize);
        IPage<Signup> signupPage = this.page(page, wrapper);
        
        // 转换为VO
        List<SignupReviewVO> voList = signupPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 如果有关键词，在内存中过滤
        if (StrUtil.isNotBlank(request.getKeyword())) {
            String keyword = request.getKeyword().trim().toLowerCase();
            voList = voList.stream()
                    .filter(vo -> (vo.getStudentName() != null && vo.getStudentName().toLowerCase().contains(keyword))
                            || (vo.getStudentNo() != null && vo.getStudentNo().toLowerCase().contains(keyword)))
                    .collect(Collectors.toList());
        }
        
        Page<SignupReviewVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setRecords(voList);
        voPage.setTotal(signupPage.getTotal());
        voPage.setSize(signupPage.getSize());
        voPage.setCurrent(signupPage.getCurrent());
        voPage.setPages(signupPage.getPages());
        
        return voPage;
    }
    
    private SignupReviewVO convertToVO(Signup signup) {
        SignupReviewVO vo = new SignupReviewVO();
        vo.setSignupId(signup.getId());
        vo.setActivityId(signup.getActivityId());
        vo.setStudentId(signup.getUserId());
        vo.setCheckInTime(signup.getSignInTime());
        vo.setCheckOutTime(signup.getSignOutTime());
        vo.setStudentRating(signup.getStudentRating());
        vo.setStudentEvaluation(signup.getStudentEvaluation());
        vo.setTeacherRating(signup.getTeacherRating());
        vo.setTeacherEvaluation(signup.getTeacherEvaluation());
        vo.setTeacherRatingConfirmedAt(signup.getTeacherRatingConfirmedAt());
        
        // 设置审核状态
        if (signup.getTeacherRating() != null && signup.getTeacherRatingConfirmedAt() != null) {
            vo.setReviewStatus("REVIEWED");
        } else {
            vo.setReviewStatus("PENDING");
        }
        
        // 查询活动信息
        Activity activity = activityMapper.selectById(signup.getActivityId());
        if (activity != null) {
            vo.setActivityTitle(activity.getTitle());
        }
        
        // 查询学生信息
        User student = userMapper.selectById(signup.getUserId());
        if (student != null) {
            vo.setStudentName(student.getName());
            vo.setStudentNo(student.getUsername()); // username就是学号
        }
        
        return vo;
    }

    private SignTokenResponse toResponse(CheckToken token) {
        return SignTokenResponse.builder()
                .token(token.getToken())
                .expiresAt(token.getExpiresAt())
                .action(token.getType())
                .build();
    }
}
