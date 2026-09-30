package com.tongxin.ai.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户注册请求 DTO
 * 对应接口文档 UserRegisterRequest schema
 *
 * @author wyq
 */
@Data
public class UserRegisterRequest {

    /** 用户名（4-20 位） */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度需为 4-20 位")
    private String username;

    /** 密码（6-20 位） */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需为 6-20 位")
    private String password;

    /** 真实姓名 */
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    /** 手机号 */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 身份证号（可选） */
    private String idCard;

    /** 性别：0未知/1男/2女（可选） */
    @Min(value = 0, message = "性别取值需为 0/1/2")
    @Max(value = 2, message = "性别取值需为 0/1/2")
    private Integer gender;

    /** 出生日期（可选） */
    private LocalDate birthDate;
}
