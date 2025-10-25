package com.hngy.cvs.integration;

import com.hngy.cvs.dto.NotificationDTO;
import com.hngy.cvs.entity.Activity;
import com.hngy.cvs.entity.Registration;
import com.hngy.cvs.enums.NotificationType;
import com.hngy.cvs.enums.RegistrationStatus;
import com.hngy.cvs.mapper.ActivityMapper;
import com.hngy.cvs.mapper.NotificationMapper;
import com.hngy.cvs.mapper.RegistrationMapper;
import com.hngy.cvs.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 通知系统集成测试
 * 测试通知创建到查看的完整流程
 */
@DisplayName("通知系统集成测试")
public class NotificationSystemIntegrationTest extends NotificationIntegrationTestBase {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Test
    @DisplayName("测试完整的通知创建到查看流程")
    void testCompleteNotificationFlow() throws Exception {
        // 1. 创建通知
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setUserId(TEACHER1_ID);
        notificationDTO.setType(NotificationType.REGISTRATION_PENDING);
        notificationDTO.setTitle("新的报名申请");
        notificationDTO.setContent("学生张三申请参加活动");
        notificationDTO.setLinkUrl("/activities/1/signups");

        notificationService.createNotification(notificationDTO);

        // 2. 验证通知已创建
        int unreadCount = notificationService.getUnreadCount(TEACHER1_ID);
        assertTrue(unreadCount > 0, "应该有未读通知");

        // 3. 通过API获取未读数量
        mockMvc.perform(authenticatedGet("/api/notifications/unread-count", teacher1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(greaterThan(0)));

        // 4. 获取通知列表
        MvcResult result = mockMvc.perform(authenticatedGet("/api/notifications?page=1&size=10", teacher1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records", hasSize(greaterThan(0))))
                .andReturn();

        // 5. 验证通知内容
        mockMvc.perform(authenticatedGet("/api/notifications?page=1&size=10", teacher1Token))
                .andExpect(jsonPath("$.data.records[?(@.title == '新的报名申请')]").exists())
                .andExpect(jsonPath("$.data.records[?(@.content == '学生张三申请参加活动')]").exists());
    }

    @Test
    @DisplayName("测试报名审核通知流程")
    void testRegistrationNotificationFlow() throws Exception {
        // 1. 模拟学生报名
        Registration registration = new Registration();
        registration.setActivityId(1L);
        registration.setUserId(STUDENT3_ID);
        registration.setStatus(RegistrationStatus.PENDING);
        registration.setApplicationReason("我想参加这个活动");
        registrationMapper.insert(registration);

        // 2. 创建报名待审核通知
        NotificationDTO pendingNotification = new NotificationDTO();
        pendingNotification.setUserId(TEACHER1_ID);
        pendingNotification.setType(NotificationType.REGISTRATION_PENDING);
        pendingNotification.setTitle("新的报名申请");
        pendingNotification.setContent("学生刘同学申请参加\"环保志愿活动\"");
        pendingNotification.setLinkUrl("/activities/1/signups");
        notificationService.createNotification(pendingNotification);

        // 3. 验证教师收到通知
        mockMvc.perform(authenticatedGet("/api/notifications/unread-count", teacher1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(greaterThan(0)));

        // 4. 模拟审核通过
        registration.setStatus(RegistrationStatus.APPROVED);
        registration.setReviewedBy(TEACHER1_ID);
        registration.setReviewedAt(LocalDateTime.now());
        registrationMapper.updateById(registration);

        // 5. 创建审核结果通知
        NotificationDTO approvedNotification = new NotificationDTO();
        approvedNotification.setUserId(STUDENT3_ID);
        approvedNotification.setType(NotificationType.REGISTRATION_APPROVED);
        approvedNotification.setTitle("报名审核通过");
        approvedNotification.setContent("您的\"环保志愿活动\"报名申请已通过");
        approvedNotification.setLinkUrl("/activities/1");
        notificationService.createNotification(approvedNotification);

        // 6. 验证学生收到审核结果通知
        mockMvc.perform(authenticatedGet("/api/notifications", student3Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[?(@.type == 'REGISTRATION_APPROVED')]").exists());
    }

    @Test
    @DisplayName("测试通知已读/未读状态管理")
    void testNotificationReadStatusManagement() throws Exception {
        // 1. 获取初始未读数量
        MvcResult initialResult = mockMvc.perform(authenticatedGet("/api/notifications/unread-count", student1Token))
                .andExpect(status().isOk())
                .andReturn();

        // 2. 获取通知列表，找到一个未读通知
        MvcResult listResult = mockMvc.perform(authenticatedGet("/api/notifications", student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[?(@.isRead == false)]").exists())
                .andReturn();

        // 3. 标记第一个未读通知为已读（使用测试数据中的通知ID 4）
        mockMvc.perform(authenticatedPut("/api/notifications/4/read", student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 4. 验证未读数量减少
        mockMvc.perform(authenticatedGet("/api/notifications/unread-count", student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(lessThan(2))); // 应该只剩1个或0个未读

        // 5. 测试全部标记为已读
        mockMvc.perform(authenticatedPut("/api/notifications/mark-all-read", student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 6. 验证未读数量为0
        mockMvc.perform(authenticatedGet("/api/notifications/unread-count", student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(0));
    }

    @Test
    @DisplayName("测试通知删除功能")
    void testNotificationDeletion() throws Exception {
        // 1. 获取初始通知数量
        MvcResult initialResult = mockMvc.perform(authenticatedGet("/api/notifications", teacher1Token))
                .andExpect(status().isOk())
                .andReturn();

        // 2. 删除一个通知（使用测试数据中的通知ID 3）
        mockMvc.perform(authenticatedDelete("/api/notifications/3", teacher1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 3. 验证通知已被删除
        mockMvc.perform(authenticatedGet("/api/notifications", teacher1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[?(@.id == 3)]").doesNotExist());

        // 4. 先标记一些通知为已读
        mockMvc.perform(authenticatedPut("/api/notifications/1/read", teacher1Token))
                .andExpect(status().isOk());

        // 5. 清空已读通知
        mockMvc.perform(authenticatedDelete("/api/notifications/clear-read", teacher1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 6. 验证已读通知被清空，只剩未读通知
        mockMvc.perform(authenticatedGet("/api/notifications", teacher1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[?(@.isRead == true)]").doesNotExist());
    }

    @Test
    @DisplayName("测试通知筛选功能")
    void testNotificationFiltering() throws Exception {
        // 1. 按类型筛选 - 只获取报名相关通知
        mockMvc.perform(authenticatedGet("/api/notifications?type=REGISTRATION_PENDING", teacher1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[*].type").value(everyItem(equalTo("REGISTRATION_PENDING"))));

        // 2. 按已读状态筛选 - 只获取未读通知
        mockMvc.perform(authenticatedGet("/api/notifications?isRead=false", student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[*].isRead").value(everyItem(equalTo(false))));

        // 3. 按已读状态筛选 - 只获取已读通知
        mockMvc.perform(authenticatedGet("/api/notifications?isRead=true", student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[*].isRead").value(everyItem(equalTo(true))));

        // 4. 组合筛选 - 获取未读的报名通知
        mockMvc.perform(authenticatedGet("/api/notifications?type=REGISTRATION_PENDING&isRead=false", teacher1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[*].type").value(everyItem(equalTo("REGISTRATION_PENDING"))))
                .andExpect(jsonPath("$.data.records[*].isRead").value(everyItem(equalTo(false))));
    }

    @Test
    @DisplayName("测试通知权限控制")
    void testNotificationPermissionControl() throws Exception {
        // 1. 尝试访问其他用户的通知 - 应该返回空列表或无权限
        mockMvc.perform(authenticatedGet("/api/notifications", student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[?(@.userId != " + STUDENT1_ID + ")]").doesNotExist());

        // 2. 尝试标记其他用户的通知为已读 - 应该失败
        mockMvc.perform(authenticatedPut("/api/notifications/1/read", student1Token))
                .andExpect(status().isForbidden());

        // 3. 尝试删除其他用户的通知 - 应该失败
        mockMvc.perform(authenticatedDelete("/api/notifications/1", student1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("测试通知跳转链接功能")
    void testNotificationLinkGeneration() throws Exception {
        // 1. 创建带跳转链接的通知
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setUserId(STUDENT1_ID);
        notificationDTO.setType(NotificationType.REGISTRATION_APPROVED);
        notificationDTO.setTitle("报名审核通过");
        notificationDTO.setContent("您的活动报名已通过");
        notificationDTO.setLinkUrl("/activities/1");
        notificationService.createNotification(notificationDTO);

        // 2. 验证通知包含正确的跳转链接
        mockMvc.perform(authenticatedGet("/api/notifications", student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[?(@.title == '报名审核通过')].linkUrl").value("/activities/1"));

        // 3. 创建审核类通知，验证跳转到审核页面
        NotificationDTO auditNotification = new NotificationDTO();
        auditNotification.setUserId(TEACHER1_ID);
        auditNotification.setType(NotificationType.CHECKOUT_PENDING);
        auditNotification.setTitle("签退审核申请");
        auditNotification.setContent("有新的签退申请需要审核");
        auditNotification.setLinkUrl("/activities/1/checkouts");
        notificationService.createNotification(auditNotification);

        // 4. 验证审核通知的跳转链接
        mockMvc.perform(authenticatedGet("/api/notifications", teacher1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[?(@.title == '签退审核申请')].linkUrl").value("/activities/1/checkouts"));
    }
}