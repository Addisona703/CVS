package com.hngy.cvs.service.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.cvs.entity.Activity;
import com.hngy.cvs.entity.Signup;
import com.hngy.cvs.entity.enums.ActivityStatus;
import com.hngy.cvs.entity.enums.SignupStatus;
import com.hngy.cvs.mapper.ActivityMapper;
import com.hngy.cvs.mapper.SignupMapper;
import com.hngy.cvs.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知系统定时任务调度器
 * 负责检查活动状态变化并发送相应通知
 * 
 * 需求: 1.1, 1.2 - 活动开始和结束时间通知
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationTask {

    private final ActivityMapper activityMapper;
    private final NotificationService notificationService;
    private final SignupMapper signupMapper;

    private static final String AUTO_REJECT_REASON = "超时未审核";

    /**
     * 检查活动开始时间并发送通知（每分钟执行一次）
     * 需求: 1.1 - WHEN 活动开始时间到达, THE CVS系统 SHALL 向活动发起人发送活动开始通知
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkActivityStart() {
        try {
            LocalDateTime now = LocalDateTime.now();
            // 检查当前时间前后1分钟内开始的活动
            LocalDateTime startTime = now.minusMinutes(1);
            LocalDateTime endTime = now.plusMinutes(1);

            log.debug("检查活动开始时间: {} 到 {}", startTime, endTime);

            // 查询在指定时间范围内开始的已发布活动
            List<Activity> activities = activityMapper.selectList(
                    new LambdaQueryWrapper<Activity>()
                            .eq(Activity::getStatus, ActivityStatus.PUBLISHED)
                            .between(Activity::getStartTime, startTime, endTime)
            );

            int successCount = 0;
            int failureCount = 0;

            for (Activity activity : activities) {
                try {
                    notificationService.sendActivityStartNotification(activity.getId());

                    if (activity.getStatus() == ActivityStatus.PUBLISHED) {
                        activity.setStatus(ActivityStatus.ONGOING);
                        activityMapper.updateById(activity);
                        log.info("更新活动状态为进行中: activityId={}, title={}", activity.getId(), activity.getTitle());
                    }

                    notificationService.sendActivityOngoingNotification(activity.getId());
                    successCount++;
                    log.info("成功发送活动开始通知: activityId={}, title={}, startTime={}", 
                            activity.getId(), activity.getTitle(), activity.getStartTime());
                } catch (Exception e) {
                    failureCount++;
                    log.error("发送活动开始通知失败: activityId={}, title={}, error={}", 
                            activity.getId(), activity.getTitle(), e.getMessage(), e);
                }
            }

            if (!activities.isEmpty()) {
                log.info("活动开始/进行中通知检查完成: 总数={}, 成功={}, 失败={}", 
                        activities.size(), successCount, failureCount);
            }

        } catch (Exception e) {
            log.error("检查活动开始时间任务执行异常", e);
        }
    }

    /**
     * 自动拒绝已过报名截止时间仍未审核的报名（每分钟执行一次）
     */
    @Scheduled(cron = "0 * * * * ?")
    public void rejectExpiredPendingSignups() {
        try {
            LocalDateTime now = LocalDateTime.now();

            List<Activity> expiredActivities = activityMapper.selectList(
                    new LambdaQueryWrapper<Activity>()
                            .isNotNull(Activity::getRegistrationDeadline)
                            .le(Activity::getRegistrationDeadline, now)
            );

            if (expiredActivities.isEmpty()) {
                return;
            }

            int totalRejected = 0;

            for (Activity activity : expiredActivities) {
                List<Signup> pendingSignups = signupMapper.selectList(
                        new LambdaQueryWrapper<Signup>()
                                .eq(Signup::getActivityId, activity.getId())
                                .eq(Signup::getStatus, SignupStatus.PENDING)
                );

                if (pendingSignups.isEmpty()) {
                    continue;
                }

                for (Signup signup : pendingSignups) {
                    try {
                        signup.setStatus(SignupStatus.REJECTED);
                        signup.setRejectReason(AUTO_REJECT_REASON);
                        signupMapper.updateById(signup);

                        notificationService.sendRegistrationResultNotification(
                                activity.getId(), signup.getUserId(), false, AUTO_REJECT_REASON
                        );
                        totalRejected++;
                    } catch (Exception e) {
                        log.error("自动拒绝报名失败: signupId={}, activityId={}, error={}", signup.getId(), activity.getId(), e.getMessage(), e);
                    }
                }
            }

            if (totalRejected > 0) {
                log.info("自动拒绝超时报名任务完成: 共拒绝 {} 条记录", totalRejected);
            }
        } catch (Exception e) {
            log.error("自动拒绝超时报名任务执行异常", e);
        }
    }

    /**
     * 检查活动结束时间并发送通知（每分钟执行一次）
     * 需求: 1.2 - WHEN 活动结束时间到达, THE CVS系统 SHALL 向活动发起人发送活动结束通知
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkActivityEnd() {
        try {
            LocalDateTime now = LocalDateTime.now();
            // 检查当前时间前后1分钟内结束的活动
            LocalDateTime startTime = now.minusMinutes(1);
            LocalDateTime endTime = now.plusMinutes(1);

            log.debug("检查活动结束时间: {} 到 {}", startTime, endTime);

            // 查询在指定时间范围内结束的活动（已发布或进行中状态）
            List<Activity> activities = activityMapper.selectList(
                    new LambdaQueryWrapper<Activity>()
                            .in(Activity::getStatus, ActivityStatus.PUBLISHED, ActivityStatus.ONGOING)
                            .between(Activity::getEndTime, startTime, endTime)
            );

            int successCount = 0;
            int failureCount = 0;

            for (Activity activity : activities) {
                try {
                    // 发送活动结束通知
                    notificationService.sendActivityEndNotification(activity.getId());
                    
                    // 更新活动状态为已完成
                    activity.setStatus(ActivityStatus.COMPLETED);
                    activityMapper.updateById(activity);
                    
                    successCount++;
                    log.info("成功发送活动结束通知并更新状态: activityId={}, title={}, endTime={}", 
                            activity.getId(), activity.getTitle(), activity.getEndTime());
                } catch (Exception e) {
                    failureCount++;
                    log.error("发送活动结束通知失败: activityId={}, title={}, error={}", 
                            activity.getId(), activity.getTitle(), e.getMessage(), e);
                }
            }

            if (activities.size() > 0) {
                log.info("活动结束通知检查完成: 总数={}, 成功={}, 失败={}", 
                        activities.size(), successCount, failureCount);
            }

        } catch (Exception e) {
            log.error("检查活动结束时间任务执行异常", e);
        }
    }
}
