package com.lemnt.system.adapter.web.auth.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * 菜单信息响应
 *
 * @author wangxx
 */
@Schema(description = "菜单信息响应")
public record SystemMenuListRes(
        /**
 * 菜单ID
 */
        @Schema(description = "菜单ID")
        Long menuId,
        /**
 * 菜单名称
 */
        @Schema(description = "菜单名称")
        String menuName,
        /**
 * 父菜单ID
 */
        @Schema(description = "父菜单ID")
        Long parentId,
        /**
 * 显示顺序
 */
        @Schema(description = "显示顺序")
        Integer orderNum,
        /**
 * 路由地址
 */
        @Schema(description = "路由地址")
        String path,
        /**
 * 组件路径
 */
        @Schema(description = "组件路径")
        String component,
        /**
 * 路由参数
 */
        @Schema(description = "路由参数")
        String query,
        /**
 * 路由名称
 */
        @Schema(description = "路由名称")
        String routeName,
        /**
 * 是否为外链（0是 1否）
 */
        @Schema(description = "是否为外链（0是 1否）")
        String isFrame,
        /**
 * 是否缓存（0缓存 1不缓存）
 */
        @Schema(description = "是否缓存（0缓存 1不缓存）")
        String isCache,
        /**
 * 菜单类型（M目录 C菜单 F按钮）
 */
        @Schema(description = "菜单类型（M目录 C菜单 F按钮）")
        String menuType,
        /**
 * 显示状态（0显示 1隐藏）
 */
        @Schema(description = "显示状态（0显示 1隐藏）")
        String visible,
        /**
 * 菜单状态（1正常 2停用）
 */
        @Schema(description = "菜单状态（1正常 2停用）")
        String status,
        /**
 * 权限标识
 */
        @Schema(description = "权限标识")
        String perms,
        /**
 * 菜单图标
 */
        @Schema(description = "菜单图标")
        String icon,
        /**
 * 子菜单
 */
        @Schema(description = "子菜单")
        List<SystemMenuListRes> children
) implements Serializable {}
