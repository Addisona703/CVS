package com.hngy.cvs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.common.result.Result;
import com.hngy.cvs.common.util.PageUtil;
import com.hngy.cvs.common.security.UserPrincipal;
import com.hngy.cvs.dto.request.SignInOutDTO;
import com.hngy.cvs.dto.request.SignupApprovalDTO;
import com.hngy.cvs.dto.request.SignupCreateDTO;
import com.hngy.cvs.dto.request.SignupSearchDTO;
import com.hngy.cvs.dto.request.PageDTO;
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
import jakarta.validation.constraints.Min;

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

    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "报名活动")
    public Result<SignupVO> signupActivity(
            @Valid @RequestBody SignupCreateDTO dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        SignupVO signupVO = signupService.signupActivity(dto, principal.getUserId());
        return Result.success("报名成功", signupVO);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取报名详情")
    public Result<SignupVO> getSignupById(@PathVariable Long id) {
        SignupVO signupVO = signupService.getSignupById(id);
        return Result.success(signupVO);
    }

    @GetMapping
    @Operation(summary = "分页查询报名列表", description = "支持按活动名称、学生名称模糊查询，按报名状态筛选")
    public Result<PageVO<SignupVO>> getSignupList(@Valid PageDTO<SignupSearchDTO> pageRequest) {
        PageVO<SignupVO> result = signupService.getSignupList(pageRequest);
        return Result.success(result);
    }

    
}
