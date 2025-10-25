package com.hngy.cvs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.cvs.dto.response.AdminDashboardStatsVO;
import com.hngy.cvs.dto.response.ProductRankingVO;
import com.hngy.cvs.dto.response.ProductVO;
import com.hngy.cvs.dto.response.RedemptionStatisticsVO;
import com.hngy.cvs.dto.response.RedemptionVO;
import com.hngy.cvs.dto.response.StudentDashboardStatsVO;
import com.hngy.cvs.dto.response.TeacherDashboardStatsVO;
import com.hngy.cvs.entity.Signup;
import com.hngy.cvs.entity.Activity;
import com.hngy.cvs.entity.Certificate;
import com.hngy.cvs.entity.enums.CertificateStatus;
import com.hngy.cvs.entity.enums.SignupStatus;
import com.hngy.cvs.mapper.ProductMapper;
import com.hngy.cvs.mapper.RedemptionMapper;
import com.hngy.cvs.service.SignupService;
import com.hngy.cvs.service.CertificateService;
import com.hngy.cvs.service.RecordService;
import com.hngy.cvs.service.StatisticsService;
import com.hngy.cvs.service.UserService;
import com.hngy.cvs.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
    private final RedemptionMapper redemptionMapper;
    private final ProductMapper productMapper;

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
        Long totalSignupsCount = signupService.countApprovedSignupsByOrganizer(teacherId);

        // 3. 统计老师活动的待审核报名人数（状态为PENDING）
        Long pendingApprovalsCount = signupService.countPendingSignupsByOrganizer(teacherId);

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
        // 1. 统计学生的报名数
        Long mySignupsCount = signupService.countSignupsByUser(studentId);
        
        // 2. 统计学生的总服务时长
        Double totalServiceHours = recordService.getTotalServiceHoursByUser(studentId);
        
        // 3. 统计学生的总积分（从 RecordMapper 查询）
        Long totalPoints = recordService.sumPointsByUser(studentId);
        
        // 4. 统计学生的证明数量
        Long certificatesCount = certificateService.countCertificatesByUser(studentId);

        return StudentDashboardStatsVO.builder()
                .mySignupsCount(mySignupsCount)
                .totalServiceHours(totalServiceHours)
                .totalPoints(totalPoints)
                .certificatesCount(certificatesCount)
                .build();
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

    // ==================== 积分商城统计方法实现 ====================

    @Override
    public RedemptionStatisticsVO getRedemptionStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        RedemptionStatisticsVO statistics = new RedemptionStatisticsVO();

        // 总兑换次数和总消耗积分
        if (startDateTime != null && endDateTime != null) {
            statistics.setTotalRedemptions(redemptionMapper.countRedemptionsByDateRange(startDateTime, endDateTime));
            statistics.setTotalPointsSpent(redemptionMapper.sumPointsSpentByDateRange(startDateTime, endDateTime));
        } else {
            // 如果没有指定时间范围，统计所有数据
            statistics.setTotalRedemptions(redemptionMapper.countRedemptionsByDateRange(null, null));
            statistics.setTotalPointsSpent(redemptionMapper.sumPointsSpentByDateRange(null, null));
        }

        // 按状态统计
        statistics.setPendingRedemptions(redemptionMapper.countRedemptionsByStatus(0)); // 待领取
        statistics.setVerifiedRedemptions(redemptionMapper.countRedemptionsByStatus(1)); // 已领取
        statistics.setCancelledRedemptions(redemptionMapper.countRedemptionsByStatus(2)); // 已取消

        // 今日、本周、本月统计
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate monthStart = today.withDayOfMonth(1);

        statistics.setTodayRedemptions(redemptionMapper.countRedemptionsByDateRange(
                today.atStartOfDay(), today.atTime(23, 59, 59)));
        statistics.setWeekRedemptions(redemptionMapper.countRedemptionsByDateRange(
                weekStart.atStartOfDay(), today.atTime(23, 59, 59)));
        statistics.setMonthRedemptions(redemptionMapper.countRedemptionsByDateRange(
                monthStart.atStartOfDay(), today.atTime(23, 59, 59)));

        return statistics;
    }

    @Override
    public List<ProductRankingVO> getProductRanking(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10; // 默认返回前10名
        }
        List<ProductRankingVO> rankings = redemptionMapper.selectProductRanking(limit);
        
        // 设置排名
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }
        
        return rankings;
    }

    @Override
    public List<ProductVO> getLowStockProducts() {
        return productMapper.selectLowStockProducts();
    }

    @Override
    public byte[] exportRedemptions(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        // 查询兑换记录
        List<RedemptionVO> redemptions = redemptionMapper.selectRedemptionsForExport(startDateTime, endDateTime);

        // 创建Excel工作簿
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("兑换记录");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"兑换ID", "学生姓名", "学号", "商品名称", "消耗积分", "凭证编号", "兑换状态", "兑换时间", "核销时间", "核销人员"};
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据行
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < redemptions.size(); i++) {
                Row row = sheet.createRow(i + 1);
                RedemptionVO redemption = redemptions.get(i);

                row.createCell(0).setCellValue(redemption.getId());
                row.createCell(1).setCellValue(redemption.getUserName());
                row.createCell(2).setCellValue(redemption.getUserUsername());
                row.createCell(3).setCellValue(redemption.getProductName());
                row.createCell(4).setCellValue(redemption.getPointsSpent());
                row.createCell(5).setCellValue(redemption.getVoucherCode());
                row.createCell(6).setCellValue(redemption.getStatusText());
                row.createCell(7).setCellValue(redemption.getCreatedAt().format(formatter));
                row.createCell(8).setCellValue(redemption.getVerifiedAt() != null ? 
                    redemption.getVerifiedAt().format(formatter) : "");
                row.createCell(9).setCellValue(redemption.getVerifiedByName() != null ? 
                    redemption.getVerifiedByName() : "");
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("导出Excel文件失败", e);
        }
    }
}
