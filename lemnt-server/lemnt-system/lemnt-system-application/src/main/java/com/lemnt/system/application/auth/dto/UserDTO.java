package com.lemnt.system.application.auth.dto;

import com.lemnt.system.application.org.dto.DeptDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户 DTO
 *
 * @author wangxx
 */
@Data
public class UserDTO {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phonenumber;

    /**
     * 用户性别（0男 1女 2未知）
     */
    private String sex;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 账号状态（1正常 2停用）
     */
    private String status;

    /**
     * 最后登录IP
     */
    private String loginIp;

    /**
     * 最后登录时间
     */
    private LocalDateTime loginDate;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 密码（仅用于修改密码/重置密码场景，不回显）
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /**
     * 角色列表
     */
    private List<RoleDTO> roles;

    /**
     * 部门
     */
    private DeptDTO dept;
}
