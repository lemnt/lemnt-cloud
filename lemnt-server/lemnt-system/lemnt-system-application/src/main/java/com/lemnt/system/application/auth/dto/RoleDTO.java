package com.lemnt.system.application.auth.dto;

import lombok.Data;

/**
 * 角色 DTO
 *
 * @author wangxx
 */
@Data
public class RoleDTO {
    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色权限标识
     */
    private String roleKey;

    /**
     * 角色排序
     */
    private Integer roleSort;

    /**
     * 数据范围
     */
    private String dataScope;

    /**
     * 角色状态（1正常 2停用）
     */
    private String status;

    /**
     * 用户是否存在此角色标识
     */
    private boolean flag = false;
}
