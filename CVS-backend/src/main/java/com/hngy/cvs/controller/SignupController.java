package com.hngy.cvs.controller;

import com.hngy.cvs.common.result.Result;
import com.hngy.cvs.common.security.UserPrincipal;
import com.hngy.cvs.dto.request.PageDTO;
import com.hngy.cvs.dto.request.SignupApprovalDTO;
import com.hngy.cvs.dto.request.SignupCreateDTO;
import com.hngy.cvs.dto.request.SignupSearchDTO;
import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.dto.response.SignupVO;
import com.hngy.cvs.service.SignupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 报名管理控制器
 * 
 * @author CVS Team
 */
@RestController
@RequestMapping("/api/signups")
@Tag(name = "报名管理", description = "报名相关接口")
@RequiredArgsConstructor
public class SignupController {

    private final SignupService signupService;

    // 1. 学生报名活动
    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "报名活动")
    public Result<SignupVO> signupActivity(
            @Valid @RequestBody SignupCreateDTO dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        SignupVO signupVO = signupService.signupActivity(dto, principal.getUserId());
        return Result.success("报名成功", signupVO);
    }

    // 2.获取报名详情
    @GetMapping("/{id}")
    @Operation(summary = "获取报名详情")
    public Result<SignupVO> getSignupById(@PathVariable Long id) {
        SignupVO signupVO = signupService.getSignupById(id);
        return Result.success(signupVO);
    }

    // 3.老师分页联表自己发布活动的报名列表
    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "分页查询报名列表", description = "支持按活动名称、学生名称模糊查询，按报名状态筛选")
    public Result<PageVO<SignupVO>> getSignupList(
            @Valid @RequestBody PageDTO<SignupSearchDTO> pageRequest,
            @AuthenticationPrincipal UserPrincipal principal) {
        PageVO<SignupVO> result = signupService.getSignupList(pageRequest, principal.getUserId());
        return Result.success(result);
    }

    // 4. 学生查询自己报名的活动列表
    @PostMapping("/my")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "获取我的报名列表")
    public Result<PageVO<SignupVO>> getMySignups(
            @Valid @RequestBody PageDTO<SignupSearchDTO> pageRequest,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        PageVO<SignupVO> result = signupService.getMySignups(pageRequest, principal.getUserId());
        return Result.success(result);
    }

    // 5. 老师审核报名（通过/拒绝（需写明拒绝理由））可批量操作
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "审核通过报名")
    public Result<Void> approveSignup(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        signupService.approveSignup(id, principal.getUserId());
        return Result.success("审核通过成功");
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "拒绝报名")
    public Result<Void> rejectSignup(
            @PathVariable Long id,
            @Valid @RequestBody SignupApprovalDTO dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        signupService.rejectSignup(id, dto.getRejectReason(), principal.getUserId());
        return Result.success("拒绝报名成功");
    }

    @PutMapping("/batch/approve")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "批量审核通过报名")
    public Result<Void> batchApproveSignups(
            @RequestBody @Parameter(description = "报名ID列表") java.util.List<Long> signupIds,
            @AuthenticationPrincipal UserPrincipal principal) {
        signupService.batchApproveSignups(signupIds, principal.getUserId());
        return Result.success("批量审核通过成功");
    }

    @PutMapping("/batch/reject")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "批量拒绝报名")
    public Result<Void> batchRejectSignups(
            @Valid @RequestBody SignupApprovalDTO dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        signupService.batchRejectSignups(dto.getSignupIds(), dto.getRejectReason(), principal.getUserId());
        return Result.success("批量拒绝报名成功");
    }

    // 6. 获取已签到但未签退的人员列表（教师仪表盘用）
    @PostMapping("/checked-in")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "获取已签到但未签退的人员列表")
    public Result<PageVO<SignupVO>> getCheckedInSignups(
            @Valid @RequestBody PageDTO<SignupSearchDTO> pageRequest,
            @AuthenticationPrincipal UserPrincipal principal) {
        PageVO<SignupVO> result = signupService.getCheckedInSignups(pageRequest, principal.getUserId());
        return Result.success(result);
    }

    // 7. 学生取消报名
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "取消报名")
    public Result<Void> cancelSignup(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        signupService.cancelSignup(id, principal.getUserId());
        return Result.success("取消报名成功");
    }

    // 8. 获取指定活动的报名列表
    @PostMapping("/activity/{activityId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "获取指定活动的报名列表", description = "支持按状态、学生姓名、签到签退状态筛选")
    public Result<PageVO<SignupVO>> getActivitySignups(
            @PathVariable Long activityId,
            @Valid @RequestBody PageDTO<SignupSearchDTO> pageRequest) {
        PageVO<SignupVO> result = signupService.getActivitySignups(activityId, pageRequest);
        return Result.success(result);
    }
}
