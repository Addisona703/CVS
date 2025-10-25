package com.hngy.cvs.controller;

import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.common.result.Result;
import com.hngy.cvs.common.security.UserPrincipal;
import com.hngy.cvs.dto.request.ServiceRecordSearchDTO;
import com.hngy.cvs.dto.request.PageDTO;
import com.hngy.cvs.dto.response.ServiceRecordVO;
import com.hngy.cvs.dto.response.ServiceStatsVO;
import com.hngy.cvs.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 记录管理控制器
 * 
 * @author CVS Team
 */
@RestController
@RequestMapping("/api/service-records")
@Tag(name = "记录管理", description = "记录相关接口")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    // 1. 获取服务记录详情
    @GetMapping("/{id}")
    @Operation(summary = "获取服务记录详情")
    public Result<ServiceRecordVO> getServiceRecordById(
            @Parameter(description = "服务记录ID") @PathVariable Long id) {
        ServiceRecordVO serviceRecordVO = recordService.getServiceRecordById(id);
        return Result.success(serviceRecordVO);
    }

    // 2. 获取我的服务记录（学生端）
    @PostMapping("/my")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "获取我的服务记录")
    public Result<PageVO<ServiceRecordVO>> getMyServiceRecords(
            @Valid @RequestBody PageDTO<ServiceRecordSearchDTO> pageRequest,
            @AuthenticationPrincipal UserPrincipal principal) {
        PageVO<ServiceRecordVO> result = recordService.getMyServiceRecords(pageRequest, principal.getUserId());
        return Result.success(result);
    }

    // 3. 获取教师管理的所有服务记录
    @PostMapping("/managed")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "获取教师管理的服务记录")
    public Result<PageVO<ServiceRecordVO>> getManagedServiceRecords(
            @Valid @RequestBody PageDTO<ServiceRecordSearchDTO> pageRequest,
            @AuthenticationPrincipal UserPrincipal principal) {
        PageVO<ServiceRecordVO> result = recordService.getManagedServiceRecords(pageRequest, principal.getUserId());
        return Result.success(result);
    }

    // 4. 获取用户服务统计数据
    @GetMapping("/stats/{userId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    @Operation(summary = "获取用户服务统计数据")
    public Result<ServiceStatsVO> getUserServiceStats(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        ServiceStatsVO stats = recordService.getUserServiceStats(userId, principal.getUserId());
        return Result.success(stats);
    }

    // 5. 获取我的服务统计数据
    @GetMapping("/stats/my")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "获取我的服务统计数据")
    public Result<ServiceStatsVO> getMyServiceStats(
            @AuthenticationPrincipal UserPrincipal principal) {
        ServiceStatsVO stats = recordService.getUserServiceStats(principal.getUserId(), principal.getUserId());
        return Result.success(stats);
    }

    // 6. 获取所有服务记录（管理员端）
    @PostMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有服务记录", description = "管理员端获取所有服务记录，支持分页和搜索")
    public Result<PageVO<ServiceRecordVO>> getAllServiceRecords(
            @Valid @RequestBody PageDTO<ServiceRecordSearchDTO> pageRequest) {
        PageVO<ServiceRecordVO> result = recordService.getAllServiceRecords(pageRequest);
        return Result.success(result);
    }
}
