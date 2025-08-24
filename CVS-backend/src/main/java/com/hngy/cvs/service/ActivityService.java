package com.hngy.cvs.service;

import com.hngy.cvs.dto.request.ActivityCreateDTO;
import com.hngy.cvs.dto.request.ActivitySearchDTO;
import com.hngy.cvs.dto.request.ActivityUpdateDTO;
import com.hngy.cvs.dto.request.PageDTO;
import com.hngy.cvs.dto.response.ActivityVO;
import com.hngy.cvs.dto.response.PageVO;

/**
 * 活动服务接口
 * 
 * @author CVS Team
 */
public interface ActivityService {

    /**
     * 创建活动
     */
    ActivityVO createActivity(ActivityCreateDTO request, Long organizerId);

    /**
     * 更新活动
     */
    ActivityVO updateActivity(ActivityUpdateDTO request, Long organizerId);

    /**
     * 删除活动
     */
    void deleteActivity(Long id, Long organizerId);

    /**
     * 根据ID获取活动详情
     */
    ActivityVO getActivityById(Long id);

    /**
     * 分页查询活动列表
     * 支持按标题、创建者名字模糊查询，按活动状态筛选
     */
    PageVO<ActivityVO> getActivityList(PageDTO<ActivitySearchDTO> pageRequest);

    /**
     * 获取我创建的活动列表（新版）
     * 使用PageDTO参数，在Service层完成PageVO转换
     */
    PageVO<ActivityVO> getMyActivitiesList(PageDTO<ActivitySearchDTO> pageRequest, Long organizerId);

    /**
     * 发布活动
     */
    void publishActivity(Long id, Long organizerId);

    /**
     * 取消活动
     */
    void cancelActivity(Long id, Long organizerId);

    /**
     * 统计活动数量
     */
    Long countActivities();

    /**
     * 统计用户创建的活动数量
     */
    Long countActivitiesByOrganizer(Long organizerId);
}
