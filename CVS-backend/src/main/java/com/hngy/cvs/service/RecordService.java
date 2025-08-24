package com.hngy.cvs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hngy.cvs.dto.request.ServiceRecordCreateDTO;
import com.hngy.cvs.dto.response.ServiceRecordVO;

/**
 * 记录服务接口
 * 
 * @author CVS Team
 */
public interface RecordService {

    /**
     * 创建服务记录
     */
    ServiceRecordVO createServiceRecord(ServiceRecordCreateDTO request);

    /**
     * 更新服务记录
     */
    ServiceRecordVO updateServiceRecord(Long id, ServiceRecordCreateDTO request);

    /**
     * 删除服务记录
     */
    void deleteServiceRecord(Long id);

    /**
     * 根据ID获取服务记录
     */
    ServiceRecordVO getServiceRecordById(Long id);

    /**
     * 获取用户的服务记录
     */
    IPage<ServiceRecordVO> getUserServiceRecords(Long userId, int page, int size);

    /**
     * 获取活动的服务记录
     */
    IPage<ServiceRecordVO> getActivityServiceRecords(Long activityId, int page, int size);

    /**
     * 统计用户的总服务时长（小时）
     */
    Double getUserTotalServiceHours(Long userId);

    /**
     * 统计活动的总服务时长（小时）
     */
    Double getActivityTotalServiceHours(Long activityId);

    /**
     * 统计用户的服务记录数
     */
    Long countServiceRecordsByUser(Long userId);

    /**
     * 统计总服务记录数
     */
    Long countServiceRecords();

    /**
     * 统计总服务记录数（别名方法）
     */
    Long countTotal();

    /**
     * 统计用户的总服务时长（小时）- 别名方法
     */
    Double getTotalServiceHoursByUser(Long userId);

    /**
     * 根据签到签退记录自动生成服务记录
     */
    void generateServiceRecordFromSignup(Long signupId);
}
