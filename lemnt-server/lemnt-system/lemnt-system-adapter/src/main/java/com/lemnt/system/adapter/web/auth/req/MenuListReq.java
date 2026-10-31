package com.lemnt.system.adapter.web.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 菜单列表查询条件
 *
 * @author wangxx
 */
@Schema(description = "菜单列表查询条件")
public record MenuListReq(
        /**
 * 菜单名称
 */
        @Schema(description = "菜单名称")
        String menuName,
        /**
 * 菜单状态（1正常 2停用）
 */
        @Schema(description = "菜单状态（1正常 2停用）")
        @Pattern(regexp = "[12]", message = "状态值只能为1或2")
        String status,
        /**
 * 显示状态（0显示 1隐藏）
 */
        @Schema(description = "显示状态（0显示 1隐藏）")
        String visible,
        /**
 * 查询用户ID（非管理员时按用户权限过滤）
 */
        @Schema(description = "查询用户ID")
        Long userId
) implements Serializable {}
