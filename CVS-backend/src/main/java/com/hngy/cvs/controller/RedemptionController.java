package com.hngy.cvs.controller;

import com.hngy.cvs.common.result.Result;
import com.hngy.cvs.common.security.UserPrincipal;
import com.hngy.cvs.dto.request.PageDTO;
import com.hngy.cvs.dto.request.RedemptionQueryRequest;
import com.hngy.cvs.dto.request.RedemptionRequest;
import com.hngy.cvs.dto.request.VerifyRedemptionRequest;
import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.dto.response.RedemptionVO;
import com.hngy.cvs.service.RedemptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 兑换管理控制器
 *
 * @author CVS Team
 */
@RestController
@RequestMapping("/api/redemptions")
@Tag(name = "兑换管理", description = "积分兑换相关接口")
@RequiredArgsConstructor
public class RedemptionController {

    private final RedemptionService redemptionService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "兑换商品")
    public Result<RedemptionVO> redeemProduct(
            @Valid @RequestBody RedemptionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        RedemptionVO redemption = redemptionService.redeemProduct(principal.getUserId(), request);
        return Result.success("兑换成功", redemption);
    }

    @PostMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "获取我的兑换记录")
    public Result<PageVO<RedemptionVO>> getMyRedemptions(
            @Valid @RequestBody PageDTO<RedemptionQueryRequest> request,
            @AuthenticationPrincipal UserPrincipal principal) {
        PageVO<RedemptionVO> redemptions = redemptionService.getUserRedemptions(principal.getUserId(), request);
        return Result.success(redemptions);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取兑换详情")
    public Result<RedemptionVO> getRedemptionDetail(
            @Parameter(description = "兑换记录ID") @PathVariable @NotNull Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        RedemptionVO redemption = redemptionService.getRedemptionDetail(id, principal.getUserId());
        return Result.success(redemption);
    }

    @PostMapping("/verify")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "核销兑换凭证")
    public Result<Void> verifyRedemption(
            @Valid @RequestBody VerifyRedemptionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        redemptionService.verifyRedemption(request.getVoucherCode(), principal.getUserId());
        return Result.success("核销成功");
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "取消兑换")
    public Result<Void> cancelRedemption(
            @Parameter(description = "兑换记录ID") @PathVariable @NotNull Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        redemptionService.cancelRedemption(id, principal.getUserId());
        return Result.success("取消兑换成功");
    }

    @PostMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有兑换记录（学工处）")
    public Result<PageVO<RedemptionVO>> getAllRedemptions(
            @Valid @RequestBody PageDTO<RedemptionQueryRequest> request) {
        PageVO<RedemptionVO> redemptions = redemptionService.getAllRedemptions(request);
        return Result.success(redemptions);
    }

    @GetMapping("/voucher/{voucherCode}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "根据凭证编号获取兑换记录")
    public Result<RedemptionVO> getRedemptionByVoucherCode(
            @Parameter(description = "凭证编号") @PathVariable String voucherCode) {
        RedemptionVO redemption = redemptionService.getRedemptionByVoucherCode(voucherCode);
        return Result.success(redemption);
    }

    @PostMapping("/by-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "根据状态获取兑换记录列表", description = "支持筛选今日核销、累计核销、待核销")
    public Result<PageVO<RedemptionVO>> getRedemptionsByStatus(
            @Valid @RequestBody PageDTO<RedemptionQueryRequest> request,
            @Parameter(description = "状态类型：TODAY(今日核销)、VERIFIED(累计核销)、PENDING(待核销)")
            @RequestParam(required = false) String statusType) {
        PageVO<RedemptionVO> redemptions = redemptionService.getRedemptionsByStatus(request, statusType);
        return Result.success(redemptions);
    }
}