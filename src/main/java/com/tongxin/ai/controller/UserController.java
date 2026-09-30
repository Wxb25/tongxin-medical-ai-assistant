package com.tongxin.ai.controller;

import com.tongxin.ai.common.Result;
import com.tongxin.ai.common.UserContext;
import com.tongxin.ai.dto.LoginResponseDTO;
import com.tongxin.ai.dto.UserInfoDTO;
import com.tongxin.ai.dto.UserLoginRequest;
import com.tongxin.ai.dto.UserRegisterRequest;
import com.tongxin.ai.service.IUsersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 *
 * @author wyq
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final IUsersService usersService;

    /**
     * 用户注册
     * POST /api/users/register
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody UserRegisterRequest req) {
        usersService.register(req);
        return Result.success();
    }

    /**
     * 用户登录
     * POST /api/users/login
     */
    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@Valid @RequestBody UserLoginRequest req) {
        return Result.success(usersService.login(req));
    }

    /**
     * 获取当前登录用户信息
     * GET /api/users/me  （需 Bearer Token，由 JwtAuthInterceptor 解析后存入 UserContext）
     */
    @GetMapping("/me")
    public Result<UserInfoDTO> getCurrentUser() {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(usersService.getCurrentUser(userId));
    }
}
