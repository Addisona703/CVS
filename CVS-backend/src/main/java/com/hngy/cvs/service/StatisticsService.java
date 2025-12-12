package com.hngy.cvs.service;

import com.hngy.cvs.dto.response.AdminDashboardStatsVO;
import com.hngy.cvs.dto.response.ProductRankingVO;
import com.hngy.cvs.dto.response.ProductVO;
import com.hngy.cvs.dto.response.RedemptionStatisticsVO;
import com.hngy.cvs.dto.response.StudentDashboardStatsVO;
import com.hngy.cvs.dto.response.TeacherDashboardStatsVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 统计服务接口
 * 
 * @author CVS Team
 */
public interface StatisticsService {

    /**
     * 获取管理员仪表板统计数据
     */
    AdminDashboardStatsVO getAdminDashboardStats();

    /**
     * 获取教师仪表板统计数据
     *
     * @param teacherId 教师ID
     * @return 教师仪表板统计数据
     */
    TeacherDashboardStatsVO getTeacherDashboardStats(Long teacherId);

    /**
     * 获取学生仪表板统计数据
     *
     * @param studentId 学生ID
     * @return 学生仪表板统计数据
     */
    StudentDashboardStatsVO getStudentDashboardStats(Long studentId);

    // ==================== 积分商城统计方法 ====================

    /**
     * 获取兑换统计数据
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 兑换统计数据
     */
    RedemptionStatisticsVO getRedemptionStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 获取商品兑换排行
     *
     * @param limit 限制数量
     * @return 商品兑换排行列表
     */
    List<ProductRankingVO> getProductRanking(Integer limit);

    /**
     * 获取库存预警列表
     *
     * @return 库存不足的商品列表
     */
    List<ProductVO> getLowStockProducts();

    /**
     * 获取核销统计数据
     *
     * @return 核销统计数据
     */
    com.hngy.cvs.dto.response.VerifyStatisticsVO getVerifyStatistics();

    /**
     * 导出兑换记录Excel
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return Excel文件字节数组
     */
    byte[] exportRedemptions(LocalDate startDate, LocalDate endDate);
}
