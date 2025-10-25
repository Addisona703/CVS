package com.hngy.cvs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hngy.cvs.common.result.Result;
import com.hngy.cvs.common.security.UserPrincipal;
import com.hngy.cvs.dto.request.CheckInRequest;
import com.hngy.cvs.dto.request.CheckOutRequest;
import com.hngy.cvs.dto.request.PageDTO;
import com.hngy.cvs.dto.request.ReviewSearchRequest;
import com.hngy.cvs.dto.request.SignupReviewRequest;
import com.hngy.cvs.dto.response.PendingSignStudentVO;
import com.hngy.cvs.dto.response.SignTokenResponse;
import com.hngy.cvs.dto.response.SignupReviewVO;
import com.hngy.cvs.service.CheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 二维码签到/签退控制器
 */
@RestController
@RequestMapping("/api")
@Tag(name = "签到签退", description = "二维码签到签退接口")
@RequiredArgsConstructor
public class CheckController {

    private final CheckService checkService;

    @PostMapping("/checkin/token/{activityId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "生成签到二维码token")
    public Result<SignTokenResponse> createCheckInToken(
            @PathVariable Long activityId,
            @AuthenticationPrincipal UserPrincipal principal) {
        SignTokenResponse response = checkService.createCheckInToken(activityId, principal.getUserId());
        return Result.success("生成签到二维码成功", response);
    }

    @PostMapping("/checkout/token/{activityId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "生成签退二维码token")
    public Result<SignTokenResponse> createCheckOutToken(
            @PathVariable Long activityId,
            @AuthenticationPrincipal UserPrincipal principal) {
        SignTokenResponse response = checkService.createCheckOutToken(activityId, principal.getUserId());
        return Result.success("生成签退二维码成功", response);
    }

    @PostMapping("/checkin")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "学生签到")
    public Result<Void> checkIn(
            @Valid @RequestBody CheckInRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        checkService.checkIn(principal.getUserId(), request);
        return Result.success("签到成功");
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "学生签退")
    public Result<Void> checkOut(
            @Valid @RequestBody CheckOutRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        checkService.checkOut(principal.getUserId(), request);
        return Result.success("签退成功");
    }

    @PostMapping("/review/search")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "查询待审核的签退列表")
    public Result<IPage<SignupReviewVO>> searchReviews(
            @Valid @RequestBody PageDTO<ReviewSearchRequest> request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ReviewSearchRequest params = request.getParams() != null ? request.getParams() : new ReviewSearchRequest();
        IPage<SignupReviewVO> page = checkService.searchReviews(
                params,
                principal.getUserId(),
                request.getPageNum(),
                request.getPageSize()
        );
        return Result.success("查询成功", page);
    }

    @PatchMapping("/review/{signupId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "教师调整评分与评价")
    public Result<Void> reviewSignup(
            @PathVariable Long signupId,
            @Valid @RequestBody SignupReviewRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        checkService.review(signupId, request, principal.getUserId());
        return Result.success("调整成功");
    }

    @GetMapping("/checkin/{activityId}/pending")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "查询未签到学生列表")
    public Result<List<PendingSignStudentVO>> listPendingCheckInStudents(
            @PathVariable Long activityId,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<PendingSignStudentVO> list = checkService.listPendingCheckInStudents(activityId, principal.getUserId());
        return Result.success("查询成功", list);
    }

    @GetMapping("/checkout/{activityId}/pending")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "查询未签退学生列表")
    public Result<List<PendingSignStudentVO>> listPendingCheckOutStudents(
            @PathVariable Long activityId,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<PendingSignStudentVO> list = checkService.listPendingCheckOutStudents(activityId, principal.getUserId());
        return Result.success("查询成功", list);
    }
}
