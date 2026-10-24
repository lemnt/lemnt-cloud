package com.lemnt.system.api.domain.dto;

import java.util.Set;

import com.lemnt.common.api.domain.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 用户信息
 *
 * @author wangxx
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class LoginUserDTO extends BaseDTO {

    /**
     * 用户唯一标识
     */
    private String token;

    /**
     * 用户名id
     */
    private Long userId;

    /**
     * 是否模拟登陆
     */
    private Boolean simulation;

    /**
     * 用户名
     */
    private String username;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 过期时间
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
    private UserInfoDTO detail;
}
