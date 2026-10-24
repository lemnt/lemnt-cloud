package com.lemnt.common.security.utils;

import jakarta.servlet.http.HttpServletRequest;

import com.lemnt.common.security.enums.RoleKeyEnum;
import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.security.constant.TokenConstants;
import com.lemnt.common.security.context.SecurityContextHolder;
import com.lemnt.common.security.model.SecurityUser;
import com.lemnt.common.core.utils.ServletUtils;
import com.lemnt.common.core.utils.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Objects;
import java.util.Set;

/**
 * 权限获取工具类
 *
 * @author wangxx
 */
public class SecurityUtils {
    /**
     * 获取用户ID
     */
    public static Long getUserId() {
        return SecurityContextHolder.getUserId();
    }

    /**
     * 获取用户名称
     */
    public static String getUsername() {
        return SecurityContextHolder.getUserName();
    }

    /**
     * 获取用户key
     */
    public static String getUserKey() {
        return SecurityContextHolder.getUserKey();
    }

    /**
     * 获取登录用户信息
     */
    public static SecurityUser getLoginUser() {
        return SecurityContextHolder.get(SecurityConstants.LOGIN_USER, SecurityUser.class);
    }

    public static Set<String> getRoleKey() {
        return SecurityContextHolder.getRoleKey();
    }

    /**
     * 获取请求token
     */
    public static String getToken() {
        return getToken(ServletUtils.getRequest());
    }

    /**
     * 根据request获取请求token
     */
    public static String getToken(HttpServletRequest request) {
        // 从header获取token标识
        String token = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
        return replaceTokenPrefix(token);
    }

    /**
     * 裁剪token前缀
     */
    public static String replaceTokenPrefix(String token) {
        // 如果前端设置了令牌前缀，则裁剪掉前缀
        if (StringUtils.isNotBlank(token) && token.startsWith(TokenConstants.PREFIX)) {
            token = token.replaceFirst(TokenConstants.PREFIX, "");
        }
        return token;
    }

    /**
     * 是否为管理员
     *
     * @param userId 用户ID
     * @return 结果
     */
    public static boolean isAdmin(Long userId) {
        return Objects.nonNull(userId) && Objects.equals(1L, userId);
    }

    /**
     * 是否为管理员
     *
     * @return 结果
     */
    public static boolean isAdmin() {
        Long userId = getUserId();
        return Objects.nonNull(userId) && Objects.equals(1L, userId);
    }

    public static boolean isRoleAdmin(String roleKey) {
        return StringUtils.isNotBlank(roleKey) && (Objects.equals(RoleKeyEnum.ADMIN.getRoleKey(), roleKey) || Objects.equals(RoleKeyEnum.SYSTEM.getRoleKey(), roleKey));
    }

    /**
     * 生成BCryptPasswordEncoder密码
     *
     * @param password 密码
     * @return 加密字符串
     */
    public static String encryptPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    /**
     * 判断密码是否相同
     *
     * @param rawPassword     真实密码
     * @param encodedPassword 加密后字符
     * @return 结果
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public static boolean hasRole(String roleKey) {
        Set<String> roleKeys = getRoleKey();
        return roleKeys.contains(roleKey);
    }

    public static boolean hasAdminRole() {
        Set<String> roleKeys = getRoleKey();
        return roleKeys.contains(RoleKeyEnum.ADMIN.getRoleKey()) || roleKeys.contains(RoleKeyEnum.SYSTEM.getRoleKey());
    }
}
