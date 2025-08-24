package com.hngy.cvs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.cvs.common.exception.BusinessException;
import com.hngy.cvs.common.result.ResultCode;
import com.hngy.cvs.common.util.AssertUtils;
import com.hngy.cvs.common.util.BeanUtil;
import com.hngy.cvs.common.util.PageUtil;
import com.hngy.cvs.dto.request.SignInOutDTO;
import com.hngy.cvs.dto.request.SignupApprovalDTO;
import com.hngy.cvs.dto.request.SignupCreateDTO;
import com.hngy.cvs.dto.request.SignupSearchDTO;
import com.hngy.cvs.dto.request.PageDTO;
import com.hngy.cvs.dto.response.SignupVO;
import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.entity.Signup;
import com.hngy.cvs.entity.Activity;
import com.hngy.cvs.entity.enums.ActivityStatus;
import com.hngy.cvs.entity.enums.SignupStatus;
import com.hngy.cvs.mapper.SignupMapper;
import com.hngy.cvs.mapper.ActivityMapper;
import com.hngy.cvs.service.SignupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 报名服务实现类
 * 
 * @author CVS Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignupServiceImpl implements SignupService {

    private final SignupMapper signupMapper;
    private final ActivityMapper activityMapper;

    @Override
    @Transactional
    public SignupVO signupActivity(SignupCreateDTO request, Long userId) {
        // 检查活动是否存在
        Activity activity = activityMapper.selectById(request.getActivityId());
        AssertUtils.notNull(activity, ResultCode.ACTIVITY_NOT_FOUND);

        // 检查活动状态
        AssertUtils.isTrue(activity.getStatus() == ActivityStatus.PUBLISHED,
                          ResultCode.ACTIVITY_NOT_PUBLISHED);

        // 检查报名截止时间
        if (activity.getRegistrationDeadline() != null) {
            AssertUtils.isFalse(activity.getRegistrationDeadline().isBefore(LocalDateTime.now()),
                               ResultCode.BAD_REQUEST);
        }

        // 检查活动是否已过期
        AssertUtils.isFalse(activity.getStartTime().isBefore(LocalDateTime.now()),
                           ResultCode.ACTIVITY_EXPIRED);

        // 检查是否已经报名
        AssertUtils.isFalse(
                signupMapper.exists(
                        new LambdaQueryWrapper<Signup>()
                                .eq(Signup::getUserId, userId)
                                .eq(Signup::getActivityId, activity.getId())),
                           ResultCode.ALREADY_SIGNED_UP);

        // 创建报名记录
        Signup signup = BeanUtil.to(request, Signup.class);
        signup.setUserId(userId);
        signup.setStatus(SignupStatus.PENDING);
        signup.setSignedIn(false);
        signup.setSignedOut(false);

        signupMapper.insert(signup);
        log.info("用户 {} 报名活动 {} 成功", userId, request.getActivityId());

        // 转换为VO返回
        return BeanUtil.to(signup, SignupVO.class);
    }

    @Override
    public SignupVO getSignupById(Long id) {
        Signup signup = signupMapper.selectById(id);
        AssertUtils.notNull(signup, ResultCode.SIGNUP_NOT_FOUND);
        
        return BeanUtil.to(signup, SignupVO.class);
    }

    @Override
    public PageVO<SignupVO> getSignupList(PageDTO<SignupSearchDTO> pageRequest) {
        // 从PageDTO中提取查询条件
        SignupSearchDTO searchDTO = pageRequest.getParams();
        
        // 创建分页对象
        Page<SignupVO> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        
        // 执行分页查询
        IPage<SignupVO> result = signupMapper.selectSignupPage(page, searchDTO);

        return PageUtil.convert(result, SignupVO.class);
    }
}
