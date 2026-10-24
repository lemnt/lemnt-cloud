package com.lemnt.system.adapter.web.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * 新增菜单请求
 *
 * @author wangxx
 */
@Schema(description = "新增菜单请求")
public record MenuAddReq(
        @NotBlank(message = "菜单名称不能为空")
        @Schema(description = "菜单名称")
        String menuName,
        @NotNull(message = "父菜单ID不能为空")
        @Schema(description = "父菜单ID")
        Long parentId,
        @Schema(description = "显示顺序")
        Integer orderNum,
        @Schema(description = "路由地址")
        String path,
        @Schema(description = "组件路径")
        String component,
        @Schema(description = "路由参数")
        String query,
        @Schema(description = "路由名称")
        String routeName,
        @Schema(description = "是否为外链（0是 1否）")
        String isFrame,
        @Schema(description = "是否缓存（0缓存 1不缓存）")
        String isCache,
        @NotBlank(message = "菜单类型不能为空")
        @Schema(description = "菜单类型（M目录 C菜单 F按钮）")
        String menuType,
        @Schema(description = "显示状态（0显示 1隐藏）")
        String visible,
        @Schema(description = "菜单状态（1正常 2停用）")
        @Pattern(regexp = "[12]", message = "状态值只能为1或2")
        String status,
        @Schema(description = "权限标识")
        String perms,
        @Schema(description = "菜单图标")
        String icon
) implements Serializable {}
