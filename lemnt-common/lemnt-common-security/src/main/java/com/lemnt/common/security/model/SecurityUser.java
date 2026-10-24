package com.lemnt.common.security.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * 用户信息
 *
 * @author wangxx
 */
@Data
@Accessors(chain = true)
public class SecurityUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户唯一标识
     */
    private String token;

    /**
     * 用户名id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 会话最近签发或续期时间，Unix 毫秒时间戳
     */
    private Long loginTime;

    /**
     * 当前会话过期时间，Unix 毫秒时间戳，与 Redis TTL 对应
     */
    private Long expireTime;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 角色key
     */
    private String roleKey;

    /**
     * 权限列表
     */
    private Set<String> permissions;

    /**
     * 角色列表
     */
    private Set<String> roles;

    /**
     * 用户信息
     */
    private SecurityUserDetail detail;
}
