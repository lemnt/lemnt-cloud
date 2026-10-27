package com.lemnt.system.adapter.web.org;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.common.log.annotation.Log;
import com.lemnt.common.log.enums.BusinessType;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.system.adapter.web.org.converter.DeptReqConverter;
import com.lemnt.system.adapter.web.org.converter.DeptResConverter;
import com.lemnt.system.adapter.web.org.req.DeptAddReq;
import com.lemnt.system.adapter.web.org.req.DeptListReq;
import com.lemnt.system.adapter.web.org.req.DeptSortUpdateReq;
import com.lemnt.system.adapter.web.org.req.DeptUpdateReq;
import com.lemnt.system.adapter.web.org.res.SystemDeptExcludeChildRes;
import com.lemnt.system.adapter.web.org.res.SystemDeptGetInfoRes;
import com.lemnt.system.adapter.web.org.res.SystemDeptListRes;
import com.lemnt.system.application.org.dto.DeptDTO;
import com.lemnt.system.application.org.executor.DeptAddCmdExe;
import com.lemnt.system.application.org.executor.DeptDeleteCmdExe;
import com.lemnt.system.application.org.executor.DeptDetailQueryExe;
import com.lemnt.system.application.org.executor.DeptListQueryExe;
import com.lemnt.system.application.org.executor.DeptUpdateCmdExe;
import com.lemnt.system.application.org.executor.DeptUpdateSortCmdExe;
import com.lemnt.system.application.org.qry.DeptListQry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.ArrayUtils;
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

/**
 * 部门信息
 *
 * @author wangxx
 */
@Tag(name = "部门管理")
@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DeptController {
    private final DeptListQueryExe deptListQueryExe;
    private final DeptDetailQueryExe deptDetailQueryExe;
    private final DeptAddCmdExe deptAddCmdExe;
    private final DeptUpdateCmdExe deptUpdateCmdExe;
    private final DeptDeleteCmdExe deptDeleteCmdExe;
    private final DeptUpdateSortCmdExe deptUpdateSortCmdExe;

    @Operation(summary = "获取部门列表")
    @RequiresPermissions("system:dept:list")
    @GetMapping
    public R<List<SystemDeptListRes>> list(@Validated DeptListReq req) {
        return R.ok(DeptResConverter.toResListToSystemDeptListRes(deptListQueryExe.execute(DeptReqConverter.toQry(req))));
    }

    @Operation(summary = "查询部门列表（排除节点）")
    @RequiresPermissions("system:dept:list")
    @GetMapping("/exclude/{deptId}")
    public R<List<SystemDeptExcludeChildRes>> excludeChild(@PathVariable(value = "deptId",
            required = false) Long deptId) {
        List<DeptDTO> depts = deptListQueryExe.execute(new DeptListQry(null, null));
        depts.removeIf(d -> d.getDeptId().intValue() == deptId
                || ArrayUtils.contains(StringUtils.split(d.getAncestors(), ","), deptId + ""));
        return R.ok(DeptResConverter.toResListToSystemDeptExcludeChildRes(depts));
    }

    @Operation(summary = "根据部门编号获取详细信息")
    @RequiresPermissions("system:dept:query")
    @GetMapping(value = "/{deptId}")
    public R<SystemDeptGetInfoRes> getInfo(@PathVariable Long deptId) {
        return R.ok(DeptResConverter.toResToSystemDeptGetInfoRes(deptDetailQueryExe.execute(deptId)));
    }

    @Operation(summary = "新增部门")
    @RequiresPermissions("system:dept:add")
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Boolean> add(@Validated @RequestBody DeptAddReq req) {
        return R.ok(deptAddCmdExe.execute(DeptReqConverter.toCmd(req)));
    }

    @Operation(summary = "修改部门")
    @RequiresPermissions("system:dept:edit")
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Boolean> edit(@Validated @RequestBody DeptUpdateReq req) {
        return R.ok(deptUpdateCmdExe.execute(DeptReqConverter.toCmd(req)));
    }

    @Operation(summary = "保存部门排序")
    @RequiresPermissions("system:dept:edit")
    @Log(title = "保存部门排序", businessType = BusinessType.UPDATE)
    @PutMapping("/update-sort")
    public R<Boolean> updateSort(@Validated @RequestBody DeptSortUpdateReq req) {
        return R.ok(deptUpdateSortCmdExe.execute(req.deptIds(), req.orderNums()));
    }

    @Operation(summary = "删除部门")
    @RequiresPermissions("system:dept:remove")
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptId}")
    public R<Boolean> remove(@PathVariable Long deptId) {
        return R.ok(deptDeleteCmdExe.execute(deptId));
    }
}
