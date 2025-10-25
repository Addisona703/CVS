package com.hngy.cvs.controller;

import com.hngy.cvs.common.result.Result;
import com.hngy.cvs.common.security.UserPrincipal;
import com.hngy.cvs.dto.response.AdminDashboardStatsVO;
import com.hngy.cvs.dto.response.ProductRankingVO;
import com.hngy.cvs.dto.response.ProductVO;
import com.hngy.cvs.dto.response.RedemptionStatisticsVO;
import com.hngy.cvs.dto.response.StudentDashboardStatsVO;
import com.hngy.cvs.dto.response.TeacherDashboardStatsVO;
import com.hngy.cvs.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 统计报表控制器
 *
 * @author CVS Team
 */
@RestController
@RequestMapping("/api/statistics")
@Tag(name = "统计报表", description = "统计相关接口")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    // ==================== 仪表盘统计接口 ====================

    @GetMapping("/admin-dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取管理员仪表板统计")
    public Result<AdminDashboardStatsVO> getAdminDashboardStats() {
        AdminDashboardStatsVO stats = statisticsService.getAdminDashboardStats();
        return Result.success("获取管理员仪表板统计成功", stats);
    }

    @GetMapping("/teacher-dashboard")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "获取教师仪表板统计")
    public Result<TeacherDashboardStatsVO> getTeacherDashboardStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        TeacherDashboardStatsVO stats = statisticsService.getTeacherDashboardStats(userPrincipal.getUserId());
        return Result.success("获取教师仪表板统计成功", stats);
    }

    @GetMapping("/student-dashboard")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "获取学生仪表板统计")
    public Result<StudentDashboardStatsVO> getStudentDashboardStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        StudentDashboardStatsVO stats = statisticsService.getStudentDashboardStats(userPrincipal.getUserId());
        return Result.success("获取学生仪表板统计成功", stats);
    }

    // ==================== 积分商城统计接口 ====================

    @GetMapping("/redemptions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取兑换统计数据")
    public Result<RedemptionStatisticsVO> getRedemptionStatistics(
            @Parameter(description = "开始日期") 
            @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") 
            @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        RedemptionStatisticsVO statistics = statisticsService.getRedemptionStatistics(startDate, endDate);
        return Result.success("获取兑换统计数据成功", statistics);
    }

    @GetMapping("/ranking")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取商品兑换排行")
    public Result<List<ProductRankingVO>> getProductRanking(
            @Parameter(description = "限制数量，默认10") 
            @RequestParam(defaultValue = "10") Integer limit) {
        
        List<ProductRankingVO> ranking = statisticsService.getProductRanking(limit);
        return Result.success("获取商品兑换排行成功", ranking);
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取库存预警列表")
    public Result<List<ProductVO>> getLowStockProducts() {
        List<ProductVO> lowStockProducts = statisticsService.getLowStockProducts();
        return Result.success("获取库存预警列表成功", lowStockProducts);
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "导出兑换记录Excel")
    public ResponseEntity<byte[]> exportRedemptions(
            @Parameter(description = "开始日期") 
            @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") 
            @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        byte[] excelData = statisticsService.exportRedemptions(startDate, endDate);
        
        String filename = "兑换记录_" + LocalDate.now() + ".xlsx";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }
}