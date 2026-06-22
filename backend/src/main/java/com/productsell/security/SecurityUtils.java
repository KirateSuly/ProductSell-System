package com.productsell.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 * <p>
 * 提供从 SecurityContextHolder 获取当前登录用户信息的静态方法。
 * 可以在 Controller、Service 等任何层调用，无需注入。
 */
public class SecurityUtils {

    private SecurityUtils() {
        // 工具类禁止实例化
    }

    /**
     * 获取当前登录用户名
     * <p>
     * 从 SecurityContextHolder 的 Authentication 中提取用户名（subject）。
     * 未认证时返回 null。
     *
     * @return 当前用户名，未认证返回 null
     */
    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return auth.getName();
    }

    /**
     * 判断当前请求是否已认证
     *
     * @return true 已认证，false 未认证
     */
    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated();
    }
}
