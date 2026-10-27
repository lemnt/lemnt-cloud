package com.lemnt.system.adapter.web.auth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.log.annotation.Log;
import com.lemnt.common.log.enums.BusinessType;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.system.adapter.web.auth.converter.*;
import com.lemnt.system.adapter.web.auth.req.*;
import com.lemnt.system.adapter.web.auth.res.*;
import com.lemnt.system.application.auth.executor.*;
import com.lemnt.system.domain.auth.model.TreeSelect;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

/** 角色数据范围接口。 */
@Tag(name = "角色数据范围")
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleDataScopeController {
    private final RoleDataScopeUpdateCmdExe roleDataScopeUpdateCmdExe;
    private final RoleDeptTreeQueryExe roleDeptTreeQueryExe;

    @Operation(summary = "获取对应角色部门树列表")
    @SuppressWarnings("unchecked")
    @RequiresPermissions("system:role:query")
    @GetMapping(value = "/dept-tree/{roleId}")
    public R<RoleDeptTreeRes> deptTree(@PathVariable("roleId") Long roleId) {
        Map<String, Object> result = roleDeptTreeQueryExe.execute(roleId);
        RoleDeptTreeRes res = new RoleDeptTreeRes(
                (List<Long>) result.get("checkedKeys"),
                TreeSelectResConverter.toResList((List<TreeSelect>) result.get("depts")));
        return R.ok(res);
    }

    @Operation(summary = "修改保存数据权限")
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/data-scope")
    public R<Boolean> dataScope(@RequestBody RoleDataScopeUpdateReq req) {
        return R.ok(roleDataScopeUpdateCmdExe.execute(RoleReqConverter.toCmd(req)));
    }
}
