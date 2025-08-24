package com.hngy.cvs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.cvs.common.result.ResultCode;
import com.hngy.cvs.common.util.AssertUtils;
import com.hngy.cvs.common.util.BeanUtil;
import com.hngy.cvs.common.util.PageUtil;
import com.hngy.cvs.dto.request.ActivityCreateDTO;
import com.hngy.cvs.dto.request.ActivitySearchDTO;
import com.hngy.cvs.dto.request.ActivityUpdateDTO;
import com.hngy.cvs.dto.request.PageDTO;
import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.dto.response.ActivityVO;
import com.hngy.cvs.entity.Activity;
import com.hngy.cvs.entity.enums.ActivityStatus;
import com.hngy.cvs.mapper.ActivityMapper;
import com.hngy.cvs.service.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 活动服务实现类
 * 
 * @author CVS Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityMapper activityMapper;

    @Override
    @Transactional
    public ActivityVO createActivity(ActivityCreateDTO request, Long organizerId) {
        // 验证活动时间
        validateActivityTimes(request.getStartTime(), request.getEndTime(), request.getRegistrationDeadline());

        // 创建活动
        Activity activity = BeanUtil.to(request, Activity.class);
        activity.setOrganizerId(organizerId);

        activityMapper.insert(activity);
        log.info("创建活动成功: {}, 组织者: {}", activity.getTitle(), organizerId);

        return BeanUtil.to(activity, ActivityVO.class);
    }

    @Override
    @Transactional
    public ActivityVO updateActivity(ActivityUpdateDTO request, Long organizerId) {
        Activity activity = activityMapper.selectById(request.getId());
        AssertUtils.notNull(activity, ResultCode.ACTIVITY_NOT_FOUND);

        // 检查权限
        AssertUtils.isTrue(activity.getOrganizerId().equals(organizerId),
                          ResultCode.INSUFFICIENT_PERMISSIONS);

        // 检查活动状态
        AssertUtils.isFalse(activity.getStatus() == ActivityStatus.ONGOING || activity.getStatus() == ActivityStatus.COMPLETED,
                           ResultCode.CANNOT_CANCEL_ACTIVITY);

        // 验证活动时间
        validateActivityTimes(request.getStartTime(), request.getEndTime(), request.getRegistrationDeadline());

        // 更新活动信息
        BeanUtil.to(request, activity, "id", "organizerId", "status", "createdAt");
        activityMapper.updateById(activity);
        log.info("更新活动成功: {}", activity.getId());

        return BeanUtil.to(activity, ActivityVO.class);
    }

    @Override
    @Transactional
    public void deleteActivity(Long id, Long organizerId) {
        Activity activity = activityMapper.selectById(id);
        AssertUtils.notNull(activity, ResultCode.ACTIVITY_NOT_FOUND);

        // 检查权限
        AssertUtils.isTrue(activity.getOrganizerId().equals(organizerId),
                          ResultCode.INSUFFICIENT_PERMISSIONS);

        // 检查活动状态
        AssertUtils.isFalse(activity.getStatus() == ActivityStatus.ONGOING || activity.getStatus() == ActivityStatus.COMPLETED,
                           ResultCode.CANNOT_CANCEL_ACTIVITY);

        // 使用MyBatis-Plus的逻辑删除功能
        activityMapper.deleteById(id);
        log.info("删除活动成功: {}", id);
    }

    @Override
    public ActivityVO getActivityById(Long id) {
        Activity activity = activityMapper.selectById(id);
        AssertUtils.notNull(activity, ResultCode.ACTIVITY_NOT_FOUND);
        return BeanUtil.to(activity, ActivityVO.class);
    }

    @Override
    public PageVO<ActivityVO> getActivityList(PageDTO<ActivitySearchDTO> pageRequest) {
        // 从PageDTO中提取查询条件
        ActivitySearchDTO searchDTO = pageRequest.getParams();
        
        // 创建分页对象
        Page<ActivityVO> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        
        // 执行分页查询
        IPage<ActivityVO> result = activityMapper.selectActivityPage(page, searchDTO);
        
        // 转换为PageVO
        return PageUtil.convert(result, ActivityVO.class);
    }

    @Override
    public PageVO<ActivityVO> getMyActivitiesList(PageDTO<ActivitySearchDTO> pageRequest, Long organizerId) {
        // 从PageDTO中提取查询条件
        ActivitySearchDTO searchDTO = pageRequest.getParams();
        
        // 创建分页对象
        Page<ActivityVO> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        
        // 执行分页查询
        IPage<ActivityVO> result = activityMapper.selectMyActivities(page, searchDTO, organizerId);

        return PageUtil.convert(result, ActivityVO.class);
    }

    @Override
    @Transactional
    public void publishActivity(Long id, Long organizerId) {
        Activity activity = activityMapper.selectById(id);
        AssertUtils.notNull(activity, ResultCode.ACTIVITY_NOT_FOUND);

        // 检查权限
        AssertUtils.isTrue(activity.getOrganizerId().equals(organizerId),
                          ResultCode.INSUFFICIENT_PERMISSIONS);

        // 检查活动状态
        AssertUtils.isTrue(activity.getStatus() == ActivityStatus.DRAFT,
                          ResultCode.BAD_REQUEST);

        // 检查活动时间
        AssertUtils.isFalse(activity.getStartTime().isBefore(LocalDateTime.now()),
                           ResultCode.BAD_REQUEST);

        activity.setStatus(ActivityStatus.PUBLISHED);
        activityMapper.updateById(activity);
        log.info("发布活动成功: {}", id);
    }

    @Override
    @Transactional
    public void cancelActivity(Long id, Long organizerId) {
        Activity activity = activityMapper.selectById(id);
        AssertUtils.notNull(activity, ResultCode.ACTIVITY_NOT_FOUND);

        // 检查权限
        AssertUtils.isTrue(activity.getOrganizerId().equals(organizerId),
                          ResultCode.INSUFFICIENT_PERMISSIONS);

        // 检查活动状态
        AssertUtils.isFalse(activity.getStatus() == ActivityStatus.COMPLETED,
                           ResultCode.CANNOT_CANCEL_ACTIVITY);

        activity.setStatus(ActivityStatus.CANCELLED);
        activityMapper.updateById(activity);
        log.info("取消活动成功: {}", id);
    }

    @Override
    public Long countActivities() {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getDeleted, 0);
        return activityMapper.selectCount(wrapper);
    }

    @Override
    public Long countActivitiesByOrganizer(Long organizerId) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getOrganizerId, organizerId)
                .eq(Activity::getDeleted, 0);
        return activityMapper.selectCount(wrapper);
    }

    /**
     * 验证活动时间的合法性
     *
     * @param startTime 活动开始时间
     * @param endTime 活动结束时间
     * @param registrationDeadline 报名截止时间（可选）
     */
    private void validateActivityTimes(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime registrationDeadline) {
        // 验证结束时间不能早于开始时间
        AssertUtils.isFalse(endTime.isBefore(startTime),
                           ResultCode.BAD_REQUEST);

        // 验证报名截止时间（如果设置了）
        if (registrationDeadline != null) {
            AssertUtils.isFalse(registrationDeadline.isAfter(startTime),
                               ResultCode.BAD_REQUEST);
        }
    }
}
