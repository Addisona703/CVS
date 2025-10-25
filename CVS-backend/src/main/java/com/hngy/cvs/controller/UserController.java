package com.hngy.cvs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hngy.cvs.dto.request.PageDTO;
import com.hngy.cvs.dto.request.UserSearchDTO;
import com.hngy.cvs.dto.request.UserUpdateDTO;
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

import jakarta.validation.Valid;
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

    // 1.获取当前登录的用户信息
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #userPrincipal.userId == #id")
    @Operation(summary = "获取用户信息")
    public Result<UserVO> getUserById(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserVO userVO = userService.getUserById(id);
        return Result.success(userVO);
    }

    // 2. 所有用户的列表（管理员端），不包含管理员账户，可以筛选老师或学生
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "分页查询用户列表", description = "管理员端用户管理，支持按用户名、姓名、角色等条件筛选，不包含管理员账户")
    public Result<PageVO<UserVO>> getUserList(@Valid @RequestBody PageDTO<UserSearchDTO> pageRequest) {
        PageVO<UserVO> result = userService.getUserList(pageRequest);
        return Result.success(result);
    }

    // 3. 修改个人资料
    @PutMapping
    @PreAuthorize("hasRole('ADMIN') or #userPrincipal.userId == #request.id")
    @Operation(summary = "更新用户信息", description = "用户可以修改自己的资料，管理员可以修改任何用户的资料")
    public Result<UserVO> updateUser(
            @Valid @RequestBody UserUpdateDTO request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserVO userVO = userService.updateUser(request, userPrincipal.getUserId());
        return Result.success("更新成功", userVO);
    }

    // 4. 删除用户（仅管理员）
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除用户", description = "仅管理员可以删除用户账户，不能删除管理员账户和自己")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        userService.deleteUser(id, userPrincipal.getUserId());
        return Result.success("删除成功");
    }
}
