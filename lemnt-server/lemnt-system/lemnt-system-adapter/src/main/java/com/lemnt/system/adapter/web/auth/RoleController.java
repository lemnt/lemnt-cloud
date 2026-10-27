package com.lemnt.system.adapter.web.auth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.log.annotation.Log;
import com.lemnt.common.log.enums.BusinessType;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.system.adapter.web.auth.converter.*;
import com.lemnt.system.adapter.web.auth.req.*;
import com.lemnt.system.adapter.web.auth.res.*;
import com.lemnt.system.adapter.web.auth.res.SystemRoleGetInfoRes;
import com.lemnt.system.adapter.web.auth.res.SystemRoleListRes;
import com.lemnt.system.adapter.web.auth.res.SystemRoleOptionselectRes;
import com.lemnt.system.application.auth.dto.RoleDTO;
import com.lemnt.system.application.auth.executor.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 角色管理接口。 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RolePageQueryExe rolePageQueryExe;
    private final RoleDetailQueryExe roleDetailQueryExe;
    private final RoleAddCmdExe roleAddCmdExe;
    private final RoleUpdateCmdExe roleUpdateCmdExe;
    private final RoleDeleteCmdExe roleDeleteCmdExe;
    private final RoleStatusUpdateCmdExe roleStatusUpdateCmdExe;
    private final RoleOptionselectQueryExe roleOptionselectQueryExe;

    @Operation(summary = "获取角色列表")
    @RequiresPermissions("system:role:list")
    @GetMapping
    public R<SystemRoleListRes> list(@Validated RolePageReq req) {
        PageResult<RoleDTO> result = rolePageQueryExe.execute(RoleReqConverter.toQry(req));
        return R.page(RoleResConverter.toResListToSystemRoleListRes(result.getRecords()), result.getTotal());
    }

    @Operation(summary = "根据角色编号获取详细信息")
    @RequiresPermissions("system:role:query")
    @GetMapping(value = "/{roleId}")
    public R<SystemRoleGetInfoRes> getInfo(@PathVariable Long roleId) {
        return R.ok(RoleResConverter.toResToSystemRoleGetInfoRes(roleDetailQueryExe.execute(roleId)));
    }

    @Operation(summary = "新增角色")
    @RequiresPermissions("system:role:add")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Boolean> add(@Validated @RequestBody RoleAddReq req) {
        return R.ok(roleAddCmdExe.execute(RoleReqConverter.toCmd(req)));
    }

    @Operation(summary = "修改保存角色")
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Boolean> edit(@Validated @RequestBody RoleUpdateReq req) {
        return R.ok(roleUpdateCmdExe.execute(RoleReqConverter.toCmd(req)));
    }

    @Operation(summary = "状态修改")
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/change-status")
    public R<Boolean> changeStatus(@Validated @RequestBody RoleStatusUpdateReq req) {
        return R.ok(roleStatusUpdateCmdExe.execute(RoleReqConverter.toCmd(req)));
    }

    @Operation(summary = "删除角色")
    @RequiresPermissions("system:role:remove")
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{roleIds}")
    public R<Boolean> remove(@PathVariable List<Long> roleIds) {
        return R.ok(roleDeleteCmdExe.execute(roleIds));
    }

    @Operation(summary = "获取角色选择框列表")
    @RequiresPermissions("system:role:query")
    @GetMapping("/options")
    public R<List<SystemRoleOptionselectRes>> optionselect() {
        return R.ok(RoleResConverter.toResListToSystemRoleOptionselectRes(roleOptionselectQueryExe.execute()));
    }
}
