package com.hngy.cvs.service.strategy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.cvs.entity.Signup;
import com.hngy.cvs.mapper.SignupMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于服务小时数的资格验证策略
 * 
 * @author CVS Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceHoursEligibilityStrategy implements CertificateEligibilityStrategy {
    
    private final SignupMapper signupMapper;
    
    @Value("${certificate.eligibility.min-hours:2}")
    private double minRequiredHours;
    
    @Override
    public EligibilityResult validate(Long userId) {
        // 1. 查询所有已完成的志愿服务（签到和签退都不为空）
        List<Signup> completedSignups = signupMapper.selectList(
            new LambdaQueryWrapper<Signup>()
                .eq(Signup::getUserId, userId)
                .isNotNull(Signup::getSignInTime)
                .isNotNull(Signup::getSignOutTime)
        );
        
        // 2. 计算累计服务小时数
        double totalHours = 0.0;
        for (Signup signup : completedSignups) {
            LocalDateTime signIn = signup.getSignInTime();
            LocalDateTime signOut = signup.getSignOutTime();
            if (signIn != null && signOut != null) {
                long minutes = Duration.between(signIn, signOut).toMinutes();
                totalHours += minutes / 60.0;
            }
        }
        
        // 3. 构建验证结果
        EligibilityResult result = new EligibilityResult();
        result.setEligible(totalHours >= minRequiredHours);
        
        Map<String, Object> details = new HashMap<>();
        details.put("totalHours", totalHours);
        details.put("minRequiredHours", minRequiredHours);
        details.put("completedActivities", completedSignups.size());
        result.setDetails(details);
        
        if (!result.isEligible()) {
            result.setMessage(String.format(
                "您的累计志愿服务时长为%.1f小时，需要至少%.1f小时才能申请证书",
                totalHours, minRequiredHours
            ));
        }
        
        log.debug("用户 {} 资格验证结果: 累计时长={}小时, 最小要求={}小时, 符合资格={}", 
                  userId, totalHours, minRequiredHours, result.isEligible());
        
        return result;
    }
}
