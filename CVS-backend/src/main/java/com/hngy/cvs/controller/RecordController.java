package com.hngy.cvs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.common.result.Result;
import com.hngy.cvs.common.util.PageUtil;
import com.hngy.cvs.common.security.UserPrincipal;
import com.hngy.cvs.dto.request.ServiceRecordCreateDTO;
import com.hngy.cvs.dto.response.ServiceRecordVO;
import com.hngy.cvs.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

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

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "创建服务记录")
    public Result<ServiceRecordVO> createServiceRecord(@Valid @RequestBody ServiceRecordCreateDTO dto) {
        ServiceRecordVO serviceRecordVO = recordService.createServiceRecord(dto);
        return Result.success("创建服务记录成功", serviceRecordVO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "更新服务记录")
    public Result<ServiceRecordVO> updateServiceRecord(
            @Parameter(description = "服务记录ID") @PathVariable Long id,
            @Valid @RequestBody ServiceRecordCreateDTO dto) {
        ServiceRecordVO serviceRecordVO = recordService.updateServiceRecord(id, dto);
        return Result.success("更新服务记录成功", serviceRecordVO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "删除服务记录")
    public Result<Void> deleteServiceRecord(@Parameter(description = "服务记录ID") @PathVariable Long id) {
        recordService.deleteServiceRecord(id);
        return Result.success("删除服务记录成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取服务记录详情")
    public Result<ServiceRecordVO> getServiceRecordById(@Parameter(description = "服务记录ID") @PathVariable Long id) {
        ServiceRecordVO serviceRecordVO = recordService.getServiceRecordById(id);
        return Result.success(serviceRecordVO);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    @Operation(summary = "获取我的服务记录")
    public Result<PageVO<ServiceRecordVO>> getMyServiceRecords(
            @RequestParam(defaultValue = "1") @Min(1) @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "10") @Min(1) @Parameter(description = "每页大小") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        IPage<ServiceRecordVO> result = recordService.getUserServiceRecords(principal.getUserId(), page, size);
        return Result.success(PageUtil.convert(result));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "获取用户的服务记录")
    public Result<PageVO<ServiceRecordVO>> getUserServiceRecords(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @RequestParam(defaultValue = "1") @Min(1) @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "10") @Min(1) @Parameter(description = "每页大小") int size) {
        IPage<ServiceRecordVO> result = recordService.getUserServiceRecords(userId, page, size);
        return Result.success(PageUtil.convert(result));
    }

    @GetMapping("/activity/{activityId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "获取活动的服务记录")
    public Result<PageVO<ServiceRecordVO>> getActivityServiceRecords(
            @Parameter(description = "活动ID") @PathVariable Long activityId,
            @RequestParam(defaultValue = "1") @Min(1) @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "10") @Min(1) @Parameter(description = "每页大小") int size) {
        IPage<ServiceRecordVO> result = recordService.getActivityServiceRecords(activityId, page, size);
        return Result.success(PageUtil.convert(result));
    }

    @GetMapping("/stats/hours/{userId}")
    @Operation(summary = "获取用户总服务时长")
    public Result<Double> getUserTotalServiceHours(@Parameter(description = "用户ID") @PathVariable Long userId) {
        Double totalHours = recordService.getUserTotalServiceHours(userId);
        return Result.success(totalHours);
    }

    @PostMapping("/generate/{signupId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "根据报名记录生成服务记录")
    public Result<Void> generateServiceRecord(@Parameter(description = "报名ID") @PathVariable Long signupId) {
        recordService.generateServiceRecordFromSignup(signupId);
        return Result.success("生成服务记录成功");
    }
}
