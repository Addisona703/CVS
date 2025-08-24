package com.hngy.cvs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hngy.cvs.dto.response.PageVO;
import com.hngy.cvs.common.result.Result;
import com.hngy.cvs.common.util.PageUtil;
import com.hngy.cvs.common.security.UserPrincipal;
import com.hngy.cvs.dto.response.UserVO;
import com.hngy.cvs.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;

/**
 * 用户管理控制器
 * 
 * @author CVS Team
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户信息管理相关接口")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #userPrincipal.userId == #id")
    @Operation(summary = "获取用户信息")
    public Result<UserVO> getUserById(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserVO userVO = userService.getUserById(id);
        return Result.success(userVO);
    }

//    @GetMapping
//    @PreAuthorize("hasRole('ADMIN')")
//    @Operation(summary = "分页查询用户列表")
//    public Result<PageVO<UserVO>> getUserList(
//            @Parameter(description = "页码") @RequestParam(defaultValue = "1") @Min(1) Integer page,
//            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") @Min(1) Integer size) {
//        IPage<UserVO> userPage = userService.getUserList(page, size);
//        return Result.success(PageUtil.convert(userPage));
//    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除用户")
    public Result<Void> deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除成功");
    }
}
