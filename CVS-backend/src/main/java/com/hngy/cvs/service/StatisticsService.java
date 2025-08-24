package com.hngy.cvs.service;

import com.hngy.cvs.dto.response.AdminDashboardStatsVO;
import com.hngy.cvs.dto.response.StudentDashboardStatsVO;
import com.hngy.cvs.dto.response.TeacherDashboardStatsVO;

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
//    TeacherDashboardStatsVO getTeacherDashboardStats(Long teacherId);

    /**
     * 获取学生仪表板统计数据
     * 
     * @param studentId 学生ID
     * @return 学生仪表板统计数据
     */
//    StudentDashboardStatsVO getStudentDashboardStats(Long studentId);
}
