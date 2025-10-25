package com.hngy.cvs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hngy.cvs.common.exception.BusinessException;
import com.hngy.cvs.common.exception.NotificationServiceException;
import com.hngy.cvs.common.result.Result;
import com.hngy.cvs.common.result.ResultCode;
import com.hngy.cvs.common.security.UserPrincipal;
import com.hngy.cvs.dto.request.NotificationQuery;
import com.hngy.cvs.dto.response.NotificationVO;
import com.hngy.cvs.entity.enums.NotificationType;
import com.hngy.cvs.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 消息通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "消息通知", description = "消息通知相关接口")
@RequiredArgsConstructor
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    @Operation(summary = "获取未读通知数量", description = "获取当前用户的未读通知数量")
    public Result<Long> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {
        validateUserPrincipal(principal);
        
        try {
            Long count = notificationService.getUnreadCount(principal.getUserId());
            return Result.success("查询成功", count);
        } catch (Exception e) {
            log.error("获取未读通知数量失败, userId: {}", principal.getUserId(), e);
            throw new NotificationServiceException("获取未读通知数量失败", e);
        }
    }

    /**
     * 获取通知列表（支持分页和筛选）
     */
    @GetMapping
    @Operation(summary = "获取通知列表", description = "获取用户通知列表，支持分页和按类型、状态筛选")
    public Result<IPage<NotificationVO>> getNotifications(
            @Parameter(description = "页码，从1开始") 
            @RequestParam(defaultValue = "1") 
            @Min(value = 1, message = "页码必须大于0") 
            @Max(value = 1000, message = "页码不能超过1000") 
            Integer pageNum,
            @Parameter(description = "每页大小") 
            @RequestParam(defaultValue = "10") 
            @Min(value = 1, message = "每页大小必须大于0") 
            @Max(value = 100, message = "每页大小不能超过100") 
            Integer pageSize,
            @Parameter(description = "通知类型") @RequestParam(required = false) NotificationType type,
            @Parameter(description = "是否已读") @RequestParam(required = false) Boolean isRead,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        validateUserPrincipal(principal);

        try {
            // 构建查询参数
            NotificationQuery query = new NotificationQuery();
            query.setPageNum(pageNum);
            query.setPageSize(pageSize);
            query.setType(type);
            query.setIsRead(isRead);

            IPage<NotificationVO> page = notificationService.getNotificationList(principal.getUserId(), query);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            log.error("获取通知列表失败, userId: {}, pageNum: {}, pageSize: {}", 
                     principal.getUserId(), pageNum, pageSize, e);
            throw new NotificationServiceException("获取通知列表失败", e);
        }
    }

    /**
     * 标记单个通知为已读
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "标记通知已读", description = "将指定通知标记为已读状态")
    public Result<Void> markAsRead(
            @Parameter(description = "通知ID") 
            @PathVariable 
            @NotNull(message = "通知ID不能为空") 
            @Min(value = 1, message = "通知ID必须大于0") 
            Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        validateUserPrincipal(principal);
        validateNotificationId(id);

        try {
            notificationService.markAsRead(id, principal.getUserId());
            return Result.success("标记成功");
        } catch (Exception e) {
            log.error("标记通知已读失败, notificationId: {}, userId: {}", id, principal.getUserId(), e);
            throw new NotificationServiceException("标记通知已读失败", e);
        }
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/mark-all-read")
    @Operation(summary = "标记所有通知已读", description = "将当前用户的所有未读通知标记为已读")
    public Result<Void> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal principal) {
        
        validateUserPrincipal(principal);

        try {
            notificationService.markAllAsRead(principal.getUserId());
            return Result.success("标记成功");
        } catch (Exception e) {
            log.error("标记所有通知已读失败, userId: {}", principal.getUserId(), e);
            throw new NotificationServiceException("标记所有通知已读失败", e);
        }
    }

    /**
     * 删除单个通知
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知", description = "删除指定的通知消息")
    public Result<Void> deleteNotification(
            @Parameter(description = "通知ID") 
            @PathVariable 
            @NotNull(message = "通知ID不能为空") 
            @Min(value = 1, message = "通知ID必须大于0") 
            Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        validateUserPrincipal(principal);
        validateNotificationId(id);

        try {
            notificationService.deleteNotification(id, principal.getUserId());
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除通知失败, notificationId: {}, userId: {}", id, principal.getUserId(), e);
            throw new NotificationServiceException("删除通知失败", e);
        }
    }

    /**
     * 清空已读通知
     */
    @DeleteMapping("/clear-read")
    @Operation(summary = "清空已读通知", description = "删除当前用户所有已读的通知消息")
    public Result<Void> clearReadNotifications(
            @AuthenticationPrincipal UserPrincipal principal) {
        
        validateUserPrincipal(principal);

        try {
            notificationService.clearReadNotifications(principal.getUserId());
            return Result.success("清空成功");
        } catch (Exception e) {
            log.error("清空已读通知失败, userId: {}", principal.getUserId(), e);
            throw new NotificationServiceException("清空已读通知失败", e);
        }
    }

    // 保留原有的接口以保持向后兼容性
    
    /**
     * 获取未读消息列表
     */
    @GetMapping("/unread")
    @Operation(summary = "获取未读消息列表", description = "获取当前用户的未读通知列表")
    public Result<List<NotificationVO>> getUnreadNotifications(
            @AuthenticationPrincipal UserPrincipal principal) {
        
        validateUserPrincipal(principal);
        
        try {
            List<NotificationVO> notifications = notificationService.getUnreadNotifications(principal.getUserId());
            return Result.success("查询成功", notifications);
        } catch (Exception e) {
            log.error("获取未读通知列表失败, userId: {}", principal.getUserId(), e);
            throw new NotificationServiceException("获取未读通知列表失败", e);
        }
    }

    /**
     * 验证用户身份
     */
    private void validateUserPrincipal(UserPrincipal principal) {
        if (principal == null || principal.getUserId() == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }
    }

    /**
     * 验证通知ID
     */
    private void validateNotificationId(Long notificationId) {
        if (notificationId == null || notificationId <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "通知ID无效");
        }
    }
}
