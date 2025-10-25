package com.hngy.cvs.controller;

import com.hngy.cvs.dto.response.ActivityStatisticsVO;
import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.common.result.Result;
import com.hngy.cvs.common.security.UserPrincipal;
import com.hngy.cvs.dto.request.ActivityCreateDTO;
import com.hngy.cvs.dto.request.ActivitySearchDTO;
import com.hngy.cvs.dto.request.ActivityUpdateDTO;
import com.hngy.cvs.dto.request.PageDTO;
import com.hngy.cvs.dto.response.ActivityVO;
import com.hngy.cvs.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 活动管理控制器
 * 
 * @author CVS Team
 */
@RestController
@RequestMapping("/api/activities")
@Tag(name = "活动管理", description = "活动相关接口")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "创建活动")
    public Result<ActivityVO> createActivity(
            @Valid @RequestBody ActivityCreateDTO dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        ActivityVO activityVO = activityService.createActivity(dto, principal.getUserId());
        return Result.success("创建活动成功", activityVO);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "更新活动")
    public Result<ActivityVO> updateActivity(
            @Valid @RequestBody ActivityUpdateDTO dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        ActivityVO activityVO = activityService.updateActivity(dto, principal.getUserId());
        return Result.success("更新活动成功", activityVO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "删除活动")
    public Result<Void> deleteActivity(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        activityService.deleteActivity(id, principal.getUserId());
        return Result.success("删除活动成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取活动详情")
    public Result<ActivityVO> getActivityById(@PathVariable Long id) {
        ActivityVO activityVO = activityService.getActivityById(id);
        return Result.success(activityVO);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "发布活动")
    public Result<Void> publishActivity(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        activityService.publishActivity(id, principal.getUserId());
        
        // 根据用户角色返回不同的消息
        boolean isAdmin = "ADMIN".equals(principal.getRole());
        String message = isAdmin ? "发布成功" : "已提交审核，请等待管理员审核";
        
        return Result.success(message);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "审核活动")
    public Result<Void> approveActivity(
            @PathVariable Long id,
            @RequestParam Boolean approved,
            @RequestParam(required = false) String rejectReason,
            @AuthenticationPrincipal UserPrincipal principal) {
        activityService.approveActivity(id, approved, rejectReason, principal.getUserId());
        return Result.success(approved ? "审核通过" : "已拒绝");
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "取消活动")
    public Result<Void> cancelActivity(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        activityService.cancelActivity(id, principal.getUserId());
        return Result.success("取消活动成功");
    }

    @PostMapping("/search")
    @Operation(summary = "分页查询活动列表", description = "支持按标题、创建者名字模糊查询，按活动状态筛选")
    public Result<PageVO<ActivityVO>> getActivityList(@Valid @RequestBody PageDTO<ActivitySearchDTO> pageRequest) {
        PageVO<ActivityVO> result = activityService.getActivityList(pageRequest);
        return Result.success(result);
    }

    @PostMapping("/my/search")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "获取我创建的活动列表")
    public Result<PageVO<ActivityVO>> getMyActivities(
            @Valid @RequestBody PageDTO<ActivitySearchDTO> pageRequest,
            @AuthenticationPrincipal UserPrincipal principal) {
        PageVO<ActivityVO> result = activityService.getMyActivitiesList(pageRequest, principal.getUserId());
        return Result.success(result);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "获取活动统计数据", description = "获取指定天数内的每日活动数和参与人数统计")
    public Result<ActivityStatisticsVO> getActivityStatistics(
            @RequestParam(name = "days", defaultValue = "7") Integer days) {
        ActivityStatisticsVO statistics = activityService.getActivityStatistics(days);
        return Result.success(statistics);
    }
}
