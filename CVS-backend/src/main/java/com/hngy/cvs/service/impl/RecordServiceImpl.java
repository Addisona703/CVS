package com.hngy.cvs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.cvs.common.exception.BusinessException;
import com.hngy.cvs.common.result.ResultCode;
import com.hngy.cvs.common.util.AssertUtils;
import com.hngy.cvs.common.util.BeanUtil;
import com.hngy.cvs.dto.request.ServiceRecordCreateDTO;
import com.hngy.cvs.dto.response.ServiceRecordVO;
import com.hngy.cvs.entity.Signup;
import com.hngy.cvs.entity.RecordEntity;
import com.hngy.cvs.entity.Activity;
import com.hngy.cvs.mapper.SignupMapper;
import com.hngy.cvs.mapper.RecordMapper;
import com.hngy.cvs.mapper.ActivityMapper;
import com.hngy.cvs.service.RecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 记录服务实现类
 * 
 * @author CVS Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final RecordMapper recordMapper;
    private final SignupMapper signupMapper;
    private final ActivityMapper activityMapper;

    @Override
    @Transactional
    public ServiceRecordVO createServiceRecord(ServiceRecordCreateDTO request) {
        // 验证用户和活动是否存在
        Activity activity = activityMapper.selectById(request.getActivityId());
        AssertUtils.notNull(activity, ResultCode.ACTIVITY_NOT_FOUND);

        // 创建服务记录
        RecordEntity serviceRecord = BeanUtil.to(request, RecordEntity.class);

        // 如果没有指定积分，使用活动的默认积分
        if (serviceRecord.getPointsEarned() == null || serviceRecord.getPointsEarned() == 0) {
            serviceRecord.setPointsEarned(activity.getPoints() != null ? activity.getPoints() : 0);
        }

        recordMapper.insert(serviceRecord);
        log.info("创建服务记录成功: 用户 {}, 活动 {}", request.getUserId(), request.getActivityId());

        return convertToVO(serviceRecord);
    }

    @Override
    @Transactional
    public ServiceRecordVO updateServiceRecord(Long id, ServiceRecordCreateDTO request) {
        RecordEntity serviceRecord = recordMapper.selectById(id);
        AssertUtils.notNull(serviceRecord, ResultCode.NOT_FOUND);

        // 更新服务记录
        BeanUtil.to(request, serviceRecord, "id", "userId", "activityId", "createdAt");

        recordMapper.updateById(serviceRecord);
        log.info("更新服务记录成功: {}", id);

        return convertToVO(serviceRecord);
    }

    @Override
    @Transactional
    public void deleteServiceRecord(Long id) {
        RecordEntity serviceRecord = recordMapper.selectById(id);
        AssertUtils.notNull(serviceRecord, ResultCode.NOT_FOUND);

        recordMapper.deleteById(id);
        log.info("删除服务记录成功: {}", id);
    }

    @Override
    public ServiceRecordVO getServiceRecordById(Long id) {
        RecordEntity serviceRecord = recordMapper.selectById(id);
        AssertUtils.notNull(serviceRecord, ResultCode.NOT_FOUND);
        return convertToVO(serviceRecord);
    }

    @Override
    public IPage<ServiceRecordVO> getUserServiceRecords(Long userId, int page, int size) {
        Page<ServiceRecordVO> recordPage = new Page<>(page, size);
        return recordMapper.selectMyRecords(recordPage, userId);
    }

    @Override
    public IPage<ServiceRecordVO> getActivityServiceRecords(Long activityId, int page, int size) {
        Page<ServiceRecordVO> recordPage = new Page<>(page, size);
        return recordMapper.selectActivityRecords(recordPage, activityId);
    }

    @Override
    public Double getUserTotalServiceHours(Long userId) {
        LambdaQueryWrapper<RecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecordEntity::getUserId, userId);
        List<RecordEntity> records = recordMapper.selectList(wrapper);
        
        int totalMinutes = records.stream()
                .mapToInt(RecordEntity::getDurationMinutes)
                .sum();
        
        return totalMinutes / 60.0;
    }

    @Override
    public Double getActivityTotalServiceHours(Long activityId) {
        LambdaQueryWrapper<RecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecordEntity::getActivityId, activityId);
        List<RecordEntity> records = recordMapper.selectList(wrapper);
        
        int totalMinutes = records.stream()
                .mapToInt(RecordEntity::getDurationMinutes)
                .sum();
        
        return totalMinutes / 60.0;
    }

    @Override
    public Long countServiceRecordsByUser(Long userId) {
        LambdaQueryWrapper<RecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecordEntity::getUserId, userId);
        return recordMapper.selectCount(wrapper);
    }

    @Override
    public Long countServiceRecords() {
        return recordMapper.selectCount(null);
    }

    @Override
    @Transactional
    public void generateServiceRecordFromSignup(Long signupId) {
        Signup signup = signupMapper.selectById(signupId);
        AssertUtils.notNull(signup, ResultCode.SIGNUP_NOT_FOUND);

        // 检查是否已签到签退
        AssertUtils.isTrue(signup.getSignedIn() && signup.getSignedOut(),
                          ResultCode.BAD_REQUEST);

        // 计算服务时长
        Duration duration = Duration.between(signup.getSignInTime(), signup.getSignOutTime());
        int durationMinutes = (int) duration.toMinutes();

        // 创建服务记录
        RecordEntity serviceRecord = new RecordEntity();
        serviceRecord.setUserId(signup.getUserId());
        serviceRecord.setActivityId(signup.getActivityId());
        serviceRecord.setDurationMinutes(durationMinutes);
        serviceRecord.setDescription("根据签到签退记录自动生成");

        recordMapper.insert(serviceRecord);
        log.info("根据报名记录 {} 自动生成服务记录", signupId);
    }

    @Override
    public Long countTotal() {
        return countServiceRecords();
    }

    @Override
    public Double getTotalServiceHoursByUser(Long userId) {
        return getUserTotalServiceHours(userId);
    }

    /**
     * 转换为VO对象
     */
    private ServiceRecordVO convertToVO(RecordEntity serviceRecord) {
        return BeanUtil.to(serviceRecord, ServiceRecordVO.class);
    }
}
