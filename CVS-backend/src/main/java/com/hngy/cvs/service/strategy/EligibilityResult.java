package com.hngy.cvs.service.strategy;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 资格验证结果
 * 
 * @author CVS Team
 */
@Data
public class EligibilityResult {
    
    /**
     * 是否符合资格
     */
    private boolean eligible;
    
    /**
     * 验证消息（不符合资格时的提示信息）
     */
    private String message;
    
    /**
     * 详细信息（如累计时长、活动数量等）
     */
    private Map<String, Object> details;
    
    /**
     * 构造方法
     */
    public EligibilityResult() {
        this.details = new HashMap<>();
    }
    
    /**
     * 创建成功结果
     * 
     * @return 成功的验证结果
     */
    public static EligibilityResult success() {
        EligibilityResult result = new EligibilityResult();
        result.setEligible(true);
        return result;
    }
    
    /**
     * 创建失败结果
     * 
     * @param message 失败消息
     * @return 失败的验证结果
     */
    public static EligibilityResult failure(String message) {
        EligibilityResult result = new EligibilityResult();
        result.setEligible(false);
        result.setMessage(message);
        return result;
    }
}
