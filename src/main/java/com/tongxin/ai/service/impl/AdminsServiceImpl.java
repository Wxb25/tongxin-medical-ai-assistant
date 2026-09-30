package com.tongxin.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tongxin.ai.common.BusinessException;
import com.tongxin.ai.dto.AdminInfoDTO;
import com.tongxin.ai.dto.AdminLoginRequest;
import com.tongxin.ai.dto.LoginResponseDTO;
import com.tongxin.ai.dto.UserInfoDTO;
import com.tongxin.ai.entity.po.Admins;
import com.tongxin.ai.mapper.AdminsMapper;
import com.tongxin.ai.service.IAdminsService;
import com.tongxin.ai.util.JwtUtil;
import com.tongxin.ai.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 管理员服务实现
 *
 * @author wyq
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminsServiceImpl extends ServiceImpl<AdminsMapper, Admins> implements IAdminsService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponseDTO login(AdminLoginRequest req) {
        // 1. 按用户名查
        Admins admin = this.getOne(new LambdaQueryWrapper<Admins>()
                .eq(Admins::getUsername, req.getUsername()));
        if (admin == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 2. 账号状态
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 3. 密码校验
        if (!passwordEncoder.matches(req.getPassword(), admin.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 4. 签发 token（role=admin）
        String token = jwtUtil.generateToken(admin.getId(), admin.getUsername(), "admin");

        // 5. 组装响应
        UserInfoDTO userInfo = new UserInfoDTO();
        BeanUtils.copyProperties(admin, userInfo);
        return new LoginResponseDTO(token, userInfo);
    }

    @Override
    public AdminInfoDTO getCurrentAdmin(Long adminId) {
        Admins admin = this.getById(adminId);
        if (admin == null) {
            throw new BusinessException(404, "管理员不存在");
        }
        AdminInfoDTO dto = new AdminInfoDTO();
        BeanUtils.copyProperties(admin, dto);
        return dto;
    }
}
