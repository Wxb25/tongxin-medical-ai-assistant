package com.tongxin.ai.common;

/**
 * 当前登录用户上下文（基于 ThreadLocal）
 * 由 JwtAuthInterceptor / AdminAuthInterceptor 在 preHandle 中写入，Controller/Service 直接取用
 *
 * 区分普通用户（user）与管理员（admin），通过不同 ThreadLocal 存储
 *
 * @author wyq
 */
public class UserContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> CURRENT_ADMIN_ID = new ThreadLocal<>();

    public static void setCurrentUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static Long getCurrentUserId() {
        return CURRENT_USER_ID.get();
    }

    public static void setCurrentAdminId(Long adminId) {
        CURRENT_ADMIN_ID.set(adminId);
    }

    public static Long getCurrentAdminId() {
        return CURRENT_ADMIN_ID.get();
    }

    public static void clear() {
        CURRENT_USER_ID.remove();
        CURRENT_ADMIN_ID.remove();
    }
}
