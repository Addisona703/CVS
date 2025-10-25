package com.hngy.cvs.service.impl;

import com.hngy.cvs.entity.enums.NotificationType;
import com.hngy.cvs.service.NotificationTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知模板服务实现
 */
@Slf4j
@Service
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    @Override
    public String generateTitle(NotificationType type, Map<String, Object> templateData) {
        switch (type) {
            case ACTIVITY_START:
                return "活动即将开始";
            case ACTIVITY_ONGOING:
                return "活动进行中";
            case ACTIVITY_END:
                return "活动已结束";
            case REGISTRATION_PENDING:
                return "新的报名申请";
            case REGISTRATION_APPROVED:
                return "报名审核通过";
            case REGISTRATION_REJECTED:
                return "报名审核未通过";
            case CHECKOUT_PENDING:
                return "新的签退申请";
            case CHECKOUT_APPROVED:
                return "签退审核通过";
            case CHECKOUT_REJECTED:
                return "签退审核未通过";
            default:
                return "系统通知";
        }
    }

    @Override
    public String generateContent(NotificationType type, Map<String, Object> templateData) {
        switch (type) {
            case ACTIVITY_START:
                return generateActivityStartContent(templateData);
            case ACTIVITY_ONGOING:
                return generateActivityOngoingContent(templateData);
            case ACTIVITY_END:
                return generateActivityEndContent(templateData);
            case REGISTRATION_PENDING:
                return generateRegistrationPendingContent(templateData);
            case REGISTRATION_APPROVED:
                return generateRegistrationApprovedContent(templateData);
            case REGISTRATION_REJECTED:
                return generateRegistrationRejectedContent(templateData);
            case CHECKOUT_PENDING:
                return generateCheckoutPendingContent(templateData);
            case CHECKOUT_APPROVED:
                return generateCheckoutApprovedContent(templateData);
            case CHECKOUT_REJECTED:
                return generateCheckoutRejectedContent(templateData);
            default:
                return "您有新的系统通知，请及时查看。";
        }
    }

    @Override
    public String generateLinkUrl(NotificationType type, Map<String, Object> templateData) {
        Long activityId = (Long) templateData.get("activityId");
        if (activityId == null) {
            return null;
        }

        switch (type) {
            case ACTIVITY_START:
                // 活动开始通知 - 跳转到活动详情页面，并高亮签到区域
                return "/activities/" + activityId + "?tab=checkin&highlight=signin";
            case ACTIVITY_ONGOING:
                // 活动进行中通知 - 跳转到活动详情页面，继续提醒签到
                return "/activities/" + activityId + "?tab=checkin&highlight=signin";
            case ACTIVITY_END:
                // 活动结束通知 - 跳转到活动详情页面，并高亮签退区域
                return "/activities/" + activityId + "?tab=checkin&highlight=signout";
            case REGISTRATION_PENDING:
                // 报名待审核通知 - 跳转到审核页面，并定位到待审核列表
                return "/activities/" + activityId + "/review?tab=registration&status=pending";
            case REGISTRATION_APPROVED:
                // 报名审核通过通知 - 跳转到活动详情页面，并高亮活动信息
                return "/activities/" + activityId + "?tab=info&highlight=approved";
            case REGISTRATION_REJECTED:
                // 报名审核拒绝通知 - 跳转到活动详情页面，并显示拒绝信息
                return "/activities/" + activityId + "?tab=info&highlight=rejected";
            case CHECKOUT_PENDING:
                // 签退待审核通知 - 跳转到审核页面，并定位到签退审核列表
                return "/activities/" + activityId + "/review?tab=checkout&status=pending";
            case CHECKOUT_APPROVED:
                // 签退审核通过通知 - 跳转到活动详情页面，并高亮签退成功信息
                return "/activities/" + activityId + "?tab=checkin&highlight=checkout-approved";
            case CHECKOUT_REJECTED:
                // 签退审核拒绝通知 - 跳转到活动详情页面，并显示拒绝信息
                return "/activities/" + activityId + "?tab=checkin&highlight=checkout-rejected";
            default:
                return "/activities/" + activityId;
        }
    }

    @Override
    public Map<String, Object> createRegistrationPendingData(Long activityId, String activityTitle, String studentName, String studentNumber) {
        Map<String, Object> data = new HashMap<>();
        data.put("activityId", activityId);
        data.put("activityTitle", activityTitle);
        data.put("studentName", studentName);
        data.put("studentNumber", studentNumber);
        return data;
    }

    @Override
    public Map<String, Object> createCheckoutPendingData(Long activityId, String activityTitle, String studentName, String studentNumber, String checkoutReason) {
        Map<String, Object> data = new HashMap<>();
        data.put("activityId", activityId);
        data.put("activityTitle", activityTitle);
        data.put("studentName", studentName);
        data.put("studentNumber", studentNumber);
        data.put("checkoutReason", checkoutReason);
        return data;
    }

    @Override
    public Map<String, Object> createApprovalResultData(Long activityId, String activityTitle, boolean approved, String reason) {
        Map<String, Object> data = new HashMap<>();
        data.put("activityId", activityId);
        data.put("activityTitle", activityTitle);
        data.put("approved", approved);
        data.put("reason", reason);
        return data;
    }

    @Override
    public Map<String, Object> createActivityStatusData(Long activityId, String activityTitle, String activityTime, String activityLocation) {
        Map<String, Object> data = new HashMap<>();
        data.put("activityId", activityId);
        data.put("activityTitle", activityTitle);
        data.put("activityTime", activityTime);
        data.put("activityLocation", activityLocation);
        return data;
    }

    private String generateActivityStartContent(Map<String, Object> templateData) {
        String activityTitle = (String) templateData.get("activityTitle");
        String activityTime = (String) templateData.get("activityTime");
        String activityLocation = (String) templateData.get("activityLocation");

        StringBuilder content = new StringBuilder();
        content.append("您的活动《").append(activityTitle).append("》即将开始");
        
        if (StringUtils.hasText(activityTime)) {
            content.append("，开始时间：").append(activityTime);
        }
        
        if (StringUtils.hasText(activityLocation)) {
            content.append("，活动地点：").append(activityLocation);
        }
        
        content.append("。请做好准备，准时参加。");
        return content.toString();
    }

    private String generateActivityOngoingContent(Map<String, Object> templateData) {
        String activityTitle = (String) templateData.get("activityTitle");
        String activityLocation = (String) templateData.get("activityLocation");

        StringBuilder content = new StringBuilder();
        content.append("活动《").append(activityTitle).append("》已进入进行阶段");

        if (StringUtils.hasText(activityLocation)) {
            content.append("，地点：").append(activityLocation);
        }

        content.append("。请及时签到并积极参与。");
        return content.toString();
    }

    private String generateActivityEndContent(Map<String, Object> templateData) {
        String activityTitle = (String) templateData.get("activityTitle");
        return String.format("活动《%s》已结束，请及时完成相关后续工作。", activityTitle);
    }

    private String generateRegistrationPendingContent(Map<String, Object> templateData) {
        String studentName = (String) templateData.get("studentName");
        String studentNumber = (String) templateData.get("studentNumber");
        String activityTitle = (String) templateData.get("activityTitle");

        return String.format("学生 %s（学号：%s）申请报名活动《%s》，请及时审核。", 
                studentName, studentNumber, activityTitle);
    }

    private String generateRegistrationApprovedContent(Map<String, Object> templateData) {
        String activityTitle = (String) templateData.get("activityTitle");
        String activityTime = (String) templateData.get("activityTime");
        String activityLocation = (String) templateData.get("activityLocation");

        StringBuilder content = new StringBuilder();
        content.append("恭喜！您的报名申请已通过，活动《").append(activityTitle).append("》");
        
        if (StringUtils.hasText(activityTime)) {
            content.append("，活动时间：").append(activityTime);
        }
        
        if (StringUtils.hasText(activityLocation)) {
            content.append("，活动地点：").append(activityLocation);
        }
        
        content.append("。请准时参加。");
        return content.toString();
    }

    private String generateRegistrationRejectedContent(Map<String, Object> templateData) {
        String activityTitle = (String) templateData.get("activityTitle");
        String reason = (String) templateData.get("reason");

        StringBuilder content = new StringBuilder();
        content.append("很抱歉，您的报名申请《").append(activityTitle).append("》未通过审核");
        
        if (StringUtils.hasText(reason)) {
            content.append("，原因：").append(reason);
        }
        
        content.append("。");
        return content.toString();
    }

    private String generateCheckoutPendingContent(Map<String, Object> templateData) {
        String studentName = (String) templateData.get("studentName");
        String studentNumber = (String) templateData.get("studentNumber");
        String activityTitle = (String) templateData.get("activityTitle");
        String checkoutReason = (String) templateData.get("checkoutReason");

        StringBuilder content = new StringBuilder();
        content.append("学生 ").append(studentName).append("（学号：").append(studentNumber)
               .append("）申请从活动《").append(activityTitle).append("》签退");
        
        if (StringUtils.hasText(checkoutReason)) {
            content.append("，原因：").append(checkoutReason);
        }
        
        content.append("。请及时审核。");
        return content.toString();
    }

    private String generateCheckoutApprovedContent(Map<String, Object> templateData) {
        String activityTitle = (String) templateData.get("activityTitle");
        return String.format("您的签退申请已通过，活动《%s》签退成功。", activityTitle);
    }

    private String generateCheckoutRejectedContent(Map<String, Object> templateData) {
        String activityTitle = (String) templateData.get("activityTitle");
        String reason = (String) templateData.get("reason");

        StringBuilder content = new StringBuilder();
        content.append("很抱歉，您的签退申请《").append(activityTitle).append("》未通过审核");
        
        if (StringUtils.hasText(reason)) {
            content.append("，原因：").append(reason);
        }
        
        content.append("。");
        return content.toString();
    }

    @Override
    public String generateLinkUrlWithParams(NotificationType type, Map<String, Object> templateData, Map<String, String> additionalParams) {
        // 先生成基础URL
        String baseUrl = generateLinkUrl(type, templateData);
        if (baseUrl == null) {
            return null;
        }

        // 如果没有额外参数，直接返回基础URL
        if (additionalParams == null || additionalParams.isEmpty()) {
            return baseUrl;
        }

        // 构建带参数的URL
        return buildUrlWithParams(baseUrl, additionalParams);
    }

    @Override
    public String buildUrlWithParams(String baseUrl, Map<String, String> params) {
        if (baseUrl == null || params == null || params.isEmpty()) {
            return baseUrl;
        }

        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        
        // 检查URL是否已经包含查询参数
        boolean hasParams = baseUrl.contains("?");
        
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            if (StringUtils.hasText(key) && StringUtils.hasText(value)) {
                if (hasParams) {
                    urlBuilder.append("&");
                } else {
                    urlBuilder.append("?");
                    hasParams = true;
                }
                
                // URL编码参数值（简单处理，实际项目中可能需要更完善的编码）
                urlBuilder.append(key).append("=").append(value);
            }
        }
        
        return urlBuilder.toString();
    }
}
