package com.hngy.cvs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.cvs.common.exception.BusinessException;
import com.hngy.cvs.common.result.ResultCode;
import com.hngy.cvs.common.util.AssertUtils;
import com.hngy.cvs.common.util.BeanUtil;
import com.hngy.cvs.common.util.PageUtil;
import com.hngy.cvs.dto.request.ServiceRecordCreateDTO;
import com.hngy.cvs.dto.request.ServiceRecordSearchDTO;
import com.hngy.cvs.dto.request.PageDTO;
import com.hngy.cvs.dto.response.ServiceRecordVO;
import com.hngy.cvs.dto.response.ServiceStatsVO;
import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.entity.Signup;
import com.hngy.cvs.entity.RecordEntity;
import com.hngy.cvs.entity.Activity;
import com.hngy.cvs.entity.User;
import com.hngy.cvs.mapper.SignupMapper;
import com.hngy.cvs.mapper.RecordMapper;
import com.hngy.cvs.mapper.ActivityMapper;
import com.hngy.cvs.mapper.UserMapper;
import com.hngy.cvs.service.RecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 记录服务实现类
 * 
 * @author CVS Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecordServiceImpl
        extends ServiceImpl<RecordMapper, RecordEntity>
        implements RecordService {

    private final SignupMapper signupMapper;
    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;

    @Override
    public ServiceRecordVO getServiceRecordById(Long id) {
        RecordEntity serviceRecord = this.getById(id);
        AssertUtils.notNull(serviceRecord, ResultCode.NOT_FOUND);
        return BeanUtil.to(serviceRecord, ServiceRecordVO.class);
    }

    @Override
    public PageVO<ServiceRecordVO> getMyServiceRecords(PageDTO<ServiceRecordSearchDTO> pageRequest, Long userId) {
        ServiceRecordSearchDTO searchDTO = pageRequest.getParams();
        if (searchDTO == null) {
            searchDTO = new ServiceRecordSearchDTO();
        }
        searchDTO.setUserId(userId); // 强制设置为当前用户
        
        Page<RecordEntity> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        LambdaQueryWrapper<RecordEntity> wrapper = buildSearchWrapper(searchDTO);
        
        IPage<RecordEntity> result = this.page(page, wrapper);
        PageVO<ServiceRecordVO> pageVO = PageUtil.convert(result, ServiceRecordVO.class);
        
        // 补充关联信息
        enrichServiceRecordVOList(pageVO.getRecords());
        return pageVO;
    }

    @Override
    public PageVO<ServiceRecordVO> getManagedServiceRecords(PageDTO<ServiceRecordSearchDTO> pageRequest, Long teacherId) {
        ServiceRecordSearchDTO searchDTO = pageRequest.getParams();
        if (searchDTO == null) {
            searchDTO = new ServiceRecordSearchDTO();
        }
        
        // 获取该教师发布的所有活动ID
        LambdaQueryWrapper<Activity> teacherActivityWrapper = new LambdaQueryWrapper<Activity>()
                .eq(Activity::getOrganizerId, teacherId)
                .select(Activity::getId);
        List<Activity> teacherActivities = activityMapper.selectList(teacherActivityWrapper);
        
        if (teacherActivities.isEmpty()) {
            return PageUtil.empty(pageRequest.getPageNum(), pageRequest.getPageSize());
        }
        
        List<Long> teacherActivityIds = teacherActivities.stream()
                .map(Activity::getId)
                .collect(Collectors.toList());
        
        Page<RecordEntity> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        LambdaQueryWrapper<RecordEntity> wrapper = buildSearchWrapper(searchDTO)
                .in(RecordEntity::getActivityId, teacherActivityIds);
        
        IPage<RecordEntity> result = this.page(page, wrapper);
        PageVO<ServiceRecordVO> pageVO = PageUtil.convert(result, ServiceRecordVO.class);
        
        // 补充关联信息
        enrichServiceRecordVOList(pageVO.getRecords());
        return pageVO;
    }

    @Override
    public ServiceStatsVO getUserServiceStats(Long userId, Long currentUserId) {
        // 权限检查：只能查看自己的统计数据（除非是管理员）
        if (!userId.equals(currentUserId)) {
            // TODO:这里可以添加管理员权限检查，但目前简化处理
            AssertUtils.fail(ResultCode.INSUFFICIENT_PERMISSIONS);
        }
        
        LambdaQueryWrapper<RecordEntity> wrapper = new LambdaQueryWrapper<RecordEntity>()
                .eq(RecordEntity::getUserId, userId);
        List<RecordEntity> records = this.list(wrapper);
        
        ServiceStatsVO stats = new ServiceStatsVO();
        stats.setTotalRecords((long) records.size());
        
        if (!records.isEmpty()) {
            // 计算总服务时长
            int totalMinutes = records.stream()
                    .mapToInt(RecordEntity::getDurationMinutes)
                    .sum();
            double totalHours = Math.round(totalMinutes / 60.0 * 10.0) / 10.0;
            stats.setTotalServiceHours(totalHours);
            
            // 计算总积分
            int totalPoints = records.stream()
                    .mapToInt(record -> record.getPointsEarned() != null ? record.getPointsEarned() : 0)
                    .sum();
            stats.setTotalPointsEarned(totalPoints);
            
            // 计算平均服务时长
            double averageHours = Math.round((stats.getTotalServiceHours() / records.size()) * 10.0) / 10.0;
            stats.setAverageServiceHours(averageHours);
            
            // 计算最长和最短服务时长
            OptionalInt maxMinutes = records.stream()
                    .mapToInt(RecordEntity::getDurationMinutes)
                    .max();
            OptionalInt minMinutes = records.stream()
                    .mapToInt(RecordEntity::getDurationMinutes)
                    .min();

            stats.setMaxServiceHours(Math.round(maxMinutes.getAsInt() / 60.0 * 10.0) / 10.0);
            stats.setMinServiceHours(Math.round(minMinutes.getAsInt() / 60.0 * 10.0) / 10.0);

            // 计算参与活动数量
            long totalActivities = records.stream()
                    .map(RecordEntity::getActivityId)
                    .distinct()
                    .count();
            stats.setTotalActivities(totalActivities);
        } else {
            stats.setTotalServiceHours(0.0);
            stats.setTotalPointsEarned(0);
            stats.setAverageServiceHours(0.0);
            stats.setMaxServiceHours(0.0);
            stats.setMinServiceHours(0.0);
            stats.setTotalActivities(0L);
        }
        
        return stats;
    }

    @Override
    public PageVO<ServiceRecordVO> getAllServiceRecords(PageDTO<ServiceRecordSearchDTO> pageRequest) {
        ServiceRecordSearchDTO searchDTO = pageRequest.getParams();
        if (searchDTO == null) {
            searchDTO = new ServiceRecordSearchDTO();
        }
        
        Page<RecordEntity> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        LambdaQueryWrapper<RecordEntity> wrapper = buildSearchWrapper(searchDTO);
        
        IPage<RecordEntity> result = this.page(page, wrapper);
        PageVO<ServiceRecordVO> pageVO = PageUtil.convert(result, ServiceRecordVO.class);
        
        // 补充关联信息
        enrichServiceRecordVOList(pageVO.getRecords());
        return pageVO;
    }
    
    /**
     * 构建搜索条件
     */
    private LambdaQueryWrapper<RecordEntity> buildSearchWrapper(ServiceRecordSearchDTO searchDTO) {
        LambdaQueryWrapper<RecordEntity> wrapper = new LambdaQueryWrapper<RecordEntity>()
                .eq(searchDTO.getActivityId() != null, RecordEntity::getActivityId, searchDTO.getActivityId())
                .eq(searchDTO.getUserId() != null, RecordEntity::getUserId, searchDTO.getUserId())
                .like(searchDTO.getDescription() != null && !searchDTO.getDescription().trim().isEmpty(),
                      RecordEntity::getDescription, searchDTO.getDescription())
                .ge(searchDTO.getMinDurationMinutes() != null, RecordEntity::getDurationMinutes, searchDTO.getMinDurationMinutes())
                .le(searchDTO.getMaxDurationMinutes() != null, RecordEntity::getDurationMinutes, searchDTO.getMaxDurationMinutes())
                .ge(searchDTO.getMinPointsEarned() != null, RecordEntity::getPointsEarned, searchDTO.getMinPointsEarned())
                .le(searchDTO.getMaxPointsEarned() != null, RecordEntity::getPointsEarned, searchDTO.getMaxPointsEarned())
                .orderByDesc(RecordEntity::getCreatedAt);
        
        // 按活动名称搜索
        if (searchDTO.getActivityTitle() != null && !searchDTO.getActivityTitle().trim().isEmpty()) {
            LambdaQueryWrapper<Activity> activityWrapper = new LambdaQueryWrapper<Activity>()
                    .like(Activity::getTitle, searchDTO.getActivityTitle())
                    .select(Activity::getId);
            List<Activity> activities = activityMapper.selectList(activityWrapper);
            if (activities.isEmpty()) {
                // 如果没有匹配的活动，返回空结果
                wrapper.eq(RecordEntity::getId, -1L); // 使用不存在的ID
            } else {
                List<Long> activityIds = activities.stream().map(Activity::getId).collect(Collectors.toList());
                wrapper.in(RecordEntity::getActivityId, activityIds);
            }
        }
        
        // 按用户姓名搜索
        if (searchDTO.getUserName() != null && !searchDTO.getUserName().trim().isEmpty()) {
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<User>()
                    .like(User::getName, searchDTO.getUserName())
                    .select(User::getId);
            List<User> users = userMapper.selectList(userWrapper);
            if (users.isEmpty()) {
                wrapper.eq(RecordEntity::getId, -1L); // 使用不存在的ID
            } else {
                List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
                wrapper.in(RecordEntity::getUserId, userIds);
            }
        }
        
        return wrapper;
    }
    
    @Override
    @Transactional
    public void createServiceRecordFromSignup(Long signupId, Integer rating, String evaluation, String description) {
        // 查询报名记录
        Signup signup = signupMapper.selectById(signupId);
        AssertUtils.notNull(signup, ResultCode.NOT_FOUND);
        
        // 验证报名状态必须是已签退
        AssertUtils.isTrue(signup.getSignOutTime() != null, ResultCode.SIGNUP_NOT_SIGNED_OUT);
        AssertUtils.isTrue(signup.getSignInTime() != null, ResultCode.SIGNUP_NOT_SIGNED_IN);
        
        // 查询活动信息
        Activity activity = activityMapper.selectById(signup.getActivityId());
        AssertUtils.notNull(activity, ResultCode.NOT_FOUND);
        
        // 计算服务时长（分钟）
        Duration duration = Duration.between(signup.getSignInTime(), signup.getSignOutTime());
        int durationMinutes = (int) duration.toMinutes();
        
        // 计算获得积分（根据服务时长计算，这里简单按1小时1积分计算）
        int pointsEarned = Math.max(1, durationMinutes / 60);
        
        // 创建服务记录
        RecordEntity serviceRecord = new RecordEntity();
        serviceRecord.setUserId(signup.getUserId());
        serviceRecord.setActivityId(signup.getActivityId());
        serviceRecord.setDurationMinutes(durationMinutes);
        serviceRecord.setPointsEarned(pointsEarned);
        serviceRecord.setRating(rating);
        serviceRecord.setEvaluation(evaluation);
        serviceRecord.setDescription(description);
        serviceRecord.setCreatedAt(LocalDateTime.now());
        serviceRecord.setUpdatedAt(LocalDateTime.now());
        
        // 保存服务记录
        this.save(serviceRecord);
        
        log.info("服务记录创建成功 - 用户ID: {}, 活动ID: {}, 报名ID: {}, 服务时长: {}分钟, 积分: {}", 
                signup.getUserId(), signup.getActivityId(), signupId, durationMinutes, pointsEarned);
    }

    /**
     * 补充服务记录列表的关联信息
     */
    private void enrichServiceRecordVOList(List<ServiceRecordVO> serviceRecordVOs) {
        if (serviceRecordVOs.isEmpty()) {
            return;
        }
        
        // 获取所有活动ID和用户ID
        List<Long> activityIds = serviceRecordVOs.stream()
                .map(ServiceRecordVO::getActivityId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> userIds = serviceRecordVOs.stream()
                .map(ServiceRecordVO::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询活动和用户信息
        List<Activity> activities = activityMapper.selectBatchIds(activityIds);
        List<User> users = userMapper.selectBatchIds(userIds);
        
        Map<Long, Activity> activityMap = activities.stream()
                .collect(Collectors.toMap(Activity::getId, Function.identity()));
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        
        // 补充信息
        serviceRecordVOs.forEach(vo -> {
            Activity activity = activityMap.get(vo.getActivityId());
            if (activity != null) {
                vo.setActivityTitle(activity.getTitle());
            }
            
            User user = userMap.get(vo.getUserId());
            if (user != null) {
                vo.setUserName(user.getName());
            }
        });
    }

    @Override
    public Long countTotal() {
        return this.count();
    }

    @Override
    public Double getTotalServiceHoursByUser(Long userId) {
        LambdaQueryWrapper<RecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecordEntity::getUserId, userId);
        List<RecordEntity> records = this.list(wrapper);
        
        if (records.isEmpty()) {
            return 0.0;
        }
        
        int totalMinutes = records.stream()
                .mapToInt(RecordEntity::getDurationMinutes)
                .sum();
        
        // 转换为小时，保留一位小数
        return Math.round(totalMinutes / 60.0 * 10.0) / 10.0;
    }

    @Override
    public Long sumPointsByUser(Long userId) {
        LambdaQueryWrapper<RecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecordEntity::getUserId, userId);
        List<RecordEntity> records = this.list(wrapper);
        
        if (records.isEmpty()) {
            return 0L;
        }
        
        return records.stream()
                .mapToLong(record -> record.getPointsEarned() != null ? record.getPointsEarned() : 0)
                .sum();
    }
}