package com.tongxin.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tongxin.ai.dto.LoginResponseDTO;
import com.tongxin.ai.dto.UserLoginRequest;
import com.tongxin.ai.dto.UserRegisterRequest;
import com.tongxin.ai.dto.UserInfoDTO;
import com.tongxin.ai.entity.po.Users;

/**
 * 用户服务接口
 *
 * @author wyq
 * @since 2026-09-02
 */
public interface IUsersService extends IService<Users> {

    /**
     * 用户注册
     */
    void register(UserRegisterRequest req);

    /**
     * 用户登录：校验密码并签发 token + userInfo
     */
    LoginResponseDTO login(UserLoginRequest req);

    /**
     * 根据 userId 获取当前用户脱敏信息（/me 接口用）
     */
    UserInfoDTO getCurrentUser(Long userId);
}
