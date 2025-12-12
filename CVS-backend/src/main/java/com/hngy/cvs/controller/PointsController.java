package com.hngy.cvs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hngy.cvs.dto.request.PageDTO;
import com.hngy.cvs.dto.request.PointsSearchDTO;
import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.common.result.Result;
import com.hngy.cvs.common.util.PageUtil;
import com.hngy.cvs.common.security.UserPrincipal;
import com.hngy.cvs.dto.response.PointsRecordVO;
import com.hngy.cvs.dto.response.PointsStatsVO;
import com.hngy.cvs.dto.response.PointsRankingVO;
import com.hngy.cvs.service.PointsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
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

    @GetMapping("/stats/my")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "获取当前用户积分统计信息")
    public Result<PointsStatsVO> getCurrentUserPointsStats(
            @AuthenticationPrincipal UserPrincipal principal) {
        PointsStatsVO stats = pointsService.getCurrentUserPointsStats(principal.getUserId());
        return Result.success(stats);
    }

    @GetMapping("/ranking/page")
    @Operation(summary = "获取积分排行榜（分页显示）")
    public Result<PageVO<PointsRankingVO>> getPointsRankingPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        PageVO<PointsRankingVO> ranking = pointsService.getPointsRankingPage(page, size);
        return Result.success(ranking);
    }

    @PostMapping("/records/all")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "获取所有用户的积分记录（主要体现服务记录积分）")
    public Result<PageVO<PointsRecordVO>> getAllPointsRecords(
            @Valid @RequestBody PageDTO<PointsSearchDTO> pageRequest) {
        PageVO<PointsRecordVO> records = pointsService.getAllPointsRecords(pageRequest);
        return Result.success(records);
    }

    @PostMapping("/award")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "发放积分")
    public Result<Void> awardPoints(
            @Parameter(description = "用户ID") @RequestParam @NotNull Long userId,
            @Parameter(description = "积分数量") @RequestParam @NotNull Integer points) {
        pointsService.awardPoints(userId, points);
        return Result.success();
    }
}
