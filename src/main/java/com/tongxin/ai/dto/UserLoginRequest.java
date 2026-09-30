package com.tongxin.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求 DTO
 * 对应接口文档 UserLoginRequest schema
 *
 * @author wyq
 */
@Data
public class UserLoginRequest {

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;
}
