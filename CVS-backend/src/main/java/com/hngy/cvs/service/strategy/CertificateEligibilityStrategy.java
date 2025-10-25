package com.hngy.cvs.service.strategy;

/**
 * 证书资格验证策略接口
 * 
 * @author CVS Team
 */
public interface CertificateEligibilityStrategy {
    
    /**
     * 验证用户是否有资格申请证书
     * 
     * @param userId 用户ID
     * @return 验证结果
     */
    EligibilityResult validate(Long userId);
}
