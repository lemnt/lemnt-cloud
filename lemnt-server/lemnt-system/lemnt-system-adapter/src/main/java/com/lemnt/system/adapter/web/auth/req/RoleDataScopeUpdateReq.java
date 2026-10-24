package com.lemnt.system.adapter.web.auth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

/**
 * 修改角色数据范围请求
 *
 * @author wangxx
 */
@Schema(description = "修改角色数据范围请求")
public record RoleDataScopeUpdateReq(
        @NotNull(message = "角色ID不能为空")
        @Schema(description = "角色ID")
        Long roleId,
        @Schema(description = "数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）")
        String dataScope,
        @Schema(description = "部门组")
        List<Long> deptIds
) implements Serializable {}
