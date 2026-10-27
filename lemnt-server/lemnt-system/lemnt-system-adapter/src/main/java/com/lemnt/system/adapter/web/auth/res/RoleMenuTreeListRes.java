package com.lemnt.system.adapter.web.auth.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * 角色菜单树列表响应
 *
 * @author wangxx
 */
@Schema(description = "角色菜单树列表响应")
public record RoleMenuTreeListRes(
        @Schema(description = "已选中菜单ID列表")
        List<Long> checkedKeys,
        @Schema(description = "菜单树列表")
        List<TreeSelectRes> menus
) implements Serializable {}
