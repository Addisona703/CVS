package com.hngy.cvs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.cvs.entity.Notification;
import com.hngy.cvs.entity.enums.NotificationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 通知Mapper接口
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 获取用户未读通知数量
     * @param userId 用户ID
     * @return 未读通知数量
     */
    Long countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 分页查询用户通知列表
     * @param page 分页参数
     * @param userId 用户ID
     * @param type 通知类型（可选）
     * @param isRead 是否已读（可选）
     * @return 通知列表
     */
    IPage<Notification> selectNotificationPage(
            Page<Notification> page,
            @Param("userId") Long userId,
            @Param("type") NotificationType type,
            @Param("isRead") Boolean isRead
    );

    /**
     * 批量标记用户所有通知为已读
     * @param userId 用户ID
     * @return 更新数量
     */
    int markAllAsReadByUserId(@Param("userId") Long userId);

    /**
     * 删除用户所有已读通知
     * @param userId 用户ID
     * @return 删除数量
     */
    int deleteReadNotificationsByUserId(@Param("userId") Long userId);

    /**
     * 获取用户各类型通知的未读数量统计
     * @param userId 用户ID
     * @return 各类型未读数量统计
     */
    java.util.List<java.util.Map<String, Object>> countUnreadByTypeAndUserId(@Param("userId") Long userId);

    /**
     * 批量标记指定通知为已读
     * @param notificationIds 通知ID列表
     * @param userId 用户ID
     * @return 更新数量
     */
    int batchMarkAsRead(@Param("notificationIds") java.util.List<Long> notificationIds, @Param("userId") Long userId);

    /**
     * 高级筛选查询通知列表（支持时间范围、多类型等复杂条件）
     * @param page 分页参数
     * @param userId 用户ID
     * @param types 通知类型列表（可选）
     * @param isRead 是否已读（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 通知列表
     */
    IPage<Notification> selectNotificationPageAdvanced(
            Page<Notification> page,
            @Param("userId") Long userId,
            @Param("types") java.util.List<NotificationType> types,
            @Param("isRead") Boolean isRead,
            @Param("startTime") java.time.LocalDateTime startTime,
            @Param("endTime") java.time.LocalDateTime endTime
    );
}
