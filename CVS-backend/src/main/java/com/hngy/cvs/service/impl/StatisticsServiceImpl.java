package com.hngy.cvs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.cvs.dto.response.AdminDashboardStatsVO;
import com.hngy.cvs.dto.response.StudentDashboardStatsVO;
import com.hngy.cvs.dto.response.TeacherDashboardStatsVO;
import com.hngy.cvs.entity.Signup;
import com.hngy.cvs.entity.Activity;
import com.hngy.cvs.entity.Certificate;
import com.hngy.cvs.entity.enums.CertificateStatus;
import com.hngy.cvs.entity.enums.SignupStatus;
import com.hngy.cvs.service.SignupService;
import com.hngy.cvs.service.CertificateService;
import com.hngy.cvs.service.RecordService;
import com.hngy.cvs.service.StatisticsService;
import com.hngy.cvs.service.UserService;
import com.hngy.cvs.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 统计服务实现类
 * 
 * @author CVS Team
 */
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final UserService userService;
    private final ActivityService activityService;
    private final SignupService signupService;
    private final RecordService recordService;
    private final CertificateService certificateService;

    @Override
    public AdminDashboardStatsVO getAdminDashboardStats() {
        return AdminDashboardStatsVO.builder()
                .totalUsers(userService.countTotal())
                .totalActivities(activityService.countActivities())
                .totalSignups(signupService.countSignups())
                .totalServiceRecords(recordService.countTotal())
                .systemUptime("运行中")
                .onlineUsers(0L) // 暂时设为0，需要实现在线用户统计
                .build();
    }

    @Override
    public TeacherDashboardStatsVO getTeacherDashboardStats(Long teacherId) {
        // 1. 统计老师创建的所有活动数
        Long myActivitiesCount = activityService.countActivitiesByOrganizer(teacherId);
        
        // 2. 统计老师所有活动的已通过报名总人数（状态为APPROVED）
        Long totalSignupsCount = countApprovedSignupsByOrganizer(teacherId);
        
        // 3. 统计老师活动的待审核报名人数（状态为PENDING）
        Long pendingApprovalsCount = countPendingSignupsByOrganizer(teacherId);
        
        // 4. 统计以当前老师为审批人的待审核志愿证明数（状态为PENDING）
        Long pendingCertificatesCount = countPendingCertificatesByApprover(teacherId);
        
        return TeacherDashboardStatsVO.builder()
                .myActivitiesCount(myActivitiesCount)
                .totalSignupsCount(totalSignupsCount)
                .pendingApprovalsCount(pendingApprovalsCount)
                .serviceRecordsCount(pendingCertificatesCount)
                .build();
    }

    @Override
    public StudentDashboardStatsVO getStudentDashboardStats(Long studentId) {
        return StudentDashboardStatsVO.builder()
                .mySignupsCount(signupService.countSignupsByUser(studentId))
                .totalServiceHours(recordService.getTotalServiceHoursByUser(studentId))
                .totalPoints(0L) // 需要实现积分统计
                .certificatesCount(0L) // 需要实现证明统计
                .build();
    }

    /**
     * 统计教师所有活动的已通过报名总人数
     */
    private Long countApprovedSignupsByOrganizer(Long organizerId) {
        return signupService.countApprovedSignupsByOrganizer(organizerId);
    }

    /**
     * 统计教师所有活动的待审核报名人数
     */
    private Long countPendingSignupsByOrganizer(Long organizerId) {
        return signupService.countPendingSignupsByOrganizer(organizerId);
    }

    /**
     * 统计以指定教师为审批人的待审核志愿证明数
     */
    private Long countPendingCertificatesByApprover(Long approverId) {
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Certificate::getApproverId, approverId)
               .eq(Certificate::getStatus, CertificateStatus.PENDING);

        return certificateService.countByCondition(wrapper);
    }
}
