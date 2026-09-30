package com.tongxin.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tongxin.ai.dto.AdminInfoDTO;
import com.tongxin.ai.dto.AdminLoginRequest;
import com.tongxin.ai.dto.LoginResponseDTO;
import com.tongxin.ai.entity.po.Admins;

/**
 * 管理员服务接口
 *
 * @author wyq
 */
public interface IAdminsService extends IService<Admins> {

    /**
     * 管理员登录
     */
    LoginResponseDTO login(AdminLoginRequest req);

    /**
     * 获取当前管理员信息
     */
    AdminInfoDTO getCurrentAdmin(Long adminId);
}
