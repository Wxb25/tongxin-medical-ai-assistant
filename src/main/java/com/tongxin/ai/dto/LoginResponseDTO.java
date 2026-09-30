package com.tongxin.ai.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 登录响应 DTO
 * 对应接口文档 LoginResponseData schema（token + userInfo）
 *
 * @author wyq
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    /** JWT Token */
    private String token;

    /** 用户信息 */
    private UserInfoDTO userInfo;
}
