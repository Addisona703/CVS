package com.hngy.cvs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.common.result.Result;
import com.hngy.cvs.common.util.PageUtil;
import com.hngy.cvs.common.security.UserPrincipal;
import com.hngy.cvs.dto.response.PointsRecordVO;
import com.hngy.cvs.service.PointsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 积分管理控制器
 * 
 * @author CVS Team
 */
@RestController
@RequestMapping("/api/points")
@Tag(name = "积分管理", description = "积分相关接口")
@RequiredArgsConstructor
public class PointsController {

    private final PointsService pointsService;

    @PostMapping("/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "手动调整用户积分")
    public Result<Void> adjustUserPoints(
            @Parameter(description = "用户ID") @RequestParam @NotNull Long userId,
            @Parameter(description = "积分数量（正数为增加，负数为扣除）") @RequestParam @NotNull Integer points) {
        pointsService.adjustUserPoints(userId, points);
        return Result.success("积分调整成功");
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "获取我的积分记录")
    public Result<PageVO<PointsRecordVO>> getMyPointsRecords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") @Min(1) Integer size,
            @AuthenticationPrincipal UserPrincipal principal) {
        IPage<PointsRecordVO> pointsPage = pointsService.getUserPointsRecords(principal.getUserId(), page, size);
        return Result.success(PageUtil.convert(pointsPage));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "获取用户的积分记录")
    public Result<PageVO<PointsRecordVO>> getUserPointsRecords(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        IPage<PointsRecordVO> pointsPage = pointsService.getUserPointsRecords(userId, page, size);
        return Result.success(PageUtil.convert(pointsPage));
    }

    @GetMapping("/total/{userId}")
    @Operation(summary = "获取用户总积分")
    public Result<Long> getUserTotalPoints(@Parameter(description = "用户ID") @PathVariable Long userId) {
        Long totalPoints = pointsService.getUserTotalPoints(userId);
        return Result.success(totalPoints);
    }

    @GetMapping("/ranking")
    @Operation(summary = "获取积分排行榜")
    public Result<List<PointsRecordVO>> getPointsRanking(
            @Parameter(description = "排行榜数量") @RequestParam(defaultValue = "10") @Min(1) Integer limit) {
        List<PointsRecordVO> ranking = pointsService.getPointsRanking(limit);
        return Result.success(ranking);
    }

    @GetMapping("/ranking/{userId}")
    @Operation(summary = "获取用户积分排名")
    public Result<Long> getUserPointsRanking(@Parameter(description = "用户ID") @PathVariable Long userId) {
        Long ranking = pointsService.getUserPointsRanking(userId);
        return Result.success(ranking);
    }

    @PostMapping("/award-activity")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "根据活动完成奖励积分")
    public Result<Void> awardPointsForActivity(
            @Parameter(description = "用户ID") @RequestParam @NotNull Long userId,
            @Parameter(description = "活动ID") @RequestParam @NotNull Long activityId) {
        pointsService.awardPointsForActivity(userId, activityId);
        return Result.success("奖励积分成功");
    }
}
