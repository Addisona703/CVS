package com.hngy.cvs.controller;

import com.hngy.cvs.common.result.Result;
import com.hngy.cvs.common.security.UserPrincipal;
import com.hngy.cvs.dto.response.AdminDashboardStatsVO;
import com.hngy.cvs.dto.response.StudentDashboardStatsVO;
import com.hngy.cvs.dto.response.TeacherDashboardStatsVO;
import com.hngy.cvs.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计分析控制器
 * 
 * @author CVS Team
 */
@RestController
@RequestMapping("/api/statistics")
@Tag(name = "统计分析", description = "角色特定的仪表板统计接口")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/admin-dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取管理员仪表板统计数据")
    public Result<AdminDashboardStatsVO> getAdminDashboardStats() {
        AdminDashboardStatsVO stats = statisticsService.getAdminDashboardStats();
        return Result.success(stats);
    }

//    @GetMapping("/teacher-dashboard")
//    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
//    @Operation(summary = "获取教师仪表板统计数据")
//    public Result<TeacherDashboardStatsVO> getTeacherDashboardStats(
//            @AuthenticationPrincipal UserPrincipal principal) {
//
//        Long teacherId = principal.getUserId();
//        TeacherDashboardStatsVO stats = statisticsService.getTeacherDashboardStats(teacherId);
//
//        return Result.success(stats);
//    }

//    @GetMapping("/student-dashboard")
//    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
//    @Operation(summary = "获取学生仪表板统计数据")
//    public Result<StudentDashboardStatsVO> getStudentDashboardStats(
//            @AuthenticationPrincipal UserPrincipal principal) {
//
//        Long studentId = principal.getUserId();
//        StudentDashboardStatsVO stats = statisticsService.getStudentDashboardStats(studentId);
//
//        return Result.success(stats);
//    }
}
