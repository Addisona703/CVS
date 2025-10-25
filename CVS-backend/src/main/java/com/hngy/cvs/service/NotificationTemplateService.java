package com.hngy.cvs.service;

import com.hngy.cvs.entity.enums.NotificationType;

import java.util.Map;

/**
 * 通知模板服务接口
 */
public interface NotificationTemplateService {

    /**
     * 生成通知标题
     * @param type 通知类型
     * @param templateData 模板数据
     * @return 生成的标题
     */
    String generateTitle(NotificationType type, Map<String, Object> templateData);

    /**
     * 生成通知内容
     * @param type 通知类型
     * @param templateData 模板数据
     * @return 生成的内容
     */
    String generateContent(NotificationType type, Map<String, Object> templateData);

    /**
     * 生成跳转链接URL
     * @param type 通知类型
     * @param templateData 模板数据
     * @return 生成的链接URL
     */
    String generateLinkUrl(NotificationType type, Map<String, Object> templateData);

    /**
     * 创建报名审核通知的模板数据
     * @param activityId 活动ID
     * @param activityTitle 活动标题
     * @param studentName 学生姓名
     * @param studentNumber 学号
     * @return 模板数据
     */
    Map<String, Object> createRegistrationPendingData(Long activityId, String activityTitle, String studentName, String studentNumber);

    /**
     * 创建签退审核通知的模板数据
     * @param activityId 活动ID
     * @param activityTitle 活动标题
     * @param studentName 学生姓名
     * @param studentNumber 学号
     * @param checkoutReason 签退原因
     * @return 模板数据
     */
    Map<String, Object> createCheckoutPendingData(Long activityId, String activityTitle, String studentName, String studentNumber, String checkoutReason);

    /**
     * 创建审核结果通知的模板数据
     * @param activityId 活动ID
     * @param activityTitle 活动标题
     * @param approved 是否通过
     * @param reason 拒绝原因（如果有）
     * @return 模板数据
     */
    Map<String, Object> createApprovalResultData(Long activityId, String activityTitle, boolean approved, String reason);

    /**
     * 创建活动状态通知的模板数据
     * @param activityId 活动ID
     * @param activityTitle 活动标题
     * @param activityTime 活动时间
     * @param activityLocation 活动地点
     * @return 模板数据
     */
    Map<String, Object> createActivityStatusData(Long activityId, String activityTitle, String activityTime, String activityLocation);

    /**
     * 生成带参数的跳转链接URL
     * @param type 通知类型
     * @param templateData 模板数据
     * @param additionalParams 额外参数
     * @return 生成的链接URL
     */
    String generateLinkUrlWithParams(NotificationType type, Map<String, Object> templateData, Map<String, String> additionalParams);

    /**
     * 构建URL查询参数
     * @param baseUrl 基础URL
     * @param params 参数映射
     * @return 完整的URL
     */
    String buildUrlWithParams(String baseUrl, Map<String, String> params);
}