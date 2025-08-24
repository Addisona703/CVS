package com.hngy.cvs.controller;

import com.hngy.cvs.common.result.Result;
import com.hngy.cvs.dto.request.LoginDTO;
import com.hngy.cvs.dto.request.RegisterDTO;
import com.hngy.cvs.dto.response.LoginVO;
import com.hngy.cvs.dto.response.UserVO;
import com.hngy.cvs.dto.request.SendCodeDTO;
import com.hngy.cvs.dto.request.VerifyCodeDTO;
import com.hngy.cvs.dto.response.VerifyCodeVO;
import com.hngy.cvs.service.EmailService;
import com.hngy.cvs.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 认证控制器
 * 
 * @author CVS Team
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "用户认证相关接口")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final EmailService emailService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO loginVO = userService.login(dto);
        return Result.success("登录成功", loginVO);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO dto) {
        // 验证验证码
        VerifyCodeDTO verifyCodeDTO = new VerifyCodeDTO();
        verifyCodeDTO.setEmail(dto.getEmail());
        verifyCodeDTO.setCode(dto.getVerificationCode());
        verifyCodeDTO.setType("register");
        
        VerifyCodeVO verifyResult = emailService.verifyCode(verifyCodeDTO);
        if (!verifyResult.getVerified()) {
            return Result.error(400, verifyResult.getMessage());
        }
        
        UserVO userVO = userService.register(dto);
        return Result.success("注册成功", userVO);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public Result<Void> logout() {
        // JWT是无状态的，客户端删除token即可
        return Result.success("登出成功");
    }

    @PostMapping("/send-code")
    @Operation(summary = "发送邮箱验证码")
    public Result<Void> sendVerificationCode(@Valid @RequestBody SendCodeDTO dto) {
        emailService.sendVerificationCode(dto);
        return Result.success("验证码发送成功");
    }

    @PostMapping("/verify-code")
    @Operation(summary = "验证验证码")
    public Result<VerifyCodeVO> verifyCode(@Valid @RequestBody VerifyCodeDTO dto) {
        VerifyCodeVO result = emailService.verifyCode(dto);
        if (result.getVerified()) {
            return Result.success("验证成功", result);
        } else {
            return Result.error(400, result.getMessage());
        }
    }
}
