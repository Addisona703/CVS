package com.hngy.cvs.service;

import com.hngy.cvs.dto.request.ServiceRecordSearchDTO;
import com.hngy.cvs.dto.request.PageDTO;
import com.hngy.cvs.dto.response.ServiceRecordVO;
import com.hngy.cvs.dto.response.ServiceStatsVO;
import com.hngy.cvs.dto.response.PageVO;

/**
 * 记录服务接口
 * 
 * @author CVS Team
 */
public interface RecordService {

    /**
     * 根据ID获取服务记录
     */
    ServiceRecordVO getServiceRecordById(Long id);

   /**
     * 获取我的服务记录（支持搜索和分页）
     */
    PageVO<ServiceRecordVO> getMyServiceRecords(PageDTO<ServiceRecordSearchDTO> pageRequest, Long userId);

    /**
     * 获取教师管理的所有服务记录（支持搜索和分页）
     */
    PageVO<ServiceRecordVO> getManagedServiceRecords(PageDTO<ServiceRecordSearchDTO> pageRequest, Long teacherId);

    /**
     * 获取用户服务统计数据
     */
    ServiceStatsVO getUserServiceStats(Long userId, Long currentUserId);

    /**
     * 获取所有服务记录（管理员端）
     */
    PageVO<ServiceRecordVO> getAllServiceRecords(PageDTO<ServiceRecordSearchDTO> pageRequest);

    /**
     * 根据报名记录和评价信息创建服务记录
     */
    void createServiceRecordFromSignup(Long signupId, Integer rating, String evaluation, String description);

    /**
     * 统计总服务记录数
     */
    Long countTotal();

    /**
     * 获取用户总服务时长（小时）
     */
    Double getTotalServiceHoursByUser(Long userId);

    /**
     * 统计用户从服务记录中获得的总积分
     */
    Long sumPointsByUser(Long userId);
}
