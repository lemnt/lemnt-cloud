package com.lemnt.system.adapter.web.auth;

import java.util.List;
import java.util.Map;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.log.annotation.Log;
import com.lemnt.common.log.enums.BusinessType;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.system.adapter.web.auth.converter.*;
import com.lemnt.system.adapter.web.auth.req.*;
import com.lemnt.system.adapter.web.auth.res.*;
import com.lemnt.system.application.auth.dto.RoleDTO;
import com.lemnt.system.application.auth.dto.UserDTO;
import com.lemnt.system.application.auth.executor.*;
import com.lemnt.system.domain.auth.model.TreeSelect;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色信息
 *
 * @author wangxx
 */
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
    private final RoleDataScopeUpdateCmdExe roleDataScopeUpdateCmdExe;
    private final RoleOptionselectQueryExe roleOptionselectQueryExe;
    private final RoleAuthBatchCmdExe roleAuthBatchCmdExe;
    private final RoleAuthCancelCmdExe roleAuthCancelCmdExe;
    private final RoleAuthCancelBatchCmdExe roleAuthCancelBatchCmdExe;
    private final RoleDeptTreeQueryExe roleDeptTreeQueryExe;
    private final UserAllocatedPageQueryExe userAllocatedPageQueryExe;
    private final UserUnAllocatedPageQueryExe userUnAllocatedPageQueryExe;

    @Operation(summary = "获取角色列表")
    @RequiresPermissions("system:role:list")
    @GetMapping
    public R<RoleRes> list(@Validated RolePageReq req) {
        PageResult<RoleDTO> result = rolePageQueryExe.execute(RoleReqConverter.toQry(req));
        return R.page(RoleResConverter.toResList(result.getRecords()), result.getTotal());
    }

    @Operation(summary = "查询已分配用户角色列表")
    @RequiresPermissions("system:role:list")
    @GetMapping("/auth-user/allocated-list")
    public R<UserRes> allocatedList(@Validated UserPageReq req) {
        PageResult<UserDTO> result = userAllocatedPageQueryExe.execute(UserReqConverter.toQry(req));
        return R.page(UserResConverter.toResList(result.getRecords()), result.getTotal());
    }

    @Operation(summary = "查询未分配用户角色列表")
    @RequiresPermissions("system:role:list")
    @GetMapping("/auth-user/unallocated-list")
    public R<UserRes> unallocatedList(@Validated UserPageReq req) {
        PageResult<UserDTO> result = userUnAllocatedPageQueryExe.execute(UserReqConverter.toQry(req));
        return R.page(UserResConverter.toResList(result.getRecords()), result.getTotal());
    }

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

    @Operation(summary = "根据角色编号获取详细信息")
    @RequiresPermissions("system:role:query")
    @GetMapping(value = "/{roleId}")
    public R<RoleRes> getInfo(@PathVariable Long roleId) {
        return R.ok(RoleResConverter.toRes(roleDetailQueryExe.execute(roleId)));
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

    @Operation(summary = "修改保存数据权限")
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/data-scope")
    public R<Boolean> dataScope(@RequestBody RoleDataScopeUpdateReq req) {
        return R.ok(roleDataScopeUpdateCmdExe.execute(RoleReqConverter.toCmd(req)));
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
    public R<List<RoleRes>> optionselect() {
        return R.ok(RoleResConverter.toResList(roleOptionselectQueryExe.execute()));
    }

    @Operation(summary = "取消授权用户")
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/auth-user/cancel")
    public R<Boolean> cancelAuthUser(@RequestBody RoleAuthCancelReq req) {
        return R.ok(roleAuthCancelCmdExe.execute(RoleReqConverter.toAuthCancelCmd(req)));
    }

    @Operation(summary = "批量取消授权用户")
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/auth-user/cancel-all")
    public R<Boolean> cancelAuthUserAll(@RequestBody RoleAuthCancelReq req) {
        return R.ok(roleAuthCancelBatchCmdExe.execute(RoleReqConverter.toAuthCancelCmd(req)));
    }

    @Operation(summary = "批量选择用户授权")
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/auth-user/select-all")
    public R<Boolean> selectAuthUserAll(@RequestBody RoleAuthReq req) {
        return R.ok(roleAuthBatchCmdExe.execute(RoleReqConverter.toAuthCmd(req)));
    }
}
