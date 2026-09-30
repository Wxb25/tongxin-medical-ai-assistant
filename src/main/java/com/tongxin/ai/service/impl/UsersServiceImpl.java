package com.tongxin.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tongxin.ai.common.BusinessException;
import com.tongxin.ai.dto.LoginResponseDTO;
import com.tongxin.ai.dto.UserInfoDTO;
import com.tongxin.ai.dto.UserLoginRequest;
import com.tongxin.ai.dto.UserRegisterRequest;
import com.tongxin.ai.entity.po.Users;
import com.tongxin.ai.mapper.UsersMapper;
import com.tongxin.ai.service.IUsersService;
import com.tongxin.ai.util.JwtUtil;
import com.tongxin.ai.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现
 *
 * @author wyq
 * @since 2026-09-02
 */
@Service
@RequiredArgsConstructor
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public void register(UserRegisterRequest req) {
        // 1. 用户名唯一性校验
        long count = this.count(new LambdaQueryWrapper<Users>()
                .eq(Users::getUsername, req.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 构造用户实体（DTO 字段名与实体一致，可直接拷贝）
        Users user = new Users();
        BeanUtils.copyProperties(req, user);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(0);           // 默认患者
        user.setStatus(1);         // 默认启用
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 3. 写入
        this.save(user);
    }

    @Override
    public LoginResponseDTO login(UserLoginRequest req) {
        // 1. 按用户名查
        Users user = this.getOne(new LambdaQueryWrapper<Users>()
                .eq(Users::getUsername, req.getUsername()));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 2. 账号状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 3. 密码校验
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 4. 签发 token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 5. 组装响应
        UserInfoDTO userInfo = new UserInfoDTO();
        BeanUtils.copyProperties(user, userInfo);
        return new LoginResponseDTO(token, userInfo);
    }

    @Override
    public UserInfoDTO getCurrentUser(Long userId) {
        Users user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        UserInfoDTO dto = new UserInfoDTO();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }
}
