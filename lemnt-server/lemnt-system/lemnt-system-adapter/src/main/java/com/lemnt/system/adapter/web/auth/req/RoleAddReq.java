package com.lemnt.system.adapter.web.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;

/**
 * 新增角色请求
 *
 * @author wangxx
 */
@Schema(description = "新增角色请求")
public record RoleAddReq(
        @NotBlank(message = "角色名称不能为空")
        @Schema(description = "角色名称")
        String roleName,
        @NotBlank(message = "角色权限标识不能为空")
        @Schema(description = "角色权限标识")
        String roleKey,
        @Schema(description = "角色排序")
        Integer roleSort,
        @Schema(description = "数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）")
        String dataScope,
        @Schema(description = "角色状态（1正常 2停用）")
        @Pattern(regexp = "[12]", message = "状态值只能为1或2")
        String status,
        @Schema(description = "菜单组")
        List<Long> menuIds
) implements Serializable {}
