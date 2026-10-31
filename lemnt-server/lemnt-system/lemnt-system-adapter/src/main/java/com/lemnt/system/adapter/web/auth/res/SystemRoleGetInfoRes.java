package com.lemnt.system.adapter.web.auth.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 角色信息响应
 *
 * @author wangxx
 */
@Schema(description = "角色信息响应")
public record SystemRoleGetInfoRes(
        /**
 * 角色ID
 */
        @Schema(description = "角色ID")
        Long roleId,
        /**
 * 角色名称
 */
        @Schema(description = "角色名称")
        String roleName,
        /**
 * 角色权限标识
 */
        @Schema(description = "角色权限标识")
        String roleKey,
        /**
 * 角色排序
 */
        @Schema(description = "角色排序")
        Integer roleSort,
        /**
 * 数据范围
 */
        @Schema(description = "数据范围")
        String dataScope,
        /**
 * 角色状态（1正常 2停用）
 */
        @Schema(description = "角色状态（1正常 2停用）")
        String status,
        /**
 * 用户是否存在此角色标识
 */
        @Schema(description = "用户是否存在此角色标识")
        boolean flag
) implements Serializable {}
