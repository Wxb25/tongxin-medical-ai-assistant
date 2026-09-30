package com.tongxin.ai.controller;

import com.tongxin.ai.common.Result;
import com.tongxin.ai.common.UserContext;
import com.tongxin.ai.dto.AdminInfoDTO;
import com.tongxin.ai.dto.AdminLoginRequest;
import com.tongxin.ai.dto.LoginResponseDTO;
import com.tongxin.ai.service.IAdminsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员控制器
 * POST /api/admin/login   管理员登录（公开）
 * GET  /api/admin/me      获取当前管理员信息（需管理员 token）
 *
 * @author wyq
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IAdminsService adminsService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@Valid @RequestBody AdminLoginRequest req) {
        return Result.success(adminsService.login(req));
    }

    /**
     * 获取当前管理员信息
     */
    @GetMapping("/me")
    public Result<AdminInfoDTO> getCurrentAdmin() {
        Long adminId = UserContext.getCurrentAdminId();
        return Result.success(adminsService.getCurrentAdmin(adminId));
    }
}
