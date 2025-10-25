package com.hngy.cvs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.cvs.entity.Notification;
import com.hngy.cvs.dto.request.NotificationDTO;
import com.hngy.cvs.dto.request.NotificationQuery;
import com.hngy.cvs.dto.response.NotificationVO;

import java.util.List;

/**
 * 消息通知服务
 */
public interface NotificationService extends IService<Notification> {

    /**
     * 创建通知
     * @param dto 通知数据传输对象
     * @return 创建的通知ID
     */
    Long createNotification(NotificationDTO dto);

    /**
     * 获取用户未读通知数量
     * @param userId 用户ID
     * @return 未读通知数量
     */
    Long getUnreadCount(Long userId);

    /**
     * 获取通知列表
     * @param userId 用户ID
     * @param query 查询参数
     * @return 通知列表
     */
    IPage<NotificationVO> getNotificationList(Long userId, NotificationQuery query);

    /**
     * 标记通知为已读
     * @param notificationId 通知ID
     * @param userId 用户ID
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 标记所有通知为已读
     * @param userId 用户ID
     */
    void markAllAsRead(Long userId);

    /**
     * 删除通知
     * @param notificationId 通知ID
     * @param userId 用户ID
     */
    void deleteNotification(Long notificationId, Long userId);

    /**
     * 清空已读通知
     * @param userId 用户ID
     */
    void clearReadNotifications(Long userId);

    /**
     * 发送活动开始通知
     */
    void sendActivityStartNotification(Long activityId);

    /**
     * 发送活动进行中通知
     */
    void sendActivityOngoingNotification(Long activityId);

    /**
     * 发送活动结束通知
     */
    void sendActivityEndNotification(Long activityId);

    /**
     * 发送活动取消通知
     */
    void sendActivityCancelNotification(Long activityId);

    /**
     * 发送报名审核通知
     * @param activityId 活动ID
     * @param studentId 学生ID
     * @param teacherId 教师ID
     */
    void sendRegistrationPendingNotification(Long activityId, Long studentId, Long teacherId);

    /**
     * 发送报名审核结果通知
     * @param activityId 活动ID
     * @param studentId 学生ID
     * @param approved 是否通过
     * @param reason 拒绝原因
     */
    void sendRegistrationResultNotification(Long activityId, Long studentId, boolean approved, String reason);

    /**
     * 发送签退审核通知
     * @param activityId 活动ID
     * @param studentId 学生ID
     * @param teacherId 教师ID
     * @param checkoutReason 签退原因
     */
    void sendCheckoutPendingNotification(Long activityId, Long studentId, Long teacherId, String checkoutReason);

    /**
     * 发送签退审核结果通知
     * @param activityId 活动ID
     * @param studentId 学生ID
     * @param approved 是否通过
     * @param reason 拒绝原因
     */
    void sendCheckoutResultNotification(Long activityId, Long studentId, boolean approved, String reason);

    /**
     * 获取用户未读消息列表
     */
    List<NotificationVO> getUnreadNotifications(Long userId);

    /**
     * 获取用户消息列表（分页）
     */
    IPage<NotificationVO> getUserNotifications(Long userId, int pageNum, int pageSize, Boolean isRead);

    /**
     * 发送活动审核通知（通知管理员）
     * @param activityId 活动ID
     */
    void sendActivityApprovalNotification(Long activityId);

    /**
     * 发送活动审核结果通知（通知教师）
     * @param activityId 活动ID
     * @param approved 是否通过
     */
    void sendActivityApprovalResultNotification(Long activityId, Boolean approved);
}
