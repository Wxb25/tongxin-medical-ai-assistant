package com.tongxin.ai.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tongxin.ai.common.Result;
import com.tongxin.ai.common.UserContext;
import com.tongxin.ai.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理员鉴权拦截器
 * 校验 token 中的 role 是否为 admin，是则将 adminId 写入 UserContext
 *
 * @author wyq
 */
@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "未登录或登录已过期");
            return false;
        }

        String token = authHeader.substring(7).trim();
        if (!jwtUtil.validateToken(token)) {
            writeUnauthorized(response, "无效的登录凭证");
            return false;
        }

        // 校验角色必须是 admin
        String role = jwtUtil.extractRole(token);
        if (!"admin".equals(role)) {
            writeUnauthorized(response, "无管理员权限");
            return false;
        }

        Long adminId = jwtUtil.extractUserId(token);
        if (adminId == null) {
            writeUnauthorized(response, "登录凭证缺少管理员信息");
            return false;
        }

        UserContext.setCurrentAdminId(adminId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, message)));
    }
}
